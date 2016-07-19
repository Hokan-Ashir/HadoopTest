package ru.hokan;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

import java.io.IOException;

public class WordLengthCountReducer extends Reducer<LongWritable, Text, Text, Text> {

    /**
     * {@inheritDoc}
     */
    @Override
    protected void reduce(LongWritable key, Iterable<Text> values, Context context) throws IOException, InterruptedException {

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void run(Context context) throws IOException, InterruptedException {
        context.nextKey();
        context.write(new Text(context.getCurrentKey().toString()), new Text(context.getValues().iterator().next()));
    }
}