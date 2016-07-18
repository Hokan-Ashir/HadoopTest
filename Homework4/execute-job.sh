#!/bin/bash

INPUT_JOB_PATH=job_input
OUTPUT_JOB_PATH=job_output
FILE_NAMES_ARRAY[0]=bid.20131019.txt.bz2
FILE_NAMES_ARRAY[1]=bid.20131020.txt.bz2
FILE_NAMES_ARRAY[2]=bid.20131021.txt.bz2
FILE_NAMES_ARRAY[3]=bid.20131022.txt.bz2
FILE_NAMES_ARRAY[4]=bid.20131023.txt.bz2
FILE_NAMES_ARRAY[5]=bid.20131024.txt.bz2
FILE_NAMES_ARRAY[6]=bid.20131025.txt.bz2
FILE_NAMES_ARRAY[7]=bid.20131026.txt.bz2
FILE_NAMES_ARRAY[8]=bid.20131027.txt.bz2

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
#bin/hadoop jar /opt/homework-3.jar ru.hokan.text.IpBytesCounter hdfs://$HOSTNAME:9000/$INPUT_JOB_PATH/ hdfs://$HOSTNAME:9000/$OUTPUT_JOB_PATH
#bin/hadoop jar /opt/homework-4.jar ru.hokan.custom.IpBytesCounter hdfs://$HOSTNAME:9000/$INPUT_JOB_PATH/ hdfs://$HOSTNAME:9000/$OUTPUT_JOB_PATH
echo "MR job has finished"