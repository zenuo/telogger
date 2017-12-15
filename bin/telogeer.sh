#!/bin/bash

##Configure
#Working Directory
WORK_DIRECTORY="/home/user/project/telogger"
#Jar file name
JAR_FILE_PATH="/home/user/project/telogger/target/telogger-1.0-jar-with-dependencies.jar"
#Boot type, "start" or "restart"
TYPE=""
#Log file path
LOG_FILE_PATH="log"
#Command
CMD="java -Dfile.encoding=UTF8 -Duser.language=en -Duser.region=US -Dport=8007 -Dssl=true -DlogFile=D:\omts\log\cato.log -jar "$JAR_FILE_PATH
#PID File
PID_FILE_PATH="pid"

##Boot
#change working directory
cd $WORK_DIRECTORY
#judge jar file exists or not
if [ ! -e "$JAR_FILE_PATH" ] ; then
    #if not exists, exit
    echo -e "\nTime："`date`"\nError：File "$JAR_FILE_PATH" not exist." >> $LOG_FILE_PATH
    tail -n 2 $LOG_FILE_PATH
    exit 1
fi
#judge pid file exists or not
if [ ! -e "$PID_FILE_PATH" ] ; then
        #写入空PID
    echo '' > $PID_FILE_PATH
        PID=''
fi
#get pid
PID=`cat $PID_FILE_PATH`
#judge process exists or not
if [[ "$PID" -eq "" ]] ; then
    #if not exists, start
    TYPE="Start\n"
else
    #if exists, restart
    TYPE="Restart\n"
    #stop process
    kill -9 $PID
    #Sleep
    sleep 10
fi
#start
nohup $CMD >> $LOG_FILE_PATH 2>&1 &
#overwrite pid to pid file
PID=`jps -lm | grep -a $JAR_FILE_PATH | awk '{print $1}'`
echo $PID > $PID_FILE_PATH
#promote info
echo -e "\nTime："`date`"\nOperation："$TYPE"\nPID: "$PID
echo -e "\nTime："`date`"\nOperation："$TYPE"\nPID: "$PID >> $LOG_FILE_PATH
exit