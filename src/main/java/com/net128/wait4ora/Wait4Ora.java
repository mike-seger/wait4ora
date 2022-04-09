package com.net128.wait4ora;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.TimeZone;
import java.util.stream.Collectors;

public class Wait4Ora {

    public class Parameters {
        private final ObjectMapper om = new ObjectMapper();

        public Parameters(String[] args) throws JsonProcessingException {
            if(args.length!=1 || args[0].trim().matches("(-h|-help|--help)")) {
                log.info("\nUsage:", Wait4Ora.class.getSimpleName(), "<JSON parameters>",
                    "\nThe expected input is a JSON being least '{}'.",
                    "\nThe default values are:\n"+map(new Parameters()));
                System.exit(1);
            }
            om.readerForUpdating(this).readValue(args[0]);
        }

        private Parameters() {}

        public Integer connTimeoutSecs = 10;
        public Integer pollInterval = 10;
        public String sqlCheck = "SELECT 1 AS ID FROM DUAL";
        public Jdbc jdbc = new Jdbc();

        public class Jdbc {
            public String driver = "oracle.jdbc.driver.OracleDriver";
            public String url = "jdbc:oracle:thin:@localhost:1521:XE";
            public String user = "system";
            public String password = "oracle";
        }

        private String map(Parameters parameters) {
            try { return om.writerWithDefaultPrettyPrinter().writeValueAsString(parameters); }
            catch (JsonProcessingException e) { return "???"; }
        }

        public String toString() { return map(this); }
    }

    private final Log log = new Log();

    public Wait4Ora() {}

    private static class Log {
        public void info(Object... objects) {
            System.out.println(
                OffsetDateTime.now(ZoneOffset.UTC).format(DateTimeFormatter.ISO_DATE_TIME)+": "+
                Arrays.stream(objects).map(o -> (o+""))
                    .collect(Collectors.joining(" ")).trim());
        }
    }

    public static void main(String[] args) throws ClassNotFoundException, JsonProcessingException {
        new Wait4Ora().checkConnection(args);
    }

    public void checkConnection(String[] args) throws ClassNotFoundException, JsonProcessingException {
        Parameters parameters = new Parameters(args);
        try {
            var timeZone = TimeZone.getTimeZone("UTC");
            TimeZone.setDefault(timeZone);
            System.setProperty("oracle.jdbc.timezoneAsRegion", "false");
            Class.forName(parameters.jdbc.driver);
            log.info("Connecting to database: ", parameters);
            long startedSecs = System.currentTimeMillis() / 1000;
            boolean first = true;
            while (System.currentTimeMillis() / 1000 - startedSecs < parameters.connTimeoutSecs) {
                if(!first)
                    //noinspection BusyWait
                    Thread.sleep(1000L*parameters.pollInterval);
                first = false;
                try (Connection conn = DriverManager.getConnection(
                        parameters.jdbc.url, parameters.jdbc.user, parameters.jdbc.password)) {
                    log.info("Running SQL: ", parameters.sqlCheck);
                    try (var stmt = conn.createStatement()) {
                        try (ResultSet rs = stmt.executeQuery(parameters.sqlCheck)) {
                            while (rs.next()) {
                                log.info("ID", rs.getString("id"));
                            }
                            log.info("Connection established after", System.currentTimeMillis() / 1000 - startedSecs, "seconds");
                            return;
                        } catch (SQLException e) {
                            e.printStackTrace();
                            System.exit(1);
                        }
                    }
                } catch (SQLException e) {
                    log.info(e.getMessage());
                }
            }
            throw new IllegalStateException("Could not connect within "+parameters.connTimeoutSecs+ " seconds");
        } catch (ClassNotFoundException e) {
            log.info(parameters.jdbc.driver, "driver not found on classpath.");
            throw e;
        } catch (InterruptedException e2) {
            log.info("Application got interrupted");
            System.exit(1);
        }
    }
}
