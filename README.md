# wait4ora

This small utility can be used to wait until an oracle instance is fully started.

## Start application
```
./gradlew shadowjar
java -jar build/libs/*all.jar \
    oracle.jdbc.driver.OracleDriver \
    jdbc:oracle:thin:@localhost:1521:XE \
    system oracle 180
```

## Start Oracle XE in the background
```
sudo rm -fR /opt/oracle/oradata
sudo mkdir -p /opt/oracle/oradata
sudo chmod a+rw /opt/oracle
sudo chown -R $USER /opt/oracle
docker run --rm --name oracle-xe \
    -p 1521:1521 -p 8980:8080 \
    -d wnameless/oracle-xe-11g-r2

#     --shm-size="2g" \
#    -v oracle-xe-volumne:/opt/oracle/oradata \    
#    oracleinanutshell/oracle-xe-11g
```
