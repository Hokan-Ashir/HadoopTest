#!/bin/bash

cd $HADOOP_PREFIX

while [ -z `jps | grep NodeManager` ]
do
    echo "Waiting for NodeManager to start ..."
    sleep 3
done

echo "NodeManager started. Hadoop cluster initialized. Leaving NameNode from SafeMode state ..."
bin/hdfs dfsadmin -safemode leave
echo "NameNode leaved SafeMode state"

echo "Staring history server"
./sbin/mr-jobhistory-daemon.sh start historyserver
echo "History server started"

#echo "Installing java8 ..."
#cd /root
#curl -LO 'http://download.oracle.com/otn-pub/java/jdk/8u91-b14/jdk-8u91-linux-x64.rpm' -H 'Cookie: oraclelicense=accept-securebackup-cookie'
#rpm -i jdk-8u91-linux-x64.rpm
#echo "Installing java8 complete"

echo "Running a job ..."
java -jar /opt/gs-yarn-basic-client-1.0-SNAPSHOT.jar
echo "Job has finished"