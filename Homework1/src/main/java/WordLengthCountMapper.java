import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.lib.input.FileSplit;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WordLengthCountMapper extends Mapper<LongWritable, Text, Text, Text> {
    private static final Logger LOGGER = Logger.getLogger(WordLengthCountMapper.class);
    private Pattern pattern;
    // full file path
    private Text keyOut;

    /**
     * {@inheritDoc}
     */
    @Override
    public void setup(Context context) throws IOException {
//        TODO use StringTokenizer
        pattern = Pattern.compile("(\\w+)");

        Path filePath = ((FileSplit) context.getInputSplit()).getPath();
        keyOut = new Text(filePath.toString());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void map(LongWritable key, Text valueIn, Context context)
            throws IOException, InterruptedException {
        String input = valueIn.toString();
        Matcher matcher = pattern.matcher(input);

        int maximumFoundWordLength = Integer.MIN_VALUE;
        String maximumLengthWord = "";
        while (matcher.find()) {
            String matchedWord = matcher.group();
            int lengthOfWord = matchedWord.length();

            // TODO what if we find 2+ words with same maximum length?
            if (lengthOfWord > maximumFoundWordLength) {
                maximumFoundWordLength = lengthOfWord;
                maximumLengthWord = matchedWord;
            }
        }

        if (!maximumLengthWord.isEmpty()) {
            LOGGER.info("Found longest word \'" + maximumLengthWord + "\'(" + maximumFoundWordLength + ") in line \'" + key + "\' in file \'" + keyOut + "\'");
            context.write(keyOut, new Text(maximumLengthWord));
        }
    }
}