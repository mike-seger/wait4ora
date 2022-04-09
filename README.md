# wait4ora

This small utility waits until an oracle instance is fully started.

## Start application
```
./gradlew shadowjar
java -jar build/libs/*all.jar \
    oracle.jdbc.driver.OracleDriver \
    jdbc:oracle:thin:@localhost:1521:XE \
    system oracle 180
```

## Start an Oracle instance
```
docker run --rm --name oracle-xe \
    -p 1521:1521 -p 8980:8080 \
    -d wnameless/oracle-xe-11g-r2
```
