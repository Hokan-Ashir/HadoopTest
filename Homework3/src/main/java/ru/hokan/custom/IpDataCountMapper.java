package ru.hokan.custom;

import eu.bitwalker.useragentutils.Browser;
import eu.bitwalker.useragentutils.UserAgent;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import ru.hokan.counters.BrowsersList;
import ru.hokan.counters.MATCH_COUNTER;

import java.io.IOException;

public class IpDataCountMapper extends Mapper<LongWritable, Text, Text, AverageTotalBytesWritable> {


    private static final int POSITION_NUMBER_OF_BYTES_IN_STRING = 9;
    private static final int POSITION_IP_ADDR_IN_STRING = 0;

    /**
     * {@inheritDoc}
     */
    @Override
    public void map(LongWritable key, Text valueIn, Context context)
            throws IOException, InterruptedException {
        String input = valueIn.toString();
        String[] split = input.split("\\s");
        String ipName = split[POSITION_IP_ADDR_IN_STRING];

        UserAgent userAgent = UserAgent.parseUserAgentString(input);
        Browser browser = userAgent.getBrowser();
        if (BrowsersList.INSTANCE.IE_BROWSERS.contains(browser)) {
            context.getCounter(MATCH_COUNTER.IE_BROWSER_COUNTER).increment(1);
        } else if (BrowsersList.INSTANCE.FIREFOX_BROWSERS.contains(browser)) {
            context.getCounter(MATCH_COUNTER.MOZZILA_BROWSER_COUNTER).increment(1);
        } else {
            context.getCounter(MATCH_COUNTER.OTHER_BROWSER_COUNTER).increment(1);
        }

        String numberOfBytesStringPart = split[POSITION_NUMBER_OF_BYTES_IN_STRING];
        if (numberOfBytesStringPart.equals("-")) {
            return;
        }
        Integer numberOfBytes = Integer.valueOf(numberOfBytesStringPart);
        context.write(new Text(ipName), new AverageTotalBytesWritable(numberOfBytes, numberOfBytes));
    }
}