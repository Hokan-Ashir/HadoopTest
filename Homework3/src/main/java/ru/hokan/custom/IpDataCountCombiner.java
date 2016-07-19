package ru.hokan.custom;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

import java.io.IOException;

public class IpDataCountCombiner extends Reducer<Text, CountTotalBytesWritable, Text, CountTotalBytesWritable> {

    /**
     * {@inheritDoc}
     */
    @Override
    public void reduce(Text keyIn, Iterable<CountTotalBytesWritable> valuesIn, Context context)
            throws IOException, InterruptedException {

        Integer totalNumberOfBytes = 0;
        int numberOfValues = 0;
        for (CountTotalBytesWritable writable : valuesIn) {
            totalNumberOfBytes += writable.getTotalValue();
            numberOfValues++;
        }

        context.write(keyIn, new CountTotalBytesWritable(numberOfValues, totalNumberOfBytes));
    }
}