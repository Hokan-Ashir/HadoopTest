package ru.hokan;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

import java.io.IOException;
import java.util.StringTokenizer;

public class WordLengthCountMapper extends Mapper<LongWritable, Text, LongWritable, Text> {

    /**
     * {@inheritDoc}
     */
    @Override
    public void map(LongWritable key, Text valueIn, Context context)
            throws IOException, InterruptedException {
        String input = valueIn.toString();
        StringTokenizer tokenizer = new StringTokenizer(input);

        while (tokenizer.hasMoreTokens()) {
            String matchedWord = tokenizer.nextToken();
            context.write(new LongWritable(matchedWord.length()), new Text(matchedWord));
        }
    }
}