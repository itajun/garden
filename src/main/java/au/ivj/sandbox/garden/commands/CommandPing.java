package au.ivj.sandbox.garden.commands;

import au.ivj.sandbox.garden.processors.CallbackProcessor;
import au.ivj.sandbox.garden.processors.SerialProcessor;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

/**
 * Pings Arduino and waits for pong
 */
@Component(value = "command.ping")
@Scope("prototype")
public class CommandPing extends CloudSyncCommand implements Command
{
    private static final Logger LOGGER = Logger.getLogger(CommandPing.class);
    public static final int TIMEOUT = 30_000;

    @Autowired
    private CallbackProcessor callbackProcessor;

    @Autowired
    private SerialProcessor serialProcessor;

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    @Override
    @Async
    public void execute(List<String> payload)
    {
        String uniqueId = callbackProcessor.getUniqueId();
        serialProcessor.sendCommand("ping " + uniqueId);
        LOGGER.debug("Waiting for pong " + uniqueId);
        Optional<Callback> commandCallback = callbackProcessor.waitForCallback(uniqueId, TIMEOUT);
        if (commandCallback.isPresent()) {
            LOGGER.info("Great! Got a pong back " + commandCallback.get());
        } else {
            LOGGER.warn("Ouch! Didn't receive a pong " + uniqueId);

            jdbcTemplate
                    .update("INSERT INTO COMMUNICATION_FAILS(COMMAND_TIME, COMMAND) VALUES (:COMMAND_TIME, :COMMAND)",
                            ImmutableMap.<String, Object> builder()
                                    .put("COMMAND_TIME", new Date())
                                    .put("COMMAND", "ping")
                                    .build()
                    );

        }

        post2Cloud(String.format("postLog?device=Laptop&group=ping&details=%s&when=%s", commandCallback
                        .isPresent() ? "OK" : "FAILED", TIMESTAMP_FORMAT.format(new Date())));
    }
}
