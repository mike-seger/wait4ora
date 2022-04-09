# wait4ora

This small utility waits until an oracle database instance is fully started.

Why is this needed?  
Unlike other databases, Oracle listens on the TCP port before it allows SQL interactions. It doesn't allow the standard TCP port check to be used. This is inconvenient in CI/CD pipelines.

## Build wait4ora application (jar)
A runnable JAR with all dependencies will be created here build/libs/*all.jar as follows:
```
./gradlew clean shadowjar
```

## Run wait4ora application (jar)
```
# getting help on available options
java -jar build/libs/*all.jar
# or just run with default values
java -jar build/libs/*all.jar '{}'
```

## Start an Oracle instance
### wnameless
- https://hub.docker.com/r/wnameless/oracle-xe-11g-r2
- Time: 11s
- Size: 2.1GB
```
docker run --rm --name oracle-xe \
    -p 1521:1521 -p 8980:8080 \
    -d wnameless/oracle-xe-11g-r2
```

### gvenzl
- https://hub.docker.com/r/gvenzl/oracle-xe
- Time: 21s
- Size: 601MB
```
docker run --rm --name oracle-xe \
    -p 1521:1521 -e ORACLE_PASSWORD=oracle \
    -d gvenzl/oracle-xe:11-slim
```
