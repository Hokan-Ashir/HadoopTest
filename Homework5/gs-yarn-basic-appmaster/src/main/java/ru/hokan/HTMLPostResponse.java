package ru.hokan;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

public class HTMLPostResponse {
    @Min(256)
    @Max(4096)
    private String amountOfRAM;

    @Min(1)
    @Max(3)
    private String priority;

    @Min(1)
    @Max(5)
    private String numberOfContainers;

    public String getAmountOfRAM() {
        return amountOfRAM;
    }

    public void setAmountOfRAM(String amountOfRAM) {
        this.amountOfRAM = amountOfRAM;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getNumberOfContainers() {
        return numberOfContainers;
    }

    public void setNumberOfContainers(String numberOfContainers) {
        this.numberOfContainers = numberOfContainers;
    }
}
