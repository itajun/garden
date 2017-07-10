package au.ivj.sandbox.garden.commands;

import com.google.common.collect.ImmutableMap;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;
import java.util.List;

/**
 * Logs the light level received from the sensor
 */
@Component(value = "command.log_light")
public class CommandLogLight implements Command
{
    private static final Logger LOGGER = Logger.getLogger(CommandLogLight.class);

    private static final int STORE_INTERVAL = 5 * 60 * 1000; // 5 minutes

    @Value("${iot.url}")
    private String iotURL;

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
            LOGGER.debug(String.format("Received light update with %d, but won't store for another %d minutes",
                    value,
                    waitFor / 1000 / 60));
            return;
        }

        LOGGER.info("Will store light incidence to DB " + value);

        lastOneStored = System.currentTimeMillis();

        jdbcTemplate
                .update("INSERT INTO LIGHT_LOG(READING_TIME, READING_VALUE) VALUES (:READING_TIME, :READING_VALUE)",
                        ImmutableMap.<String, Object> builder()
                                .put("READING_TIME", new Date())
                                .put("READING_VALUE", value)
                                .build()
                );

        try {
            URL obj = new URL(String.format(iotURL, "Laptop", "Light", value, new Date()));
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();
            LOGGER.debug("Response Code from IOT sync: " + con.getResponseCode());
        }
        catch (IOException e)
        {
            LOGGER.warn("Error synchronizing with IOT server", e);
        }
    }
}
