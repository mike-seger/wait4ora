package com.net128.wait4ora;

import java.sql.*;
import java.util.Arrays;
import java.util.TimeZone;
import java.util.stream.Collectors;

public class Wait4Ora {

    public static class Parameters {
        public Parameters(String[] args) {
            if(args.length<3 || args.length>6)
                throw new IllegalArgumentException("Expected 3-6 input parameters");
            this.jdbc.driver = args[0];
            this.jdbc.url = args[1];
            this.jdbc.user = args[2];
            if(args.length>3) this.jdbc.password = args[3];
            if(this.jdbc.password.trim().length()==0) this.jdbc.password = null;
            if(args.length>4) this.connTimeoutSecs = Integer.parseInt(args[4]);
            if(args.length>5) this.pollInterval = Integer.parseInt(args[4]);
        }

        public Integer connTimeoutSecs = 1;
        public Integer pollInterval = 10;
        public Jdbc jdbc = new Jdbc();

        public static class Jdbc {
            public String driver;
            public String url;
            public String user;
            public String password;
            public String toString() { return driver + " / "+url + " / "+user + " / "+password; }
        }

        public String toString() { return jdbc + " / "+connTimeoutSecs + " / "+pollInterval; }
    }

    private final Log log = new Log();
    private final Parameters parameters;

    public Wait4Ora(Parameters parameters) {
        this.parameters = parameters;
    }

    private static class Log {
        public void info(Object... objects) {
            System.out.println(Arrays.stream(objects).map(o -> (o+"")).collect(Collectors.joining(" ")));
        }
    }

    public static void main(String[] args) throws ClassNotFoundException {
        new Wait4Ora(new Parameters(args)).checkConnection();
    }

    public void checkConnection() throws ClassNotFoundException {
        try {
            var timeZone = TimeZone.getTimeZone("UTC");
            TimeZone.setDefault(timeZone);
            System.setProperty("oracle.jdbc timezoneAsRegion", "false");
            Class.forName(parameters.jdbc.driver);
            log.info("Connecting to database: "+parameters);
            long startedSecs = System.currentTimeMillis() / 1000;
            while (System.currentTimeMillis() / 1000 - startedSecs < parameters.connTimeoutSecs) {
                try (Connection conn = DriverManager.getConnection(
                        parameters.jdbc.url, parameters.jdbc.user, parameters.jdbc.password)) {
                    log.info("Creating statement...");
                    try (var stmt = conn.createStatement()) {
                        try (ResultSet rs = stmt.executeQuery("SELECT 1 AS ID FROM DUAL")) {
                            while (rs.next()) {
                                log.info(rs.getString("id"));
                            }
                            log.info("Connection established after", System.currentTimeMillis() / 1000 - startedSecs, "seconds");
                            return;
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                    }
                } catch (SQLException e) {
                    log.info(e.getMessage());
                    //noinspection BusyWait
                    Thread.sleep(1000L*parameters.pollInterval);
                }
            }
            throw new IllegalStateException("Could not connect within "+parameters.connTimeoutSecs);
        } catch (ClassNotFoundException e) {
            log.info(parameters.jdbc.driver, "driver not found on classpath");
            throw e;
        } catch (InterruptedException e2) {
            log.info("Application got interrupted");
            System.exit(1);
        }
    }
}
