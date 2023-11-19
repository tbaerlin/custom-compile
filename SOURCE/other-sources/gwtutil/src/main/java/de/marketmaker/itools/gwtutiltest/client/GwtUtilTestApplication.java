package de.marketmaker.itools.gwtutiltest.client;

import com.google.gwt.core.client.Callback;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLTable;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootLayoutPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.client.ui.impl.ClippedImagePrototype;
import de.marketmaker.itools.gwtutil.client.i18n.NumberFormatterIfc;
import de.marketmaker.itools.gwtutil.client.util.CssUtil;
import de.marketmaker.itools.gwtutil.client.util.Firebug;
import de.marketmaker.itools.gwtutil.client.util.date.JsDateFormatter;
import de.marketmaker.itools.gwtutil.client.util.date.MmJsDate;
import de.marketmaker.itools.gwtutil.client.widgets.BreakLabel;
import de.marketmaker.itools.gwtutil.client.widgets.Button;
import de.marketmaker.itools.gwtutil.client.widgets.Dialog;
import de.marketmaker.itools.gwtutil.client.widgets.DropDown;
import de.marketmaker.itools.gwtutil.client.widgets.ImageSelectButton;
import de.marketmaker.itools.gwtutil.client.widgets.LeftRightToolbar;
import de.marketmaker.itools.gwtutil.client.widgets.ProgressBar;
import de.marketmaker.itools.gwtutil.client.widgets.Separator;
import de.marketmaker.itools.gwtutil.client.widgets.SimpleButton;
import de.marketmaker.itools.gwtutil.client.widgets.SliderGraph;
import de.marketmaker.itools.gwtutil.client.widgets.Tooltip;
import de.marketmaker.itools.gwtutil.client.widgets.chart.BarChart;
import de.marketmaker.itools.gwtutil.client.widgets.chart.Gauge;
import de.marketmaker.itools.gwtutil.client.widgets.highcharts.Highcharts;
import de.marketmaker.itools.gwtutil.client.widgets.chart.MiniBar;
import de.marketmaker.itools.gwtutil.client.widgets.chart.PieChart;
import de.marketmaker.itools.gwtutil.client.widgets.chart.SelectableChart;
import de.marketmaker.itools.gwtutil.client.widgets.datepicker.DateBox;
import de.marketmaker.itools.gwtutil.client.widgets.floating.FloatingPanel;
import de.marketmaker.itools.gwtutil.client.widgets.input.CheckBox;
import de.marketmaker.itools.gwtutil.client.widgets.input.Radio;
import de.marketmaker.itools.gwtutil.client.widgets.input.RadioGroup;
import de.marketmaker.itools.gwtutil.client.widgets.input.ValidatingBigDecimalBox;
import de.marketmaker.itools.gwtutil.client.widgets.menu.Menu;
import de.marketmaker.itools.gwtutil.client.widgets.menu.MenuButton;
import de.marketmaker.itools.gwtutil.client.widgets.menu.MenuItem;
import de.marketmaker.itools.gwtutil.client.widgets.menu.SelectButton;
import de.marketmaker.itools.gwtutil.client.widgets.notification.NotificationBox;
import de.marketmaker.itools.gwtutil.client.widgets.notification.NotificationMessage;
import de.marketmaker.itools.gwtutil.client.widgets.notification.Notifications;
import de.marketmaker.itools.gwtutiltest.client.util.date.DateParserTest;
import org.moxieapps.gwt.highcharts.client.Chart;
import org.moxieapps.gwt.highcharts.client.Credits;
import org.moxieapps.gwt.highcharts.client.Pane;
import org.moxieapps.gwt.highcharts.client.PaneBackground;
import org.moxieapps.gwt.highcharts.client.Point;
import org.moxieapps.gwt.highcharts.client.Series;
import org.moxieapps.gwt.highcharts.client.StockChart;
import org.moxieapps.gwt.highcharts.client.YAxis;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.ParseException;

/**
 * @author umaurer
 */
public class GwtUtilTestApplication implements EntryPoint {

    private InlineLabel labelInfo;

    public void onModuleLoad() {
        GWT.setUncaughtExceptionHandler(new GWT.UncaughtExceptionHandler() {
            public void onUncaughtException(Throwable e) {
                GWT.log("uncaught exception", e);
            }
        });
        try {
            final DockLayoutPanel dlp = new DockLayoutPanel(Style.Unit.PX);
            final FlowPanel panel = new FlowPanel();

            panel.add(createMmGauge());

            panel.add(createGauge());
            panel.add(createHighchart());
            panel.add(createStockHighchart());

            panel.add(Button.text("MessageBox")
                    .clickHandler(new ClickHandler() {
                        @Override
                        public void onClick(ClickEvent event) {
                            new Dialog()
                                    .withTitle("title")
                                    .withImage(new Image("svg/mm/gwtutil/save.svg"))
                                    .withMessage("Soll eine Nachricht hinzugefügt werden?")
                                    .withButton("Ja", new Command() {
                                        @Override
                                        public void execute() {
                                            showMessage("Du hast 'Ja' angeklickt -> Dies ist die Nachricht");
                                        }
                                    })
                                    .withButton("Nee, lieber ned")
                                    .show();
                        }
                    })
                    .build());

            panel.add(new InlineLabel("Letzte Aktion: "));
            labelInfo = new InlineLabel();
            panel.add(labelInfo);

            final Label pLabel = new Label("Hier ist ein Text, der mit \"width: 100px; overflow: hidden; white-space: nowrap;\" begrenzt ist und das Attribut completion=\"auto\" hat");
            pLabel.getElement().getStyle().setWidth(100, Style.Unit.PX);
            pLabel.getElement().getStyle().setOverflow(Style.Overflow.HIDDEN);
            pLabel.getElement().getStyle().setTextOverflow(Style.TextOverflow.ELLIPSIS);
            pLabel.getElement().getStyle().setWhiteSpace(Style.WhiteSpace.NOWRAP);
            pLabel.getElement().setAttribute("completion", "auto");
            panel.add(pLabel);
            final Label xLabel = new Label("Hier ist ein Text, der nicht mit \"width: 100px; overflow: hidden; white-space: nowrap;\" begrenzt ist, aber das Attribut completion=\"auto\" hat");
            xLabel.getElement().setAttribute("completion", "auto");
            panel.add(xLabel);

//            panel.add(createVml());
            panel.add(createCharts());

            panel.add(createValidatingBox());

            panel.add(createMinBars());

            addStyleSupport(panel, "borderRadius");
            addStyleSupport(panel, "boxShadow");
            panel.add(new Label("HelloWorld1"));
            panel.add(new InlineLabel("Line before <br/>"));
            panel.add(new BreakLabel());
            panel.add(new InlineLabel("Line after <br/>"));
            panel.add(createSliderGraph());

            final SimpleButton b1 = new SimpleButton("SimpleButton (needs styling)");
            b1.addClickHandler(new ClickHandler() {
                public void onClick(ClickEvent event) {
                    labelInfo.setText("SimpleButton was clicked");
                }
            });
            panel.add(b1);

            final DropDown<Integer> dropDownInteger = new DropDown<>();
            dropDownInteger.add(1, "eins");
            dropDownInteger.add(2, "zwei");
            dropDownInteger.add(3, "drei");
            dropDownInteger.add(4, "vier");
            dropDownInteger.add(5, "fünf");
            dropDownInteger.add(6, "sechs");
            dropDownInteger.add(7, "sieben");
            dropDownInteger.add(8, "acht");
            dropDownInteger.add(9, "neun");
            dropDownInteger.add(10, "zehn");
            dropDownInteger.add(11, "elf");
            dropDownInteger.add(12, "zwölf");
            dropDownInteger.add(13, "dreizehn");
            dropDownInteger.add(14, "vierzehn");
            dropDownInteger.setSelectedIndex(0);
            dropDownInteger.addValueChangeHandler(new ValueChangeHandler<Integer>() {
                public void onValueChange(ValueChangeEvent<Integer> event) {
                    labelInfo.setText("DropDown Wert " + event.getValue());
                }
            });
            panel.add(dropDownInteger);

            final DateBox dateBox = new DateBox(true);
            dateBox.addValueChangeHandler(new ValueChangeHandler<MmJsDate>() {
                public void onValueChange(ValueChangeEvent<MmJsDate> event) {
                    labelInfo.setText("DateBox Wert " + JsDateFormatter.formatDdmmyyyy(event.getValue(), true));
                }
            });
            panel.add(dateBox);

            panel.add(Button.text("DateParser Test")
                    .tooltip(SafeHtmlUtils.fromTrustedString("Dies ist <ein> Tooltip"))
                    .clickHandler(new ClickHandler() {
                        public void onClick(ClickEvent event) {
                            new DateParserTest();
                        }
                    })
                    .build());

            final ProgressBar progressBar = new ProgressBar();
            progressBar.setProgress(30);
            panel.add(progressBar);
            panel.add(Button.text("start progress")
                    .clickHandler(new ClickHandler() {
                        @Override
                        public void onClick(ClickEvent event) {
                            final Timer timer = new Timer() {
                                private int progress = 0;

                                @Override
                                public void run() {
                                    if (progress < 100) {
                                        progress++;
                                    }
                                    else {
                                        cancel();
                                    }
                                    progressBar.setProgress(progress);
                                }
                            };
                            timer.scheduleRepeating(25);
                        }
                    })
                    .build());

            panel.add(createToolbar());
            Button.setRendererType(Button.RendererType.SIMPLE);
            panel.add(createToolbar());

            panel.add(createInputWidgets());

            dlp.addNorth(new FloatingPanel(FloatingPanel.Orientation.HORIZONTAL).withWidget(createTopLineWidget()), 15);
            dlp.addWest(new FloatingPanel(FloatingPanel.Orientation.VERTICAL).withWidget(createWestWidget()), 150);
            dlp.addSouth(createSouthPanel(), 27);
            dlp.add(new ScrollPanel(panel));

            RootLayoutPanel.get().add(dlp);
            Tooltip.initialize();
        } catch (Exception e) {
            GWT.log("exception while creating app", e);
        }
    }

    private Widget createMmGauge() {
        final FlowPanel panel = new FlowPanel();
        final TextBox textBox = new TextBox();
        panel.add(textBox);
        final Gauge gauge = new Gauge(-5, 5, new NumberFormatterIfc() {
            @Override
            public String format(Number number) {
                return number.toString();
            }
        });
        gauge.addTicks(new Gauge.Tick(4, "4"));
        gauge.setValue(2.3);
        panel.add(gauge);

        textBox.addKeyDownHandler(new KeyDownHandler() {
            @Override
            public void onKeyDown(KeyDownEvent event) {
                if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
                    gauge.setValue(new BigDecimal(textBox.getValue()));
                }
            }
        });
        return panel;
    }

    private Widget createGauge() {
        final SimplePanel panel = new SimplePanel();
        Highcharts.initialize(Highcharts.Type.SOLIDGAUGE, new Callback<Void, Exception>() {
            @Override
            public void onFailure(Exception reason) {
                Notifications.add("error", "could not initialize highcharts - " + reason.getMessage());
                panel.add(new Label("Highchart initialization error"));
            }

            @Override
            public void onSuccess(Void result) {
                final Chart chart = new Chart()
//                        .setType(Series.Type.GAUGE) // SOLIDGAUGE is not available in gwt-highcharts-1.6.0
                        .setOption("/chart/type", "solidgauge")
                        .setChartTitleText("Mein Tacho")
                        .setMarginRight(10)
                        .setCredits(new Credits().setEnabled(false));
                chart.setPane(new Pane().setStartAngle(-90).setEndAngle(90).setBackground(new PaneBackground().setOption("shape", "arc")));
                final YAxis yAxis = chart.getYAxis();
                yAxis.setMin(-3.5).setMax(7.8)
                        .setAxisTitleText("km/h")
                        .setPlotBands(
                                yAxis.createPlotBand().setColor("#00ff00").setFrom(0).setTo(120),
                                yAxis.createPlotBand().setColor("#ffff00").setFrom(120).setTo(160),
                                yAxis.createPlotBand().setColor("#ff0000").setFrom(160).setTo(200)
                        );
                final Series series = chart.createSeries()
                        .setName("Geschwindigkeit")
                        .setPoints(new Point[]{
                                new Point(4.2)
                        });
                chart.addSeries(series);
                panel.setWidget(chart);
            }
        });
        return panel;
    }

    private Widget createHighchart() {
        final SimplePanel panel = new SimplePanel();
        Highcharts.initialize(Highcharts.Type.DEFAULT, new Callback<Void, Exception>() {
            @Override
            public void onFailure(Exception reason) {
                Notifications.add("error", "could not initialize highcharts - " + reason.getMessage());
                panel.add(new Label("Highchart initialization error"));
            }

            @Override
            public void onSuccess(Void result) {
                final Chart chart = new Chart()
                        .setType(Series.Type.PIE)
                        .setChartTitleText("Lawn Tunnels")
                        .setMarginRight(10)
                        .setCredits(new Credits().setEnabled(false));
                final Series series = chart.createSeries()
                        .setName("Moles per Yard")
                        .setPoints(new Point[]{
                                new Point("Eins", 163),
                                new Point("Zwei", 203),
                                new Point("Drei", 276),
                                new Point("Vier", 408),
                                new Point("Fünf", 547.5),
                                new Point("Sechs", 729),
                                new Point("Sieben", 128)
                        });
                chart.addSeries(series);
                panel.setWidget(chart);
            }
        });
        return panel;
    }

    private Widget createStockHighchart() {
        final SimplePanel panel = new SimplePanel();
        Highcharts.initialize(Highcharts.Type.DEFAULT, new Callback<Void, Exception>() {
            @Override
            public void onFailure(Exception reason) {
                Notifications.add("error", "could not initialize highcharts - " + reason.getMessage());
                panel.add(new Label("Highchart initialization error"));
            }

            @Override
            public void onSuccess(Void result) {
                final StockChart chart = new StockChart()
                        .setChartTitleText("Lawn Tunnels")
                        .setMarginRight(10)
                        .setCredits(new Credits().setEnabled(false));
                final Series series = chart.createSeries()
                        .setName("Moles per Yard")
                        .setPoints(new Point[]{
                                new Point("Eins", 163),
                                new Point("Zwei", 203),
                                new Point("Drei", 276),
                                new Point("Vier", 408),
                                new Point("Fünf", 547.5),
                                new Point("Sechs", 729),
                                new Point("Sieben", 628)
                        });
                chart.addSeries(series);
                panel.setWidget(chart);
            }
        });
        return panel;
    }

    private static Boolean svgSupported = null;

    public static boolean isSvgSupported() {
        if (svgSupported == null) {
            svgSupported = _isSvgSupported();
        }
        return svgSupported;
    }

    private static native boolean _isSvgSupported() /*-{
        return !!document.createElementNS && !!document.createElementNS('http://www.w3.org/2000/svg', "svg").createSVGRect;
    }-*/;

    private Widget createCharts() {
        final VerticalPanel panel = new VerticalPanel();
        final HorizontalPanel hPanel = new HorizontalPanel();
        // final BarChart barChart3 = createAnotherBarChart(BarChart.Style.VERTICAL);

        if (isSvgSupported()) {
            final PieChart pieChart = createPieChart();
            hPanel.add(pieChart);
            pieChart.addSelectionHandler(new SelectionHandler<SelectableChart.Index>() {
                @Override
                public void onSelection(SelectionEvent<SelectableChart.Index> event) {
                    pieChart.setSelectedValue(event.getSelectedItem());
                }
            });
        }
/*
        final VmlPieChart vmlPieChart = createVmlPieChart();
        hPanel.add(vmlPieChart);

        final Label clickLabel = new Label("vml click");
        clickLabel.addMouseDownHandler(new MouseDownHandler() {
            @Override
            public void onMouseDown(MouseDownEvent event) {
                vmlPieChart.setSelectedIndex(2);
                Firebug.debug("down");
            }
        });
        clickLabel.addMouseUpHandler(new MouseUpHandler() {
            @Override
            public void onMouseUp(MouseUpEvent event) {
                vmlPieChart.setSelectedIndex(null);
                Firebug.debug("up");
            }
        });
        hPanel.add(clickLabel);
*/

        panel.add(hPanel);
        final BarChart barChart1 = createBarChart(false);
        final BarChart barChart2 = createBarChart(true);
        barChart1.addSelectionHandler(new SelectionHandler<SelectableChart.Index>() {
            @Override
            public void onSelection(SelectionEvent<SelectableChart.Index> event) {
                barChart2.setSelectedValue(event.getSelectedItem());
            }
        });
        barChart2.addSelectionHandler(new SelectionHandler<SelectableChart.Index>() {
            @Override
            public void onSelection(SelectionEvent<SelectableChart.Index> event) {
                barChart1.setSelectedValue(event.getSelectedItem());
            }
        });
        final FlexTable chartTable = new FlexTable();
        chartTable.setWidget(0, 0, barChart1);
        chartTable.setWidget(0, 1, barChart2);
        chartTable.getFlexCellFormatter().setRowSpan(0, 1, 2);
        chartTable.setWidget(1, 0, createBarChart2(false));
        panel.add(chartTable);
        //panel.add(barChart3);
        panel.add(createNextBarChart2(false));
        return panel;
    }

    private PieChart createPieChart() {
        final PieChart pieChart = new PieChart();
        pieChart.setPixelSize(200, 200);
        pieChart.drawChart(
                new PieChart.Entry("Erstes", 55),
                new PieChart.Entry("Zweites", 25),
                new PieChart.Entry("Drittes", 8),
                new PieChart.Entry("Viertes", 5),
                new PieChart.Entry("Fünftes", 5)
        );
        return pieChart;
    }

    private BarChart createNextBarChart(boolean horizontal) {
        final BarChart barChart = new BarChart();
        barChart.config().horizontal(horizontal);
        barChart.setPixelSize(200, 200);
        final BarChart.Entry[] entries = {
                new BarChart.Entry("Erstes", "red", 1.1f),
                new BarChart.Entry("Zweites", "blue", 1.5f),
                new BarChart.Entry("Drittes", "green", -2f),
                new BarChart.Entry("Viertes", "yellow", -1.1f)};
        barChart.drawChart(entries);
        return barChart;
    }

    private BarChart createNextBarChart2(boolean horizontal) {
        final BarChart barChart = new BarChart();
        barChart.config().horizontal(horizontal);
        barChart.setPixelSize(200, 200);
        final BarChart.Entry[] entries = {
                new BarChart.Entry("A", "a", 11319.811f),
                new BarChart.Entry("B", "b", 5300f),
                new BarChart.Entry("C", "c", 74240f),
                new BarChart.Entry("D", "d", 159.058f),
                new BarChart.Entry("E", "e", 0f),
                new BarChart.Entry("F", "f", 99917.8f),
                new BarChart.Entry("G", "g", 69057.49f),
                new BarChart.Entry("H", "h", 151256.164f)
        };
        barChart.drawChart(entries);
        return barChart;
    }


    private BarChart createAnotherBarChart(boolean horizontal) {
        final BarChart barChart = new BarChart();
        barChart.config().horizontal(horizontal);
        barChart.setPixelSize(200, 200);
        final BarChart.Entry[] entries = {
                new BarChart.Entry("Erstes", "red", (float) 1.125d),
                new BarChart.Entry("Zweites", "blue", (float) 1.125d),
                new BarChart.Entry("Drittes", "green", (float) 1.125d),
                new BarChart.Entry("Viertes", "yellow", (float) 1.125d)};
        barChart.drawChart(entries);
        return barChart;
    }

    private BarChart createBarChart(boolean horizontal) {
        final BarChart barChart = new BarChart();
        barChart.config().horizontal(horizontal).entryLabels();
        barChart.drawChart(
                entry2("Erstes", "red", 55),
                entry2("Zweites", "blue", 25),
                entry2("Drittes", "green", 8),
                entry2("Viertes", "yellow", 5),
                entry2("6", "orange", 5),
                entry2("7", "orange", -15),
                entry2("8", "orange", 0),
                entry2("9", "orange", 24),
                entry2("10", "orange", 50),
                entry2("11", "orange", 25),
                entry2("12", "orange", 35),
                entry2("13", "orange", 45),
                entry2("14", "orange", -35),
                entry2("15", "orange", 13),
                entry2("16", "orange", 5),
                entry2("17", "orange", 15),
                entry2("18", "orange", 25),
                entry2("19", "orange", 24),
                entry2("20", "orange", 51),
                entry2("21", "orange", 78),
                entry2("22", "orange", 35),
                entry2("23", "orange", 45),
                entry2("24", "orange", 35),
                entry2("25", "orange", 11),
                entry2("26", "orange", 5),
                entry2("31", "orange", 25),
                entry2("27", "orange", 15),
                entry2("28", "orange", 25),
                entry2("29", "orange", 14),
                entry2("30", "orange", 51),
                entry2("32", "orange", 35),
                entry2("33", "orange", 15),
                entry2("34", "orange", 35),
                entry2("35", "orange", 13)
        );
        return barChart;
    }

    private BarChart createBarChart2(boolean horizontal) {
        final BarChart barChart = new BarChart();
        barChart.config().horizontal(horizontal).entryGap(10).entryLabels();
        barChart.drawChart(
                entry2("Erstes\nmit zweiter Zeile", "red", 55, 57, 25, 21, 8, 12, 5, 5, 5, 7, -15, 3, 25, -1),
                entry2("Zweites", "orange", 24, 0, 50, 55, 25, 11, 35, 19, 45, 17, -35, -20, 13, 17),
                entry2("Drittes", "orange", 5, 45, 15, 11, 25, 6, 24, 38, 51, 60, 85, 49, 35, 28),
                entry2("Viertes", "orange", 45, 16, 35, 75, 11, 4, 5, 30, 15, 18, 25, 19, 14, 20),
                entry2("Fünftes", "orange", 51, 21, 25, 22, 35, 23, 15, 24, 35, 25, 13, 26, -23, 75)
        );
        return barChart;
    }

    private BarChart.Entry entry2(String name, String style, float... values) {
        final BarChart.Value[] v = new BarChart.Value[values.length];
        for (int i = 0; i < values.length; i++) {
            v[i] = new BarChart.Value(values[i]).withTooltip(name + ": " + Float.toString(values[i]));
        }
        return new BarChart.Entry(name, style, v);
    }

    private Widget createWestWidget() {
        final SafeHtmlBuilder sb = new SafeHtmlBuilder();
        for (int i = 0; i < 520; i++) {
            sb.appendEscaped(String.valueOf(i));
            sb.appendHtmlConstant("<br/>");
        }
        final HTML html = new HTML(sb.toSafeHtml());
        html.getElement().getStyle().setBackgroundColor("orange");
        return html;
    }

    int counter = 0;

    private void addStyleSupport(FlowPanel panel, final String styleName) {
        final boolean supported = CssUtil.supportsStyle(styleName);
        final Label label = new Label(styleName + (supported ? " +" : " -"));
        label.getElement().setId(String.valueOf("xx-" + counter++));
        Tooltip.addCompletion(label, SafeHtmlUtils.fromTrustedString(styleName + (supported ? "<br>supported" : " not supported"))).withBackground("orange");
        label.getElement().getStyle().setFontSize(120, Style.Unit.PCT);
        label.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                Window.alert("The following style is " + (supported ? "" : "not ") + "supported: " + styleName);
            }
        });
        panel.add(label);
    }

    private Widget createToolbar() {
        final LeftRightToolbar panel = new LeftRightToolbar();
        panel.addLeft(Button.build());
        panel.addLeft(Button.text("ein Button").build());
        panel.addLeft(Button.icon("saveIcon").text("ein Button").build());
        panel.addLeft(Button.icon("saveIcon", Button.IconPosition.LEFT).build());
        panel.addLeft(new Separator());
        panel.addLeft(createDisabledButton());
        panel.addLeft(createActiveButton());
        panel.addLeft(createSelectButton(Button.getRendererType()));
        panel.addLeft(createSelectButton(Button.RendererType.SPAN));
        panel.addLeft(createActiveSelectButton());
        panel.addRight(createImageSelectButton());
        panel.addRight(createMenuButton());
        return panel;
    }

    private Button createDisabledButton() {
        final Button button = Button.text("Disabled Button").build();
        button.setEnabled(false);
        return button;
    }

    private Button createActiveButton() {
        return Button.text("Active Button").active().build();
    }

    private ImageSelectButton createImageSelectButton() {
        final Image image = new ClippedImagePrototype("svg/mm/gwtutil/save.svg", 0, 0, 19, 19).createImage();
        final ImageSelectButton imageSelectButton = new ImageSelectButton(image, null, null);

        Menu menu = new Menu();
        for (int i = 0; i < 100; i++) {
            final MenuItem menuItem = new MenuItem("item " + i);
            menuItem.setData("value", i);
            menu.add(menuItem);
        }

        imageSelectButton.withMenu(menu);
        imageSelectButton.setSelectedItem(menu.getItems().get(0));
        imageSelectButton.setClickOpensMenu(true);

        imageSelectButton.addSelectionHandler(new SelectionHandler<MenuItem>() {
            @Override
            public void onSelection(SelectionEvent<MenuItem> menuItemSelectionEvent) {
                final MenuItem menuItem = imageSelectButton.getSelectedItem();
                final Integer value = (Integer) menuItem.getData("value");
                labelInfo.setText("Selected value is: " + value);
            }
        });

        return imageSelectButton;
    }

    private Button createMenuButton() {
        final Menu menu = new Menu();
        /*
        menu.add(createItem("Item 1"));
        menu.addSeparator();
        final MenuItem menuItem = createItem("Item 2");
        menuItem.setIcon(new ClippedImagePrototype("svg/mm/gwtutil/save.svg", 0, 0, 19, 19));
        menu.add(menuItem);
        */
        final MenuButton button = new MenuButton("Menu Button").withMenu(menu);
        Tooltip.addQtip(button, "Hier kommt ein ganzganzganzganzganzganzganzganzganzganzganzganzganzganz langer Tooltip");
        return button;
    }

    private Button createSelectButton(Button.RendererType rendererType) {
        final Menu menu = new Menu();
        for (int count = 0; count < 30; count++) {
            char c = (char) (count + 65);
            menu.add(new MenuItem(c + " item " + count, null));
        }
        final SelectButton button = new SelectButton(rendererType).withMenu(menu);
        button.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                showMessage(button.getSelectedItem().getHtml());
            }
        });
        return button;
    }

    private Button createActiveSelectButton() {
        final Button button = createSelectButton(Button.getRendererType());
        button.setActive(true);
        return button;
    }

    private MenuItem createItem(final String text) {
        return new MenuItem(text, "saveIcon", new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                showMessage(text);
            }
        });
    }

    private void showMessage(String text) {
        this.labelInfo.setText(text);
    }

    private Widget createTopLineWidget() {
        HTML html = new HTML("<div class=\"as-topLine-element\" title=\"DAX, ETR\"><span class=\"name\">DAX</span><span class=\"time\">17:21</span><span class=\"price p\">7,748.49</span><span class=\"diff p\">+1.57%</span></div><div class=\"as-topLine-element\" title=\"TecDAX, ETR\"><span class=\"name\">TecDAX</span><span class=\"time\">17:21</span><span class=\"price p\">904.60</span><span class=\"diff p\">+0.76%</span></div><div class=\"as-topLine-element\" title=\"MDAX, ETR\"><span class=\"name\">MDAX</span><span class=\"time\">17:21</span><span class=\"price p\">13,168.67</span><span class=\"diff p\">+1.39%</span></div><div class=\"as-topLine-element\" title=\"SMI, CH\"><span class=\"name\">SMI</span><span class=\"time\">17:06</span><span class=\"price p\">7,568.23</span><span class=\"diff p\">+0.95%</span></div><div class=\"as-topLine-element\" title=\"Euro STOXX 50, STX\"><span class=\"name\">ESTX 50</span><span class=\"time\">17:21</span><span class=\"price p\">2,661.40</span><span class=\"diff p\">+1.71%</span></div><div class=\"as-topLine-element\" title=\"DJ Industrial Average, DJ\"><span class=\"name\">Dow Jones</span><span class=\"time\">17:21</span><span class=\"price p\">14,027.05</span><span class=\"diff p\">+0.32%</span></div><div class=\"as-topLine-element\" title=\"Nasdaq Composite, IQ\"><span class=\"name\">Nasdaq C</span><span class=\"time\">17:06</span><span class=\"price p\">3,200.0012</span><span class=\"diff p\">+0.25%</span></div><div class=\"as-topLine-element\" title=\"Nikkei 225, NIKKEI\"><span class=\"name\">Nikkei 225</span><span class=\"time\">18.02.</span><span class=\"price p\">11,407.87</span><span class=\"diff p\">+2.09%</span></div><div class=\"as-topLine-element\" title=\"EUR Europäischer Euro (USD), FXVWD\"><span class=\"name\">EUR/USD</span><span class=\"time\">17:21</span><span class=\"price p\">1.336305</span><span class=\"diff p\">+0.07%</span></div><div class=\"as-topLine-element\" title=\"Umlaufrendite, BUBA\"><span class=\"name\">U-Rendite</span><span class=\"time\">12:03</span><span class=\"price p\">1.32</span><span class=\"diff p\">+0.76%</span></div><div class=\"as-topLine-element\" title=\"FGBL Future endl, DTB\"><span class=\"name\">Bund-Fut</span><span class=\"time\">17:21</span><span class=\"price p\">142.85</span><span class=\"diff p\">+0.06%</span></div><div class=\"as-topLine-element\" title=\"Dated Brent FOB Sul. V, RSM\"><span class=\"name\">Rohöl</span><span class=\"time\">17:19</span><span class=\"price n\">116.93</span><span class=\"diff n\">-0.95%</span></div><div class=\"as-topLine-element\" title=\"Gold Unze, FXVWD\"><span class=\"name\">Gold $/Oz</span><span class=\"time\">17:21</span><span class=\"price n\">1,603.714</span><span class=\"diff n\">-0.39%</span></div>");
        html.setStyleName("as-topLine");
        return html;
    }

    private Widget createInputWidgets() {
        final FlexTable table = new FlexTable();
        int row = 0;
        final RadioGroup<String> radioGroup = new RadioGroup<>();
        final CheckBox checkBox = new CheckBox(true);
        table.setWidget(row, 0, checkBox.createLabel("enabled"));
        table.setWidget(row, 1, checkBox);
        checkBox.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {
                radioGroup.setEnabled(event.getValue());
            }
        });
        row++;
        addRadio(table, row++, radioGroup, "Radio " + row, true, true);
        addRadio(table, row++, radioGroup, "Radio " + row, false, true);
        addRadio(table, row++, radioGroup, "Radio " + row, false, false);
        addRadio(table, row++, radioGroup, "Radio " + row, false, true);
        addRadio(table, row++, radioGroup, "Radio " + row, false, true);
        final int lastRow = row;
        table.getFlexCellFormatter().setColSpan(row, 0, 2);
        radioGroup.addValueChangeHandler(new ValueChangeHandler<String>() {
            @Override
            public void onValueChange(ValueChangeEvent<String> event) {
                table.setText(lastRow, 0, event.getValue());
            }
        });
        return table;
    }

    private void addRadio(FlexTable table, int row, RadioGroup<String> radioGroup, String text, boolean checked, boolean enabled) {
        final Radio<String> radio = radioGroup.add(text + " Value", checked);
        radio.setEnabled(enabled);
        table.setWidget(row, 0, radio.createLabel(text));
        table.setWidget(row, 1, radio);
    }

    private Widget createSliderGraph() {
        final HorizontalPanel hp = new HorizontalPanel();
        Tooltip.addQtipLabel(hp, "slider graph").withStyle("bigger");
        final SliderGraph sliderGraph = new SliderGraph("svg/mm/gwtutil/slider-graph.svg", "svg/mm/gwtutil/slider-slider.svg", true);
        sliderGraph.setExplainText("sliderGraph");
        sliderGraph.setLowHighTexts("0", "100");
        sliderGraph.setSliderVisible(true);
        sliderGraph.setValue(0);
        hp.add(sliderGraph);
        hp.add(createSliderSelector(sliderGraph));
        return hp;
    }

    private Button createSliderSelector(final SliderGraph sliderGraph) {
        final Menu menu = new Menu();
        for (int value = 0; value <= 100; value += 10) {
            menu.add(new MenuItem("Slider Wert: " + value, null).withData("value", value));
        }
        final SelectButton button = new SelectButton().withMenu(menu);
        button.addSelectionHandler(new SelectionHandler<MenuItem>() {
            @Override
            public void onSelection(SelectionEvent<MenuItem> event) {
                final Integer value = (Integer) event.getSelectedItem().getData("value");
                sliderGraph.setValue(value);
            }
        });
        return button;
    }

    private Widget createSouthPanel() {
        final DockLayoutPanel dlp = new DockLayoutPanel(Style.Unit.PX);
        dlp.setStyleName("southPanel");
        dlp.addWest(Button.text("notification")
                .clickHandler(new ClickHandler() {
                    int counter = 0;

                    @Override
                    public void onClick(ClickEvent event) {
                        final SafeHtmlBuilder sb = new SafeHtmlBuilder();
                        sb.appendEscaped("Dies ist eine lange Mitteilung");
                        sb.appendHtmlConstant("<br/>");
                        sb.appendEscaped("mit drei Zeilen");
                        sb.appendHtmlConstant("<br/>");
                        sb.appendEscaped("counter: ").append(counter++);

                        final NotificationMessage message = Notifications.add("Meldung", sb.toSafeHtml());
                        message.requestStateDelayed(NotificationMessage.State.DELETED, 10);
                    }
                })
                .build(), 100);

        dlp.addWest(Button.text("longLast")
                .clickHandler(new ClickHandler() {
                    @Override
                    public void onClick(ClickEvent event) {
                        Notifications.add("Langlebige Meldung", "Das hier verschwindet nicht mehr. Es muss explizit geschlossen werden");
                    }
                })
                .build(), 100);

        dlp.addWest(Button.text("progress")
                .clickHandler(new ClickHandler() {
                    @Override
                    public void onClick(ClickEvent event) {
                        final NotificationMessage message = Notifications.add("Fortschritt", "Das dauert jetzt mal ein bischen, danach noch mal und pötzlich ist es wech!", 0d);
                        Scheduler.get().scheduleFixedPeriod(new Scheduler.RepeatingCommand() {
                            final long start = System.currentTimeMillis();

                            public boolean execute() {
                                final double duration = System.currentTimeMillis() - this.start;
                                message.setProgress(duration / 15000d);
                                if (duration < 15000) {
                                    return true;
                                }
                                else {
                                    message.requestStateDelayed(NotificationMessage.State.DELETED, 10);
                                    return false;
                                }
                            }
                        }, 100);
                    }
                })
                .build(), 100);

        dlp.addWest(Button.text("hidden").clickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                final NotificationMessage message = Notifications.addHidden("Hidden", "Eine Meldung, die nur versteckt hinzugefügt wird und erscheint, wenn der Fortschritt abgeschlossen ist.", 0);
                Scheduler.get().scheduleFixedPeriod(new Scheduler.RepeatingCommand() {
                    final long start = System.currentTimeMillis();

                    public boolean execute() {
                        final double duration = System.currentTimeMillis() - this.start;
                        message.setProgress(duration / 15000d);
                        if (duration < 15000) {
                            return true;
                        }
                        else {
                            message.requestState(NotificationMessage.State.VISIBLE);
                            return false;
                        }
                    }
                }, 100);
            }
        }).build(), 100);

        dlp.addEast(new NotificationBox(new NotificationBox.I18nCallback() {
            @Override
            public String nNotifications(int count) {
                return count == 1 ? "1 Benachrichtigung" : (count + " Benachrichtigungen");
            }
        }), 140);
        return dlp;
    }

    private Widget createValidatingBox() {
        final HorizontalPanel hp = new HorizontalPanel();
        final Label label = new Label("xx");
        final ValidatingBigDecimalBox box = new ValidatingBigDecimalBox("Format: " + ValidatingBigDecimalBox.BigDecimalRenderer.instance().render(new BigDecimal("1234567.8888")));
        box.addValueChangeHandler(new ValueChangeHandler<BigDecimal>() {
            @Override
            public void onValueChange(ValueChangeEvent<BigDecimal> event) {
                Firebug.debug("value change event: " + event.getValue().movePointRight(-3).setScale(2, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString());
                label.setText(event.getValue().toPlainString());
            }
        });
        hp.add(box);
        hp.add(label);
        hp.add(Button.text("1.1")
                .clickHandler(new ClickHandler() {
                    @Override
                    public void onClick(ClickEvent event) {
                        box.setValue(new BigDecimal("123123.1"));
                    }
                })
                .build());
        try {
            hp.add(new Label("  render: " + ValidatingBigDecimalBox.BigDecimalRenderer.instance().render(new BigDecimal("1.1"))));
            addParsed(hp, "1.1");
            addParsed(hp, "1,1");
        } catch (ParseException e) {
            hp.add(new Label(e.getMessage()));
        }
        return hp;
    }

    private void addParsed(HorizontalPanel hp, String value) throws ParseException {
        final BigDecimal parsed;
        try {
            parsed = ValidatingBigDecimalBox.BigDecimalParser.instance().parse(value);
            hp.add(new HTML("&nbsp;&nbsp;&nbsp;parse " + value + ": " + parsed.toPlainString()));
        } catch (ParseException e) {
            hp.add(new HTML("&nbsp;&nbsp;&nbsp;parse " + value + " exception: " + e.getMessage()));
        }
    }

    private Widget createMinBars() {
        final FlexTable table = new FlexTable();
        final HTMLTable.ColumnFormatter columnFormatter = table.getColumnFormatter();
        columnFormatter.setWidth(1, "50px");
        columnFormatter.setWidth(2, "50px");
        addMiniBar(table, 5, -10, 15);
        addMiniBar(table, -5, -10, 15);
        addMiniBar(table, -8, -10, 15);
        addMiniBar(table, -10, -10, 15);
        addMiniBar(table, 15, -10, 15);
        addMiniBar(table, 10, -10, 15);
        addMiniBar(table, 3, -10, 15);
        addMiniBar(table, 0, -10, 15);
        return table;
    }

    private void addMiniBar(FlexTable table, double value, double minValue, double maxValue) {
        final int row = table.getRowCount();
        table.setText(row, 0, Double.toString(value));
        table.setWidget(row, 1, new MiniBar(value, minValue, maxValue));
        table.setHTML(row, 2, new SimplePanel(new MiniBar(value, minValue, maxValue)).getElement().getInnerHTML());
    }
}
