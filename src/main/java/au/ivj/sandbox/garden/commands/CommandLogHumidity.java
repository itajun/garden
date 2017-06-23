package au.ivj.sandbox.garden.commands;

import com.google.common.collect.ImmutableMap;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

/**
 * Logs the humidity level received from the sensor
 */
@Component(value = "command.log_humidity")
public class CommandLogHumidity implements Command
{
    private static final Logger LOGGER = Logger.getLogger(CommandLogHumidity.class);

    private static final int STORE_INTERVAL = 10 * 60 * 1000; // 10 minutes

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    private long lastOneStored = 0;

    @Override
    @Async
    public void execute(List<String> payload)
    {
        int value = Integer.valueOf(payload.get(0));
        long waitFor = lastOneStored + STORE_INTERVAL - System.currentTimeMillis();
        if (waitFor > 0) {
            LOGGER.debug(String.format("Received humidity update with %d, but won't store for another %d seconds",
                    value,
                    waitFor));
            return;
        }

        LOGGER.debug("Will store humidity to DB " + value);

        lastOneStored = System.currentTimeMillis();

        jdbcTemplate
                .update("INSERT INTO HUMIDITY_LOG(READING_TIME, READING_VALUE) VALUES (:READING_TIME, :READING_VALUE)",
                        ImmutableMap.<String, Object> builder()
                                .put("READING_TIME", new Date())
                                .put("READING_VALUE", value)
                                .build()
                );
    }
}