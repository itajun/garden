package au.ivj.sandbox.garden.commands;

import au.ivj.sandbox.garden.processors.EMailProcessor;
import com.google.common.collect.ImmutableMap;
import org.apache.log4j.Logger;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYSplineRenderer;
import org.jfree.data.time.Minute;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.XYDataset;
import org.jfree.ui.RectangleInsets;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import javax.mail.MessagingException;
import javax.mail.util.ByteArrayDataSource;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.AbstractMap;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Sends the html summary e-mail
 */
@Component(value = "command.send_summary_email_html")
public class CommandSendSummaryEMailHTML implements Command
{
    private static final Logger LOGGER = Logger.getLogger(CommandSendSummaryEMailHTML.class);

    @Autowired
    private EMailProcessor eMailProcesor;

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    private long lastSummaryEmail = 0;

    /**
     * @return the starting date for the filters
     */
    private java.sql.Date getStartingAt() {
        /*return new java.sql.Date(LocalDateTime.now().atZone(ZoneId.systemDefault()).minusMinutes(12)
                .toInstant().getNano());*/
        return new java.sql.Date(lastSummaryEmail);
    }

    private TimeSeries getHumiditySeries() {
        TimeSeries result = new TimeSeries("Humidity");
        jdbcTemplate
                .query("SELECT READING_TIME, READING_VALUE" +
                                " FROM HUMIDITY_LOG WHERE READING_TIME >= :STARTING_AT",
                        ImmutableMap.of("STARTING_AT", getStartingAt()),
                        new RowMapper<AbstractMap.Entry<Minute, Long>>() {
                            @Override
                            public Map.Entry<Minute, Long> mapRow(ResultSet resultSet, int i) throws SQLException {
                                return new AbstractMap.SimpleImmutableEntry<>(
                                        new Minute(Date.from(resultSet.getTimestamp(1).toInstant())),
                                        resultSet.getLong(2));
                            }
                        }
                )
                .stream()
                .collect(Collectors.groupingBy(Map.Entry::getKey, Collectors.averagingLong(Map.Entry::getValue)))
                .entrySet()
                .forEach(e -> result.add(e.getKey(), e.getValue()));
        return result;
    }

    private TimeSeries getTemperatureSeries() {
        TimeSeries result = new TimeSeries("Temperature");
        jdbcTemplate
                .query("SELECT READING_TIME, READING_VALUE" +
                                " FROM TEMPERATURE_LOG WHERE READING_TIME >= :STARTING_AT",
                        ImmutableMap.of("STARTING_AT", getStartingAt()),
                        new RowMapper<AbstractMap.Entry<Minute, Long>>() {
                            @Override
                            public Map.Entry<Minute, Long> mapRow(ResultSet resultSet, int i) throws SQLException {
                                return new AbstractMap.SimpleImmutableEntry<>(
                                        new Minute(Date.from(resultSet.getTimestamp(1).toInstant())),
                                        resultSet.getLong(2));
                            }
                        }
                )
                .stream()
                .collect(Collectors.groupingBy(Map.Entry::getKey, Collectors.averagingLong(Map.Entry::getValue)))
                .entrySet()
                .forEach(e -> result.add(e.getKey(), e.getValue()));
        return result;
    }

    private TimeSeries getLightSeries() {
        TimeSeries result = new TimeSeries("Light");
        jdbcTemplate
                .query("SELECT READING_TIME, READING_VALUE" +
                                " FROM LIGHT_LOG WHERE READING_TIME >= :STARTING_AT",
                        ImmutableMap.of("STARTING_AT", getStartingAt()),
                        new RowMapper<AbstractMap.Entry<Minute, Long>>() {
                            @Override
                            public Map.Entry<Minute, Long> mapRow(ResultSet resultSet, int i) throws SQLException {
                                return new AbstractMap.SimpleImmutableEntry<>(
                                        new Minute(Date.from(resultSet.getTimestamp(1).toInstant())),
                                        resultSet.getLong(2));
                            }
                        }
                )
                .stream()
                .collect(Collectors.groupingBy(Map.Entry::getKey, Collectors.averagingLong(Map.Entry::getValue)))
                .entrySet()
                .forEach(e -> result.add(e.getKey(), e.getValue()));
        return result;
    }

    private TimeSeries getPingFailSeries() {
        TimeSeries result = new TimeSeries("Ping fails");
        jdbcTemplate
                .query("SELECT COMMAND_TIME" +
                                " FROM COMMUNICATION_FAILS WHERE COMMAND_TIME >= :STARTING_AT AND COMMAND LIKE 'ping%'",
                        ImmutableMap.of("STARTING_AT", getStartingAt()),
                        new RowMapper<AbstractMap.Entry<Minute, Long>>() {
                            @Override
                            public Map.Entry<Minute, Long> mapRow(ResultSet resultSet, int i) throws SQLException {
                                return new AbstractMap.SimpleImmutableEntry<>(
                                        new Minute(Date.from(resultSet.getTimestamp(1).toInstant())),
                                        1L);
                            }
                        }
                )
                .stream()
                .collect(Collectors.groupingBy(Map.Entry::getKey, Collectors.summingLong(Map.Entry::getValue)))
                .entrySet()
                .forEach(e -> result.add(e.getKey(), e.getValue()));
        return result;
    }

    private TimeSeries getInputFailSeries() {
        TimeSeries result = new TimeSeries("Input fails");
        jdbcTemplate
                .query("SELECT COMMAND_TIME" +
                                " FROM COMMUNICATION_FAILS WHERE COMMAND_TIME >= :STARTING_AT AND COMMAND LIKE 'input%'",
                        ImmutableMap.of("STARTING_AT", getStartingAt()),
                        new RowMapper<AbstractMap.Entry<Minute, Long>>() {
                            @Override
                            public Map.Entry<Minute, Long> mapRow(ResultSet resultSet, int i) throws SQLException {
                                return new AbstractMap.SimpleImmutableEntry<>(
                                        new Minute(Date.from(resultSet.getTimestamp(1).toInstant())),
                                        1L);
                            }
                        }
                )
                .stream()
                .collect(Collectors.groupingBy(Map.Entry::getKey, Collectors.summingLong(Map.Entry::getValue)))
                .entrySet()
                .forEach(e -> result.add(e.getKey(), e.getValue()));
        return result;
    }

    private TimeSeries getCommFailSeries() {
        TimeSeries result = new TimeSeries("Comm fails");
        jdbcTemplate
                .query("SELECT COMMAND_TIME" +
                                " FROM COMMUNICATION_FAILS WHERE COMMAND_TIME >= :STARTING_AT AND COMMAND NOT IN ('ping', " +
                                "'input')",
                        ImmutableMap.of("STARTING_AT", getStartingAt()),
                        new RowMapper<AbstractMap.Entry<Minute, Long>>() {
                            @Override
                            public Map.Entry<Minute, Long> mapRow(ResultSet resultSet, int i) throws SQLException {
                                return new AbstractMap.SimpleImmutableEntry<>(
                                        new Minute(Date.from(resultSet.getTimestamp(1).toInstant())),
                                        1L);
                            }
                        }
                )
                .stream()
                .collect(Collectors.groupingBy(Map.Entry::getKey, Collectors.summingLong(Map.Entry::getValue)))
                .entrySet()
                .forEach(e -> result.add(e.getKey(), e.getValue()));
        return result;
    }

    private XYDataset getDataset() {
        TimeSeriesCollection result = new TimeSeriesCollection();
        result.addSeries(getHumiditySeries());
        result.addSeries(getLightSeries());
        result.addSeries(getTemperatureSeries());
        result.addSeries(getPingFailSeries());
        result.addSeries(getCommFailSeries());
        result.addSeries(getInputFailSeries());

        return result;
    }

    private byte[] generateChart() throws IOException {
        JFreeChart timeSeriesChart = ChartFactory.createTimeSeriesChart("Progress",
                "Time",
                "Reading",
                getDataset(),
                true,
                false,
                false);
        ChartPanel chartPanel = new ChartPanel(timeSeriesChart, false);

        // Create plot
        NumberAxis xAxis = new NumberAxis("Minute");
        NumberAxis yAxis = new NumberAxis("Value");
        XYSplineRenderer renderer = new XYSplineRenderer();
        XYPlot plot = new XYPlot(getDataset(), xAxis, yAxis, renderer);
        plot.setBackgroundPaint(Color.lightGray);
        plot.setDomainGridlinePaint(Color.white);
        plot.setRangeGridlinePaint(Color.white);
        plot.setAxisOffset(new RectangleInsets(4, 4, 4, 4));

        // Draw png
        BufferedImage bi = new BufferedImage(600, 400,
                BufferedImage.TYPE_INT_BGR);
        Graphics graphics = bi.getGraphics();
        chartPanel.setBounds(0, 0, 600, 400);
        chartPanel.paint(graphics);
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream())
        {
            ImageIO.write(bi, "png", baos);
            return baos.toByteArray();
        }
    }

    private List<Map.Entry<String, Long>> avarageWateringTime() {
        try {
            return jdbcTemplate
                    .query("SELECT PUMP, AVG(PERIOD) FROM PUMP_COMMANDS WHERE COMMAND_TIME >= :LAST_READING GROUP BY PUMP",
                            ImmutableMap.of("LAST_READING", getStartingAt()),
                            new RowMapper<Map.Entry<String, Long>>() {
                                @Override
                                public Map.Entry<String, Long> mapRow(ResultSet resultSet, int i) throws SQLException {
                                    return new AbstractMap.SimpleImmutableEntry<String, Long>(resultSet.getString(1), resultSet.getLong(2));
                                }
                            }
                    );
        } catch (Exception e) {
            LOGGER.error("Error fetching watering time", e);
            return null;
        }
    }

    @Override
    @Async
    public void execute(List<String> payload)
    {
        Map<String, Object> context = new HashMap<>();
        context.put("lastUpdate", lastSummaryEmail == 0 ? null : new Date(lastSummaryEmail));
        context.put("averageWateringTime", avarageWateringTime());
        try
        {
            generateChart();
        }
        catch (IOException e)
        {
            LOGGER.error("Couldn't generate chart to add to e-mail", e);
            return;
        }
        eMailProcesor.sendEMail(EMailProcessor.EMailTemplate.SUMMARY_HTML, context, new Consumer<MimeMessageHelper>() {
            @Override
            public void accept(MimeMessageHelper mimeMessageHelper) {
                try {
                    mimeMessageHelper.addInline("chart", new ByteArrayDataSource(generateChart(), "image/png"));
                } catch (MessagingException | IOException e) {
                    throw new IllegalStateException("Couldn't attach chart", e);
                }
            }
        });
        lastSummaryEmail = System.currentTimeMillis();
    }
}
