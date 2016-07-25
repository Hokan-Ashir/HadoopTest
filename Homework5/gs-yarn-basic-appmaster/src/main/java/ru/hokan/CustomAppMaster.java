package ru.hokan;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.yarn.api.records.Container;
import org.springframework.yarn.am.AbstractEventingAppmaster;
import org.springframework.yarn.am.YarnAppmaster;
import org.springframework.yarn.am.allocate.AbstractAllocator;
import org.springframework.yarn.am.allocate.ContainerAllocateData;
import org.springframework.yarn.am.allocate.DefaultContainerAllocator;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CustomAppMaster extends AbstractEventingAppmaster implements YarnAppmaster {

    private int amountOfRAM;
    private static final Log LOGGER = LogFactory.getLog(AppmasterApplication.class);

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onInit() throws Exception {
        super.onInit();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onContainerAllocated(Container container) {
        List<String> commands = new ArrayList<String>(getCommands());
        commands.add("-Xmx" + amountOfRAM + "m");
        commands.add("-Xms" + amountOfRAM + "m");
        getLauncher().launchContainer(container, commands);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void submitApplication() {
        registerAppmaster();
        start();
        if(getAllocator() instanceof AbstractAllocator) {
            ((AbstractAllocator)getAllocator()).setApplicationAttemptId(getApplicationAttemptId());
        }
    }

    public void runApplicationWithParameters(int amountOfRAM, int priority, int numberOfContainers) {
        LOGGER.info("Running " + numberOfContainers + " containers with priority level: " + priority + " and " + amountOfRAM + "Mb. RAM");
        DefaultContainerAllocator allocator = (DefaultContainerAllocator) getAllocator();
        allocator.setMemory(amountOfRAM);
        allocator.setPriority(priority);

        ContainerAllocateData data = new ContainerAllocateData();
        data.addAny(numberOfContainers);
        String allocationGroupId = UUID.randomUUID().toString();
        data.setId(allocationGroupId);

        allocator.setAllocationValues(allocationGroupId, priority, null, 1, amountOfRAM, false);

        this.amountOfRAM = amountOfRAM;
        allocator.allocateContainers(data);
    }
}
