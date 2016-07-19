package ru.hokan.custom;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

import java.io.IOException;

public class IpDataCountReducer extends Reducer<Text, CountTotalBytesWritable, Text, Text> {

    /**
     * {@inheritDoc}
     */
    @Override
    public void reduce(Text keyIn, Iterable<CountTotalBytesWritable> valuesIn, Context context)
            throws IOException, InterruptedException {

        Integer totalNumberOfBytes = 0;
        int totalNumberOfValues = 0;
        for (CountTotalBytesWritable writable : valuesIn) {
            int numberOfValues = writable.getNumberOfValues();
            int totalBytesCount = writable.getTotalValue();
            totalNumberOfBytes += totalBytesCount;
            totalNumberOfValues += numberOfValues;
        }

        float averageNumberOfBytes = (float) totalNumberOfBytes / totalNumberOfValues;
        context.write(keyIn, new Text(averageNumberOfBytes + "," + totalNumberOfBytes));
    }
}