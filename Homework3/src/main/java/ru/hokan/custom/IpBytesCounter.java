package ru.hokan.custom;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.compress.CompressionCodec;
import org.apache.hadoop.io.compress.SnappyCodec;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.NLineInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

public class IpBytesCounter extends Configured implements Tool {

    public static void main(String[] args) throws Exception {
        if (args.length != 2) {
            System.out.println("Usage: <inDir> <outDir>");
            ToolRunner.printGenericCommandUsage(System.out);
            System.exit(-1);
        }

        int res = ToolRunner.run(new Configuration(), new IpBytesCounter(), args);
        System.exit(res);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int run(String[] args) throws Exception {
        Configuration config = new Configuration();
        config.set("mapred.textoutputformat.separator", ",");
        config.setBoolean("mapred.output.compress", true);
        config.setClass("mapred.output.compression.codec", SnappyCodec.class, CompressionCodec.class);
        config.setInt(NLineInputFormat.LINES_PER_MAP, 1000);

        Job job = Job.getInstance(config);
        job.setJarByClass(IpBytesCounter.class);

        FileInputFormat.addInputPath(job, new Path(args[0]));
        FileOutputFormat.setOutputPath(job, new Path(args[1]));

        job.setInputFormatClass(NLineInputFormat.class);
        job.setOutputFormatClass(TextOutputFormat.class);

        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(Text.class);
        job.setMapOutputKeyClass(Text.class);
        job.setMapOutputValueClass(AverageTotalBytesWritable.class);

        job.setMapperClass(IpDataCountMapper.class);
        job.setReducerClass(IpDataCountReducer.class);
        job.setCombinerClass(IpDataCountCombiner.class);

        return job.waitForCompletion(true) ? 0 : 1;
    }
}