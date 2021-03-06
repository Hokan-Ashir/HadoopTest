#!/bin/bash

INPUT_JOB_PATH=job_input
OUTPUT_JOB_PATH=job_output
FILE_NAMES_ARRAY[0]=bid.20130606.txt.bz2
FILE_NAMES_ARRAY[1]=bid.20130607.txt.bz2
FILE_NAMES_ARRAY[2]=bid.20130608.txt.bz2
FILE_NAMES_ARRAY[3]=bid.20130609.txt.bz2
FILE_NAMES_ARRAY[4]=bid.20130610.txt.bz2
FILE_NAMES_ARRAY[5]=bid.20130611.txt.bz2
FILE_NAMES_ARRAY[6]=bid.20130612.txt.bz2
#FILE_NAMES_ARRAY[7]=bid.20131019.txt.bz2
#FILE_NAMES_ARRAY[8]=bid.20131020.txt.bz2

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

# TODO add pre-decompression phase
bin/hdfs dfs -mkdir hdfs://$HOSTNAME:9000/$INPUT_JOB_PATH
bin/hdfs dfs -mkdir hdfs://$HOSTNAME:9000/$OUTPUT_JOB_PATH
for n in "${FILE_NAMES_ARRAY[@]}"; do
    echo "Coping ${n} from local docker FS to HDFS ..."
    bin/hdfs dfs -copyFromLocal /opt/${n} hdfs://$HOSTNAME:9000/$INPUT_JOB_PATH/${n}
    echo "Coping ${n} from local docker FS to HDFS complete"
done

#echo "Installing java8 ..."
#cd /root
#curl -LO 'http://download.oracle.com/otn-pub/java/jdk/8u91-b14/jdk-8u91-linux-x64.rpm' -H 'Cookie: oraclelicense=accept-securebackup-cookie'
#rpm -i jdk-8u91-linux-x64.rpm
#echo "Installing java8 complete"

cd $HADOOP_PREFIX
#export HADOOP_CLIENT_OPTS="--Xms8g --Xmx8g --Xmn2g  -XX:PermSize=64M -XX:MaxPermSize=256M -XX:-OmitStackTraceInFastThrow -XX:SurvivorRatio=2 -XX:+UseG1GC -XX:NewSize=4g -XX:MaxNewSize=5g -XX:ConcGCThreads -XX:+UseStringDeduplication"
#-XX:+UseG1GC -XX:+UseStringDeduplication
#-Xmn4g
# -XX:+UseConcMarkSweepGC -XX:+CMSParallelRemarkEnabled -XX:+UseCMSInitiatingOccupancyOnly -XX:CMSInitiatingOccupancyFraction=70 -XX:+ScavengeBeforeFullGC -XX:+CMSScavengeBeforeRemark

# settings for remote jvisualvm debugging
# on host, from where you deploy your jvisualvm you must create SSH tunnel via this command (on Windows you may use gitBash)
# ssh -L 9010:localhost:9010 root@10.16.10.109
# where 10.16.10.109 is host of virtual box (see "remote.host.ip" property in pom-file)
# also you MUST expose port 9010 in docker container via "docker run ... -p 9010:9010 ... "
#
# then simply run jvisualvm and add remote host 10.16.10.109:9010
export HADOOP_CLIENT_OPTS="-Dcom.sun.management.jmxremote.rmi.port=9010
-Dcom.sun.management.jmxremote=true
-Dcom.sun.management.jmxremote.port=9010
-Dcom.sun.management.jmxremote.ssl=false
-Dcom.sun.management.jmxremote.authenticate=false
-Dcom.sun.management.jmxremote.local.only=false
-Djava.rmi.server.hostname=localhost $HADOOP_CLIENT_OPTS"
export HADOOP_CLIENT_OPTS="-Xmx4g -Xmn1g -Xms4g $HADOOP_CLIENT_OPTS"

echo "Running a job ..."
bin/hadoop jar /opt/homework-2.jar ru.hokan.MultipleFilesProcessor hdfs://$HOSTNAME:9000/$INPUT_JOB_PATH/ hdfs://$HOSTNAME:9000/$OUTPUT_JOB_PATH/
echo "Job has finished"