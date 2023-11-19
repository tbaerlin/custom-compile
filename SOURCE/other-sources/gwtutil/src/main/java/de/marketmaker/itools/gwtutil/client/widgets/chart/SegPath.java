package de.marketmaker.itools.gwtutil.client.widgets.chart;

/**
 * @author Ulrich Maurer
 *         Date: 29.01.2016
 */
public class SegPath {
    final StringBuilder sb = new StringBuilder();

    // internal -----------------------------------------------------------------------------------------

    private void append(char letter, float v) {
        this.sb.append(letter).append(v).append(' ');
    }

    private void append(char letter, float x, float y) {
        this.sb.append(letter);
        append(x, y);
    }

    private void append(float x, float y) {
        this.sb.append(x).append(',').append(y).append(' ');
    }

    private void append(float value) {
        this.sb.append(value).append(' ');
    }

    private void append(boolean flag) {
        this.sb.append(flag ? '1' : '0').append(' ');
    }

    private void append(char letter) {
        this.sb.append(letter).append(' ');
    }

    // external -----------------------------------------------------------------------------------------



    public void lineToHorizontalRel(float x) {
        append('h', x);
    }

    public void moveToAbs(Point p) {
        append('M', p.x, p.y);
    }

    public void moveToAbs(float x, float y) {
        append('M', x, y);
    }

    public void moveToRel(float x, float y) {
        append('m', x, y);
    }

    public void lineToAbs(Point p) {
        append('L', p.x, p.y);
    }

    public void lineToAbs(float x, float y) {
        append('L', x, y);
    }

    public void lineToRel(float x, float y) {
        append('l', x, y);
    }

    public void curveToCubicRel(float x, float y, float x1, float y1, float x2, float y2) {
        append('c', x1, y1);
        append(x2, y2);
        append(x, y);
    }

    public void arc(char letter, float x, float y, float radiusX, float radiusY, float angle, boolean largeArcFlag, boolean sweepFlag) {
        append(letter, radiusX, radiusY);
        append(angle);
        append(largeArcFlag);
        append(sweepFlag);
        append(x, y);
    }

    public void arcAbs(float x, float y, float radiusX, float radiusY, float angle, boolean largeArcFlag, boolean sweepFlag) {
        arc('A', x, y, radiusX, radiusY, angle, largeArcFlag, sweepFlag);
    }

    public void arcAbs(Point p, float radiusX, float radiusY, float angle, boolean largeArcFlag, boolean sweepFlag) {
        arc('A', p.x, p.y, radiusX, radiusY, angle, largeArcFlag, sweepFlag);
    }

    public void arcRel(float x, float y, float radiusX, float radiusY, float angle, boolean largeArcFlag, boolean sweepFlag) {
        arc('a', x, y, radiusX, radiusY, angle, largeArcFlag, sweepFlag);
    }

    public void closePath() {
        append('z');
    }

    @Override
    public String toString() {
        return sb.toString();
    }
}
