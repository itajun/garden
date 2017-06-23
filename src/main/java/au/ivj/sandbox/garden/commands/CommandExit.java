package au.ivj.sandbox.garden.commands;

import au.ivj.sandbox.garden.processors.CommandProcessor;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Tells the command processor it is time to say goodbye.
 */
@Component(value = "command.exit")
@Scope("prototype")
public class CommandExit implements Command
{
    private static final Logger LOGGER = Logger.getLogger(CommandExit.class);

    @Autowired
    private CommandProcessor commandProcessor;

    @Override
    public void execute(List<String> payload)
    {
        LOGGER.info("Exit request received");
        commandProcessor.setExitRequested(true);
    }
}
