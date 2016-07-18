package ru.hokan;

import org.apache.hadoop.classification.InterfaceAudience;
import org.apache.hadoop.io.WritableComparable;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

@InterfaceAudience.Public
public class RegionCityWritable implements WritableComparable<RegionCityWritable> {

    private Integer regionValue;
    private Integer cityValue;

    public RegionCityWritable() {
    }

    public RegionCityWritable(int regionValue, int cityValue) {
        this.regionValue = regionValue;
        this.cityValue = cityValue;
    }

    public Integer getRegionValue() {
        return regionValue;
    }

    public Integer getCityValue() {
        return cityValue;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int compareTo(RegionCityWritable o) {
        float thisAverageValue = this.regionValue;
        int thisTotalValue = this.cityValue;
        float thatAverageValue = o.regionValue;
        int thatTotalValue = o.cityValue;
        if (thisAverageValue < thatAverageValue || thisTotalValue < thatTotalValue) {
            return -1;
        } else if (thisAverageValue == thatAverageValue && thisTotalValue == thatTotalValue) {
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
        dataOutput.writeFloat(regionValue);
        dataOutput.writeInt(cityValue);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void readFields(DataInput dataInput) throws IOException {
        regionValue = dataInput.readInt();
        cityValue = dataInput.readInt();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return regionValue + " " + cityValue;
    }
}
