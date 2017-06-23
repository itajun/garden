package au.ivj.sandbox.garden.commands;

import au.ivj.sandbox.garden.processors.CallbackProcessor;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Just echoes the params back. Used for tests
 */
@Component(value = "command.callback")
@Scope("prototype")
public class CommandCallback implements Command
{
    private static final Logger LOGGER = Logger.getLogger(CommandCallback.class);

    @Autowired
    private CallbackProcessor callbackProcessor;

    @Override
    public void execute(List<String> payload)
    {
        LOGGER.info("Received callback: " + payload);
        callbackProcessor.callbackReceived(new Callback(LocalDateTime.now(), payload, payload.get(0)));
    }
}
