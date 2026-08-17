/**
 * Copyright (C) 2026 Michael Schnell. All rights reserved.
 * http://www.fuin.org/
 * <p>
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option) any
 * later version.
 * <p>
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * <p>
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library. If not, see http://www.gnu.org/licenses/.
 */
package org.fuin.fx.navidraw;

import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import org.jspecify.annotations.Nullable;

/**
 * A box of rows that folds away, used by a {@link NavigationGroup} for its children and by a
 * {@link NavigationSection} for its items.
 * <p>
 * The box is clipped to its own bounds, so while the height shrinks the rows slide out of view instead of
 * spilling over whatever sits below. When closed it is also unmanaged, so it takes no space at all rather
 * than a zero-height slot the parent still has to lay out.
 */
final class CollapsibleBox extends VBox {

    private @Nullable Timeline transition;

    private boolean open;

    CollapsibleBox(final String styleClass) {
        super();
        getStyleClass().setAll(styleClass);
        setFillWidth(true);
        setMinHeight(0);
        final Rectangle clip = new Rectangle();
        clip.widthProperty().bind(widthProperty());
        clip.heightProperty().bind(heightProperty());
        setClip(clip);
        applyClosed();
    }

    /**
     * Returns whether the box is showing its rows.
     *
     * @return {@code true} if open.
     */
    boolean isOpen() {
        return open;
    }

    /**
     * Opens or closes the box.
     *
     * @param value    {@code true} to show the rows.
     * @param animate  {@code true} to move over time. The caller decides this - there has to be a scene to
     *                 drive the timeline, and the drawer may have animations switched off altogether.
     * @param duration How long the movement takes when animated.
     */
    void setOpen(final boolean value, final boolean animate, final Duration duration) {
        stopTransition();
        final boolean wasShowing = isManaged();
        open = value;
        if (!value) {
            if (animate && wasShowing) {
                animateTo(0, duration, this::applyClosed);
            } else {
                applyClosed();
            }
            return;
        }
        setVisible(true);
        setManaged(true);
        if (animate) {
            animateTo(contentHeight(), duration, () -> setPrefHeight(USE_COMPUTED_SIZE));
        } else {
            setPrefHeight(USE_COMPUTED_SIZE);
        }
    }

    /**
     * Stops a running transition. Called before the box is thrown away.
     */
    void dispose() {
        stopTransition();
    }

    private void applyClosed() {
        setPrefHeight(0);
        setVisible(false);
        setManaged(false);
    }

    private void animateTo(final double target, final Duration duration, final Runnable onFinished) {
        setPrefHeight(getHeight());
        final Timeline timeline = new Timeline(new KeyFrame(duration,
                new KeyValue(prefHeightProperty(), target, Interpolator.EASE_BOTH)));
        timeline.setOnFinished(event -> {
            transition = null;
            onFinished.run();
        });
        transition = timeline;
        timeline.play();
    }

    /**
     * The height the rows want. The box cannot simply be asked for it, because the animation drives its
     * preferred height and it would answer with whatever the animation last set.
     *
     * @return Height in pixels.
     */
    private double contentHeight() {
        final Insets insets = getInsets();
        double height = insets.getTop() + insets.getBottom();
        int count = 0;
        for (final Node node : getChildren()) {
            if (node.isManaged()) {
                height = height + node.prefHeight(getWidth());
                count++;
            }
        }
        if (count > 1) {
            height = height + getSpacing() * (count - 1);
        }
        return height;
    }

    private void stopTransition() {
        final Timeline running = transition;
        if (running != null) {
            running.stop();
            transition = null;
        }
    }

}
