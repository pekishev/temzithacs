package ru.temzit.wificontrol;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import com.jjoe64.graphview.DefaultLabelFormatter;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.LegendRenderer;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import java.text.SimpleDateFormat;
import java.util.Date;

/* loaded from: classes.dex */
public class GraphActivity extends AppCompatActivity {
    @Override // android.support.v7.app.AppCompatActivity, android.support.v4.app.FragmentActivity, android.support.v4.app.SupportActivity, android.app.Activity
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(temzit.wificontrol.R.layout.activity_graph);
        GraphView graphView = (GraphView) findViewById(temzit.wificontrol.R.id.graph);
        graphView.getViewport().setScalable(true);
        graphView.getViewport().setYAxisBoundsManual(true);
        graphView.getViewport().setMinY(-40.0d);
        double d = 100.0d;
        graphView.getViewport().setMaxY(100.0d);
        DataPoint[] dataPointArr = new DataPoint[10000];
        for (int i = 0; i < 10000; i++) {
            dataPointArr[i] = new DataPoint(i * 10000, (int) ((Math.random() * 100.0d) - 20.0d));
        }
        LineGraphSeries lineGraphSeries = new LineGraphSeries(dataPointArr);
        graphView.addSeries(lineGraphSeries);
        DataPoint[] dataPointArr2 = new DataPoint[10000];
        int i2 = 0;
        while (i2 < 10000) {
            dataPointArr2[i2] = new DataPoint(i2 * 10000, (int) ((Math.random() * d) - 20.0d));
            i2++;
            d = 100.0d;
        }
        LineGraphSeries lineGraphSeries2 = new LineGraphSeries(dataPointArr2);
        graphView.addSeries(lineGraphSeries2);
        lineGraphSeries.setTitle("Tout");
        lineGraphSeries.setColor(-32768);
        lineGraphSeries2.setTitle("Tin");
        lineGraphSeries2.setColor(-16776961);
        graphView.getLegendRenderer().setVisible(true);
        graphView.getLegendRenderer().setAlign(LegendRenderer.LegendAlign.TOP);
        graphView.getGridLabelRenderer().setNumHorizontalLabels(3);
        graphView.getViewport().setMinX(10000.0d);
        graphView.getViewport().setMaxX(150000.0d);
        graphView.getViewport().setXAxisBoundsManual(true);
        graphView.getGridLabelRenderer().setHumanRounding(false);
        graphView.getGridLabelRenderer().setLabelFormatter(new DefaultLabelFormatter() { // from class: ru.temzit.wificontrol.GraphActivity.1
            @Override // com.jjoe64.graphview.DefaultLabelFormatter, com.jjoe64.graphview.LabelFormatter
            public String formatLabel(double d2, boolean z) {
                if (z) {
                    return new SimpleDateFormat("HH:mm\ndd MMM").format(new Date(((long) d2) * 1000));
                }
                return super.formatLabel(d2, z);
            }
        });
    }
}
