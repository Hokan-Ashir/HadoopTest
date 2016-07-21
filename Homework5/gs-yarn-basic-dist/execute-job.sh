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


echo "Port 9000" >> /etc/ssh/sshd_config
service sshd restart

#cd $HADOOP_PREFIX/etc/hadoop
#sed 's/<value>.*/<value>hdfs:\/\/localhost:9000<\/value>/' core-site.xml.template > core-site.xml
#
#cd $HADOOP_PREFIX/sbin
#sh stop-all.sh
#sh start-all.sh

echo "Staring history server"
./sbin/mr-jobhistory-daemon.sh start historyserver
echo "History server started"

export HADOOP_CLIENT_OPTS="-Xmx4g -Xmn1g -Xms4g $HADOOP_CLIENT_OPTS"
echo "Running a job ..."
java -jar /opt/gs-yarn-basic-client-1.0-SNAPSHOT.jar
echo "Job has finished"