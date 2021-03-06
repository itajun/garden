package au.ivj.sandbox.garden.processors;

import com.google.common.base.MoreObjects;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;

/**
 * Config various scheduled services
 */
@Service
public class ScheduleProcessor {
    private static final Logger LOGGER = Logger.getLogger(ScheduleProcessor.class);

    @Autowired
    private CommandProcessor commandProcessor;

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    @Value("${schedule.water.pump_a.threshold}")
    Long pumpAThreshold;

    @Scheduled(cron = "${schedule.email.summary.cron}")
    public void summaryEMail() {
        commandProcessor.processLine("send_summary_email");
    }

    @Scheduled(cron = "${schedule.email.summary_html.cron}")
    public void summaryEMailHTML() {
        commandProcessor.processLine("send_summary_email_html");
    }

    @Scheduled(cron = "${schedule.water.pump_a.cron}")
    public void pumpA() {
        if (MoreObjects.firstNonNull(getLastMoistureReading(), pumpAThreshold) <= pumpAThreshold) {
            commandProcessor.processLine("pump a");
        } else {
            LOGGER.info(String.format("Won't turn pump a on because moisture reading is greater than %d", pumpAThreshold));
        }
    }

    private Long getLastMoistureReading() {
        return jdbcTemplate
                .query("SELECT READING_VALUE FROM MOISTURE_LOG ORDER BY READING_TIME DESC",
                        Collections.emptyMap(),
                        new ResultSetExtractor<Long>() {
                            @Override
                            public Long extractData(ResultSet resultSet) throws SQLException, DataAccessException {
                                return resultSet.next() ? resultSet.getLong(1) : null;
                            }
                        }
                );
    }

    @Scheduled(cron = "${schedule.water.pump_b.cron}")
    public void pumpB() {
        commandProcessor.processLine("pump b");
    }

    @Scheduled(cron = "${schedule.ping.cron}")
    public void ping() {
        commandProcessor.processLine("ping");
    }
}
