import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.lib.input.FileSplit;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.StringTokenizer;

public class IpDataCountMapper extends Mapper<LongWritable, Text, Text, Text> {
    private static final Logger LOGGER = Logger.getLogger(IpDataCountMapper.class);

    /**
     * {@inheritDoc}
     */
    @Override
    public void map(LongWritable key, Text valueIn, Context context)
            throws IOException, InterruptedException {
        String input = valueIn.toString();
        LOGGER.info(input);
        context.write(new Text("lala"), new Text("da"));
    }
}