package hello.appmaster;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;

@EnableAutoConfiguration
public class AppmasterApplication {

    public static void main(String[] args) {
//        TODO place here web server
        SpringApplication.run(AppmasterApplication.class, args);
    }

}
