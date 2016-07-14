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
import java.util.concurrent.Callable;

public class FileProcessor implements Callable<Integer> {

    private static final Logger LOGGER = Logger.getLogger(FileProcessor.class);
    private static final int BUFF_SIZE = 4096;

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
    public Integer call() throws Exception {
        Path inputPath = fileStatus.getPath();
        CompressionCodecFactory factory = new CompressionCodecFactory(configuration);
        CompressionCodec codec = factory.getCodec(inputPath);
        String path = inputPath.toString();
        if (codec == null) {
            LOGGER.error("No decompression codec found for: " + path);
            System.exit(1);
        }

        String processingStarted = "Processing data from file " + path + " ...";
        System.out.println(processingStarted);
        LOGGER.info(processingStarted);
        InputStream inputStream = null;
        int numberOfLinesInFile = 0;
        try {
            inputStream = codec.createInputStream(fileSystem.open(inputPath));
            numberOfLinesInFile = getNumberOfLinesInFile(inputStream);
            String resultString = "File \'" + path + "\' has " + numberOfLinesInFile + " lines in it";
            System.out.println(resultString);
            LOGGER.info(resultString);
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            IOUtils.closeStream(inputStream);
        }

        String processingFinished = "Processing data from file " + path + " completed";
        System.out.println(processingFinished);
        LOGGER.info(processingFinished);

        return numberOfLinesInFile;
    }

    private int getNumberOfLinesInFile(InputStream inputStream) throws IOException {
        try {
            byte[] c = new byte[BUFF_SIZE];
            int count = 0;
            int readChars;
            boolean empty = true;
            while ((readChars = inputStream.read(c)) != -1) {
                empty = false;
                for (int i = 0; i < readChars; ++i) {
                    if (c[i] == '\n') {
                        ++count;
                    }
                }
            }
            return (count == 0 && !empty) ? 1 : count;
        } finally {
            inputStream.close();
        }
    }
}
