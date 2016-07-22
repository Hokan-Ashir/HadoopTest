package ru.hokan;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.LocatedFileStatus;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IOUtils;
import org.apache.hadoop.io.compress.CompressionCodec;
import org.apache.hadoop.io.compress.CompressionCodecFactory;
import org.apache.log4j.Logger;

import java.io.*;
import java.net.URISyntaxException;
import java.util.*;
import java.util.concurrent.Callable;

public class FileProcessor implements Callable<Object> {

    private static final int NUMBER_OF_PROCESSING_RECORDS = 150000;
    private static final Logger LOGGER = Logger.getLogger(FileProcessor.class);
    private static final int POSITION_OF_IP_IN_YOU_ID_IN_LOG_FILE = 2;
    private final LocatedFileStatus fileStatus;
    private final FileSystem fileSystem;
    private final Configuration configuration;
    private int processingRecordsBlock = 0;
    private Map<String, Integer> resultMap = new TreeMap<String, Integer>();
    private final String outputURI;

    public FileProcessor(LocatedFileStatus fileStatus, FileSystem fileSystem, Configuration configuration, String outputURI) {
        this.fileStatus = fileStatus;
        this.fileSystem = fileSystem;
        this.configuration = configuration;
        this.outputURI = outputURI;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object call() throws Exception {
        Path inputPath = fileStatus.getPath();
        CompressionCodecFactory factory = new CompressionCodecFactory(configuration);
        CompressionCodec codec = factory.getCodec(inputPath);
        String path = inputPath.toString();
        if (codec == null) {
            LOGGER.error("No decompression codec found for: " + path);
            System.exit(1);
        }

        String processingStarted = "Processing data from file " + path + " ...";
        LOGGER.info(processingStarted);
        InputStream inputStream = null;
        try {
            inputStream = codec.createInputStream(fileSystem.open(inputPath));
            processFile(inputStream);
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            IOUtils.closeStream(inputStream);
        }

        String processingFinished = "Processing data from file " + path + " completed";
        LOGGER.info(processingFinished);

        return null;
    }

//    TODO can be enhanced via processing in parallel passing different offsets of file to threads
    private void processFile(InputStream stream) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
        try {
            for (String line; (line = reader.readLine()) != null; ) {
                String ipInYouIdent = line.split("\t")[POSITION_OF_IP_IN_YOU_ID_IN_LOG_FILE];
                if (ipInYouIdent.equals("null")) {
                    continue;
                }

                Integer previousValue = resultMap.get(ipInYouIdent);
                if (previousValue == null) {
                    resultMap.put(ipInYouIdent, 1);
                } else {
                    resultMap.put(ipInYouIdent, previousValue + 1);
                }

                if (resultMap.size() == NUMBER_OF_PROCESSING_RECORDS) {
                    dropDataToHDFS();
                    processingRecordsBlock++;
                    resultMap.clear();
                }
            }
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }

        dropDataToHDFS();
    }

    private void dropDataToHDFS() {
        try {
            writeRecordsToHDFS(resultMap);
        } catch (URISyntaxException e) {
            LOGGER.error(e.getMessage(), e);
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    private void writeRecordsToHDFS(Map<String, Integer> entries) throws URISyntaxException, IOException {
        String threadName = Thread.currentThread().getName();
        String blockFileName = threadName + "-block-" + processingRecordsBlock;
        Configuration configuration = new Configuration();
        FileSystem fileSystem = FileSystem.get(configuration);
        Path filePath = new Path(outputURI + blockFileName);
        if (fileSystem.exists(filePath)) {
            fileSystem.delete(filePath, true);
        }

        OutputStream outputStream = fileSystem.create(filePath);
        BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));
        LOGGER.info("Writing into \'" + blockFileName + "\' from \'" + threadName + "\' thread ...");
        for (Map.Entry<String, Integer> stringIntegerEntry : entries.entrySet()) {
            bufferedWriter.write(stringIntegerEntry.getKey() + "\t" + stringIntegerEntry.getValue());
            bufferedWriter.write('\n');
        }

        bufferedWriter.close();

        LOGGER.info("Writing into \'" + blockFileName + "\' from \'" + threadName + "\' thread completed");
    }
}
