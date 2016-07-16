import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

import java.io.IOException;

public class IpDataCountReducer extends Reducer<Text, Text, Text, Text> {

    /**
     * {@inheritDoc}
     */
    @Override
    public void reduce(Text keyIn, Iterable<Text> valuesIn, Context context)
            throws IOException, InterruptedException {

        Integer totalNumberOfBytes = 0;
        int numberOfValues = 0;
        for (Text text : valuesIn) {
            String value = text.toString();
            String[] split = value.split("\\s");
            float averageBytesCount = Float.parseFloat(split[0]);
            int totalBytesCount = Integer.parseInt(split[1]);
            totalNumberOfBytes += Integer.parseInt(split[1]);
            if (averageBytesCount != 0) {
                numberOfValues += Math.ceil(totalBytesCount / averageBytesCount);
            } else {
                numberOfValues++;
            }
        }

        float averageNumberOfBytes = (float) totalNumberOfBytes / numberOfValues;
        context.write(keyIn, new Text(averageNumberOfBytes + "," + totalNumberOfBytes));
    }
}