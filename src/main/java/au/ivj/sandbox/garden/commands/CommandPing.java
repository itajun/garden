package au.ivj.sandbox.garden.commands;

import au.ivj.sandbox.garden.processors.CallbackProcessor;
import au.ivj.sandbox.garden.processors.SerialProcessor;
import com.google.common.base.Optional;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Pings arduino and waits for pong
 */
@Component(value = "command.ping")
@Scope("prototype")
public class CommandPing implements Command
{
    private static final Logger LOGGER = Logger.getLogger(CommandPing.class);
    public static final int TIMEOUT = 30_000;

    @Autowired
    private CallbackProcessor callbackProcessor;

    @Autowired
    private SerialProcessor serialProcessor;

    @Override
    @Async
    public void execute(List<String> payload)
    {
        String uniqueId = callbackProcessor.getUniqueId();
        serialProcessor.sendCommand("ping " + uniqueId);
        LOGGER.debug("Waiting for pong " + uniqueId);
        Optional<Callback> commandCallback = callbackProcessor.waitForCallback(uniqueId, TIMEOUT);
        if (commandCallback.isPresent()) {
            LOGGER.info("Great! Gotta a pong back " + commandCallback.get());
        } else {
            LOGGER.info("Ouch! Didn't receive a pong " + uniqueId);
        }
    }
}
