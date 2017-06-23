package au.ivj.sandbox.garden.processors;

import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringReader;
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

    private String port = "COM1"; //serial.port

    private int timeout = 2000; // serial.timeout

    private int dataRate = 9600; // serial.dataRate

    private SerialPort serialPort;

    private BufferedReader input;

    private BufferedWriter output;

    @PostConstruct
    public void connectToComm() {
        if ("null".equals(port)) {
            LOGGER.warn("Won't actually write to serial. SIMULATION MODE");
            input = new BufferedReader(new StringReader(""));
            output = new BufferedWriter(new PrintWriter(System.out));
            return;
        }

        LOGGER.debug("Creating COMM connector");

        CommPortIdentifier portId = null;
        Enumeration portEnum = CommPortIdentifier.getPortIdentifiers();

        while (portEnum.hasMoreElements()) {
            CommPortIdentifier currPortId = (CommPortIdentifier) portEnum.nextElement();
            if (currPortId.getName().equals(port)) {
                portId = currPortId;
                break;
            }
        }

        if (portId == null) {
            LOGGER.error("Hummm... I can't find the port " + port);
            throw new IllegalStateException("Can't find port " + port);
        }

        try {
            serialPort = (SerialPort) portId.open("Garden", timeout);

            serialPort.setSerialPortParams(dataRate,
                    SerialPort.DATABITS_8,
                    SerialPort.STOPBITS_1,
                    SerialPort.PARITY_NONE);

            input = new BufferedReader(new InputStreamReader(serialPort.getInputStream()));
            output = new BufferedWriter(new OutputStreamWriter(serialPort.getOutputStream()));

            serialPort.addEventListener(this::serialEvent);
            serialPort.notifyOnDataAvailable(true);
        } catch (Exception e) {
            LOGGER.error("Ouch! Error trying to open the comm port " + port, e);
            throw new IllegalArgumentException(e);
        }
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

    @Async
    public void spinUp() {
        LOGGER.info("Will open port " + port);
        LOGGER.info("Will timeout " + timeout);

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
                LOGGER.error("Error trying to process command received from serial.", e);
            }
        }
        // Ignoring other events for now
    }
}
