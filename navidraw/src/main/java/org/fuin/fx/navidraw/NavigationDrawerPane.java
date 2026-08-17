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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.css.PseudoClass;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Region;
import org.jspecify.annotations.Nullable;

/**
 * Puts a {@link NavigationDrawer} beside or above a content node and switches between the two arrangements
 * as the pane resizes.
 * <p>
 * In {@link DrawerDisplayMode#PERSISTENT} the drawer takes space of its own at the leading edge and the
 * content is laid out next to it. In {@link DrawerDisplayMode#MODAL} the content uses the full width, the
 * drawer floats above it and a scrim dims everything behind. Clicking the scrim or pressing {@code ESCAPE}
 * dismisses the drawer and returns the focus to wherever it was when the drawer opened.
 * <p>
 * With the default {@link DrawerDisplayMode#AUTO} the pane picks the arrangement from its own width against
 * {@link #modalBreakpointProperty()}. {@link #effectiveDisplayModeProperty()} reports what that came out as,
 * and is reflected as the {@code :persistent} and {@code :modal} pseudo classes.
 * <p>
 * {@link #drawerOpenProperty()} only means something in the modal arrangement - a persistent drawer is
 * always shown. Narrowing the window past the breakpoint therefore closes the drawer rather than leaving an
 * overlay covering the content.
 */
public class NavigationDrawerPane extends Region {

    private static final String DEFAULT_STYLE_CLASS = "navigation-drawer-pane";

    private static final PseudoClass PERSISTENT_PSEUDO_CLASS = PseudoClass.getPseudoClass("persistent");

    private static final PseudoClass MODAL_PSEUDO_CLASS = PseudoClass.getPseudoClass("modal");

    private static final PseudoClass OPEN_PSEUDO_CLASS = PseudoClass.getPseudoClass("open");

    private static final double DEFAULT_MODAL_BREAKPOINT = 900;

    private final Region scrim = new Region();

    private final ObjectProperty<@Nullable Node> content = new SimpleObjectProperty<>(this, "content", null);

    private final ObjectProperty<@Nullable NavigationDrawer> drawer =
            new SimpleObjectProperty<>(this, "drawer", null);

    private final ObjectProperty<DrawerDisplayMode> displayMode =
            new SimpleObjectProperty<>(this, "displayMode", DrawerDisplayMode.AUTO);

    private final ReadOnlyObjectWrapper<DrawerDisplayMode> effectiveDisplayMode =
            new ReadOnlyObjectWrapper<>(this, "effectiveDisplayMode", DrawerDisplayMode.PERSISTENT);

    private final DoubleProperty modalBreakpoint =
            new SimpleDoubleProperty(this, "modalBreakpoint", DEFAULT_MODAL_BREAKPOINT);

    private final BooleanProperty drawerOpen = new SimpleBooleanProperty(this, "drawerOpen", false);

    /** 0 = fully pushed out of view, 1 = fully in view. Always 1 while persistent. */
    private final DoubleProperty drawerOffset = new SimpleDoubleProperty(this, "drawerOffset", 1);

    private @Nullable Timeline transition;

    private @Nullable Node focusOwnerBeforeOpen;

    /**
     * Default constructor creating an empty pane.
     */
    public NavigationDrawerPane() {
        super();
        getStyleClass().setAll(DEFAULT_STYLE_CLASS);

        scrim.getStyleClass().setAll("drawer-scrim");
        scrim.setManaged(false);
        scrim.opacityProperty().bind(drawerOffset);
        scrim.visibleProperty().bind(effectiveDisplayMode.isEqualTo(DrawerDisplayMode.MODAL)
                .and(drawerOffset.greaterThan(0)));
        scrim.mouseTransparentProperty().bind(scrim.visibleProperty().not());
        scrim.setOnMouseClicked(event -> {
            close();
            event.consume();
        });

        content.addListener(observable -> updateChildren());
        drawer.addListener(observable -> {
            updateChildren();
            updateOffset(false);
        });
        displayMode.addListener(observable -> updateEffectiveDisplayMode());
        modalBreakpoint.addListener(observable -> updateEffectiveDisplayMode());
        widthProperty().addListener(observable -> updateEffectiveDisplayMode());
        drawerOpen.addListener(observable -> onDrawerOpenChanged());
        drawerOffset.addListener(observable -> requestLayout());

        addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.ESCAPE && isModal() && isDrawerOpen()) {
                close();
                event.consume();
            }
        });

        updateChildren();
        updateEffectiveDisplayMode();
        updateOpenPseudoClass();
    }

    /**
     * Constructor with drawer and content.
     *
     * @param drawer  Drawer to show, never {@code null}.
     * @param content Node filling the rest of the pane, never {@code null}.
     */
    public NavigationDrawerPane(final NavigationDrawer drawer, final Node content) {
        this();
        setDrawer(Objects.requireNonNull(drawer, "drawer==null"));
        setContent(Objects.requireNonNull(content, "content==null"));
    }

    /**
     * The application content shown beside (persistent) or below (modal) the drawer.
     *
     * @return Property, defaults to {@code null}.
     */
    public final ObjectProperty<@Nullable Node> contentProperty() {
        return content;
    }

    /**
     * Returns the content node.
     *
     * @return Content or {@code null}.
     */
    public final @Nullable Node getContent() {
        return content.get();
    }

    /**
     * Sets the content node.
     *
     * @param value Content or {@code null} for none.
     */
    public final void setContent(final @Nullable Node value) {
        content.set(value);
    }

    /**
     * The drawer this pane arranges.
     *
     * @return Property, defaults to {@code null}.
     */
    public final ObjectProperty<@Nullable NavigationDrawer> drawerProperty() {
        return drawer;
    }

    /**
     * Returns the drawer.
     *
     * @return Drawer or {@code null}.
     */
    public final @Nullable NavigationDrawer getDrawer() {
        return drawer.get();
    }

    /**
     * Sets the drawer.
     *
     * @param value Drawer or {@code null} for none.
     */
    public final void setDrawer(final @Nullable NavigationDrawer value) {
        drawer.set(value);
    }

    /**
     * Which arrangement to use, or {@link DrawerDisplayMode#AUTO} to decide it from the pane's width.
     *
     * @return Property, defaults to {@link DrawerDisplayMode#AUTO}.
     */
    public final ObjectProperty<DrawerDisplayMode> displayModeProperty() {
        return displayMode;
    }

    /**
     * Returns the requested arrangement.
     *
     * @return Display mode, never {@code null}.
     */
    public final DrawerDisplayMode getDisplayMode() {
        return displayMode.get();
    }

    /**
     * Sets the requested arrangement.
     *
     * @param value Display mode, never {@code null}.
     */
    public final void setDisplayMode(final DrawerDisplayMode value) {
        displayMode.set(Objects.requireNonNull(value, "value==null"));
    }

    /**
     * The arrangement actually in use, which is never {@link DrawerDisplayMode#AUTO}.
     *
     * @return Read only property, never {@code null}.
     */
    public final ReadOnlyObjectProperty<DrawerDisplayMode> effectiveDisplayModeProperty() {
        return effectiveDisplayMode.getReadOnlyProperty();
    }

    /**
     * Returns the arrangement actually in use.
     *
     * @return Either {@link DrawerDisplayMode#PERSISTENT} or {@link DrawerDisplayMode#MODAL}.
     */
    public final DrawerDisplayMode getEffectiveDisplayMode() {
        return effectiveDisplayMode.get();
    }

    /**
     * Pane width below which {@link DrawerDisplayMode#AUTO} switches to the modal arrangement.
     *
     * @return Property, defaults to 900.
     */
    public final DoubleProperty modalBreakpointProperty() {
        return modalBreakpoint;
    }

    /**
     * Returns the breakpoint width.
     *
     * @return Width in pixels.
     */
    public final double getModalBreakpoint() {
        return modalBreakpoint.get();
    }

    /**
     * Sets the breakpoint width.
     *
     * @param value Width in pixels.
     */
    public final void setModalBreakpoint(final double value) {
        modalBreakpoint.set(value);
    }

    /**
     * Whether the modal drawer is shown. Ignored while the arrangement is persistent, where the drawer is
     * always visible.
     *
     * @return Property, defaults to {@code false}.
     */
    public final BooleanProperty drawerOpenProperty() {
        return drawerOpen;
    }

    /**
     * Returns whether the modal drawer is shown.
     *
     * @return {@code true} if open.
     */
    public final boolean isDrawerOpen() {
        return drawerOpen.get();
    }

    /**
     * Sets whether the modal drawer is shown.
     *
     * @param value {@code true} to show it.
     */
    public final void setDrawerOpen(final boolean value) {
        drawerOpen.set(value);
    }

    /**
     * Shows the drawer. Does nothing while the arrangement is persistent.
     */
    public final void open() {
        setDrawerOpen(true);
    }

    /**
     * Hides the drawer. Does nothing while the arrangement is persistent.
     */
    public final void close() {
        setDrawerOpen(false);
    }

    /**
     * Shows the drawer if it is hidden and hides it otherwise.
     */
    public final void toggle() {
        setDrawerOpen(!isDrawerOpen());
    }

    /**
     * Returns the scrim that dims the content behind an open modal drawer.
     *
     * @return Scrim node, never {@code null}.
     */
    public final Region getScrim() {
        return scrim;
    }

    /**
     * A user agent stylesheet reaches the node it is declared on and that node's children only. Declaring it
     * here as well as on the drawer is what styles the scrim and anything the application puts into the
     * content - a {@link DrawerToggleButton} in a tool bar, above all.
     *
     * @return The default stylesheet, never {@code null}.
     */
    @Override
    public String getUserAgentStylesheet() {
        return NavigationDrawer.defaultStylesheet();
    }

    @Override
    protected void layoutChildren() {
        final double width = getWidth();
        final double height = getHeight();
        final NavigationDrawer currentDrawer = getDrawer();
        final Node currentContent = getContent();
        final double drawerWidth = currentDrawer == null ? 0 : snapSizeX(currentDrawer.prefWidth(height));

        if (isModal()) {
            if (currentContent != null) {
                layoutInArea(currentContent, 0, 0, width, height, 0, HPos.LEFT, VPos.TOP);
            }
            if (currentDrawer != null) {
                layoutInArea(currentDrawer, 0, 0, drawerWidth, height, 0, HPos.LEFT, VPos.TOP);
                currentDrawer.setTranslateX(-drawerWidth * (1 - drawerOffset.get()));
            }
        } else {
            if (currentDrawer != null) {
                layoutInArea(currentDrawer, 0, 0, drawerWidth, height, 0, HPos.LEFT, VPos.TOP);
                currentDrawer.setTranslateX(0);
            }
            if (currentContent != null) {
                layoutInArea(currentContent, drawerWidth, 0, Math.max(0, width - drawerWidth), height, 0,
                        HPos.LEFT, VPos.TOP);
            }
        }
        layoutInArea(scrim, 0, 0, width, height, 0, HPos.LEFT, VPos.TOP);
    }

    @Override
    protected double computePrefWidth(final double height) {
        final NavigationDrawer currentDrawer = getDrawer();
        final Node currentContent = getContent();
        final double drawerWidth = currentDrawer == null ? 0 : currentDrawer.prefWidth(height);
        final double contentWidth = currentContent == null ? 0 : currentContent.prefWidth(height);
        if (getEffectiveDisplayMode() == DrawerDisplayMode.PERSISTENT) {
            return drawerWidth + contentWidth;
        }
        return Math.max(drawerWidth, contentWidth);
    }

    @Override
    protected double computePrefHeight(final double width) {
        final NavigationDrawer currentDrawer = getDrawer();
        final Node currentContent = getContent();
        final double drawerHeight = currentDrawer == null ? 0 : currentDrawer.prefHeight(-1);
        final double contentHeight = currentContent == null ? 0 : currentContent.prefHeight(-1);
        return Math.max(drawerHeight, contentHeight);
    }

    private boolean isModal() {
        return getEffectiveDisplayMode() == DrawerDisplayMode.MODAL;
    }

    private void updateChildren() {
        final List<Node> nodes = new ArrayList<>();
        final Node currentContent = getContent();
        if (currentContent != null) {
            nodes.add(currentContent);
        }
        nodes.add(scrim);
        final NavigationDrawer currentDrawer = getDrawer();
        if (currentDrawer != null) {
            nodes.add(currentDrawer);
        }
        getChildren().setAll(nodes);
    }

    private void updateEffectiveDisplayMode() {
        final DrawerDisplayMode requested = getDisplayMode();
        final DrawerDisplayMode effective;
        if (requested == DrawerDisplayMode.AUTO) {
            effective = getWidth() < getModalBreakpoint() ? DrawerDisplayMode.MODAL : DrawerDisplayMode.PERSISTENT;
        } else {
            effective = requested;
        }
        if (effective == getEffectiveDisplayMode()) {
            return;
        }
        effectiveDisplayMode.set(effective);
        pseudoClassStateChanged(MODAL_PSEUDO_CLASS, effective == DrawerDisplayMode.MODAL);
        pseudoClassStateChanged(PERSISTENT_PSEUDO_CLASS, effective == DrawerDisplayMode.PERSISTENT);
        if (effective == DrawerDisplayMode.MODAL) {
            // The window just became too narrow for a panel of its own. Leaving the drawer "open" would
            // cover the content the user was looking at.
            setDrawerOpen(false);
        }
        updateOffset(false);
        requestLayout();
    }

    private void onDrawerOpenChanged() {
        if (isDrawerOpen()) {
            final Scene scene = getScene();
            focusOwnerBeforeOpen = scene == null ? null : scene.getFocusOwner();
        } else {
            restoreFocus();
        }
        updateOpenPseudoClass();
        updateOffset(true);
    }

    private void restoreFocus() {
        final Node previous = focusOwnerBeforeOpen;
        focusOwnerBeforeOpen = null;
        if (previous != null && previous.getScene() == getScene() && getScene() != null) {
            previous.requestFocus();
        }
    }

    private void updateOpenPseudoClass() {
        pseudoClassStateChanged(OPEN_PSEUDO_CLASS, isDrawerOpen());
    }

    private void updateOffset(final boolean animate) {
        stopTransition();
        final double target = isModal() && !isDrawerOpen() ? 0 : 1;
        final NavigationDrawer currentDrawer = getDrawer();
        // Without a scene there is no pulse to drive the timeline, so the value would stay at its start.
        if (!animate || currentDrawer == null || !currentDrawer.isAnimated() || getScene() == null) {
            drawerOffset.set(target);
            return;
        }
        final Timeline timeline = new Timeline(new KeyFrame(currentDrawer.getTransitionDuration(),
                new KeyValue(drawerOffset, target, Interpolator.EASE_BOTH)));
        timeline.setOnFinished(event -> transition = null);
        transition = timeline;
        timeline.play();
    }

    private void stopTransition() {
        final Timeline running = transition;
        if (running != null) {
            running.stop();
            transition = null;
        }
    }

}
