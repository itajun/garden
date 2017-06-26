package au.ivj.sandbox.garden.commands;

import au.ivj.sandbox.garden.processors.SerialProcessor;
import com.google.common.collect.ImmutableMap;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Scope;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

/**
 * Turns a pump on/off
 */
@Component(value = "command.pump")
@Scope("prototype")
@ConfigurationProperties("pump")
public class CommandPump implements Command
{
    private static final Logger LOGGER = Logger.getLogger(CommandPump.class);

    private int period; // pump.period

    private String direction; // pump.direction

    @Autowired
    private SerialProcessor serialProcessor;

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    public int getPeriod() {
        return period;
    }

    public void setPeriod(int period) {
        this.period = period;
    }

    public String getDirection() {
        return direction;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }

    @Override
    @Async
    public void execute(List<String> payload)
    {
        Date commandTime = new Date();
        String pump = payload.get(0);

        LOGGER.info(String.format("Turning pump %s ON", pump));

        LOGGER.debug(String.format("Turning pump %s %s", pump, direction));
        serialProcessor.sendCommand(String.format("pump_%s %s", pump, direction));
        try {
            Thread.sleep(period);
        } catch (InterruptedException e) {
            LOGGER.warn("Hum... I couldn't sleep...");
        }

        LOGGER.debug(String.format("Turning pump %s OFF", pump));
        serialProcessor.sendCommand(String.format("pump_%s off", pump));

        jdbcTemplate
                .update("INSERT INTO PUMP_COMMANDS(COMMAND_TIME, PUMP, PERIOD) VALUES (:COMMAND_TIME, :PUMP, :PERIOD)",
                        ImmutableMap.<String, Object> builder()
                                .put("COMMAND_TIME", commandTime)
                                .put("PUMP", pump)
                                .put("PERIOD", period)
                                .build()
                );
    }
}
