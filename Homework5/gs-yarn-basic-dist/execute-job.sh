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

export HADOOP_CLIENT_OPTS="-Xmx4g -Xmn1g -Xms4g $HADOOP_CLIENT_OPTS"
echo "Running a job ..."
java -jar /opt/gs-yarn-basic-client-1.0-SNAPSHOT.jar
echo "Job has finished"