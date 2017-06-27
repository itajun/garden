package au.ivj.sandbox.garden.commands;

import au.ivj.sandbox.garden.processors.EMailProcessor;
import com.google.common.collect.ImmutableMap;
import org.apache.log4j.Logger;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYSplineRenderer;
import org.jfree.data.xy.XYDataItem;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.RectangleInsets;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Sends the html summary e-mail
 */
@Component(value = "command.send_html_summary_email")
public class CommandSendHTMLSummaryEMail implements Command
{
    private static final Logger LOGGER = Logger.getLogger(CommandSendHTMLSummaryEMail.class);

    @Autowired
    private EMailProcessor eMailProcesor;

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    private long lastSummaryEmail = 0;

    private XYDataset getHumidityLightDataset() {
        XYSeriesCollection result = new XYSeriesCollection();
        XYSeries humiditySeries = new XYSeries("Humidity");
        XYSeries lightSeries = new XYSeries("Light");

        jdbcTemplate
                .query("SELECT reading_time, READING_VALUE" +
                                " FROM HUMIDITY_LOG WHERE READING_TIME >= :STARTING_AT",
                        ImmutableMap.of("STARTING_AT",
                                new java.sql.Date(LocalDateTime.now().atZone(ZoneId.systemDefault()).minusHours(12)
                                        .toInstant().getNano())),
                        new RowMapper<XYDataItem>() {
                            @Override
                            public XYDataItem mapRow(ResultSet resultSet, int i) throws SQLException {
                                return new XYDataItem(resultSet.getTimestamp(1).toLocalDateTime().getHour(),
                                        resultSet.getLong(2));
                            }
                        }
                ).stream().forEach(e -> humiditySeries.add(e));

        jdbcTemplate
                .query("SELECT DATEADD(HOUR, DATEDIFF(HOUR, 0, READING_TIME), 0), READING_VALUE" +
                                " FROM LIGHT_LOG WHERE READING_TIME >= :STARTING_AT",
                        ImmutableMap.of("STARTING_AT",
                                new java.sql.Date(LocalDateTime.now().atZone(ZoneId.systemDefault()).minusHours(12)
                                        .toInstant().getNano())),
                        new RowMapper<XYDataItem>() {
                            @Override
                            public XYDataItem mapRow(ResultSet resultSet, int i) throws SQLException {
                                return new XYDataItem(resultSet.getTimestamp(1).toLocalDateTime().getHour(),
                                        resultSet.getLong(2));
                            }
                        }
                ).stream().forEach(e -> lightSeries.add(e));


        result.addSeries(humiditySeries);
        result.addSeries(lightSeries);

        return result;
    }

    private byte[] generateChart() throws IOException
    {
            // Create plot
            NumberAxis xAxis = new NumberAxis("Hour");
            NumberAxis yAxis = new NumberAxis("Value");
            XYSplineRenderer renderer = new XYSplineRenderer();
            XYPlot plot = new XYPlot(getHumidityLightDataset(), xAxis, yAxis, renderer);
            plot.setBackgroundPaint(Color.lightGray);
            plot.setDomainGridlinePaint(Color.white);
            plot.setRangeGridlinePaint(Color.white);
            plot.setAxisOffset(new RectangleInsets(4, 4, 4, 4));

            // Create chart
            JFreeChart chart = new JFreeChart("Progress",
                    JFreeChart.DEFAULT_TITLE_FONT, plot, true);
            ChartUtilities.applyCurrentTheme(chart);
            ChartPanel chartPanel = new ChartPanel(chart, false);

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

    @Override
    @Async
    public void execute(List<String> payload)
    {
        Map<String, Object> context = new HashMap<>();
        context.put("lastUpdate", lastSummaryEmail == 0 ? null : new Date(lastSummaryEmail));
        context.put("chart", lastSummaryEmail == 0 ? null : new Date(lastSummaryEmail));
        try
        {
            generateChart();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        eMailProcesor.sendEMail(EMailProcessor.EMailTemplate.SUMMARY_HTML, context);
        lastSummaryEmail = System.currentTimeMillis();
    }
}
