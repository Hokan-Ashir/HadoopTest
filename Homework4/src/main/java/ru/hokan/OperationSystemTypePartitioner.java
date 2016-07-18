package ru.hokan;

import org.apache.hadoop.classification.InterfaceAudience;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.mapreduce.Partitioner;

@InterfaceAudience.Public
public class OperationSystemTypePartitioner extends Partitioner<IntWritable, OSTypeCityIdWritable> {

    /**
     * {@inheritDoc}
     */
    @Override
    public int getPartition(IntWritable key, OSTypeCityIdWritable value, int numPartitions) {
        return Math.abs(value.getOsTypeName().hashCode()) % numPartitions;
    }
}
