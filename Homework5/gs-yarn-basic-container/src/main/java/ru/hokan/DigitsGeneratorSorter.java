package ru.hokan;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.yarn.annotation.OnContainerStart;
import org.springframework.yarn.annotation.YarnComponent;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

@YarnComponent
public class DigitsGeneratorSorter {

    private static final int NUMBER_OF_GENERATED_RANDOM_DIGITS = 1000000000;
    private static final int NUMBER_OF_DIGITS_WRITE_TO_HDFS = 100;
    private static final Log LOGGER = LogFactory.getLog(DigitsGeneratorSorter.class);
    private static final String OUTPUT_FILE_NAME_PREFIX = "result";

    @Autowired
    private Configuration configuration;

    @OnContainerStart
    public void onContainerStart() throws Exception {
        List<Integer> integerList = createAndSortDigits(NUMBER_OF_GENERATED_RANDOM_DIGITS);
        writeRecordsToHDFS(integerList, NUMBER_OF_DIGITS_WRITE_TO_HDFS);
    }

    private List<Integer> createAndSortDigits(int numberOfDigits) {
        LOGGER.info("Creating list of " + NUMBER_OF_GENERATED_RANDOM_DIGITS + " sorted digits ...");
        Random random = new Random();
        List<Integer> list = new ArrayList<Integer>();
        for (int i = 0; i < numberOfDigits; i++) {
            list.add(Math.abs(random.nextInt()));
        }

        LOGGER.info("Sorting created list of " + NUMBER_OF_GENERATED_RANDOM_DIGITS + " ...");
        Collections.sort(list);
        LOGGER.info("Sorting created list of " + NUMBER_OF_GENERATED_RANDOM_DIGITS + " complete");

        LOGGER.info("Creation list of " + NUMBER_OF_GENERATED_RANDOM_DIGITS + " sorted digits complete");
        return list;
    }

    private void writeRecordsToHDFS(List<Integer> integerList, int numberOfIntegerToWrite) throws URISyntaxException, IOException {
        String outputFileName = OUTPUT_FILE_NAME_PREFIX + "/" + UUID.randomUUID().toString();
        LOGGER.info("Writing " + numberOfIntegerToWrite + " in sorted list to HDFS in /" + outputFileName + " ...");
//        TODO can be enhanced via
//        String hostname = System.getenv("HOSTNAME");

        String hostname = "172.17.0.2";
        FileSystem fileSystem = FileSystem.get(new URI("hdfs://" + hostname + ":9000"), configuration);
        Path file = new Path("hdfs://" + hostname + ":9000/" + outputFileName);
        if (fileSystem.exists(file)) {
            fileSystem.delete(file, true);
        }

        OutputStream outputStream = fileSystem.create(file);
        BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));
        // TODO can be rewritten via streams
        for (int i = 0; i < numberOfIntegerToWrite; i++) {
            bufferedWriter.write(String.valueOf(integerList.get(i)));
            bufferedWriter.write("\n");
        }

        bufferedWriter.close();
        fileSystem.close();

        LOGGER.info("Writing " + numberOfIntegerToWrite + " in sorted list to HDFS in /" + outputFileName + " complete");
    }

}
