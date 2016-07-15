import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.LocatedFileStatus;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.RemoteIterator;
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
        Set<Future<Map<String, Integer>>> futureSet = new HashSet<Future<Map<String, Integer>>>();

        while (locatedFileStatusRemoteIterator.hasNext()) {
            LocatedFileStatus fileStatus = locatedFileStatusRemoteIterator.next();
            FileProcessor processor = new FileProcessor(fileStatus, fileSystem, configuration);
            Future<Map<String, Integer>> future = service.submit(processor);
            futureSet.add(future);
        }

        Map<String, Integer> combinedIdentsMap = createCombinedMapOfUniqueIdents(futureSet);
        List<Map.Entry<String, Integer>> sortedDescendedEntities = sortMapByValuesDescending(combinedIdentsMap);
        try {
            writeTopNipInYouIdents(NUMBER_OF_TOP_ELEMENTS_TO_PRINT_IN_HDFS, sortedDescendedEntities);
        } catch (URISyntaxException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    private Map<String, Integer> createCombinedMapOfUniqueIdents(Set<Future<Map<String, Integer>>> futureSet) {
        LOGGER.info("Creating combined map of unique idents ...");
        Map<String, Integer> combinedMap = new TreeMap<String, Integer>(Collections.<String>reverseOrder());

        for (Future<Map<String, Integer>> integerFuture : futureSet) {
            try {
                Map<String, Integer> value = integerFuture.get();
                for (Map.Entry<String, Integer> stringIntegerEntry : value.entrySet()) {
                    Integer previousValue = combinedMap.get(stringIntegerEntry.getKey());
                    if (previousValue == null) {
                        combinedMap.put(stringIntegerEntry.getKey(), stringIntegerEntry.getValue());
                    } else {
                        combinedMap.put(stringIntegerEntry.getKey(), previousValue + stringIntegerEntry.getValue());
                    }
                }
            } catch (InterruptedException e) {
                LOGGER.error(e.getMessage(), e);
            } catch (ExecutionException e) {
                LOGGER.error(e.getMessage(), e);
            }
        }

        LOGGER.info("Combined map of unique idents creation completed");

        return combinedMap;
    }

    private List<Map.Entry<String, Integer>> sortMapByValuesDescending(Map<String, Integer> map) {

        List<Map.Entry<String, Integer>> sortedEntries = new ArrayList<Map.Entry<String, Integer>>(map.entrySet());

        Collections.sort(sortedEntries, new Comparator<Map.Entry<String, Integer>>() {
                    @Override
                    public int compare(Map.Entry<String, Integer> e1, Map.Entry<String, Integer> e2) {
                        return e2.getValue().compareTo(e1.getValue());
                    }
                }
        );

        return sortedEntries;
    }

    private void writeTopNipInYouIdents(int count, List<Map.Entry<String, Integer>> entryList) throws URISyntaxException, IOException {
        LOGGER.info("Writing top " + count + " ipInYou idents ...");
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
        for (Map.Entry<String, Integer> stringIntegerEntry : entryList) {
            bufferedWriter.write(stringIntegerEntry.getKey() + " : " + stringIntegerEntry.getValue());
            bufferedWriter.write('\n');
            i++;
            if (i >= count) {
                break;
            }
        }

        bufferedWriter.close();
        fileSystem.close();

        LOGGER.info("Writing top " + count + " ipInYou idents completed");
    }
}
