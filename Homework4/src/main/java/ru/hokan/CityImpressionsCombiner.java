package ru.hokan;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

import java.io.IOException;

public class CityImpressionsCombiner extends Reducer<OSTypeCityIdWritable, IntWritable, OSTypeCityIdWritable, IntWritable> {

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

        context.write(keyIn, new IntWritable(totalNumberOfImpressions));
    }
}