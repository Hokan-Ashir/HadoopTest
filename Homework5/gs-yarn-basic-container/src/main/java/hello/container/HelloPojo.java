package hello.container;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.hadoop.fs.FsShell;
import org.springframework.yarn.annotation.OnContainerStart;
import org.springframework.yarn.annotation.YarnComponent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

@YarnComponent
public class HelloPojo {

    private static final int NUMBER_OF_GENERATED_RANDOM_DIGITS = 100000;
    private static final Log log = LogFactory.getLog(HelloPojo.class);

    @Autowired
    private Configuration configuration;

    @OnContainerStart
    public void publicVoidNoArgsMethod() throws Exception {
        log.info("Hello from HelloPojo!");
        log.info("About to list from hdfs root content");

        FsShell shell = new FsShell(configuration);
        for (FileStatus s : shell.ls(false, "/")) {
            log.info(s);
        }
        shell.close();

//        createAndSortDigits(NUMBER_OF_GENERATED_RANDOM_DIGITS);
    }

    private void createAndSortDigits(int numberOfDigits) {
        Random random = new Random();
        List<Integer> list = new ArrayList<Integer>();
        for (int i = 0; i < numberOfDigits; i++) {
            list.add(random.nextInt());
        }

        Collections.sort(list);
    }

}
