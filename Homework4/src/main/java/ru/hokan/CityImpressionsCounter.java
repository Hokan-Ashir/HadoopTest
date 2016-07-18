package ru.hokan;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.NLineInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

public class CityImpressionsCounter extends Configured implements Tool {

    public static final String HOSTNAME_HDFS_ENV_VALUE = "HOSTNAME";

    public static void main(String[] args) throws Exception {
        if (args.length != 2) {
            System.out.println("Usage: <inDir> <outDir>");
            ToolRunner.printGenericCommandUsage(System.out);
            System.exit(-1);
        }

        int res = ToolRunner.run(new Configuration(), new CityImpressionsCounter(), args);
        System.exit(res);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int run(String[] args) throws Exception {
        Configuration config = new Configuration();
        String hostname = System.getenv(HOSTNAME_HDFS_ENV_VALUE);
        config.set(HOSTNAME_HDFS_ENV_VALUE, hostname);
        config.setInt(NLineInputFormat.LINES_PER_MAP, 70000);

        Job job = Job.getInstance(config);
        job.setJarByClass(CityImpressionsCounter.class);

        FileInputFormat.addInputPath(job, new Path(args[0]));
        FileOutputFormat.setOutputPath(job, new Path(args[1]));

        job.setInputFormatClass(NLineInputFormat.class);
        job.setOutputFormatClass(TextOutputFormat.class);

        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(Text.class);
        job.setMapOutputKeyClass(IntWritable.class);
        job.setMapOutputValueClass(IntWritable.class);

        job.setMapperClass(CityImpressionsMapper.class);
        job.setReducerClass(CityImpressionsReducer.class);
        job.setCombinerClass(CityImpressionsCombiner.class);

        return job.waitForCompletion(true) ? 0 : 1;
    }
}