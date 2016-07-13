import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

import java.io.IOException;

/* Все ключи и значения типа Text */
public class RegexReducer extends Reducer<Text, Text, Text, Text> {


    /* В методе мы форматируем полученные данные для записи в файл. */
    @Override
    public void reduce(Text keyIn, Iterable<Text> valuesIn, Context context)
            throws IOException, InterruptedException {
         
        /* Для конкатенации строк воспользуемся StringBuilder. */
        StringBuilder valueOut = new StringBuilder();

        for(Text value: valuesIn)
            valueOut.append("\n" + value.toString());
        valueOut.append("\n");

        context.write(keyIn, new Text(valueOut.toString()));
    }
}