package au.ivj.sandbox.garden.processors;

import au.ivj.sandbox.garden.commands.Callback;
import com.google.common.base.Optional;
import com.google.common.base.Strings;
import org.apache.log4j.Logger;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * Handles callbacks and waiting commands.
 */
@Component
public class CallbackProcessor
{
    private static final Logger LOGGER = Logger.getLogger(CallbackProcessor.class);

    private static long CALLBACK_TIMEOUT = 60 * 5; // 5 minutes and we give up

    private ConcurrentMap<String, Callback> unreadCallbacks = new ConcurrentHashMap<>();

    private ConcurrentMap<String, Object> waitingThreads = new ConcurrentHashMap<>();

    private AtomicInteger idCounter = new AtomicInteger(0);

    @Scheduled(fixedRate=5000)
    public void scheduleCleanup() {
        LocalDateTime removeBefore = LocalDateTime.now().minusSeconds(CALLBACK_TIMEOUT);
        List<Callback> removeThese =
                unreadCallbacks.values().stream().filter(e -> e.getReceivedAt().isBefore(removeBefore)).collect(
                        Collectors.toList());

        for (Callback callback : removeThese)
        {
            LOGGER.warn("Removing unread callback " + callback);
            unreadCallbacks.remove(callback.getCorrelationId());
        }
    }

    public Optional<Callback> waitForCallback(String correlationId, long timeout) {
        if (!(unreadCallbacks.containsKey(correlationId))) {
            Object monitor = new Object();
            waitingThreads.put(correlationId, monitor);
            try
            {
                synchronized (monitor) {
                    monitor.wait(timeout);
                }
            }
            catch (InterruptedException e)
            {
                LOGGER.warn("I got tired of waiting for this callback: " + correlationId);
            }
            waitingThreads.remove(correlationId);
        }
        return Optional.fromNullable(unreadCallbacks.remove(correlationId));
    }

    public void callbackReceived(Callback callback) {
        unreadCallbacks.put(callback.getCorrelationId(), callback);
        Object monitor = waitingThreads.remove(callback.getCorrelationId());
        if (monitor != null) {
            LOGGER.debug("Received callback while thread was waiting. Delivering correlation: "
                    + callback.getCorrelationId());

            synchronized (monitor)
            {
                monitor.notify();
            }
        } else {
            LOGGER.debug("Callback was received but nobody waiting. I'll keep it for a bit " + callback);
        }
    }

    public String getUniqueId() {
        return Strings.padStart(String.valueOf(idCounter.getAndIncrement()), 9, '0');
    }
}
