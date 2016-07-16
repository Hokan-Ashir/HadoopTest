import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.log4j.Logger;

import java.io.IOException;

public class IpDataCountCombiner extends Reducer<Text, Text, Text, Text> {
    private static final Logger LOGGER = Logger.getLogger(IpDataCountMapper.class);

    /**
     * {@inheritDoc}
     */
    @Override
    public void reduce(Text keyIn, Iterable<Text> valuesIn, Context context)
            throws IOException, InterruptedException {

        int maximumFoundWordLength = Integer.MIN_VALUE;
        String maximumLengthWord = "";
        for(Text value: valuesIn) {
            String matchedWord = value.toString();
            int lengthOfWord = matchedWord.length();

            // TODO what if we find 2+ words with same maximum length?
            if (lengthOfWord > maximumFoundWordLength) {
                maximumFoundWordLength = lengthOfWord;
                maximumLengthWord = matchedWord;
            }
        }

        LOGGER.info("Found longest word \'" + maximumLengthWord + "\'(" + maximumFoundWordLength + ") in file \'" + keyIn + "\'");
        context.write(keyIn, new Text(maximumLengthWord));
    }
}