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

public class Grep {

    public static void main(String[] args)
            throws IOException, ClassNotFoundException, InterruptedException {
        if(args.length != 3) {
            System.out.println("Usage: <inDir> <outDir> <regex>");
            ToolRunner.printGenericCommandUsage(System.out);
            System.exit(-1);
        }

        Configuration config = new Configuration();
        
       
        /* Сохраняем регулярное выражение для map() метода с ключом regex. */
        config.set("regex", args[2]);

        Job job = new Job(config, "grep");
        
        /*
         * Для запуска программы из jar-файла необходимо указать любой
         * класс из вашего приложения.
         */
        job.setJarByClass(Grep.class);

        FileInputFormat.addInputPath(job, new Path(args[0]));
        FileOutputFormat.setOutputPath(job, new Path(args[1]));
       
        /*
         * Вот. TextInputFormat разбивает входные файлы на строки и подает их
         * в качестве аргумента map функциям. В качестве
         * разделителя используется перевод строки "\n".
         */
        job.setInputFormatClass(TextInputFormat.class);
        job.setOutputFormatClass(TextOutputFormat.class);

        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(Text.class);

        job.setMapperClass(RegexMapper.class);
        job.setReducerClass(RegexReducer.class);

        job.waitForCompletion(true);
    }
}