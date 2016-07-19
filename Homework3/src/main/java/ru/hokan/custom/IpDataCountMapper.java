package ru.hokan.custom;

import eu.bitwalker.useragentutils.Browser;
import eu.bitwalker.useragentutils.UserAgent;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import ru.hokan.counters.MATCH_COUNTER;

import java.io.IOException;

public class IpDataCountMapper extends Mapper<LongWritable, Text, Text, CountTotalBytesWritable> {

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

        String numberOfBytesStringPart = split[POSITION_NUMBER_OF_BYTES_IN_STRING];
        if (numberOfBytesStringPart.equals("-")) {
            context.getCounter(MATCH_COUNTER.RECORDS_NO_DATA).increment(1);
            return;
        }

        UserAgent userAgent = UserAgent.parseUserAgentString(input);
        Browser browser = userAgent.getBrowser();
        if (browser.getGroup().equals(Browser.IE)) {
            context.getCounter(MATCH_COUNTER.IE_BROWSER_COUNTER).increment(1);
        } else if (browser.getGroup().equals(Browser.FIREFOX)) {
            context.getCounter(MATCH_COUNTER.MOZZILA_BROWSER_COUNTER).increment(1);
        } else {
            context.getCounter(MATCH_COUNTER.OTHER_BROWSER_COUNTER).increment(1);
        }

        Integer numberOfBytes = Integer.valueOf(numberOfBytesStringPart);
        context.write(new Text(ipName), new CountTotalBytesWritable(numberOfBytes, numberOfBytes));
    }
}