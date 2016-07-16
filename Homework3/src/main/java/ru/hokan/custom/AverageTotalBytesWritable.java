package ru.hokan.custom;

import org.apache.hadoop.classification.InterfaceAudience;
import org.apache.hadoop.io.WritableComparable;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

@InterfaceAudience.Public
public class AverageTotalBytesWritable implements WritableComparable<AverageTotalBytesWritable> {

    private float averageValue;
    private int totalValue;

    public AverageTotalBytesWritable() {
    }

    public AverageTotalBytesWritable(float averageValue, int totalValue) {
        this.averageValue = averageValue;
        this.totalValue = totalValue;
    }

    public float getAverageValue() {
        return averageValue;
    }

    public int getTotalValue() {
        return totalValue;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int compareTo(AverageTotalBytesWritable o) {
        float thisAverageValue = this.averageValue;
        int thisTotalValue = this.totalValue;
        float thatAverageValue = o.averageValue;
        int thatTotalValue = o.totalValue;
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
        dataOutput.writeFloat(averageValue);
        dataOutput.writeInt(totalValue);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void readFields(DataInput dataInput) throws IOException {
        averageValue = dataInput.readFloat();
        totalValue = dataInput.readInt();
    }
}
