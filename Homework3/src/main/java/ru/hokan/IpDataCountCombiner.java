package ru.hokan;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

import java.io.IOException;

public class IpDataCountCombiner extends Reducer<Text, Text, Text, Text> {

    /**
     * {@inheritDoc}
     */
    @Override
    public void reduce(Text keyIn, Iterable<Text> valuesIn, Context context)
            throws IOException, InterruptedException {

        Integer totalNumberOfBytes = 0;
        int numberOfValues = 0;
        for (Text text : valuesIn) {
            totalNumberOfBytes += Integer.valueOf(text.toString());
            numberOfValues++;
        }

        float averageNumberOfBytes = (float) totalNumberOfBytes / numberOfValues;
        context.write(keyIn, new Text(averageNumberOfBytes + " " + totalNumberOfBytes));
    }
}