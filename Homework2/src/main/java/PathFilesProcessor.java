import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.*;
import org.apache.hadoop.fs.FileSystem;
import org.apache.log4j.Logger;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PathFilesProcessor {

    private static final Logger LOGGER = Logger.getLogger(PathFilesProcessor.class);
    private static final int NUMBER_OF_TOP_ELEMENTS_TO_PRINT_IN_HDFS = 100;
    private static final String RESULT_FILE_NAME = "bid_result.txt";

    private final String inputURI;
    private final String outputURI;

    public PathFilesProcessor(String inputURI, String outputURI) {
        this.inputURI = inputURI;
        this.outputURI = outputURI;
    }

    public void run() throws IOException {
        Configuration configuration = new Configuration();
        FileSystem fileSystem = FileSystem.get(configuration);
        Path directoryPath = new Path(inputURI);
        RemoteIterator<LocatedFileStatus> locatedFileStatusRemoteIterator = fileSystem.listFiles(directoryPath, true);

        processFiles(configuration, fileSystem, locatedFileStatusRemoteIterator);
    }

    private void processFiles(Configuration configuration, FileSystem fileSystem, RemoteIterator<LocatedFileStatus> locatedFileStatusRemoteIterator) throws IOException {
        ExecutorService service = Executors.newCachedThreadPool();

        List<FileProcessor> processors = new ArrayList<FileProcessor>();
        while (locatedFileStatusRemoteIterator.hasNext()) {
            LocatedFileStatus fileStatus = locatedFileStatusRemoteIterator.next();
            FileProcessor processor = new FileProcessor(fileStatus, fileSystem, configuration);
            processors.add(processor);
        }

        try {
            service.invokeAll(processors);
        } catch (InterruptedException e) {
            LOGGER.error(e.getMessage(), e);
        }

        Map<String, Integer> stringSet = new TreeMap<String, Integer>(Collections.reverseOrder());
        ;
        Path outputDirectory = new Path(outputURI);
        FileStatus[] fileStatuses = fileSystem.listStatus(outputDirectory);
        List<BufferedReader> readers = new ArrayList<BufferedReader>();
        for (FileStatus fileStatus : fileStatuses) {
            FSDataInputStream stream = fileSystem.open(fileStatus.getPath());
            BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
            readers.add(reader);
        }

        for (int i = 0; i < FileProcessor.NUMBER_OF_PROCESSING_RECORDS; ++i) {
            for (BufferedReader reader : readers) {
                String s = reader.readLine();
                if (s == null) {
                    continue;
                }

                String[] split = s.split("\\t");
                String name = split[0];
                Integer value = Integer.valueOf(split[1]);
                Map.Entry entry = ((TreeMap) stringSet).lastEntry();
                if (entry != null) {
                    Integer minimumValue = (Integer) entry.getValue();
                    if (minimumValue != null && value < minimumValue) {
                        continue;
                    }
                }

                Integer integer = stringSet.get(name);
                if (integer == null) {
                    stringSet.put(name, value);
                } else {
                    stringSet.put(name, integer + value);
                }
            }
        }

        List<Map.Entry<String, Integer>> entries = sortMapByValuesDescending(stringSet);
        try {
            writeTopNipInYouIdents(NUMBER_OF_TOP_ELEMENTS_TO_PRINT_IN_HDFS, entries);
        } catch (URISyntaxException e) {
            LOGGER.error(e.getMessage(), e);
        }

        fileSystem.close();
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
//        TODO can be rewritten via streams
        for (Map.Entry<String, Integer> stringIntegerEntry : entryList) {
            bufferedWriter.write(stringIntegerEntry.getKey() + " : " + stringIntegerEntry.getValue());
            bufferedWriter.write('\n');
            i++;
            if (i >= count) {
                break;
            }
        }

        bufferedWriter.close();

        LOGGER.info("Writing top " + count + " ipInYou idents completed");
    }
}
