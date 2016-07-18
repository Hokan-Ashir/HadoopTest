package ru.hokan;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

import java.io.IOException;

public class CityImpressionsReducer extends Reducer<RegionCityWritable, IntWritable, Text, Text> {

    /**
     * {@inheritDoc}
     */
    @Override
    public void reduce(RegionCityWritable keyIn, Iterable<IntWritable> valuesIn, Context context)
            throws IOException, InterruptedException {

        Integer totalNumberOfImpressions = 0;
        for (IntWritable value : valuesIn) {
            totalNumberOfImpressions += value.get();
        }

        context.write(new Text(keyIn.toString()), new Text(totalNumberOfImpressions.toString()));
    }
}