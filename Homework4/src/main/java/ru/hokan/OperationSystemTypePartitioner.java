package ru.hokan;

import org.apache.hadoop.classification.InterfaceAudience;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.mapreduce.Partitioner;

@InterfaceAudience.Public
public class OperationSystemTypePartitioner extends Partitioner<OSTypeCityIdWritable, IntWritable> {

    /**
     * {@inheritDoc}
     */
    @Override
    public int getPartition(OSTypeCityIdWritable key, IntWritable value, int numPartitions) {
        return Math.abs(key.getOsTypeName().hashCode()) % numPartitions;
    }
}
