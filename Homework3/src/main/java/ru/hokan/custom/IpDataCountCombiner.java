package ru.hokan.custom;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

import java.io.IOException;

public class IpDataCountCombiner extends Reducer<Text, AverageTotalBytesWritable, Text, AverageTotalBytesWritable> {

    /**
     * {@inheritDoc}
     */
    @Override
    public void reduce(Text keyIn, Iterable<AverageTotalBytesWritable> valuesIn, Context context)
            throws IOException, InterruptedException {

        Integer totalNumberOfBytes = 0;
        int numberOfValues = 0;
        for (AverageTotalBytesWritable writable : valuesIn) {
            totalNumberOfBytes += writable.getTotalValue();
            numberOfValues++;
        }

        float averageNumberOfBytes = (float) totalNumberOfBytes / numberOfValues;
        context.write(keyIn, new AverageTotalBytesWritable(averageNumberOfBytes, totalNumberOfBytes));
    }
}