import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.LocatedFileStatus;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IOUtils;
import org.apache.hadoop.io.compress.CompressionCodec;
import org.apache.hadoop.io.compress.CompressionCodecFactory;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.Callable;

public class FileProcessor implements Callable<Set<String>> {

    private static final Logger LOGGER = Logger.getLogger(FileProcessor.class);
    private static final int IP_IN_YOU_ID_LENGTH = 11;

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
    public Set<String> call() throws Exception {
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
        Set<String> ipInYouIdentSet = null;
        try {
            inputStream = codec.createInputStream(fileSystem.open(inputPath));
            ipInYouIdentSet = getIpInYouIdentSet(inputStream);
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            IOUtils.closeStream(inputStream);
        }

        String processingFinished = "Processing data from file " + path + " completed";
        LOGGER.info(processingFinished);

        return ipInYouIdentSet;
    }

    private Set<String> getIpInYouIdentSet(InputStream stream) {
        Set<String> stringSet = new TreeSet<String>(Collections.reverseOrder());

        Scanner scanner = new Scanner(stream).useDelimiter("\n");
        while (scanner.hasNext()) {
            String next = scanner.next();
            String ipInYouIdent = next.split("\t")[2];
            int length = ipInYouIdent.length();
            if (length == IP_IN_YOU_ID_LENGTH || length == IP_IN_YOU_ID_LENGTH + 1) {
                stringSet.add(ipInYouIdent);
            }
        }

        return stringSet;
    }
}
