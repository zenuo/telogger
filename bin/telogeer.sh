#!/bin/bash
#配置

#工作路径
WORK_DIR="/opt/telogger/"
#jar文件路径
JAR_PATH="telogger-1.0-jar-with-dependencies.jar"
#需要监听的日志文件
LOG_FILE="/opt/omts-cloud-service/log/omts-cloud-service.log"
#需要监听的端口，默认8007
PORT="8007"

#启动或重启

#切换工作路径
cd $WORK_DIR
#获取程序的进程号
PID=`ps -ef | grep $JAR_PATH | grep -v grep | awk '{print $2}'`
#将PID重定向至文件
echo $PID > pid
#判断进程是否存在
if [[ "$PID" -eq "" ]] ; then
        #若不存在，则是启动
        TYPE="启动\n"
else
        #否则是重启
        TYPE="重启\n"
        #停止进程
        kill $PID
fi
echo -e $TYPE
#启动
nohup java -jar -Dport=$PORT -DlogFile=$LOG_FILE $JAR_PATH >> log 2>&1 &
tail -n 1 -f log