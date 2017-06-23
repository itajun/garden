package au.ivj.sandbox.garden.commands;

import au.ivj.sandbox.garden.processors.CallbackProcessor;
import com.google.common.base.Optional;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Just echoes the params back. Used for tests
 */
@Component(value = "command.echo_async")
@Scope("prototype")
public class CommandEchoAsync implements Command
{
    private static final Logger LOGGER = Logger.getLogger(CommandEchoAsync.class);

    @Autowired
    private CallbackProcessor callbackProcessor;

    @Override
    @Async
    public void execute(List<String> payload)
    {
        String uniqueId = callbackProcessor.getUniqueId();
        LOGGER.info("I'm pretending to do something while I wait for callback " + uniqueId);
        Optional<Callback> commandCallback = callbackProcessor.waitForCallback(uniqueId, 30_000);
        if (commandCallback.isPresent()) {
            LOGGER.info("Great! My request was answered " + commandCallback.get());
        } else {
            LOGGER.info("Ouch! I was ignored... No callback received " + uniqueId);
        }
    }
}
