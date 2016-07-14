import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.hadoop.util.ToolRunner;

import java.io.IOException;

//TODO rewrite to implement Tool, Configurable
public class LongestWordCounter {

    public static void main(String[] args)
            throws IOException, ClassNotFoundException, InterruptedException {
        if(args.length != 2) {
            System.out.println("Usage: <inDir> <outDir>");
            ToolRunner.printGenericCommandUsage(System.out);
            System.exit(-1);
        }

        Configuration config = new Configuration();

        Job job = new Job(config, "grep");
        job.setJarByClass(LongestWordCounter.class);

        FileInputFormat.addInputPath(job, new Path(args[0]));
        FileOutputFormat.setOutputPath(job, new Path(args[1]));

        job.setInputFormatClass(TextInputFormat.class);
        job.setOutputFormatClass(TextOutputFormat.class);

        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(Text.class);

        job.setMapperClass(WordLengthCountMapper.class);
        job.setReducerClass(WordLengthCountReducer.class);

        job.waitForCompletion(true);
    }
}