package ru.hokan.custom;

import org.apache.hadoop.classification.InterfaceAudience;
import org.apache.hadoop.io.WritableComparable;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

@InterfaceAudience.Public
public class CountTotalBytesWritable implements WritableComparable<CountTotalBytesWritable> {

    private int numberOfValues;
    private int totalValue;

    public CountTotalBytesWritable() {
    }

    public CountTotalBytesWritable(int numberOfValues, int totalValue) {
        this.numberOfValues = numberOfValues;
        this.totalValue = totalValue;
    }

    public int getNumberOfValues() {
        return numberOfValues;
    }

    public int getTotalValue() {
        return totalValue;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int compareTo(CountTotalBytesWritable o) {
        int thisNumberOfValues = this.numberOfValues;
        int thatNumberOfValues = o.numberOfValues;
        if (thisNumberOfValues == thatNumberOfValues) {
            int thisTotalValue = this.totalValue;
            int thatTotalValue = o.totalValue;
            return thisTotalValue == thatTotalValue ? 0 : (thisTotalValue < thatTotalValue ? -1 : 1);
        }

        return thisNumberOfValues < thatNumberOfValues ? -1 : 1;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void write(DataOutput dataOutput) throws IOException {
        dataOutput.writeInt(numberOfValues);
        dataOutput.writeInt(totalValue);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void readFields(DataInput dataInput) throws IOException {
        numberOfValues = dataInput.readInt();
        totalValue = dataInput.readInt();
    }
}
