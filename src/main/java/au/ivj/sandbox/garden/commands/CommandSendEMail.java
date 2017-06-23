package au.ivj.sandbox.garden.commands;

import au.ivj.sandbox.garden.processors.EMailProcessor;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

/**
 * Just echoes the params back. Used for tests
 */
@Component(value = "command.send_email")
@Scope("prototype")
public class CommandSendEMail implements Command
{
    private static final Logger LOGGER = Logger.getLogger(CommandSendEMail.class);

    @Autowired
    private EMailProcessor eMailProcesor;

    @Override
    @Async
    public void execute(List<String> payload)
    {
        EMailProcessor.EMailTemplate template =
                payload.size() > 0 ? EMailProcessor.EMailTemplate.valueOf(payload.get(0))
                                   : EMailProcessor.EMailTemplate.TEST;
        eMailProcesor.sendEMail(template, Collections.emptyMap());
    }
}
