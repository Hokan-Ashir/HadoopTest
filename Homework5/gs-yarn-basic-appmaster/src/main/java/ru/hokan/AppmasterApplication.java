package ru.hokan;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.yarn.api.ApplicationConstants;
import org.apache.hadoop.yarn.api.records.ApplicationAttemptId;
import org.apache.hadoop.yarn.api.records.ApplicationId;
import org.apache.hadoop.yarn.api.records.ContainerId;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.yarn.am.StaticAppmaster;
import org.springframework.yarn.am.allocate.DefaultContainerAllocator;
import org.springframework.yarn.am.container.DefaultContainerLauncher;
import org.springframework.yarn.am.monitor.DefaultContainerMonitor;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;


@SpringBootApplication/*(exclude = YarnAppmasterAutoConfiguration.class)*/
public class AppmasterApplication extends StaticAppmaster {

    private static final Log LOGGER = LogFactory.getLog(AppmasterApplication.class);

    public AppmasterApplication() {
        Configuration configuration = new Configuration();
        setConfiguration(configuration);
        setAllocator(new DefaultContainerAllocator());
        setMonitor(new DefaultContainerMonitor());
        setLauncher(new DefaultContainerLauncher());
    }

    public static void main(String[] args) {
//        TODO YarnAppmaster
//        TODO YarnAppmasterBuilder
//        YarnAppmasterConfigurer
        SpringApplication.run(new Class<?>[]{AppmasterApplication.class, YarnController.class}, args);
    }

    public void runApplicationWithParameters(int amountOfRAM, int priority, int numberOfContainers) {
//        submitApplication();
//        setParameters();
//        getAllocator().allocateContainers(numberOfContainers);
//        getAllocator().
        LOGGER.info("Running " + numberOfContainers + " containers with priority level: " + priority + " and " + amountOfRAM + "Mb. RAM");
        DefaultContainerAllocator allocator = (DefaultContainerAllocator) getAllocator();
        allocator.setMemory(amountOfRAM);
        allocator.setPriority(priority);
//        allocator.setApplicationAttemptId(new ApplicationAttemptIdPBImpl());
//        ContainerAllocateData data = new ContainerAllocateData();
//        data.addHosts("172.17.0.2", numberOfContainers);

        Date date = new Date();

        ContainerId containerId = ContainerId.newContainerId(ApplicationAttemptId.newInstance(ApplicationId.newInstance(date.getTime(), 0), 0), 0);
        Map<String, String> env = new HashMap<String, String>();
        env.put(ApplicationConstants.Environment.CONTAINER_ID.name(), containerId.toString());
        setEnvironment(env);
//        getEnvironment().put(ApplicationConstants.Environment.CONTAINER_ID.name(), containerId.toString());
        registerAppmaster();
        start();

        allocator.allocateContainers(numberOfContainers);
    }

    public Integer getAmountOfRAMInMb() {
        return 1000;
    }

    public Integer getPriopiry() {
        return 1;
    }

    public Integer getNumberOfContainers() {
        return 2;
    }
}
