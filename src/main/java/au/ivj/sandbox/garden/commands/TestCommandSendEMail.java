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
 * Sends the test e-mail
 */
@Component(value = "command.test_send_email")
@Scope("prototype")
public class TestCommandSendEMail implements Command
{
    private static final Logger LOGGER = Logger.getLogger(TestCommandSendEMail.class);

    @Autowired
    private EMailProcessor eMailProcesor;

    @Override
    @Async
    public void execute(List<String> payload)
    {
        eMailProcesor.sendEMail(EMailProcessor.EMailTemplate.TEST, Collections.emptyMap());
    }
}
