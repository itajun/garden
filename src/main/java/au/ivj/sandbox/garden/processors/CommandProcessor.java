package au.ivj.sandbox.garden.processors;

import au.ivj.sandbox.garden.commands.Command;
import com.google.common.base.CharMatcher;
import com.google.common.base.Splitter;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.thymeleaf.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

@Component
public class CommandProcessor
{
    private static final Logger LOGGER = Logger.getLogger(CommandProcessor.class);

    private boolean exitRequested = false;

    @Autowired
    private ApplicationContext applicationContext;

    public void readConsole() {
        LOGGER.info("At your disposal my lord.");
        while (!exitRequested) {
            try
            {
                Scanner scanner = new Scanner(System.in);
                processLine(scanner.nextLine());
            } catch (Exception e) {
                LOGGER.error("Ops! Something that I didn't foresee happened... Trying to continue BAU...", e);
            }
        }
    }

    public void processLine(String line) {
        List<String> tokens = new ArrayList<>(Splitter.on(CharMatcher.breakingWhitespace())
                .omitEmptyStrings()
                .trimResults()
                .splitToList(line));
        if (tokens.size() == 0) {
            LOGGER.warn("Strange... Empty line received...");
            return;
        }
        String commandName = tokens.remove(0);
        Command command = getCommandFor(commandName);
        // First item was already removed. Only params are passed
        if (command == null) {
            LOGGER.error("Couldn't find processor for command " + commandName);
        } else
        {
            LOGGER.debug(String.format("Executing command %s with %s", commandName, command.getClass()));
            command.execute(tokens);
        }
    }

    private Command getCommandFor(String commandName) {
        try
        {
            return applicationContext.getBean("command." + commandName, Command.class);
        } catch (Exception e) {
            LOGGER.debug("Ops! Problem looking for bean to execute " + commandName, e);
        }
        return null;
    }

    public void setExitRequested(boolean exitRequested)
    {
        this.exitRequested = exitRequested;
    }
}
