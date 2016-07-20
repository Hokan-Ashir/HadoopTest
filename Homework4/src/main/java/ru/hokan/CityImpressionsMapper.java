package ru.hokan;

import eu.bitwalker.useragentutils.UserAgent;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

import java.io.IOException;

public class CityImpressionsMapper extends Mapper<LongWritable, Text, OSTypeCityIdWritable, IntWritable> {

    private static final int POSITION_CITY = 7;
    private static final int POSITION_BID_PRICE = 19;
    private static final int LIMIT_BID_PRICE_COUNT = 250;

    /**
     * {@inheritDoc}
     */
    @Override
    public void map(LongWritable key, Text valueIn, Context context)
            throws IOException, InterruptedException {
        String input = valueIn.toString();
        String[] split = input.split("\\t");
        String bidPrice = split[POSITION_BID_PRICE];
        if (Integer.valueOf(bidPrice) < LIMIT_BID_PRICE_COUNT) {
            return;
        }

        String city = split[POSITION_CITY];
        Integer cityValue = Integer.valueOf(city);

        UserAgent userAgent = UserAgent.parseUserAgentString(input);
        String osName = userAgent.getOperatingSystem().getName();

        context.write(new OSTypeCityIdWritable(osName, cityValue), new IntWritable(1));
    }
}