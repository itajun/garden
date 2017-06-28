package au.ivj.sandbox.garden.processors;

import com.google.common.collect.ImmutableMap;
import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;
import org.thymeleaf.util.StringUtils;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringReader;
import java.util.Date;
import java.util.Enumeration;

/**
 * Reads the serial port and processes commands.
 */
@Component
@ConfigurationProperties("serial")
public class SerialProcessor
{
    private static final Logger LOGGER = Logger.getLogger(SerialProcessor.class);

    @Autowired
    private CommandProcessor commandProcessor;

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    private String port = "COM1"; //serial.port

    private int timeout = 2000; // serial.timeout

    private int dataRate = 9600; // serial.dataRate

    private SerialPort serialPort;

    private BufferedReader input;

    private BufferedWriter output;

    private boolean connected = false;

    @PostConstruct
    public synchronized void connectToComm() {
        if (connected) {
            LOGGER.debug("Already connected");
            return;
        }

        if ("null".equals(port)) {
            LOGGER.warn("Won't actually write to serial. SIMULATION MODE");
            input = new BufferedReader(new StringReader(""));
            output = new BufferedWriter(new PrintWriter(System.out));
        } else {
            LOGGER.debug("Creating COMM connector");

            CommPortIdentifier portId = null;
            Enumeration portEnum = CommPortIdentifier.getPortIdentifiers();

            while (portEnum.hasMoreElements())
            {
                CommPortIdentifier currPortId = (CommPortIdentifier) portEnum.nextElement();
                if (currPortId.getName().equals(port))
                {
                    portId = currPortId;
                    break;
                }
            }

            if (portId == null)
            {
                LOGGER.error("Hummm... I can't find the port " + port);
                throw new IllegalStateException("Can't find port " + port);
            }

            try
            {
                serialPort = (SerialPort) portId.open("Garden", timeout);

                serialPort.setSerialPortParams(dataRate,
                        SerialPort.DATABITS_8,
                        SerialPort.STOPBITS_1,
                        SerialPort.PARITY_NONE);

                input = new BufferedReader(new InputStreamReader(serialPort.getInputStream()));
                output = new BufferedWriter(new OutputStreamWriter(serialPort.getOutputStream()));

                serialPort.addEventListener(this::serialEvent);
                serialPort.notifyOnDataAvailable(true);
                serialPort.enableReceiveTimeout(1000);
                serialPort.enableReceiveThreshold(0);
                serialPort.setDTR(true);
                serialPort.setRTS(true);
            }
            catch (Exception e)
            {
                LOGGER.error("Ouch! Error trying to open the comm port " + port, e);
                throw new IllegalArgumentException(e);
            }
        }

        LOGGER.info("Connected to " + port);
        connected = true;
    }

    public String getPort()
    {
        return port;
    }

    public void setPort(String port)
    {
        this.port = port;
    }

    public int getTimeout()
    {
        return timeout;
    }

    public void setTimeout(int timeout)
    {
        this.timeout = timeout;
    }

    public int getDataRate()
    {
        return dataRate;
    }

    public void setDataRate(int dataRate)
    {
        this.dataRate = dataRate;
    }

    public void sendCommand(String command) {
        if (!connected) {
            LOGGER.debug("Will try to connect before sending command");
            connectToComm();
            if (!connected)
            {
                LOGGER.warn("Failed to connect. Won't send command: " + command);
                return;
            }
        }

        try {
            synchronized (output) {
                output.write(command);
                output.newLine();
                output.flush();
            }
        } catch (IOException e) {
            LOGGER.error("Couldn't send command to Arduino. Device disconnected.", e);

            jdbcTemplate
                    .update("INSERT INTO COMMUNICATION_FAILS(COMMAND_TIME, COMMAND) VALUES (:COMMAND_TIME, :COMMAND)",
                            ImmutableMap.<String, Object> builder()
                                    .put("COMMAND_TIME", new Date())
                                    .put("COMMAND", StringUtils.abbreviate(command, 100))
                                    .build()
                    );

            connected = false;
        }
    }

    @PreDestroy
    public void closeComm() throws IOException
    {
        input.close();
        output.close();
        serialPort.removeEventListener();
        serialPort.close();
    }

    public synchronized void serialEvent(SerialPortEvent event) {
        if (event.getEventType() == SerialPortEvent.DATA_AVAILABLE) {
            try {
                LOGGER.debug("Received serial event " + event);
                String line = input.readLine();
                commandProcessor.processLine(line);
            } catch (Exception e) {
                if (StringUtils.contains(e.getMessage(), "returned zero bytes")) {
                    LOGGER.debug("Error due to RXTX lib bug. Ignoring", e);
                } else {
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.error("Error trying to process command received from serial.", e);
                    } else {
                        LOGGER.error("Error trying to process command received from serial.");
                    }
                    jdbcTemplate
                            .update("INSERT INTO COMMUNICATION_FAILS(COMMAND_TIME, COMMAND) VALUES (:COMMAND_TIME, :COMMAND)",
                                    ImmutableMap.<String, Object>builder()
                                            .put("COMMAND_TIME", new Date())
                                            .put("COMMAND", StringUtils.abbreviate("input", 100))
                                            .build()
                            );
                }
            }
        }
        // Ignoring other events for now
    }
}
