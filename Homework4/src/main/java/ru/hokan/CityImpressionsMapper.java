package ru.hokan;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

import java.io.IOException;

public class CityImpressionsMapper extends Mapper<LongWritable, Text, IntWritable, IntWritable> {

    private static final int POSITION_CITY = 7;

    /**
     * {@inheritDoc}
     */
    @Override
    public void map(LongWritable key, Text valueIn, Context context)
            throws IOException, InterruptedException {
        String input = valueIn.toString();
        String[] split = input.split("\\t");
        String city = split[POSITION_CITY];
        Integer cityValue = Integer.valueOf(city);

        context.write(new IntWritable(cityValue), new IntWritable(1));
    }
}