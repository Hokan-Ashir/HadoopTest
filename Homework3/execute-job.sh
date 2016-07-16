#!/bin/bash

INPUT_JOB_PATH=job_input
OUTPUT_JOB_PATH=job_output
FILE_NAMES_ARRAY[0]=000000

cd $HADOOP_PREFIX

while [ -z `jps | grep NodeManager` ]
do
    echo "Waiting for NodeManager to start ..."
    sleep 3
done

echo "NodeManager started. Hadoop cluster initialized. Awaiting 30 sec. to get NameNode come from SafeMode state ..."
sleep 30
echo "NameNode came from SafeMode state"

echo "Staring history server"
./sbin/mr-jobhistory-daemon.sh start historyserver
echo "History server started"

bin/hdfs dfs -mkdir hdfs://$HOSTNAME:9000/$INPUT_JOB_PATH
for n in "${FILE_NAMES_ARRAY[@]}"; do
    echo "Coping ${n} from local docker FS to HDFS ..."
    bin/hdfs dfs -copyFromLocal /opt/${n} hdfs://$HOSTNAME:9000/$INPUT_JOB_PATH/${n}
    echo "Coping ${n} from local docker FS to HDFS complete"
done

export HADOOP_CLIENT_OPTS="-Xmx4g -Xmn1g -Xms4g $HADOOP_CLIENT_OPTS"
echo "Running a job ..."
bin/hadoop jar /opt/homework-3.jar IpBytesCounter hdfs://$HOSTNAME:9000/$INPUT_JOB_PATH/ hdfs://$HOSTNAME:9000/$OUTPUT_JOB_PATH
echo "MR job has finished"