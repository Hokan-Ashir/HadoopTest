package ru.hokan.custom;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

import java.io.IOException;

public class IpDataCountReducer extends Reducer<Text, AverageTotalBytesWritable, Text, Text> {

    /**
     * {@inheritDoc}
     */
    @Override
    public void reduce(Text keyIn, Iterable<AverageTotalBytesWritable> valuesIn, Context context)
            throws IOException, InterruptedException {

        Integer totalNumberOfBytes = 0;
        int numberOfValues = 0;
        for (AverageTotalBytesWritable writable : valuesIn) {
            String value = writable.toString();
            String[] split = value.split("\\s");
            float averageBytesCount = writable.getAverageValue();
            int totalBytesCount = writable.getTotalValue();
            totalNumberOfBytes += totalBytesCount;
            if (averageBytesCount != 0) {
                numberOfValues += Math.ceil(totalBytesCount / averageBytesCount);
            } else {
                numberOfValues++;
            }
        }

        float averageNumberOfBytes = (float) totalNumberOfBytes / numberOfValues;
        context.write(keyIn, new Text(averageNumberOfBytes + "," + totalNumberOfBytes));
    }
}