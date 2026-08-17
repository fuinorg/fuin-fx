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
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.css.PseudoClass;
import javafx.geometry.Pos;
import javafx.scene.AccessibleRole;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import org.jspecify.annotations.Nullable;

/**
 * The caption of a {@link NavigationSection}, with a twisty when the section can be folded.
 * <p>
 * Not an {@link ItemRow}: a caption has no icon, no tooltip and no disabled state, and it does not shrink to
 * an icon in the mini size - it disappears entirely. What it does share with a row is the keyboard and the
 * accessible role, and only while the section is actually collapsible; a plain heading must not be a focus
 * stop that does nothing when pressed.
 */
final class SectionCaptionRow extends HBox {

    private static final PseudoClass COLLAPSIBLE_PSEUDO_CLASS = PseudoClass.getPseudoClass("collapsible");

    private static final PseudoClass EXPANDED_PSEUDO_CLASS = PseudoClass.getPseudoClass("expanded");

    private static final double FOLDED_ROTATE = -90;

    private static final double OPEN_ROTATE = 0;

    private final NavigationDrawer drawer;

    private final NavigationSection section;

    private final Runnable onActivate;

    private final Label label = new Label();

    private final Region twisty = new Region();

    private final BooleanProperty collapsed = new SimpleBooleanProperty(this, "collapsed", false);

    private @Nullable Timeline transition;

    SectionCaptionRow(final NavigationDrawer drawer, final NavigationSection section,
            final Runnable onActivate) {
        super();
        this.drawer = drawer;
        this.section = section;
        this.onActivate = onActivate;

        getStyleClass().setAll("navigation-section-header");
        setAlignment(Pos.CENTER_LEFT);
        setMaxWidth(Double.MAX_VALUE);

        label.getStyleClass().setAll("label");
        label.textProperty().bind(section.textProperty());

        final Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        twisty.getStyleClass().setAll("twisty");
        twisty.setRotate(OPEN_ROTATE);
        twisty.visibleProperty().bind(section.collapsibleProperty().and(collapsed.not()));
        twisty.managedProperty().bind(twisty.visibleProperty());

        getChildren().addAll(label, spacer, twisty);

        // A heading that cannot be folded is not a control: no focus stop, no button role, no hand cursor.
        focusTraversableProperty().bind(section.collapsibleProperty());
        accessibleTextProperty().bind(section.textProperty());
        visibleProperty().bind(collapsed.not());
        managedProperty().bind(visibleProperty());
        section.collapsibleProperty().addListener((obs, old, value) -> updateCollapsible(value));
        updateCollapsible(section.isCollapsible());

        setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.PRIMARY) {
                activate();
            }
        });
        addEventHandler(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.ENTER || event.getCode() == KeyCode.SPACE) {
                activate();
                event.consume();
            }
        });
    }

    /**
     * Turns the twisty to match the section.
     *
     * @param expanded {@code true} if the items are showing.
     * @param animate  {@code true} to turn it over time rather than at once.
     */
    void setExpandedState(final boolean expanded, final boolean animate) {
        pseudoClassStateChanged(EXPANDED_PSEUDO_CLASS, expanded);
        final double target = expanded ? OPEN_ROTATE : FOLDED_ROTATE;
        stopTransition();
        if (!animate || getScene() == null) {
            twisty.setRotate(target);
            return;
        }
        final Timeline timeline = new Timeline(new KeyFrame(drawer.getTransitionDuration(),
                new KeyValue(twisty.rotateProperty(), target, Interpolator.EASE_BOTH)));
        timeline.setOnFinished(event -> transition = null);
        transition = timeline;
        timeline.play();
    }

    /**
     * Hides or shows the caption. A caption without an icon has nowhere to go on the mini rail.
     *
     * @param value {@code true} for the mini presentation.
     */
    void setCollapsed(final boolean value) {
        collapsed.set(value);
    }

    /**
     * Returns the caption label, for tests.
     *
     * @return Label, never {@code null}.
     */
    Label getLabel() {
        return label;
    }

    /**
     * Returns the twisty node, for tests.
     *
     * @return Twisty, never {@code null}.
     */
    Region getTwisty() {
        return twisty;
    }

    /**
     * Detaches the caption from its section. Called before it is thrown away.
     */
    void dispose() {
        stopTransition();
        label.textProperty().unbind();
        twisty.visibleProperty().unbind();
        twisty.managedProperty().unbind();
        focusTraversableProperty().unbind();
        accessibleTextProperty().unbind();
        visibleProperty().unbind();
        managedProperty().unbind();
    }

    private void activate() {
        if (!section.isCollapsible()) {
            return;
        }
        requestFocus();
        onActivate.run();
    }

    private void updateCollapsible(final boolean value) {
        pseudoClassStateChanged(COLLAPSIBLE_PSEUDO_CLASS, value);
        setAccessibleRole(value ? AccessibleRole.BUTTON : AccessibleRole.TEXT);
    }

    private void stopTransition() {
        final Timeline running = transition;
        if (running != null) {
            running.stop();
            transition = null;
        }
    }

}
