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
import javafx.css.PseudoClass;
import javafx.scene.layout.Region;
import org.jspecify.annotations.Nullable;

/**
 * The clickable row of a {@link NavigationGroup}: the shared row plus the twisty that says whether the
 * children are showing.
 */
final class GroupHeaderRow extends ItemRow {

    /** Set while one of the group's children is the selected destination, whether they are visible or not. */
    private static final PseudoClass CONTAINS_SELECTED_PSEUDO_CLASS =
            PseudoClass.getPseudoClass("contains-selected");

    private static final PseudoClass EXPANDED_PSEUDO_CLASS = PseudoClass.getPseudoClass("expanded");

    private static final double COLLAPSED_ROTATE = -90;

    private static final double EXPANDED_ROTATE = 0;

    private final Region twisty = new Region();

    private final NavigationDrawer drawer;

    private final Runnable onActivate;

    private @Nullable Timeline transition;

    GroupHeaderRow(final NavigationDrawer drawer, final NavigationGroup group, final Runnable onActivate) {
        super(group);
        this.drawer = drawer;
        this.onActivate = onActivate;

        getStyleClass().add("navigation-group-header");

        twisty.getStyleClass().setAll("twisty");
        twisty.setRotate(COLLAPSED_ROTATE);
        // No room for it beside the icon in the mini size, where the children open in a flyout anyway.
        twisty.visibleProperty().bind(collapsedProperty().not());
        twisty.managedProperty().bind(twisty.visibleProperty());
        addTrailing(twisty);
    }

    @Override
    protected void activate() {
        onActivate.run();
    }

    /**
     * Turns the twisty to match the group.
     *
     * @param expanded {@code true} if the children are showing.
     * @param animate  {@code true} to turn it over time rather than at once.
     */
    void setExpandedState(final boolean expanded, final boolean animate) {
        pseudoClassStateChanged(EXPANDED_PSEUDO_CLASS, expanded);
        final double target = expanded ? EXPANDED_ROTATE : COLLAPSED_ROTATE;
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
     * Marks the header while one of its children is selected, so a closed group can show that the current
     * page is inside it.
     *
     * @param value {@code true} if a child is the selected destination.
     */
    void setContainsSelected(final boolean value) {
        pseudoClassStateChanged(CONTAINS_SELECTED_PSEUDO_CLASS, value);
    }

    /**
     * Returns the twisty node, for tests.
     *
     * @return Twisty, never {@code null}.
     */
    Region getTwisty() {
        return twisty;
    }

    @Override
    void dispose() {
        stopTransition();
        twisty.visibleProperty().unbind();
        twisty.managedProperty().unbind();
        super.dispose();
    }

    private void stopTransition() {
        final Timeline running = transition;
        if (running != null) {
            running.stop();
            transition = null;
        }
    }

}
