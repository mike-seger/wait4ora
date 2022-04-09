# wait4ora

This small utility waits until an oracle database instance is fully started.

## Build wait4ora application (jar)
A runnable JAR with all dependencies can be create here build/libs/*all.jar as follows:
```
./gradlew shadowjar
```

## Start wait4ora application (jar)
```
java -jar build/libs/*all.jar \
    oracle.jdbc.driver.OracleDriver \
    jdbc:oracle:thin:@localhost:1521:XE \
    system oracle 180
```

## Start an Oracle instance
### wnameless
Time: 11s
Size: 2.1GB
```
docker run --rm --name oracle-xe \
    -p 1521:1521 -p 8980:8080 \
    -d wnameless/oracle-xe-11g-r2
```

### gvenzl
Time: 21s
Size: 601MB
```
docker run --rm --name oracle-xe \
    -p 1521:1521 -e ORACLE_PASSWORD=oracle \
    -d gvenzl/oracle-xe:11-slim
```
