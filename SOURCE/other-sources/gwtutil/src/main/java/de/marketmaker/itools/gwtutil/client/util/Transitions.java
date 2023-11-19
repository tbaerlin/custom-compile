package de.marketmaker.itools.gwtutil.client.util;


import com.google.gwt.animation.client.Animation;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.logical.shared.AttachEvent;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.Widget;

import static com.google.gwt.dom.client.Style.Overflow.HIDDEN;
import static com.google.gwt.dom.client.Style.Unit.PX;

/**
 * Author: umaurer
 * Created: 10.02.14
 */
public class Transitions {
    public static final String TRANSITION_STYLE_NAME = _getTransitionStyle(new Label().getElement());
    public static final String ANIMATION_END_EVENT_NAME = _getAnimationEndEventName(new Label().getElement());

    private static native String _getTransitionStyle(Element element) /*-{
        var prefixes = ['Moz', 'Webkit', 'Khtml', 'O', 'Ms'];
        var style = element.style;
        var propName = "transition"; // $NON-NLS$
        var prefixed = null;

        if (typeof style[propName] == 'string') return propName;
        propName = "Transition"; // $NON-NLS$
        for (var i=0, l=prefixes.length; i<l; i++) {
            prefixed = prefixes[i] + propName;
            if (typeof style[prefixed] == 'string') return prefixed;
        }
        return null;
    }-*/;

    private static native String _getAnimationEndEventName(Element elt) /*-{
        if ("animationName" in elt.style) {
            return "animationend";
        }
        else if ("webkitAnimationName" in elt.style) {
            return "webkitAnimationEnd";
        }
        return "animationend";
    }-*/;

    public static void setTransitionStyle(final Widget widget, final String property, final int durationMillis, final String timingFunction, final int delayMillis) {
        setTransitionStyle(widget.getElement(), property, durationMillis, timingFunction, delayMillis);
    }

    public static void setTransitionStyle(final Element element, final String property, final int durationMillis, final String timingFunction, final int delayMillis) {
        if (TRANSITION_STYLE_NAME == null) {
            return;
        }
        if (property == null) {
            element.getStyle().clearProperty(TRANSITION_STYLE_NAME);
            return;
        }
        final StringBuilder sb = new StringBuilder();
        sb.append(property).append(' ').append(durationMillis).append("ms ").append(timingFunction);
        if (delayMillis > 0) {
            sb.append(' ').append(delayMillis).append("ms");
        }
        element.getStyle().setProperty(TRANSITION_STYLE_NAME, sb.toString());
        element.getClientHeight(); // trigger recalculation of transition
    }

    public static void fadeInAfterAttach(Widget widget, int durationMillis) {
        fadeInAfterAttach(widget, durationMillis, "ease", 0); // $NON-NLS$
    }

    public static void fadeInAfterAttach(final Widget widget, final int durationMillis, final String timingFunction, final int delayMillis) {
        final Element element = widget.getElement();
        final Style style = element.getStyle();
        style.setOpacity(0);
        if (TRANSITION_STYLE_NAME == null) {
            // JavaScript transition
            widget.addAttachHandler(new AttachEvent.Handler() {
                @Override
                public void onAttachOrDetach(final AttachEvent event) {
                    if (event.isAttached()) {
                        final Animation animation = new Animation() {
                            @Override
                            protected void onUpdate(double progress) {
                                style.setOpacity(progress);
                            }
                        };
                        animation.run(durationMillis, System.currentTimeMillis() + delayMillis);
                    }
                    else {
                        style.setOpacity(0);
                    }
                }
            });
        }
        else {
            // CSS3 transition
            style.setProperty(TRANSITION_STYLE_NAME, "opacity " + durationMillis + "ms " + timingFunction + " " + delayMillis + "ms"); // $NON-NLS$
            widget.addAttachHandler(new AttachEvent.Handler() {
                @Override
                public void onAttachOrDetach(AttachEvent event) {
                    element.getClientHeight(); // hack to trigger transition
                    style.setOpacity(event.isAttached() ? 1 : 0);
                }
            });
        }
    }

    public interface FadingTransition {
        public void fadeIn();
        public void fadeOut();
    }

    public static class CssFadingTransition implements FadingTransition {
        private final Style style;

        public CssFadingTransition(Style style, int durationMillis) {
            this.style = style;
            this.style.setProperty(TRANSITION_STYLE_NAME, "opacity " + durationMillis + "ms ease");
        }

        @Override
        public void fadeIn() {
            this.style.setOpacity(1);
        }

        @Override
        public void fadeOut() {
            this.style.setOpacity(0);
        }
    }

    public static class AnimatedFadingTransition implements FadingTransition {
        private final int durationMillis;
        private final Animation animation;
        private boolean fadeIn = true;

        public AnimatedFadingTransition(final Style style, int durationMillis) {
            this.durationMillis = durationMillis;
            this.animation = new Animation() {
                @Override
                protected void onUpdate(double progress) {
                    style.setOpacity(fadeIn ? progress : (1 - progress));
                }
            };
        }

        @Override
        public void fadeIn() {
            if (this.fadeIn) {
                return;
            }
            this.fadeIn = true;
            this.animation.run(this.durationMillis);
        }

        @Override
        public void fadeOut() {
            if (!this.fadeIn) {
                return;
            }
            this.fadeIn = false;
            this.animation.run(this.durationMillis);
        }
    }

    public static FadingTransition createFadingTransition(Widget widget, int durationMillis) {
        return TRANSITION_STYLE_NAME == null
                ? new AnimatedFadingTransition(widget.getElement().getStyle(), durationMillis)
                : new CssFadingTransition(widget.getElement().getStyle(), durationMillis);
    }


    public static void revealDownAfterAttach(final Widget widget, final int durationMillis) {
        revealDownAfterAttach(widget, durationMillis, "ease", 0);
    }

    public static void revealDownAfterAttach(final Widget widget, final int durationMillis, final String timingFunction, final int delayMillis) {
        final Element element = widget.getElement();
        final Style style = element.getStyle();
        if (TRANSITION_STYLE_NAME == null) {
            // JavaScript transition
            widget.addAttachHandler(new AttachEvent.Handler() {
                @Override
                public void onAttachOrDetach(final AttachEvent event) {
                    if (event.isAttached()) {
                        final int maxHeight = element.getScrollHeight();

                        final Animation animation = new Animation() {
                            @Override
                            protected void onUpdate(double progress) {
                                final double height = progress * maxHeight;
                                style.setHeight(height < 1 ? 1 : height, PX);
                            }

                            @Override
                            protected void onStart() {
                                style.setOverflow(HIDDEN);
                            }

                            @Override
                            protected void onCancel() {
                                style.clearHeight();
                                style.clearOverflow();
                            }

                            @Override
                            protected void onComplete() {
                                style.clearHeight();
                                style.clearOverflow();
                            }
                        };
                        animation.run(durationMillis, System.currentTimeMillis() + delayMillis);
                    }

                    else {
                        style.setHeight(0, PX);
                    }
                }
            });
        }
        else {
            // CSS3 transition
            style.setOverflow(HIDDEN);
            style.setHeight(0, PX);
            style.setProperty(TRANSITION_STYLE_NAME, "height " + durationMillis + "ms " + timingFunction + " " + delayMillis + "ms"); // $NON-NLS$
            widget.addAttachHandler(new AttachEvent.Handler() {
                @Override
                public void onAttachOrDetach(AttachEvent event) {
                    if (event.isAttached()) {
                        final int maxHeight = element.getScrollHeight();
                        element.getClientHeight(); // hack to trigger transition
                        style.setHeight(maxHeight, PX);
                        Scheduler.get().scheduleFixedPeriod(new Scheduler.RepeatingCommand() {
                            @Override
                            public boolean execute() { // reset transition styles
                                style.clearProperty(TRANSITION_STYLE_NAME);
                                style.clearOverflow();
                                style.clearHeight();
                                return false;
                            }
                        }, durationMillis + delayMillis);
                    }
                }
            });
        }
    }

    public interface TransitionEndCallback {
        void onTransitionEnd();
    }

    public static void hideUp(final Widget widget, final int durationMillis, final TransitionEndCallback teCallback) {
        hideUp(widget, durationMillis, "ease", 0, teCallback);
    }

    public static void hideUp(final Widget widget, final int durationMillis, final String timingFunction, final int delayMillis, final TransitionEndCallback teCallback) {
        final Element element = widget.getElement();
        final Style style = element.getStyle();
        final int maxHeight = element.getScrollHeight();
        style.setHeight(maxHeight, PX);
        style.setOverflow(HIDDEN);
        if (TRANSITION_STYLE_NAME == null) {
            // JavaScript transition
            final Animation animation = new Animation() {
                @Override
                protected void onUpdate(double progress) {
                    final double height = (1d - progress) * maxHeight;
                    style.setHeight(height < 1 ? 1 : height, PX);
                }

                @Override
                protected void onCancel() {
                    teCallback.onTransitionEnd();
                    style.clearHeight();
                    style.clearOverflow();
                }

                @Override
                protected void onComplete() {
                    teCallback.onTransitionEnd();
                    style.clearHeight();
                    style.clearOverflow();
                }
            };
            animation.run(durationMillis, System.currentTimeMillis() + delayMillis);
        }
        else {
            // CSS3 transition
            style.setProperty(TRANSITION_STYLE_NAME, "height " + durationMillis + "ms " + timingFunction + " " + delayMillis + "ms"); // $NON-NLS$
            element.getClientHeight();
            style.setHeight(0, PX);
            Scheduler.get().scheduleFixedPeriod(new Scheduler.RepeatingCommand() {
                @Override
                public boolean execute() { // reset transition styles
                    style.clearProperty(TRANSITION_STYLE_NAME);
                    style.clearOverflow();
                    style.clearHeight();
                    teCallback.onTransitionEnd();
                    return false;
                }
            }, durationMillis + delayMillis);
        }
    }

    public static void flyToPosition(final Widget widget, final int left, final int top, final int durationMillis, final String timingFunction, final int delayMillis) {
        final Element element = widget.getElement();
        final Style style = element.getStyle();
        if (TRANSITION_STYLE_NAME == null) {
            // JavaScript transition
            final int startLeft = widget.getAbsoluteLeft();
            final int startTop = widget.getAbsoluteTop();
            final Animation animation = new Animation() {
                @Override
                protected void onUpdate(double progress) {
                    final double progressX = startLeft + progress * (left - startLeft);
                    final double progressY = startTop + progress * (top - startTop);
                    setPosition(widget, progressX, progressY);
                }
            };
            animation.run(durationMillis, System.currentTimeMillis() + delayMillis);
        }
        else {
            // CSS3 transition
            style.setProperty(TRANSITION_STYLE_NAME, "left " + durationMillis + "ms " + timingFunction + " " + delayMillis + "ms, top " + durationMillis + "ms " + timingFunction + " " + delayMillis + "ms"); // $NON-NLS$
            element.getClientHeight();
            setPosition(widget, left, top);
        }
    }

    private static void setPosition(Widget widget, double left, double top) {
        if (widget instanceof PopupPanel) {
            ((PopupPanel) widget).setPopupPosition((int)left, (int)top);
        }
        else {
            final Style style = widget.getElement().getStyle();
            style.setLeft(left, PX);
            style.setTop(top, PX);
        }
    }


}
