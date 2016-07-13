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
echo "Running a job ..."
#bin/hadoop jar share/hadoop/mapreduce/hadoop-mapreduce-examples-2.7.1.jar grep input output 'dfs[a-z.]+'
bin/hadoop jar /opt/homework-1.jar Grep input output 'dfs[a-z.]+'
echo "MR job has finished"