package au.ivj.sandbox.garden.processors;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import javax.mail.internet.MimeMessage;
import java.util.Map;

/**
 * Sends e-mails.
 */
@Component
@ConfigurationProperties("email")
public class EMailProcessor
{
    private static final Logger LOGGER = Logger.getLogger(EMailProcessor.class);

    private String from; // email.from

    private String to; // email.to

    private String user; // email.user

    @Autowired
    private JavaMailSender javaMailSender;

    @Autowired
    private TemplateEngine templateEngine;

    public String getFrom()
    {
        return from;
    }

    public void setFrom(String from)
    {
        this.from = from;
    }

    public String getTo()
    {
        return to;
    }

    public void setTo(String to)
    {
        this.to = to;
    }

    public String getUser()
    {
        return user;
    }

    public void setUser(String user)
    {
        this.user = user;
    }

    @Async
    public void sendEMail(EMailTemplate template, Map context) {
        LOGGER.debug(String.format("Sending e-mail template %s with params %s", template, context));

        try
        {
            final MimeMessage mimeMessage = this.javaMailSender.createMimeMessage();
            final MimeMessageHelper message = new MimeMessageHelper(mimeMessage, "UTF-8");
            message.setFrom(from);
            message.setTo(to);

            final Context ctx = new Context();
            ctx.setVariables(context);
            final String subject = this.templateEngine.process(template.subject, ctx);
            message.setSubject(subject);
            final String htmlContent = this.templateEngine.process(template.fileName, ctx);
            message.setText(htmlContent);

            this.javaMailSender.send(mimeMessage);
        }
        catch (Exception e)
        {
            LOGGER.error("Damn! Error sending e-templates", e);
        }
    }

    public enum EMailTemplate {
        TEST("text/test.txt", "Testing connection"),
        GOOD_MORNING("text/good-morning.html", "Good morning ${user}");

        private String fileName;
        private String subject;

        EMailTemplate(String fileName, String subject)
        {
            this.fileName = fileName;
            this.subject = subject;
        }
    }
}
