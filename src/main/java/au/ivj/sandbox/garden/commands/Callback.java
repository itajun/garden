package au.ivj.sandbox.garden.commands;

import java.time.LocalDateTime;
import java.util.List;

/**
 * For async commands, represents the callback.
 */
public class Callback
{
    private LocalDateTime receivedAt;
    private List<String> payload;
    private String correlationId;

    public Callback(LocalDateTime receivedAt, List<String> payload, String correlationId)
    {
        this.receivedAt = receivedAt;
        this.payload = payload;
        this.correlationId = correlationId;
    }

    public String getCorrelationId()
    {
        return correlationId;
    }

    public void setCorrelationId(String correlationId)
    {
        this.correlationId = correlationId;
    }

    public List<String> getPayload()
    {
        return payload;
    }

    public void setPayload(List<String> payload)
    {
        this.payload = payload;
    }

    public LocalDateTime getReceivedAt()
    {
        return receivedAt;
    }

    public void setReceivedAt(LocalDateTime receivedAt)
    {
        this.receivedAt = receivedAt;
    }

    @Override
    public String toString()
    {
        return "Callback{" +
                "receivedAt=" + receivedAt +
                ", payload='" + payload + '\'' +
                ", correlationId='" + correlationId + '\'' +
                '}';
    }
}
