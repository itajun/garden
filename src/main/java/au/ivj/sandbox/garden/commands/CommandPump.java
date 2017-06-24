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

    private int fwPeriod; // pump.fwPeriod

    private int bwPeriod; // pump.bwPeriod

    @Autowired
    private SerialProcessor serialProcessor;

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    public int getFwPeriod() {
        return fwPeriod;
    }

    public void setFwPeriod(int fwPeriod) {
        this.fwPeriod = fwPeriod;
    }

    public int getBwPeriod() {
        return bwPeriod;
    }

    public void setBwPeriod(int bwPeriod) {
        this.bwPeriod = bwPeriod;
    }

    @Override
    @Async
    public void execute(List<String> payload)
    {
        Date commandTime = new Date();
        LOGGER.debug(String.format("Turning pump %s FW", payload.get(0)));
        serialProcessor.sendCommand(String.format("pump_%s fw", payload.get(0)));
        try {
            Thread.sleep(fwPeriod);
        } catch (InterruptedException e) {
            LOGGER.warn("Hum... I couldn't sleep...");
        }
        LOGGER.debug(String.format("Turning pump %s BW", payload.get(0)));
        serialProcessor.sendCommand(String.format("pump_%s bw", payload.get(0)));
        try {
            Thread.sleep(bwPeriod);
        } catch (InterruptedException e) {
            LOGGER.warn("Hum... I couldn't sleep...");
        }
        LOGGER.debug(String.format("Turning pump %s OFF", payload.get(0)));
        serialProcessor.sendCommand(String.format("pump_%s off", payload.get(0)));

        jdbcTemplate
                .update("INSERT INTO PUMP_COMMANDS(COMMAND_TIME, PERIOD) VALUES (:COMMAND_TIME, :PERIOD)",
                        ImmutableMap.<String, Object> builder()
                                .put("COMMAND_TIME", commandTime)
                                .put("PERIOD", fwPeriod)
                                .build()
                );

    }
}
