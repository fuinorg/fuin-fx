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

import javafx.scene.control.Label;

/**
 * The node a {@link NavigationDestination} is drawn with: the shared row plus a badge.
 */
final class DestinationCell extends ItemRow {

    private final NavigationDrawer drawer;

    private final NavigationDestination destination;

    private final Label badge = new Label();

    DestinationCell(final NavigationDrawer drawer, final NavigationDestination destination) {
        super(destination);
        this.drawer = drawer;
        this.destination = destination;

        badge.getStyleClass().setAll("badge");
        badge.textProperty().bind(destination.badgeTextProperty());
        // The mini size has no room for it, and a flyout child is never in the mini size.
        badge.visibleProperty().bind(collapsedProperty().not().and(destination.badgeTextProperty().isNotEmpty()));
        badge.managedProperty().bind(badge.visibleProperty());
        addTrailing(badge);
    }

    /**
     * Returns the destination this cell shows.
     *
     * @return Destination, never {@code null}.
     */
    NavigationDestination getDestination() {
        return destination;
    }

    /**
     * Returns the badge node, for tests.
     *
     * @return Badge label, never {@code null}.
     */
    Label getBadge() {
        return badge;
    }

    @Override
    protected void activate() {
        drawer.getSelectionModel().select(destination);
        final Runnable action = destination.getOnActivate();
        if (action != null) {
            action.run();
        }
    }

}
