import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.LocatedFileStatus;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.RemoteIterator;
import org.apache.hadoop.io.IOUtils;
import org.apache.hadoop.io.compress.CompressionCodec;
import org.apache.hadoop.io.compress.CompressionCodecFactory;
import org.apache.log4j.Logger;

import java.io.InputStream;

public class MultipleFilesProcessor {
    private static final Logger LOGGER = Logger.getLogger(MultipleFilesProcessor.class);
    private static final int BUFF_SIZE = 4096;

    public static void main(String[] args) throws Exception {
        String uri = args[0];
        Configuration conf = new Configuration();
        FileSystem fs = FileSystem.get(conf);
        Path directoryPath = new Path(uri);
        RemoteIterator<LocatedFileStatus> locatedFileStatusRemoteIterator = fs.listFiles(directoryPath, true);

        while (locatedFileStatusRemoteIterator.hasNext()) {
            LocatedFileStatus next = locatedFileStatusRemoteIterator.next();
            Path inputPath = next.getPath();
            CompressionCodecFactory factory = new CompressionCodecFactory(conf);
            CompressionCodec codec = factory.getCodec(inputPath);
            if (codec == null) {
                LOGGER.error("No decompression codec found for: " + uri);
                System.exit(1);
            }

            String processingStarted = "Processing data from file " + inputPath.toString() + " ...";
            System.out.println(processingStarted);
            LOGGER.info(processingStarted);
            InputStream in = null;
            try {
                in = codec.createInputStream(fs.open(inputPath));
                IOUtils.copyBytes(in, System.out, BUFF_SIZE, false);
            } finally {
                IOUtils.closeStream(in);
            }

            String processingFinished = "Processing data from file " + inputPath.toString() + " completed";
            System.out.println(processingFinished);
            LOGGER.info(processingStarted);
        }
    }
}