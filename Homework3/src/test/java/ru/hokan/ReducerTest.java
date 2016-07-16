package ru.hokan;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mrunit.mapreduce.ReduceDriver;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ReducerTest {
    private ReduceDriver<Text, Text, Text, Text> driver;

    @Before
    public void setUp() {
        driver = ReduceDriver.newReduceDriver(new IpDataCountReducer());
    }

    @Test
    public void shouldProcessValidRecord() throws IOException, InterruptedException {
        List<Text> inputValue = new ArrayList<Text>();
        inputValue.add(new Text("58136.0 58136"));
        driver.withInput(new Text("ip2"), inputValue)
                .withOutput(new Text("ip2"), new Text("58136.0,58136"))
                .runTest();
    }

    @Test
    public void shouldProcessValidRecordsSameIp() throws IOException,
            InterruptedException {
        List<Text> inputValue = new ArrayList<Text>();
        inputValue.add(new Text("58136.0 58136"));
        inputValue.add(new Text("55.0 55"));
        driver.withInput(new Text("ip2"), inputValue)
                .withOutput(new Text("ip2"), new Text("29095.5,58191"))
                .runTest();
    }

    @Test
    public void shouldProcessValidRecordsDifferentIp() throws IOException,
            InterruptedException {
        List<Text> inputValueIP2 = new ArrayList<Text>();
        inputValueIP2.add(new Text("58136.0 58136"));
        driver.addInput(new Text("ip2"), inputValueIP2);

        List<Text> inputValueIP3 = new ArrayList<Text>();
        inputValueIP3.add(new Text("58136.0 58136"));
        driver.addInput(new Text("ip3"), inputValueIP3);

        driver.addOutput(new Text("ip2"), new Text("58136.0,58136"));
        driver.addOutput(new Text("ip3"), new Text("58136.0,58136"));

        driver.runTest();
    }

    @Test
    public void shouldProcessValidRecordZeroBytes() throws IOException, InterruptedException {
        List<Text> inputValue = new ArrayList<Text>();
        inputValue.add(new Text("0.0 0"));
        driver.withInput(new Text("ip2"), inputValue)
                .withOutput(new Text("ip2"), new Text("0.0,0"))
                .runTest();
    }

    @Test(expected = ArrayIndexOutOfBoundsException.class)
    public void shouldFailProcessingInValidRecordNotEnoughData() throws IOException, InterruptedException {
        List<Text> inputValue = new ArrayList<Text>();
        inputValue.add(new Text("0.0"));
        driver.withInput(new Text("ip2"), inputValue)
                .withOutput(new Text("ip2"), new Text("0.0,0"))
                .runTest();
    }

    @Test(expected = NumberFormatException.class)
    public void shouldFailProcessingInValidRecord() throws IOException, InterruptedException {
        List<Text> inputValue = new ArrayList<Text>();
        inputValue.add(new Text("-"));
        driver.withInput(new Text("ip2"), inputValue)
                .withOutput(new Text("ip2"), new Text("0.0 0"))
                .runTest();
    }
}
