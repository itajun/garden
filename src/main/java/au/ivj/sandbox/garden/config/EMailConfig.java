package au.ivj.sandbox.garden.config;

import au.ivj.sandbox.garden.processors.EMailProcessor;
import org.apache.log4j.Logger;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.io.IOException;
import java.util.Properties;

@Configuration
@ConfigurationProperties("email")
public class EMailConfig
{
    private static final Logger LOGGER = Logger.getLogger(EMailConfig.class);

    private String host; // email.host

    private int port = 25; // email.port

    private String protocol = "smtp"; // email.protocol

    private String username; // email.username

    private String password; // email.password

    @Bean
    public JavaMailSenderImpl javaMailSender() {
        JavaMailSenderImpl javaMailSender = new JavaMailSenderImpl();
        javaMailSender.setHost(host);
        javaMailSender.setPort(port);
        javaMailSender.setProtocol(protocol);
        javaMailSender.setUsername(username);
        javaMailSender.setPassword(password);

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

        return javaMailSender;
    }

    public String getHost()
    {
        return host;
    }

    public void setHost(String host)
    {
        this.host = host;
    }

    public int getPort()
    {
        return port;
    }

    public void setPort(int port)
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
}
