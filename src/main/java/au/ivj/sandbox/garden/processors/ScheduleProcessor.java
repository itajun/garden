package au.ivj.sandbox.garden.processors;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * Config various scheduled services
 */
@Service
public class ScheduleProcessor {
    private static final Logger LOGGER = Logger.getLogger(ScheduleProcessor.class);

    @Autowired
    private CommandProcessor commandProcessor;

    @Scheduled(cron = "${schedule.email.summary.cron}")
    public void summaryEMail() {
        commandProcessor.processLine("send_summary_email");
    }

    @Scheduled(cron = "${schedule.water.pump_a.cron}")
    public void pumpA() {
        commandProcessor.processLine("pump a");
    }

    @Scheduled(cron = "${schedule.water.pump_b.cron}")
    public void pumpB() {
        commandProcessor.processLine("pump b");
    }

    @Scheduled(cron = "${schedule.ping.cron}")
    public void ping() {
        commandProcessor.processLine("ping");
    }
}
