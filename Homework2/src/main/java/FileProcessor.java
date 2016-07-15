import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.BufferedFSInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.LocatedFileStatus;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IOUtils;
import org.apache.hadoop.io.compress.CompressionCodec;
import org.apache.hadoop.io.compress.CompressionCodecFactory;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;
import java.util.concurrent.Callable;

public class FileProcessor implements Callable<Map<String, Integer>> {

    private static final Logger LOGGER = Logger.getLogger(FileProcessor.class);
    private static final int POSITION_OF_IP_IN_YOU_ID_IN_LOG_FILE = 2;

    private final LocatedFileStatus fileStatus;
    private final FileSystem fileSystem;
    private final Configuration configuration;

    public FileProcessor(LocatedFileStatus fileStatus, FileSystem fileSystem, Configuration configuration) {
        this.fileStatus = fileStatus;
        this.fileSystem = fileSystem;
        this.configuration = configuration;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, Integer> call() throws Exception {
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
        Map<String, Integer> ipInYouIdentMap = null;
        try {
            inputStream = codec.createInputStream(fileSystem.open(inputPath));
            ipInYouIdentMap = getIpInYouIdentSet(inputStream);
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            IOUtils.closeStream(inputStream);
        }

        String processingFinished = "Processing data from file " + path + " completed";
        LOGGER.info(processingFinished);

        return ipInYouIdentMap;
    }

    private Map<String, Integer> getIpInYouIdentSet(InputStream stream) {
        Map<String, Integer> stringSet = new TreeMap<String, Integer>(Collections.reverseOrder());

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
            }
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }

        return stringSet;
    }
}
