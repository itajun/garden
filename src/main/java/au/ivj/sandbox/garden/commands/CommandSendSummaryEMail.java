package au.ivj.sandbox.garden.commands;

import au.ivj.sandbox.garden.processors.EMailProcessor;
import com.google.common.collect.ImmutableMap;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.AbstractMap;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Sends the summary e-mail
 */
@Component(value = "command.send_summary_email")
public class CommandSendSummaryEMail implements Command
{
    private static final Logger LOGGER = Logger.getLogger(CommandSendSummaryEMail.class);

    @Autowired
    private EMailProcessor eMailProcesor;

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    private long lastSummaryEmail = 0;

    @Override
    @Async
    public void execute(List<String> payload)
    {
        Map<String, Object> context = new HashMap<>();
        context.put("lastUpdate", lastSummaryEmail == 0 ? null : new Date(lastSummaryEmail));
        context.put("averageMoisture", avarageMoisture());
        context.put("averageLightIncidence", avarageLightIncidence());
        context.put("averageTemperature", avarageTemperature());
        context.put("averageWateringTime", avarageWateringTime());
        context.put("commandFails", commandFails());
        eMailProcesor.sendEMail(EMailProcessor.EMailTemplate.SUMMARY, context);
        lastSummaryEmail = System.currentTimeMillis();
    }

    private Long avarageMoisture() {
        try {
            return jdbcTemplate
                    .queryForObject("SELECT AVG(READING_VALUE) FROM MOISTURE_LOG WHERE READING_TIME >= :LAST_READING",
                            ImmutableMap.of("LAST_READING", new Date(lastSummaryEmail)),
                            Long.class
                    );
        } catch (Exception e) {
            LOGGER.error("Error fetching moisture", e);
            return null;
        }
    }

    private Long avarageLightIncidence() {
        try {
            return jdbcTemplate
                    .queryForObject("SELECT AVG(READING_VALUE) FROM LIGHT_LOG WHERE READING_TIME >= :LAST_READING",
                            ImmutableMap.of("LAST_READING", new Date(lastSummaryEmail)),
                            Long.class
                    );
        } catch (Exception e) {
            LOGGER.error("Error fetching moisture", e);
            return null;
        }
    }

    private Long avarageTemperature() {
        try {
            return jdbcTemplate
                    .queryForObject("SELECT AVG(READING_VALUE) FROM TEMPERATURE_LOG WHERE READING_TIME >= " +
                                    ":LAST_READING",
                            ImmutableMap.of("LAST_READING", new Date(lastSummaryEmail)),
                            Long.class
                    );
        } catch (Exception e) {
            LOGGER.error("Error fetching moisture", e);
            return null;
        }
    }

    private List<Map<String,Object>> avarageWateringTime() {
        try {
            return jdbcTemplate
                    .queryForList("SELECT COMMAND_TIME, PUMP, PERIOD FROM PUMP_COMMANDS WHERE COMMAND_TIME >= :LAST_READING",
                            ImmutableMap.of("LAST_READING", new Date(lastSummaryEmail)));
        } catch (Exception e) {
            LOGGER.error("Error fetching watering time", e);
            return null;
        }
    }

    private List<Map.Entry<String, Long>> commandFails() {
        try {
            return jdbcTemplate
                    .query("SELECT COMMAND, COUNT(COMMAND_TIME) FROM COMMUNICATION_FAILS WHERE COMMAND_TIME >= " +
                                    ":LAST_READING GROUP BY COMMAND",
                            ImmutableMap.of("LAST_READING", new Date(lastSummaryEmail)),
                            new RowMapper<Map.Entry<String, Long>>()
                            {
                                @Override
                                public Map.Entry<String, Long> mapRow(ResultSet resultSet, int i) throws SQLException
                                {
                                    return new AbstractMap.SimpleImmutableEntry<String, Long>(resultSet.getString(1)
                                            , resultSet.getLong(2));
                                }
                            }
                    );
        } catch (Exception e) {
            LOGGER.error("Error fetching command fails", e);
            return null;
        }
    }

}
