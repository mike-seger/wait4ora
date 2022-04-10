# wait4ora

This small utility waits until an Oracle database instance is fully started.

Why is this needed?  
Unlike other databases, Oracle listens on the TCP port before it allows SQL interactions. This doesn't allow the standard TCP port check to be used. In particular, this is inconvenient in CI/CD pipelines.

How does it work?  
wait4ora tries to connect and send an SQL query to the database until successful or a timeout has been exceeded. 

## Build wait4ora application (jar)
A runnable JAR with all dependencies will be created here build/libs/*all.jar as follows:
```
./gradlew clean build
```

## Run wait4ora application (jar)
### getting help on available options
```
$ java -jar build/libs/*all.jar     
2022-04-10T09:03:45.45736Z: Usage: Wait4Ora <JSON parameters> 
The expected input is a JSON being at least '{}'. 
The default values are:
{
  "connTimeoutSecs" : 60,
  "pollInterval" : 3,
  "sqlCheck" : "SELECT 123456 AS ID FROM DUAL",
  "jdbc" : {
    "driver" : "oracle.jdbc.driver.OracleDriver",
    "url" : "jdbc:oracle:thin:@localhost:1521:XE",
    "user" : "system",
    "password" : "oracle"
  }
}
```

### run with default values
```
java -jar build/libs/*all.jar '{}'
```

## Start an Oracle instance
### wnameless
- https://hub.docker.com/r/wnameless/oracle-xe-11g-r2
```
docker run --rm --name oracle-xe \
    -p 1521:1521 -p 8980:8080 \
    -d wnameless/oracle-xe-11g-r2
```

### gvenzl
- https://hub.docker.com/r/gvenzl/oracle-xe
```
docker run --rm --name oracle-xe \
    -p 1521:1521 -e ORACLE_PASSWORD=oracle \
    -d gvenzl/oracle-xe:11-slim
```

## measure start times
These measurements have been taken with the following specs:
- Ubuntu 20.04.4 LTS x86_64
- Intel i7-9700K (8) @ 4.900GHz
- Samsung SSD 860 QVO 2TB
### measure the default list of images
```
$ ./measure.sh 
wnameless/oracle-xe-11g-r2:latest (2.1 GB)      : 12 s
gvenzl/oracle-xe:11-slim (601 MB)               : 12 s
gvenzl/oracle-xe:slim (2.07 GB)                 : 19 s
```

### measure a custom image
```
$ ./measure.sh gvenzl/oracle-xe:11-full
Error: No such container: oracle-xe
Unable to find image 'gvenzl/oracle-xe:11-full' locally
11-full: Pulling from gvenzl/oracle-xe
1824cb7e97fb: Already exists 
a4b0ec838951: Pull complete 
Digest: sha256:01b3735bc09ed0d606a3dd81d5c7cd67ec29b65eb35b019818e355ca0f6d053f
Status: Downloaded newer image for gvenzl/oracle-xe:11-full
3e15f307d6abffd40c55ac2085f21d29108c34b4afc53765b1161b51f819bc76
2022-04-10T10:45:57.285166Z: Connecting to database:  {
  "connTimeoutSecs" : 60,
  "pollInterval" : 3,
  "sqlCheck" : "SELECT 123456 AS ID FROM DUAL",
  "jdbc" : {
    "driver" : "oracle.jdbc.driver.OracleDriver",
    "url" : "jdbc:oracle:thin:@localhost:1521:XE",
    "user" : "system",
    "password" : "oracle"
  }
}
2022-04-10T10:45:57.44225Z: IO Error: Got minus one from a read call, connect lapse 1 ms., Authentication lapse 0 ms.
2022-04-10T10:46:00.457067Z: IO Error: Got minus one from a read call, connect lapse 1 ms., Authentication lapse 0 ms.
2022-04-10T10:46:03.477853Z: Listener refused the connection with the following error:
ORA-12505, TNS:listener does not currently know of SID given in connect descriptor
  (CONNECTION_ID=YvN0JjhxRyO2LpwHTh/RTA==)
2022-04-10T10:46:06.490722Z: Listener refused the connection with the following error:
ORA-12528, TNS:listener: all appropriate instances are blocking new connections
  (CONNECTION_ID=NydFdx7DRF+DBxjv/seqPg==)
2022-04-10T10:46:09.782108Z: Running SQL:  SELECT 123456 AS ID FROM DUAL
2022-04-10T10:46:09.861312Z: ID 123456
2022-04-10T10:46:09.861486Z: Connection established after 12 seconds
```

## links
- https://stackoverflow.com/questions/68605011/oracle-12c-docker-setup-on-apple-m1/70719148?noredirect=1#comment126132166_70719148
