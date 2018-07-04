# Telogger

A simply shell server, you can get these function below from this:

- Dynamic log file contents by `tail -f`
- Custom commands
- SSL encryption

## Getting started

### Prerequisites

- Java SE 10
- `tail` utility

> `tail` utility is provided by UNIX like operation systems; On Microsoft Windows, you can get it by [MinGW](http://www.mingw.org/) or others.

### Installing
Clone source to local
```
git clone https://github.com/zenuo/telogger.git
```
DIY, and compile
```
$ cd telogger && mvn -DskipTest=true package
```

### Custom log files and command
> * ONE line with ONE log file.
> * commented by `//`

1) Edit `logfile.conf`, add the absolute pathname of log files, for instance
```
//Log file of 'cato'
/home/user/cato/log.txt
```

2) Edit `command.conf`, add custom commands, format is
```text
{name}#{command string}#{working directory}#{help info}
```
for instance
```text
//Get date
date#date#/home/user#echo time of now
//Restart omts service
restart-omts#"C:\Program Files\Git\git-bash.exe" D:\omts\start.sh#D:\omts\#Restart omts service
```

3) Edit `telogger.conf`, change `port` or `ssl`

### Running
Execute command
```
$ chmod +x telogeer.py
$ ./telogeer.py start
```

### Use
- if enabled SSL
```
$ openssl s_client -connect 127.0.0.1:8007
```

- if not enabled SSL
```
$ telnet localhost 8007
```

below is the output:
```text
-------Welcome to Telogger-------

Commands:
sub                            subscribe a log file, if you already subscribed a log file, please unsubscribe it first. e.g. sub {FileName}
help                           get the help message.
unsub                          unsubscribe the file you subscribed.
reload                         re-opening log files.
quit                           exit the session.
date                           echo time of now

Log files:
/home/user/cato/log.txt
```

### Built With
* Maven

### Powered By
* Netty
