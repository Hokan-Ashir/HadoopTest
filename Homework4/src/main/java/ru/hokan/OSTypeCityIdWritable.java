package ru.hokan;

import org.apache.hadoop.classification.InterfaceAudience;
import org.apache.hadoop.io.DoubleWritable;
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
        String thisOsNameValue = this.osTypeName;
        Integer thisCityIdValue = this.cityId;
        String thatOsNameValue = o.osTypeName;
        Integer thatCityIdValue = o.cityId;

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
    public int hashCode() {
        return cityId.hashCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof OSTypeCityIdWritable)) {
            return false;
        }
        OSTypeCityIdWritable other = (OSTypeCityIdWritable)obj;
        return this.cityId.equals(other.cityId);
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
