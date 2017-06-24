package au.ivj.sandbox.garden;

import au.ivj.sandbox.garden.processors.CallbackProcessor;
import au.ivj.sandbox.garden.processors.CommandProcessor;
import au.ivj.sandbox.garden.processors.SerialProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.Banner;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@SpringBootApplication
@EnableAutoConfiguration
@EnableAsync
@EnableScheduling
public class GardenApplication implements CommandLineRunner
{
    private static final Logger LOGGER =
            LoggerFactory.getLogger(GardenApplication.class);

    @Autowired
    private CommandProcessor commandProcessor;

    @Autowired
    private CallbackProcessor callbackProcessor;

    @Autowired
    private SerialProcessor serialProcessor;

    public static void main(String[] args) throws Exception {
        LOGGER.info("Here we go...");
        SpringApplication app = new SpringApplication(GardenApplication.class);
        app.setBannerMode(Banner.Mode.OFF);
        app.run(args);
        LOGGER.info("See you soon!");
    }

    @Override
    public void run(String... args) throws Exception {
        commandProcessor.readConsole();
    }

}
