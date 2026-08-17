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
import javafx.beans.InvalidationListener;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import org.jspecify.annotations.Nullable;

/**
 * The button that opens, closes, expands and collapses a {@link NavigationDrawerPane}'s drawer, whichever of
 * those the current arrangement calls for.
 * <p>
 * In the modal arrangement it shows or hides the drawer, in the persistent one it switches the drawer
 * between the full and the mini size. The icon cross fades between three bars and a chevron so it always
 * shows what the next click will do.
 */
public class DrawerToggleButton extends Button {

    private static final String DEFAULT_STYLE_CLASS = "drawer-toggle";

    private static final Duration FALLBACK_DURATION = Duration.millis(200);

    private final VBox bars = new VBox();

    private final Region chevron = new Region();

    private final ObjectProperty<@Nullable NavigationDrawerPane> pane =
            new SimpleObjectProperty<>(this, "pane", null);

    private final InvalidationListener stateListener = observable -> updateVisualState(true);

    private final InvalidationListener drawerListener = observable -> onDrawerChanged();

    private @Nullable NavigationDrawerPane attachedPane;

    private @Nullable NavigationDrawer attachedDrawer;

    private @Nullable Timeline transition;

    private boolean chevronShown = true;

    /**
     * Default constructor. Set {@link #paneProperty()} to make the button do something.
     */
    public DrawerToggleButton() {
        super();
        getStyleClass().setAll(DEFAULT_STYLE_CLASS);
        setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        setAccessibleText("Toggle navigation");

        bars.getStyleClass().setAll("bars");
        bars.setAlignment(Pos.CENTER);
        bars.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        for (int i = 0; i < 3; i++) {
            final Region bar = new Region();
            bar.getStyleClass().setAll("bar");
            bars.getChildren().add(bar);
        }

        chevron.getStyleClass().setAll("chevron");
        chevron.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);

        final StackPane graphicPane = new StackPane(bars, chevron);
        graphicPane.getStyleClass().setAll("toggle-graphic");
        setGraphic(graphicPane);

        pane.addListener(observable -> onPaneChanged());
        setOnAction(event -> toggle());

        updateVisualState(false);
    }

    /**
     * Constructor with the pane to control.
     *
     * @param pane Pane, never {@code null}.
     */
    public DrawerToggleButton(final NavigationDrawerPane pane) {
        this();
        setPane(pane);
    }

    /**
     * The pane this button controls.
     *
     * @return Property, defaults to {@code null}.
     */
    public final ObjectProperty<@Nullable NavigationDrawerPane> paneProperty() {
        return pane;
    }

    /**
     * Returns the controlled pane.
     *
     * @return Pane or {@code null}.
     */
    public final @Nullable NavigationDrawerPane getPane() {
        return pane.get();
    }

    /**
     * Sets the controlled pane.
     *
     * @param value Pane or {@code null} to make the button inert.
     */
    public final void setPane(final @Nullable NavigationDrawerPane value) {
        pane.set(value);
    }

    /**
     * The button is normally placed in the application's own tool bar, outside the drawer and often outside
     * the pane, and a user agent stylesheet only reaches the node it is declared on and its children. Saying
     * it here is what makes the icon appear no matter where the button ends up.
     *
     * @return The default stylesheet, never {@code null}.
     */
    @Override
    public String getUserAgentStylesheet() {
        return NavigationDrawer.defaultStylesheet();
    }

    /**
     * Does what a click does: shows or hides a modal drawer, expands or collapses a persistent one.
     */
    public final void toggle() {
        final NavigationDrawerPane current = getPane();
        if (current == null) {
            return;
        }
        if (current.getEffectiveDisplayMode() == DrawerDisplayMode.MODAL) {
            current.toggle();
        } else {
            final NavigationDrawer currentDrawer = current.getDrawer();
            if (currentDrawer != null) {
                currentDrawer.toggleSize();
            }
        }
    }

    /**
     * Returns whether the chevron rather than the bars is shown, for tests.
     *
     * @return {@code true} if the chevron is shown.
     */
    final boolean isChevronShown() {
        return chevronShown;
    }

    private void onPaneChanged() {
        final NavigationDrawerPane previous = attachedPane;
        if (previous != null) {
            previous.effectiveDisplayModeProperty().removeListener(stateListener);
            previous.drawerOpenProperty().removeListener(stateListener);
            previous.drawerProperty().removeListener(drawerListener);
        }
        attachedPane = getPane();
        final NavigationDrawerPane current = attachedPane;
        if (current != null) {
            current.effectiveDisplayModeProperty().addListener(stateListener);
            current.drawerOpenProperty().addListener(stateListener);
            current.drawerProperty().addListener(drawerListener);
        }
        onDrawerChanged();
    }

    private void onDrawerChanged() {
        final NavigationDrawer previous = attachedDrawer;
        if (previous != null) {
            previous.sizeProperty().removeListener(stateListener);
        }
        final NavigationDrawerPane current = attachedPane;
        attachedDrawer = current == null ? null : current.getDrawer();
        final NavigationDrawer currentDrawer = attachedDrawer;
        if (currentDrawer != null) {
            currentDrawer.sizeProperty().addListener(stateListener);
        }
        updateVisualState(true);
    }

    private void updateVisualState(final boolean animate) {
        chevronShown = computeChevronShown();
        final double barsOpacity = chevronShown ? 0 : 1;
        final double chevronOpacity = chevronShown ? 1 : 0;
        final double barsRotate = chevronShown ? 90 : 0;

        stopTransition();
        final NavigationDrawer currentDrawer = attachedDrawer;
        final boolean drawerAnimates = currentDrawer == null || currentDrawer.isAnimated();
        // Without a scene there is no pulse to drive the timeline, so the icon would stay half way.
        if (!animate || !drawerAnimates || getScene() == null) {
            bars.setOpacity(barsOpacity);
            bars.setRotate(barsRotate);
            chevron.setOpacity(chevronOpacity);
            return;
        }
        final Timeline timeline = new Timeline(new KeyFrame(durationOf(),
                new KeyValue(bars.opacityProperty(), barsOpacity, Interpolator.EASE_BOTH),
                new KeyValue(bars.rotateProperty(), barsRotate, Interpolator.EASE_BOTH),
                new KeyValue(chevron.opacityProperty(), chevronOpacity, Interpolator.EASE_BOTH)));
        timeline.setOnFinished(event -> transition = null);
        transition = timeline;
        timeline.play();
    }

    private boolean computeChevronShown() {
        final NavigationDrawerPane current = attachedPane;
        if (current == null) {
            return true;
        }
        if (current.getEffectiveDisplayMode() == DrawerDisplayMode.MODAL) {
            return current.isDrawerOpen();
        }
        final NavigationDrawer currentDrawer = current.getDrawer();
        return currentDrawer == null || currentDrawer.getSize() == DrawerSize.EXPANDED;
    }

    private Duration durationOf() {
        final NavigationDrawer currentDrawer = attachedDrawer;
        return currentDrawer == null ? FALLBACK_DURATION : currentDrawer.getTransitionDuration();
    }

    private void stopTransition() {
        final Timeline running = transition;
        if (running != null) {
            running.stop();
            transition = null;
        }
    }

}
