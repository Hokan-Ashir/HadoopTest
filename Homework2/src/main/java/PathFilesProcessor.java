import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.LocatedFileStatus;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.RemoteIterator;
import org.apache.hadoop.util.Progressable;
import org.apache.log4j.Logger;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class PathFilesProcessor {

    private static final Logger LOGGER = Logger.getLogger(PathFilesProcessor.class);
    private static final int NUMBER_OF_TOP_ELEMENTS_TO_PRINT_IN_HDFS = 100;
    private static final String RESULT_FILE_NAME = "bid_result.txt";

    private final String uri;

    public PathFilesProcessor(String uri) {
        this.uri = uri;
    }

    public void run() throws IOException {
        Configuration configuration = new Configuration();
        FileSystem fileSystem = FileSystem.get(configuration);
        Path directoryPath = new Path(uri);
        RemoteIterator<LocatedFileStatus> locatedFileStatusRemoteIterator = fileSystem.listFiles(directoryPath, true);

        processFiles(configuration, fileSystem, locatedFileStatusRemoteIterator);
    }

    private void processFiles(Configuration configuration, FileSystem fileSystem, RemoteIterator<LocatedFileStatus> locatedFileStatusRemoteIterator) throws IOException {
        ExecutorService service = Executors.newCachedThreadPool();
        Set<Future<Set<String>>> futureSet = new HashSet<Future<Set<String>>>();

        while (locatedFileStatusRemoteIterator.hasNext()) {
            LocatedFileStatus fileStatus = locatedFileStatusRemoteIterator.next();
            FileProcessor processor = new FileProcessor(fileStatus, fileSystem, configuration);
            Future<Set<String>> future = service.submit(processor);
            futureSet.add(future);
        }

        Set<String> stringSet = printFilesIpInYouIdents(futureSet);
        try {
            writeTopNipInYouIdents(NUMBER_OF_TOP_ELEMENTS_TO_PRINT_IN_HDFS, stringSet);
        } catch (URISyntaxException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    private Set<String> printFilesIpInYouIdents(Set<Future<Set<String>>> futureSet) {
        Set<String> combinedSet = new TreeSet<String>(Collections.<String>reverseOrder());

        for (Future<Set<String>> integerFuture : futureSet) {
            try {
                Set<String> value = integerFuture.get();
                combinedSet.addAll(value);
            } catch (InterruptedException e) {
                LOGGER.error(e.getMessage(), e);
            } catch (ExecutionException e) {
                LOGGER.error(e.getMessage(), e);
            }
        }

        System.out.println("Resulted descended sorted set of ipInYouIdents:");
        for (String s : combinedSet) {
            LOGGER.info(s);
        }

        return combinedSet;
    }

    private void writeTopNipInYouIdents(int count, Set<String> identSet) throws URISyntaxException, IOException {
        String hostname = System.getenv("HOSTNAME");
        Configuration configuration = new Configuration();
        FileSystem fileSystem = FileSystem.get(new URI("hdfs://" + hostname + ":9000"), configuration);
        Path file = new Path("hdfs://" + hostname + ":9000/" + RESULT_FILE_NAME);
        if (fileSystem.exists(file)) {
            fileSystem.delete(file, true);
        }

        OutputStream outputStream = fileSystem.create(file);
        BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));
        int i = 0;
        for (Iterator<String> iterator = identSet.iterator(); iterator.hasNext() && i < count; ++i) {
            bufferedWriter.write(iterator.next());
            bufferedWriter.write('\n');
        }
        bufferedWriter.close();
        fileSystem.close();
    }
}
