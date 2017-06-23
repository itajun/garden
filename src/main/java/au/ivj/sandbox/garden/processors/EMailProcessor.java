package au.ivj.sandbox.garden.processors;

import org.apache.log4j.Logger;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.mail.internet.MimeMessage;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;

/**
 * Sends e-mails.
 */
@Component
@ConfigurationProperties("email")
public class EMailProcessor
{
    private static final Logger LOGGER = Logger.getLogger(EMailProcessor.class);

    private String host; // email.host

    private String port = "25"; // email.port

    private String protocol = "smtp"; // email.protocol

    private String username; // email.username

    private String password; // email.password

    private String from; // email.from

    private String to; // email.to

    private JavaMailSenderImpl javaMailSender;


    @PostConstruct
    public void createMailSender() {
        LOGGER.debug("Creating mail sender");

        javaMailSender = new JavaMailSenderImpl();
        javaMailSender.setHost(host);
        javaMailSender.setPassword(port);
        javaMailSender.setProtocol(protocol);
        javaMailSender.setUsername(username);
        javaMailSender.setProtocol(password);

        final Properties javaMailProperties = new Properties();
        try
        {
            javaMailProperties.load(EMailProcessor.class.getResourceAsStream("/javamail.properties"));
        }
        catch (IOException e)
        {
            LOGGER.warn("Couldn't open javamail.properties. Proceeding with default.", e);
        }
        javaMailSender.setJavaMailProperties(javaMailProperties);
    }

    public String getHost()
    {
        return host;
    }

    public void setHost(String host)
    {
        this.host = host;
    }

    public String getPort()
    {
        return port;
    }

    public void setPort(String port)
    {
        this.port = port;
    }

    public String getProtocol()
    {
        return protocol;
    }

    public void setProtocol(String protocol)
    {
        this.protocol = protocol;
    }

    public String getUsername()
    {
        return username;
    }

    public void setUsername(String username)
    {
        this.username = username;
    }

    public String getPassword()
    {
        return password;
    }

    public void setPassword(String password)
    {
        this.password = password;
    }

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

    @Async
    public void sendEMail(EMailTemplate template, Map context) {
        LOGGER.debug(String.format("Sending e-mail %s with params %s", template, context));

        try
        {
            final MimeMessage mimeMessage = this.javaMailSender.createMimeMessage();
            final MimeMessageHelper message = new MimeMessageHelper(mimeMessage, "UTF-8");
            message.setFrom(from);
            message.setTo(to);
            message.setSubject(template.subject);
            message.setText(template.path);
            this.javaMailSender.send(mimeMessage);
        }
        catch (Exception e)
        {
            LOGGER.error("Damn! Error sending e-mail", e);
        }
    }

    public enum EMailTemplate {
        TEST("/templates/email/test.txt", "Testing connection");

        private String path;
        private String subject;

        EMailTemplate(String path, String subject)
        {
            this.path = path;
            this.subject = subject;
        }
    }
}
