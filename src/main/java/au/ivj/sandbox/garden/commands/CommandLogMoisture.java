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
 * Logs the moisture level received from the sensor
 */
@Component(value = "command.log_moisture")
public class CommandLogMoisture implements Command
{
    private static final Logger LOGGER = Logger.getLogger(CommandLogMoisture.class);

    private static final int STORE_INTERVAL = 5 * 60 * 1000; // 5 minutes

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    private long lastOneStored = 0;

    @Override
    @Async
    public void execute(List<String> payload)
    {
        int value = 1023 - Integer.valueOf(payload.get(0));  // Makes more sense when reading
        long waitFor = lastOneStored + STORE_INTERVAL - System.currentTimeMillis();
        if (waitFor > 0) {
            LOGGER.debug(String.format("Received moisture update with %d, but won't store for another %d minutes",
                    value,
                    waitFor / 1000 / 60));
            return;
        }

        LOGGER.info("Will store moisture to DB " + value);

        lastOneStored = System.currentTimeMillis();

        jdbcTemplate
                .update("INSERT INTO MOISTURE_LOG(READING_TIME, READING_VALUE) VALUES (:READING_TIME, :READING_VALUE)",
                        ImmutableMap.<String, Object> builder()
                                .put("READING_TIME", new Date())
                                .put("READING_VALUE", value)
                                .build()
                );
    }
}
