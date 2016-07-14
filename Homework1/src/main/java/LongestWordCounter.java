import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

import java.io.IOException;

public class LongestWordCounter extends Configured implements Tool {

    public static void main(String[] args) throws Exception {
        if(args.length != 2) {
            System.out.println("Usage: <inDir> <outDir>");
            ToolRunner.printGenericCommandUsage(System.out);
            System.exit(-1);
        }

        int res = ToolRunner.run(new Configuration(), new LongestWordCounter(), args);
        System.exit(res);
    }

    @Override
    public int run(String[] args) throws Exception {
        Configuration config = new Configuration();

        Job job = Job.getInstance(config);
        job.setJarByClass(LongestWordCounter.class);

        FileInputFormat.addInputPath(job, new Path(args[0]));
        FileOutputFormat.setOutputPath(job, new Path(args[1]));

        job.setInputFormatClass(TextInputFormat.class);
        job.setOutputFormatClass(TextOutputFormat.class);

        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(Text.class);

        job.setMapperClass(WordLengthCountMapper.class);
        job.setReducerClass(WordLengthCountReducer.class);

        return job.waitForCompletion(true) ? 0 : 1;
    }
}