#!/bin/bash

INPUT_JOB_PATH=job_input
OUTPUT_JOB_PATH=job_output

FILE_NAMES_ARRAY[0]=imp.20131019.txt.bz2
FILE_NAMES_ARRAY[1]=imp.20131020.txt.bz2
FILE_NAMES_ARRAY[2]=imp.20131021.txt.bz2
FILE_NAMES_ARRAY[3]=imp.20131022.txt.bz2
FILE_NAMES_ARRAY[4]=imp.20131023.txt.bz2
FILE_NAMES_ARRAY[5]=imp.20131024.txt.bz2
FILE_NAMES_ARRAY[6]=imp.20131025.txt.bz2
FILE_NAMES_ARRAY[7]=imp.20131026.txt.bz2
FILE_NAMES_ARRAY[8]=imp.20131027.txt.bz2

CITY_CODES_FILE=city.en.txt

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

bin/hdfs dfs -mkdir hdfs://$HOSTNAME:9000/$INPUT_JOB_PATH
for n in "${FILE_NAMES_ARRAY[@]}"; do

# you can also pre-decompress files and put them into HDFS in text format, this will save Hadoop some time, or not =)
# cause of native decompression, he can manage bz2-archives easily ether way
#    echo "Decompressing ${n} into local FS ..."
	 # -f = "Force", overriding files, -d = "Decompress"
#    bzip2 -d -f /opt/${n}
#    echo "Coping ${n%????} from local docker FS to HDFS ..."
#    bin/hdfs dfs -copyFromLocal /opt/${n%????} hdfs://$HOSTNAME:9000/$INPUT_JOB_PATH/${n%????}
#    echo "Coping ${n%????} from local docker FS to HDFS complete"

    echo "Coping ${n%????} from local docker FS to HDFS ..."
    bin/hdfs dfs -copyFromLocal /opt/${n} hdfs://$HOSTNAME:9000/$INPUT_JOB_PATH/${n}
    echo "Coping ${n%????} from local docker FS to HDFS complete"
done

echo "Coping $CITY_CODES_FILE from local docker FS to HDFS ..."
bin/hdfs dfs -copyFromLocal /opt/$CITY_CODES_FILE hdfs://$HOSTNAME:9000/$CITY_CODES_FILE
echo "Coping $CITY_CODES_FILE from local docker FS to HDFS complete"

export HADOOP_CLIENT_OPTS="-Xmx4g -Xmn1g -Xms4g $HADOOP_CLIENT_OPTS"
echo "Running a job ..."
bin/hadoop jar /opt/homework-4.jar ru.hokan.CityImpressionsCounter hdfs://$HOSTNAME:9000/$INPUT_JOB_PATH/ hdfs://$HOSTNAME:9000/$OUTPUT_JOB_PATH
echo "Job has finished"