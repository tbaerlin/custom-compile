package de.marketmaker.iview.mmgwt.mmweb.client.util;

import com.google.gwt.animation.client.Animation;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author umaurer
 */
public class AnimationFactory {
    private static final String SPEC_PATTERN = "(\\w+)\\((\\d+)\\)"; // $NON-NLS$
    private static String transitionStyleName = null;

    public interface AnimationControl {
        void beforeStart();

        void run();

        Animation asAnimation();
    }

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

    private static String getTransitionStyleName() {
        if (transitionStyleName == null) {
            transitionStyleName = _getTransitionStyle(new Label().getElement());
        }
        return transitionStyleName;
    }

    public static AnimationControl createAnimation(String spec, Widget widget) {
        final WidgetAnimation animation;

        if (!spec.matches(SPEC_PATTERN)) {
            throw new IllegalArgumentException("invalid animation spec (no match): " + spec); // $NON-NLS$
        }
        final String type = spec.substring(0, spec.indexOf('('));
        final int duration = Integer.parseInt(spec.substring(spec.indexOf('(') + 1, spec.lastIndexOf(')')));
        if ("wipeBottomToTop".equals(type)) { // $NON-NLS$
            animation = new WipeBottomToTop();
        }
        else if ("wipeTopToBottom".equals(type)) { // $NON-NLS$
            animation = new WipeTopToBottom();
        }
        else if ("wipeLeftToRight".equals(type)) { // $NON-NLS$
            animation = new WipeLeftToRight();
        }
        else if ("wipeRightToLeft".equals(type)) { // $NON-NLS$
            animation = new WipeRightToLeft();
        }
        else if ("fadeIn".equals(type)) { // $NON-NLS$
            animation = new FadeIn();
        }
        else if ("fadeOut".equals(type)) { // $NON-NLS$
            animation = new FadeOut();
        }
        else if ("revealBottomToTop".equals(type)) { // $NON-NLS$
            animation = new RevealBottomToTop();
        }
        else if ("revealTopToBottom".equals(type)) { // $NON-NLS$
            animation = new RevealTopToBottom();
        }
        else if ("revealRightToLeft".equals(type)) { // $NON-NLS$
            animation = new RevealRightToLeft();
        }
        else if ("revealLeftToRight".equals(type)) { // $NON-NLS$
            animation = new RevealLeftToRight();
        }
        else
            throw new IllegalArgumentException("invalid animation spec (unknown type): " + spec); // $NON-NLS$

        animation.widget = widget;
        animation.width = widget.getElement().getClientWidth();
        animation.height = widget.getElement().getClientHeight();
        animation.duration = duration;
        return animation;
    }

    private static abstract class WidgetAnimation extends Animation implements AnimationControl {
        Widget widget;
        int width;
        int height;
        int duration = 0;

        public void beforeStart() {
            clearTransition();
            onUpdate(0d);
        }

        public Animation asAnimation() {
            return this;
        }

        public void setTransition() {
            _setTransition(this.widget, this.duration);
        }

        public void clearTransition() {
            _setTransition(this.widget, 0);
        }

        private void _setTransition(Widget widget, int millis) {
            final String transitionStyleName = getTransitionStyleName();
            if (transitionStyleName == null) {
                return;
            }

            if (millis == 0) {
                widget.getElement().getStyle().clearProperty(transitionStyleName);
            }
            else {
                widget.getElement().getStyle().setProperty(transitionStyleName, "all " + millis + "ms ease-in-out"); // $NON-NLS$
            }
        }

        public void run() {
            if (getTransitionStyleName() == null) {
                run(this.duration);
            }
            else {
                setTransition();
                onUpdate(1.0);
            }
        }
    }

    private static abstract class RevealAnimation extends WidgetAnimation {
        protected final Panel cover = new SimplePanel();

        @Override
        public void beforeStart() {
            super.beforeStart();
            int top = this.widget.getAbsoluteTop();
            int left = this.widget.getAbsoluteLeft();

            Element coverElement = this.cover.getElement();
            this.cover.setStyleName("as-revealAnimationCover");
            Style coverStyle = coverElement.getStyle();

            coverStyle.setPropertyPx("width", this.width); // $NON-NLS$
            coverStyle.setPropertyPx("height", this.height); // $NON-NLS$
            top -= Document.get().getBodyOffsetTop();
            left -= Document.get().getBodyOffsetLeft();
            coverStyle.setPropertyPx("top", top); // $NON-NLS$
            coverStyle.setPropertyPx("left", left); // $NON-NLS$

            RootPanel.get().add(this.cover);
        }

        @Override
        protected void onComplete() {
            this.cover.getElement().removeFromParent();
        }
    }


    static class WipeBottomToTop extends WidgetAnimation {
        @Override
        public void beforeStart() {
            super.beforeStart();
            widget.getElement().getStyle().setPosition(Style.Position.ABSOLUTE);
        }

        @Override
        protected void onUpdate(double progress) {
            widget.getElement().getStyle().setTop(height * (1d - progress), Style.Unit.PX);
        }
    }

    static class WipeTopToBottom extends WidgetAnimation {
        @Override
        public void beforeStart() {
            super.beforeStart();
            widget.getElement().getStyle().setPosition(Style.Position.ABSOLUTE);
        }

        @Override
        protected void onUpdate(double progress) {
            widget.getElement().getStyle().setTop(height * (progress - 1d), Style.Unit.PX);
        }
    }

    static class WipeLeftToRight extends WidgetAnimation {
        @Override
        public void beforeStart() {
            super.beforeStart();
            widget.getElement().getStyle().setPosition(Style.Position.ABSOLUTE);
        }

        @Override
        protected void onUpdate(double progress) {
            widget.getElement().getStyle().setLeft(width * (progress - 1d), Style.Unit.PX);
        }
    }

    static class WipeRightToLeft extends WidgetAnimation {
        @Override
        public void beforeStart() {
            super.beforeStart();
            widget.getElement().getStyle().setPosition(Style.Position.ABSOLUTE);
        }

        @Override
        protected void onUpdate(double progress) {
            widget.getElement().getStyle().setLeft(width * (1d - progress), Style.Unit.PX);
        }
    }

    static class FadeIn extends WidgetAnimation {
        @Override
        protected void onUpdate(double progress) {
            widget.getElement().getStyle().setOpacity(progress);
        }
    }

    static class FadeOut extends WidgetAnimation {
        @Override
        protected void onUpdate(double progress) {
            widget.getElement().getStyle().setOpacity(1d - progress);
        }
    }

    static class RevealBottomToTop extends RevealAnimation {
        @Override
        protected void onUpdate(double progress) {
            cover.getElement().getStyle().setHeight(height * (1d - progress), Style.Unit.PX);
        }
    }

    static class RevealTopToBottom extends RevealAnimation {
        int top = 0;

        @Override
        public void beforeStart() {
            super.beforeStart();
            top = widget.getAbsoluteTop();
        }

        @Override
        protected void onUpdate(double progress) {
            double position = progress * height;
            cover.getElement().getStyle().setTop(top + position, Style.Unit.PX);
            cover.getElement().getStyle().setHeight(height - position, Style.Unit.PX);
        }
    }

    static class RevealLeftToRight extends RevealAnimation {
        int left = 0;

        @Override
        public void beforeStart() {
            super.beforeStart();
            left = widget.getAbsoluteLeft();
        }

        @Override
        protected void onUpdate(double progress) {
            double position = progress * width;
            cover.getElement().getStyle().setLeft(left + position, Style.Unit.PX);
            cover.getElement().getStyle().setWidth(width - position, Style.Unit.PX);
        }
    }

    static class RevealRightToLeft extends RevealAnimation {
        @Override
        protected void onUpdate(double progress) {
            cover.getElement().getStyle().setWidth(width * (1d - progress), Style.Unit.PX);
        }
    }
}
