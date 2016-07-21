package ru.hokan;

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
        processFiesInSeparateThreads(configuration, fileSystem, locatedFileStatusRemoteIterator);

        Path outputDirectory = new Path(outputURI);
        RemoteIterator<LocatedFileStatus> listFiles = fileSystem.listFiles(outputDirectory, true);
        List<BufferedReader> readers = createBufferedReaders(fileSystem, listFiles);

        LOGGER.info("Size of readers: " + readers.size());

        TreeMap<Line, BufferedReader> currentTopRecordsFromBuffers = createBufferedReaderToLineMap(readers);

        for (Map.Entry<Line, BufferedReader> lineBufferedReaderEntry : currentTopRecordsFromBuffers.entrySet()) {
            LOGGER.info(lineBufferedReaderEntry.getKey().getNumberOfIps() + " : " + lineBufferedReaderEntry.getKey().getNameOfIp());
        }

        Map<String, Integer> resultMap = new HashMap<String, Integer>(NUMBER_OF_TOP_ELEMENTS_TO_PRINT_IN_HDFS);
        while (true) {
            Map.Entry<Line, BufferedReader> lineBufferedReaderEntry = currentTopRecordsFromBuffers.firstEntry();
            Integer value = lineBufferedReaderEntry.getKey().getNumberOfIps();
            String name = lineBufferedReaderEntry.getKey().getNameOfIp();

            addNewValueToMap(resultMap, value, name);

            readNewLineFromFile(currentTopRecordsFromBuffers, lineBufferedReaderEntry);

            if (currentTopRecordsFromBuffers.size() == 0) {
                break;
            }
        }

        List<Map.Entry<String, Integer>> entries = SortUtils.sortMapByValuesDescending(resultMap);
        try {
            writeTopNipInYouIdents(entries);
        } catch (URISyntaxException e) {
            LOGGER.error(e.getMessage(), e);
        }

        fileSystem.close();
    }

    private void processFiesInSeparateThreads(Configuration configuration, FileSystem fileSystem, RemoteIterator<LocatedFileStatus> locatedFileStatusRemoteIterator) throws IOException {
        ExecutorService service = Executors.newCachedThreadPool();

        List<FileProcessor> processors = new ArrayList<FileProcessor>();
        while (locatedFileStatusRemoteIterator.hasNext()) {
            LocatedFileStatus fileStatus = locatedFileStatusRemoteIterator.next();
            FileProcessor processor = new FileProcessor(fileStatus, fileSystem, configuration, outputURI);
            processors.add(processor);
        }

        try {
            service.invokeAll(processors);
        } catch (InterruptedException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    private List<BufferedReader> createBufferedReaders(FileSystem fileSystem, RemoteIterator<LocatedFileStatus> listFiles) throws IOException {
        List<BufferedReader> readers = new ArrayList<BufferedReader>();
        while (listFiles.hasNext()) {
            FSDataInputStream stream = fileSystem.open(listFiles.next().getPath());
            BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
            readers.add(reader);
        }
        return readers;
    }

    private TreeMap<Line, BufferedReader> createBufferedReaderToLineMap(List<BufferedReader> readers) throws IOException {
        TreeMap<Line, BufferedReader> currentTopRecordsFromBuffers = new TreeMap<Line, BufferedReader>();
        for (BufferedReader reader : readers) {
            String s = reader.readLine();
            putNewLineInBuffersMap(currentTopRecordsFromBuffers, reader, s);
        }
        return currentTopRecordsFromBuffers;
    }

    private void readNewLineFromFile(TreeMap<Line, BufferedReader> currentTopRecordsFromBuffers, Map.Entry<Line, BufferedReader> lineBufferedReaderEntry) throws IOException {
        BufferedReader reader = lineBufferedReaderEntry.getValue();
        currentTopRecordsFromBuffers.pollFirstEntry();
        String nextLine = reader.readLine();
        if (nextLine != null) {
            putNewLineInBuffersMap(currentTopRecordsFromBuffers, reader, nextLine);
        }
    }

    private void putNewLineInBuffersMap(TreeMap<Line, BufferedReader> currentTopRecordsFromBuffers, BufferedReader reader, String nextLine) {
        String[] split = nextLine.split("\\t");
        String ipInYouId = split[0];
        Integer ipInYouIdCount = Integer.valueOf(split[1]);
        Line line = new Line(ipInYouIdCount, ipInYouId);
        currentTopRecordsFromBuffers.put(line, reader);
    }

    private void addNewValueToMap(Map<String, Integer> resultMap, Integer value, String name) {
        Integer previousValue = resultMap.get(name);
        if (previousValue == null) {
            if (resultMap.size() < NUMBER_OF_TOP_ELEMENTS_TO_PRINT_IN_HDFS) {
                resultMap.put(name, value);
            } else {
                String lowestValueMapKey = getLowestValueMapKey(resultMap);
                Integer lowestValue = resultMap.get(lowestValueMapKey);
                if (value > lowestValue) {
                    resultMap.put(name, value);
                    resultMap.remove(lowestValueMapKey);
                }
            }
        } else {
            resultMap.put(name, previousValue + value);
        }
    }

    private String getLowestValueMapKey(Map<String, Integer> map) {
        int minimumValue = Integer.MAX_VALUE;
        String minimumValueKey = null;
        for (Map.Entry<String, Integer> stringIntegerEntry : map.entrySet()) {
            Integer value = stringIntegerEntry.getValue();
            if (value < minimumValue) {
                minimumValue = value;
                minimumValueKey = stringIntegerEntry.getKey();
            }
        }

        return minimumValueKey;
    }

    private void writeTopNipInYouIdents(List<Map.Entry<String, Integer>> resultMap) throws URISyntaxException, IOException {
        // TODO rewrite via outputURI usage
        LOGGER.info("Writing top " + resultMap.size() + " ipInYou idents ...");
        String hostname = System.getenv("HOSTNAME");
        Configuration configuration = new Configuration();
        FileSystem fileSystem = FileSystem.get(new URI("hdfs://" + hostname + ":9000"), configuration);
        Path file = new Path("hdfs://" + hostname + ":9000/" + RESULT_FILE_NAME);
        if (fileSystem.exists(file)) {
            fileSystem.delete(file, true);
        }

        OutputStream outputStream = fileSystem.create(file);
        BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));
        // TODO can be rewritten via streams
        for (Map.Entry<String, Integer> stringIntegerEntry : resultMap) {
            bufferedWriter.write(stringIntegerEntry.getValue() + " : " + stringIntegerEntry.getKey());
            bufferedWriter.write('\n');
        }

        bufferedWriter.close();

        LOGGER.info("Writing top " + resultMap.size() + " ipInYou idents completed");
    }

    /**
     * Pair (value - name) compared reversed by value
     */
    private static class Line implements Comparable<Line> {
        private Integer numberOfIps;
        private String nameOfIp;

        public Line(Integer numberOfIps, String nameOfIp) {
            this.numberOfIps = numberOfIps;
            this.nameOfIp = nameOfIp;
        }

        public Integer getNumberOfIps() {
            return numberOfIps;
        }

        public String getNameOfIp() {
            return nameOfIp;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int compareTo(Line o) {
            return o.getNumberOfIps().compareTo(numberOfIps);
        }
    }
}
