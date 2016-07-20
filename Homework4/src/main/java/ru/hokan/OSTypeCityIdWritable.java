package ru.hokan;

import org.apache.hadoop.classification.InterfaceAudience;
import org.apache.hadoop.io.WritableComparable;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

@InterfaceAudience.Public
public class OSTypeCityIdWritable implements WritableComparable<OSTypeCityIdWritable> {

    private String osTypeName;
    private Integer cityId;

    public OSTypeCityIdWritable() {
    }

    public OSTypeCityIdWritable(String osTypeName, int cityId) {
        this.osTypeName = osTypeName;
        this.cityId = cityId;
    }

    public String getOsTypeName() {
        return osTypeName;
    }

    public Integer getCityId() {
        return cityId;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int compareTo(OSTypeCityIdWritable o) {
        return cityId.compareTo(o.getCityId());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void write(DataOutput dataOutput) throws IOException {
        dataOutput.writeUTF(osTypeName);
        dataOutput.writeInt(cityId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void readFields(DataInput dataInput) throws IOException {
        osTypeName = dataInput.readUTF();
        cityId = dataInput.readInt();
    }
}
