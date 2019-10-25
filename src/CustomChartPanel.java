import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;

import java.awt.*;

/**
 * Created by Frykiz on 2019-10-24.
 */
public class CustomChartPanel extends ChartPanel {

    public CustomChartPanel(JFreeChart chart) {
        super(chart);
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(300, 300);
    }
}
