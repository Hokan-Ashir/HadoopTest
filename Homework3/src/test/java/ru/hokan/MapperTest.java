package ru.hokan;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mrunit.mapreduce.MapDriver;
import org.junit.Before;
import org.junit.Test;
import ru.hokan.text.IpDataCountMapper;

import java.io.IOException;

public class MapperTest {

    private MapDriver<LongWritable, Text, Text, Text> driver;

    @Before
    public void setUp() {
        driver = MapDriver.newMapDriver(new IpDataCountMapper());
    }

    @Test
    public void shouldProcessValidRecord() throws IOException, InterruptedException {
        Text value = new Text("ip2 - - [24/Apr/2011:04:20:11 -0400] \"GET /sun_ss5/ss5_int.jpg HTTP/1.1\" 200 58136 \"http://host2/sun_ss5/\" \"Mozilla/5.0 (Windows; U; Windows NT 6.1; en-US; rv:1.9.2.16) Gecko/20110319 Firefox/3.6.16\"");
        driver.withInput(new LongWritable(0), value)
                .withOutput(new Text("ip2"), new Text("58136"))
                .runTest();
    }

    @Test
    public void shouldProcessInvalidRecord() throws IOException,
            InterruptedException {
        Text value = new Text("ip2 - - [24/Apr/2011:04:20:11 -0400] \"GET /sun_ss5/ss5_int.jpg HTTP/1.1\" 200 - \"http://host2/sun_ss5/\" \"Mozilla/5.0 (Windows; U; Windows NT 6.1; en-US; rv:1.9.2.16) Gecko/20110319 Firefox/3.6.16\"");
        driver.withInput(new LongWritable(0), value)
                .runTest();
    }

    @Test(expected = NumberFormatException.class)
    public void shouldFailProcessingInvalidRecord() throws IOException,
            InterruptedException {
        Text value = new Text("ip2 - - [24/Apr/2011:04:20:11 -0400] \"GET /sun_ss5/ss5_int.jpg HTTP/1.1\" 200 \"-\" \"http://host2/sun_ss5/\" \"Mozilla/5.0 (Windows; U; Windows NT 6.1; en-US; rv:1.9.2.16) Gecko/20110319 Firefox/3.6.16\"");
        driver.withInput(new LongWritable(0), value)
                .runTest();
    }
}
