package au.ivj.sandbox.garden.commands;

import com.google.common.base.Joiner;
import org.apache.log4j.Logger;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Just echoes the params back. Used for tests
 */
@Component(value = "command.test_echo")
@Scope("prototype")
public class TestCommandEcho implements Command
{
    private static final Logger LOGGER = Logger.getLogger(TestCommandEcho.class);

    @Override
    public void execute(List<String> payload)
    {
        LOGGER.info("Echoing: " + Joiner.on(",").join(payload));
    }
}
