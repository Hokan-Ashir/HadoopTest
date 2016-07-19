#!/bin/bash

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

bin/hdfs dfs -mkdir hdfs://$HOSTNAME:9000/job_input
echo "Coping file1 from local docker FS to HDFS ..."
bin/hdfs dfs -copyFromLocal /opt/file1 hdfs://$HOSTNAME:9000/job_input/file1
echo "Coping file1 from local docker FS to HDFS complete"

echo "Coping file2 from local docker FS to HDFS ..."
bin/hdfs dfs -copyFromLocal /opt/file2 hdfs://$HOSTNAME:9000/job_input/file2
echo "Coping file2 from local docker FS to HDFS complete"

export HADOOP_CLIENT_OPTS="-Xmx4g -Xmn1g -Xms4g $HADOOP_CLIENT_OPTS"
echo "Running a job ..."
bin/hadoop jar /opt/homework-1.jar ru.hokan.LongestWordCounter /job_input /job_output
echo "MR job has finished"