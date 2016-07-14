import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.LocatedFileStatus;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.RemoteIterator;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class PathFilesProcessor {

    private static final Logger LOGGER = Logger.getLogger(PathFilesProcessor.class);

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
        FutureComparator comparator = new FutureComparator();
        Set<Future<Integer>> futureSet = new TreeSet<Future<Integer>>(comparator);

        while (locatedFileStatusRemoteIterator.hasNext()) {
            LocatedFileStatus fileStatus = locatedFileStatusRemoteIterator.next();
            FileProcessor processor = new FileProcessor(fileStatus, fileSystem, configuration);
            Future<Integer> future = service.submit(processor);
            futureSet.add(future);
        }

        printFilesLinesCount(futureSet);
    }

    private void printFilesLinesCount(Set<Future<Integer>> futureSet) {
        System.out.println("Resulted descended sorted set of line count");
        for (Future<Integer> integerFuture : futureSet) {
            try {
                Integer value = integerFuture.get();
                System.out.println(value);
                LOGGER.info(value);
            } catch (InterruptedException e) {
                LOGGER.error(e.getMessage(), e);
            } catch (ExecutionException e) {
                LOGGER.error(e.getMessage(), e);
            }
        }
    }

    private static class FutureComparator implements Comparator<Future<Integer>> {

        /**
         * {@inheritDoc}
         */
        @Override
        public int compare(Future<Integer> o1, Future<Integer> o2) {
            try {
                return o2.get().compareTo(o1.get());
            } catch (InterruptedException e) {
                LOGGER.error(e.getMessage(), e);
                return 1;
            } catch (ExecutionException e) {
                LOGGER.error(e.getMessage(), e);
                return 1;
            }
        }
    }
}
