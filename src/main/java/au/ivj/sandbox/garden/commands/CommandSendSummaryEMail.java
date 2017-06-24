package au.ivj.sandbox.garden.commands;

import au.ivj.sandbox.garden.processors.EMailProcessor;
import com.google.common.collect.ImmutableMap;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.*;

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
        context.put("averageHumidity", avarageHumidity());
        context.put("averageLightIncidence", avarageLightIncidence());
        context.put("averageTemperature", null);
        context.put("averageWateringTime", avarageWateringTime());
        context.put("pingFails", pingFails());
        eMailProcesor.sendEMail(EMailProcessor.EMailTemplate.SUMMARY, context);
        lastSummaryEmail = System.currentTimeMillis();
    }

    private Long avarageHumidity() {
        try {
            return jdbcTemplate
                    .queryForObject("SELECT AVG(READING_VALUE) FROM HUMIDITY_LOG WHERE READING_TIME >= :LAST_READING",
                            ImmutableMap.of("LAST_READING", new Date(lastSummaryEmail)),
                            Long.class
                    );
        } catch (Exception e) {
            LOGGER.error("Error fetching humidity", e);
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
            LOGGER.error("Error fetching humidity", e);
            return null;
        }
    }

    private Long avarageWateringTime() {
        try {
            return jdbcTemplate
                    .queryForObject("SELECT AVG(PERIOD) FROM PUMP_COMMANDS WHERE COMMAND_TIME >= :LAST_READING",
                            ImmutableMap.of("LAST_READING", new Date(lastSummaryEmail)),
                            Long.class
                    );
        } catch (Exception e) {
            LOGGER.error("Error fetching watering time", e);
            return null;
        }
    }

    private Long pingFails() {
        try {
            return jdbcTemplate
                    .queryForObject("SELECT COUNT(COMMAND_TIME) FROM PING_FAILS WHERE COMMAND_TIME >= :LAST_READING",
                            ImmutableMap.of("LAST_READING", new Date(lastSummaryEmail)),
                            Long.class
                    );
        } catch (Exception e) {
            LOGGER.error("Error fetching ping fails", e);
            return null;
        }
    }

}
