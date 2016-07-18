package ru.hokan;

import org.apache.hadoop.classification.InterfaceAudience;
import org.apache.hadoop.io.WritableComparable;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

@InterfaceAudience.Public
public class OSTypeCityIdWritable implements WritableComparable<OSTypeCityIdWritable> {

    private String osTypeName;
    private Integer numberOfImpressions;

    public OSTypeCityIdWritable() {
    }

    public OSTypeCityIdWritable(String osTypeName, int numberOfImpressions) {
        this.osTypeName = osTypeName;
        this.numberOfImpressions = numberOfImpressions;
    }

    public String getOsTypeName() {
        return osTypeName;
    }

    public Integer getNumberOfImpressions() {
        return numberOfImpressions;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int compareTo(OSTypeCityIdWritable o) {
        String thisOsNameValue = this.osTypeName;
        Integer thisCityIdValue = this.numberOfImpressions;
        String thatOsNameValue = o.osTypeName;
        Integer thatCityIdValue = o.numberOfImpressions;

        int osNameCompare = thisOsNameValue.compareTo(thatOsNameValue);
        int cityIdCompare = thisCityIdValue.compareTo(thatCityIdValue);
        if (osNameCompare == -1 || cityIdCompare == -1) {
            return -1;
        } else if (osNameCompare == 0 && cityIdCompare == 0) {
            return 0;
        } else {
            return 1;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void write(DataOutput dataOutput) throws IOException {
        dataOutput.writeUTF(osTypeName);
        dataOutput.writeInt(numberOfImpressions);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void readFields(DataInput dataInput) throws IOException {
        osTypeName = dataInput.readUTF();
        numberOfImpressions = dataInput.readInt();
    }
}
