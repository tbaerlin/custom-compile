package de.marketmaker.itools.gwtutil.client.widgets.floating;

import com.google.gwt.user.client.Timer;

/**
 * @author Ulrich Maurer
 *         Date: 22.10.12
 */
public class FloatingAnimation {
    private static final int FRAME_DELAY = 50;
    private static final double FRAME_UPDATE = 10d;
    private static final int STEP_SIZE = 40;
    private final FloatingWidget floatingWidget;
    private int frameDelay;
    private double frameUpdate;
    private int stepSize;
    private long startTime;
    private int startPosition;
    private double updateValue = 0d;

    public FloatingAnimation(FloatingWidget floatingWidget, int frameDelay, double frameUpdate, int stepSize) {
        this.floatingWidget = floatingWidget;
        this.frameDelay = frameDelay;
        this.frameUpdate = frameUpdate;
        this.stepSize = stepSize;
    }

    public FloatingAnimation(FloatingWidget floatingWidget) {
        this(floatingWidget, FRAME_DELAY, FRAME_UPDATE, STEP_SIZE);
    }

    public FloatingAnimation withFrameDelay(int frameDelay) {
        this.frameDelay = frameDelay;
        return this;
    }

    public FloatingAnimation withFrameUpdate(double frameUpdate) {
        this.frameUpdate = frameUpdate;
        return this;
    }

    public FloatingAnimation withStepSize(int stepSize) {
        this.stepSize = stepSize;
        return this;
    }

    private final Timer timer = new Timer(){
        @Override
        public void run() {
            updateAnimation();
        }
    };

    public void startBack() {
        start(this.frameUpdate);
    }

    public void startForward() {
        start(-this.frameUpdate);
    }

    public void stepBack() {
        this.floatingWidget.setPosition(this.floatingWidget.getPosition() + this.stepSize, true);
        this.floatingWidget.updateButtonVisibility();
    }

    public void stepForward() {
        this.floatingWidget.setPosition(this.floatingWidget.getPosition() - this.stepSize, true);
        this.floatingWidget.updateButtonVisibility();
    }

    private void start(double updateValue) {
        this.startTime = System.currentTimeMillis();
        this.startPosition = this.floatingWidget.getPosition();
        this.updateValue = updateValue;
        this.timer.schedule(frameDelay);
    }

    public void stop() {
        this.updateValue = 0d;
        this.floatingWidget.updateButtonVisibility();
    }

    private void updateAnimation() {
        if (this.updateValue == 0d) {
            return;
        }
        final double delay = System.currentTimeMillis() - this.startTime;
        final double delta = delay / frameDelay * this.updateValue;
        final int position = this.startPosition + (int) delta;
        final int newPosition = this.floatingWidget.setPosition(position, true);
        if (newPosition == position) {
            this.timer.schedule(frameDelay);
        }
        else {
            this.updateValue = 0d;
            this.floatingWidget.updateButtonVisibility();
        }
    }
}
