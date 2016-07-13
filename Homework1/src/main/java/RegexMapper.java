/*
 * Маппер.
 * Пример построен с использованием нового API org.apache.hadoop.mapreduce.*
 */
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.lib.input.FileSplit;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*
 * LongWritable - Тип входного ключа(Номер строки).
 * Text - Тип входного значения (Строка под номером ключа).
 * Text - Тип выходного ключа(Полный путь к файлу).
 * Text - Тип выходного значения(Строка из файла с совпадением).
 */
public class RegexMapper extends Mapper<LongWritable, Text, Text, Text>{
    private Pattern pattern;
    private Text    keyOut; //В качестве ключа на выход будет взят полный путь к файлу.

    /*
     * Метод setup() вызывается перед вызовом метода map()(который переопределен ниже).
     * Используется для отделения подготовительных действий от самой map() функции.
     */
    @Override
    public void setup(Context context) throws IOException{
        /*
         * Берем сохраненный аргумент(регулярное выражение),
         * который был сохранен в Driver-классе(будет описан далее).
         */
        pattern = Pattern.compile(context.getConfiguration().get("regex"));

        /* Получаем полный путь входной строки файла (valueIn). */
        Path filePath = ((FileSplit) context.getInputSplit()).getPath();
        keyOut        = new Text(filePath.toString());
    }

    /*
     * Сам map() метод. Создаем матчер и ищем в строке совпадения. В случае нахожде-
     * ния записываем полный путь файла в качестве ключа(keyOut - получен в setup()
     * методе) и строку из этого файла в качестве значения(valueIn - получена 
     * как аргумент метода). 
     */
    @Override
    public void map(LongWritable key, Text valueIn, Context context)
            throws IOException, InterruptedException {
        Matcher matcher = pattern.matcher(valueIn.toString());
        
        /*
         * По скольку входным значением является только одна строка файла, то нам доста-
         * точно найти хотя бы одно совпадение, что бы строка подходила условиям поиска.
         */
        if (matcher.find())
            context.write(keyOut, valueIn); //Запись пары ключ значение
    }
}