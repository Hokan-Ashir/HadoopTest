package ru.hokan;

import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public class CityImpressionsReducer extends Reducer<OSTypeCityIdWritable, IntWritable, Text, Text> {

    private static final String CITY_FILE_NAME = "city.en.txt";
    private static final int CITY_KEY_IN_FILE_POSITION = 0;
    private static final int CITY_NAME_IN_FILE_POSITION = 1;

    private Map<Integer, String> cityIdToNameMap = new HashMap<Integer, String>();

    @Override
    protected void setup(Context context) throws IOException, InterruptedException {
        String hostName = context.getConfiguration().get(CityImpressionsCounter.HOSTNAME_HDFS_ENV_VALUE);
        Path path = new Path("hdfs://" + hostName + ":9000/" + CITY_FILE_NAME);
        FileSystem fileSystem = FileSystem.get(context.getConfiguration());
        BufferedReader reader = new BufferedReader(new InputStreamReader(fileSystem.open(path)));
        try {
            String line;
            line = reader.readLine();
            while (line != null) {
                String[] split = line.split("\\t");
                String cityKeyInFileString = split[CITY_KEY_IN_FILE_POSITION];
                Integer cityKeyInFile = Integer.valueOf(cityKeyInFileString);
                String cityName = split[CITY_NAME_IN_FILE_POSITION];
                cityIdToNameMap.put(cityKeyInFile, cityName);
                line = reader.readLine();
            }
        } finally {
            reader.close();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void reduce(OSTypeCityIdWritable keyIn, Iterable<IntWritable> valuesIn, Context context)
            throws IOException, InterruptedException {

        Integer totalNumberOfImpressions = 0;
        for (IntWritable value : valuesIn) {
            totalNumberOfImpressions += value.get();
        }

        String cityName = getCityName(keyIn.getCityId());
        context.write(new Text(cityName), new Text(totalNumberOfImpressions.toString()));
    }

    private String getCityName(int cityKey) throws IOException {
        if (cityIdToNameMap.containsKey(cityKey)) {
            return cityIdToNameMap.get(cityKey);
        } else {
            return "";
        }
    }
}