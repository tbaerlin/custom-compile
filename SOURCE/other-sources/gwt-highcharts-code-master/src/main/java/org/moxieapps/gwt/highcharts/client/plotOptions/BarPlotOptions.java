/*
 * Copyright 2011 Moxie Group
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.moxieapps.gwt.highcharts.client.plotOptions;

import org.moxieapps.gwt.highcharts.client.Chart;
import org.moxieapps.gwt.highcharts.client.Color;
import org.moxieapps.gwt.highcharts.client.Series;

/**
 * Represents the general plot options available for all bar type series, which can be set either generically
 * on the chart via the {@link Chart#setBarPlotOptions(BarPlotOptions)} )} method or directly on a
 * series via the {@link Series#setPlotOptions(PlotOptions)} method.
 * <p/>
 * Note that these options are only needed if you want to specifically control the general options
 * for all bar type series in the entire chart.  If you instead want to control the options for all
 * series in the chart (not just those of a bar type), then you can use the {@link SeriesPlotOptions}
 * class instead.  Or, if you want to control the plot options for just one series (and not all bar type
 * series in the chart), use the {@link org.moxieapps.gwt.highcharts.client.Series#setPlotOptions(PlotOptions)} method.
 *
 * @author squinn@moxiegroup.com (Shawn Quinn)
 * @since 1.0.0
 */
public class BarPlotOptions extends PlotOptions<BarPlotOptions> {

    /**
     * Convenience method for setting the 'borderColor' option of the bar plot options
     * to a RGB hex value.  Equivalent to:
     * <pre><code>
     *     barPlotOptions.setOption("borderColor", "#CCCCCC");
     * </code></pre>
     * The color of the border surrounding each column or bar. Defaults to "#FFFFFF".
     * <p/>
     * Note that this method is intended for setting the color to a simple RBG hex value.  If you instead
     * want to set a color to include an alpha channel or a gradient, use the {@link #setBorderColor(org.moxieapps.gwt.highcharts.client.Color)}
     * version instead.
     *
     * @param borderColor The value to set as the 'borderColor' option of the bar plots.
     * @return A reference to this {@link BarPlotOptions} instance for convenient method chaining.
     * @since 1.1.3
     */
    public BarPlotOptions setBorderColor(String borderColor) {
        return this.setOption("borderColor", borderColor);
    }

    /**
     * Convenience method for setting the 'borderColor' option of the bar plot options, allowing for
     * colors with opacity or gradients.  Equivalent to:
     * <pre><code>
     *     barPlotOptions.setOption("borderColor", new Color()
     *        .setLinearGradient(0.0, 0.0, 1.0, 1.0)
     *        .addStop(new Color(255, 255, 255))
     *        .addStop(new Color(200, 200, 255))
     *     );
     * </code></pre>
     * The color of the border surrounding each column or bar. Defaults to "#FFFFFF".
     * <p/>
     * Note that this method is intended for setting the color to a gradient or color that includes
     * an alpha channel.  If you instead just want to set the color to a normal RGB hex value
     * you can use the {@link #setBorderColor(String)} version instead.
     *
     * @param borderColor The color gradient or color with an alpha channel to set as the 'borderColor' option of the area plot.
     * @return A reference to this {@link BarPlotOptions} instance for convenient method chaining.
     * @since 1.1.3
     */
    public BarPlotOptions setBorderColor(Color borderColor) {
        return this.setOption("borderColor", borderColor != null ? borderColor.getOptionValue() : null);
    }

    /**
     * Convenience method for setting the 'borderRadius' option of the bar plot options.  Equivalent to:
     * <pre><code>
     *     barPlotOptions.setOption("borderRadius", 4);
     * </code></pre>
     * The corner radius of the border surrounding each column or bar. Defaults to 0.
     *
     * @param borderRadius The corner radius of the border surrounding each column or bar.
     * @return A reference to this {@link BarPlotOptions} instance for convenient method chaining.
     * @since 1.1.3
     */
    public BarPlotOptions setBorderRadius(Number borderRadius) {
        return this.setOption("borderRadius", borderRadius);
    }

    /**
     * Convenience method for setting the 'borderWidth' option of the bar plot options.  Equivalent to:
     * <pre><code>
     *     barPlotOptions.setOption("borderWidth", 4);
     * </code></pre>
     * The width of the border surrounding each column or bar. Defaults to 1.
     *
     * @param borderWidth The width of the border surrounding each column or bar.
     * @return A reference to this {@link BarPlotOptions} instance for convenient method chaining.
     * @since 1.1.3
     */
    public BarPlotOptions setBorderWidth(Number borderWidth) {
        return this.setOption("borderWidth", borderWidth);
    }

    /**
     * Convenience method for setting the 'colorByPoint' option of the bar plot options.  Equivalent to:
     * <pre><code>
     *     barPlotOptions.setOption("colorByPoint", true);
     * </code></pre>
     * When using automatic point colors pulled from the options.colors collection, this option determines
     * whether the chart should receive one color per series or one color per point. Defaults to false.
     *
     * TODO: Fix javadoc reference to "options.colors".
     *
     * @param colorByPoint Whether the chart should receive one color per series or one color per point.
     * @return A reference to this {@link BarPlotOptions} instance for convenient method chaining.
     * @since 1.1.3
     */
    public BarPlotOptions setColorByPoint(boolean colorByPoint) {
        return this.setOption("colorByPoint", colorByPoint);
    }

    /**
     * Convenience method for setting the 'groupPadding' option of the bar plot options.  Equivalent to:
     * <pre><code>
     *     barPlotOptions.setOption("groupPadding", 4);
     * </code></pre>
     * Padding between each value groups, in x axis units. Defaults to 0.2.
     *
     * @param groupPadding Padding between each value groups, in x axis units.
     * @return A reference to this {@link BarPlotOptions} instance for convenient method chaining.
     * @since 1.1.3
     */
    public BarPlotOptions setGroupPadding(Number groupPadding) {
        return this.setOption("groupPadding", groupPadding);
    }

    /**
     * Convenience method for setting the 'minPointLength' option of the bar plot options.  Equivalent to:
     * <pre><code>
     *     barPlotOptions.setOption("minPointLength", 4);
     * </code></pre>
     * The minimal height for a column or width for a bar. By default, 0 values are not shown. To visualize
     * a 0 (or close to zero) point, set the minimal point length to a pixel value like 3. In stacked column
     * charts, minPointLength might not be respected for tightly packed values. Defaults to 0.
     *
     * @param minPointLength The minimal height for a column or width for a bar.
     * @return A reference to this {@link BarPlotOptions} instance for convenient method chaining.
     * @since 1.1.3
     */
    public BarPlotOptions setMinPointLength(Number minPointLength) {
        return this.setOption("minPointLength", minPointLength);
    }

    /**
     * Convenience method for setting the 'pointPadding' option of the bar plot options.  Equivalent to:
     * <pre><code>
     *     barPlotOptions.setOption("pointPadding", 0.5);
     * </code></pre>
     * Padding between each column or bar, in x axis units. Defaults to 0.1.
     *
     * @param pointPadding Padding between each column or bar, in x axis units.
     * @return A reference to this {@link BarPlotOptions} instance for convenient method chaining.
     * @since 1.1.3
     */
    public BarPlotOptions setPointPadding(Number pointPadding) {
        return this.setOption("pointPadding", pointPadding);
    }

    /**
     * Convenience method for setting the 'pointWidth' option of the bar plot options.  Equivalent to:
     * <pre><code>
     *     barPlotOptions.setOption("pointWidth", 2.0);
     * </code></pre>
     * A pixel value specifying a fixed width for each column or bar. When null, the width is calculated
     * from the {@link #setPointPadding(Number)} and {@link #setGroupPadding(Number)}. Defaults to null.
     *
     * @param pointWidth A pixel value specifying a fixed width for each column or bar.
     * @return A reference to this {@link BarPlotOptions} instance for convenient method chaining.
     * @since 1.1.3
     */
    public BarPlotOptions setPointWidth(Number pointWidth) {
        return this.setOption("pointWidth", pointWidth);
    }

}
