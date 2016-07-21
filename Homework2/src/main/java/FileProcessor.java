import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.LocatedFileStatus;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IOUtils;
import org.apache.hadoop.io.compress.CompressionCodec;
import org.apache.hadoop.io.compress.CompressionCodecFactory;
import org.apache.log4j.Logger;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.Callable;

public class FileProcessor implements Callable<Object> {

    public static final int NUMBER_OF_PROCESSING_RECORDS = 150000;
    private static final Logger LOGGER = Logger.getLogger(FileProcessor.class);
    private static final int POSITION_OF_IP_IN_YOU_ID_IN_LOG_FILE = 2;
    private final LocatedFileStatus fileStatus;
    private final FileSystem fileSystem;
    private final Configuration configuration;
    private int processingRecordsBlock = 0;
    private Map<String, Integer> stringSet = new TreeMap<String, Integer>(Collections.reverseOrder());
    ;

    public FileProcessor(LocatedFileStatus fileStatus, FileSystem fileSystem, Configuration configuration) {
        this.fileStatus = fileStatus;
        this.fileSystem = fileSystem;
        this.configuration = configuration;
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
            getIpInYouIdentSet(inputStream);
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            IOUtils.closeStream(inputStream);
        }

        String processingFinished = "Processing data from file " + path + " completed";
        LOGGER.info(processingFinished);

        return null;
    }

    private void getIpInYouIdentSet(InputStream stream) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
        try {
            for (String line; (line = reader.readLine()) != null; ) {
                String ipInYouIdent = line.split("\t")[POSITION_OF_IP_IN_YOU_ID_IN_LOG_FILE];
                if (ipInYouIdent.equals("null")) {
                    continue;
                }

                Integer previousValue = stringSet.get(ipInYouIdent);
                if (previousValue == null) {
                    stringSet.put(ipInYouIdent, 1);
                } else {
                    stringSet.put(ipInYouIdent, previousValue + 1);
                }

                if (stringSet.size() == NUMBER_OF_PROCESSING_RECORDS) {
                    try {
                        writeRecordsToHDFS();
                    } catch (URISyntaxException e) {
                        LOGGER.error(e.getMessage(), e);
                    }

                    processingRecordsBlock++;
                    stringSet.clear();
                }
            }
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    private void writeRecordsToHDFS() throws URISyntaxException, IOException {
        String threadName = Thread.currentThread().getName();
        String blockFileName = threadName + "-block-" + processingRecordsBlock;
        String hostname = System.getenv("HOSTNAME");
        Configuration configuration = new Configuration();
        FileSystem fileSystem = FileSystem.get(new URI("hdfs://" + hostname + ":9000"), configuration);
        Path file = new Path("hdfs://" + hostname + ":9000/job_output/" + blockFileName);
        if (fileSystem.exists(file)) {
            fileSystem.delete(file, true);
        }

        OutputStream outputStream = fileSystem.create(file);
        BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));
        LOGGER.info("Writing " + NUMBER_OF_PROCESSING_RECORDS + " into \'" + blockFileName + "\' from \'" + threadName + "\' thread ...");
        for (Map.Entry<String, Integer> stringIntegerEntry : stringSet.entrySet()) {
            bufferedWriter.write(stringIntegerEntry.getKey() + "\t" + stringIntegerEntry.getValue());
            bufferedWriter.write('\n');
        }

        bufferedWriter.close();

        LOGGER.info("Writing " + NUMBER_OF_PROCESSING_RECORDS + " into \'" + blockFileName + "\' from \'" + threadName + "\' thread completed");
    }
}
