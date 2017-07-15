package au.ivj.sandbox.garden.commands;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;

import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public abstract class CloudSyncCommand implements Command
{
    private static final Logger LOGGER = Logger.getLogger(CloudSyncCommand.class);

    protected static final SimpleDateFormat TIMESTAMP_FORMAT = new SimpleDateFormat("ddMMyyyyHHmmss");

    @Value("${post2Cloud.url}")
    String post2CloudURL;

    @Value("${post2Cloud.user}")
    String post2CloudUser;

    @Value("${post2Cloud.pass}")
    String post2CloudPassword;

    protected void post2Cloud(String contextAndParams) {
        try
        {
            String date = DateTimeFormatter.ofPattern("ddMMyyyyHHmmss").format(LocalDateTime.now());
            URL url = new URL(post2CloudURL + contextAndParams);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            String userPassword = post2CloudUser + ":" + post2CloudPassword;
            String encoding = new sun.misc.BASE64Encoder().encode(userPassword.getBytes());
            conn.setRequestProperty("Authorization", "Basic " + encoding);
            if (conn.getResponseCode() != HttpStatus.OK.value())
            {
                throw new IllegalStateException("Response code was " + conn.getResponseCode());
            }
        } catch (Exception e) {
            LOGGER.warn(String.format("Couldn't sync to cloud command %s", contextAndParams), e);
        }
    }
}
