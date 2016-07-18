package ru.hokan;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

import java.io.IOException;

public class CityImpressionsCombiner extends Reducer<IntWritable, OSTypeCityIdWritable, IntWritable, OSTypeCityIdWritable> {

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

        context.write(keyIn, new OSTypeCityIdWritable("", totalNumberOfImpressions));
    }
}