package ru.hokan;

import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class CityImpressionsReducer extends Reducer<IntWritable, OSTypeCityIdWritable, Text, Text> {

    private static final String CITY_FILE_NAME = "city.en.txt";
    private static final int CITY_KEY_IN_FILE_POSITION = 0;
    private static final int CITY_NAME_IN_FILE_POSITION = 1;
    private static final int LIMIT_IMPRESSIONS_COUNT = 250;

    /**
     * {@inheritDoc}
     */
    @Override
    public void reduce(IntWritable keyIn, Iterable<OSTypeCityIdWritable> valuesIn, Context context)
            throws IOException, InterruptedException {

        Integer totalNumberOfImpressions = 0;
        for (OSTypeCityIdWritable value : valuesIn) {
            totalNumberOfImpressions += value.getNumberOfImpressions();
        }

        if (totalNumberOfImpressions < LIMIT_IMPRESSIONS_COUNT) {
            return;
        }

        String cityName = getCityName(context, keyIn.get());
        context.write(new Text(cityName), new Text(totalNumberOfImpressions.toString()));
    }

    private String getCityName(Context context, int cityKey) throws IOException {
        String hostName = context.getConfiguration().get(CityImpressionsCounter.HOSTNAME_HDFS_ENV_VALUE);
        Path path = new Path("hdfs://" + hostName + ":9000/" + CITY_FILE_NAME);
        FileSystem fileSystem = FileSystem.get(context.getConfiguration());
        BufferedReader reader = new BufferedReader(new InputStreamReader(fileSystem.open(path)));
        String result = "";
        try {
            String line;
            line = reader.readLine();
            while (line != null) {
                String[] split = line.split("\\t");
                String cityKeyInFileString = split[CITY_KEY_IN_FILE_POSITION];
                Integer cityKeyInFile = Integer.valueOf(cityKeyInFileString);
                if (cityKeyInFile.equals(cityKey)) {
                    return split[CITY_NAME_IN_FILE_POSITION];
                } else {
                    line = reader.readLine();
                }
            }
        } finally {
            reader.close();
        }

        return result;
    }
}