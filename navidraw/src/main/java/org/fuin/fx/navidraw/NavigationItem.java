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

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import org.jspecify.annotations.Nullable;

/**
 * An entry in a {@link NavigationDrawer}: a {@link NavigationDestination}, a {@link NavigationGroup} of
 * destinations, a {@link NavigationSection} or a {@link NavigationSeparator}.
 * <p>
 * The type is sealed: the drawer renders every permitted kind with a node of its own, so a further kind
 * could not be drawn. Anything a custom item would do can be done with a destination and a graphic node of
 * your choice.
 */
public abstract sealed class NavigationItem
        permits NavigationLabeledItem, NavigationSection, NavigationSeparator {

    private final BooleanProperty visible = new SimpleBooleanProperty(this, "visible", true);

    private @Nullable Object userData;

    /**
     * Only the permitted subclasses can be created.
     */
    NavigationItem() {
        super();
    }

    /**
     * Whether the item is shown in the drawer. An invisible item takes no space and cannot be selected.
     *
     * @return Property, defaults to {@code true}.
     */
    public final BooleanProperty visibleProperty() {
        return visible;
    }

    /**
     * Returns whether the item is shown in the drawer.
     *
     * @return {@code true} if visible.
     */
    public final boolean isVisible() {
        return visible.get();
    }

    /**
     * Sets whether the item is shown in the drawer.
     *
     * @param value {@code true} to show the item.
     */
    public final void setVisible(final boolean value) {
        visible.set(value);
    }

    /**
     * Returns the application defined payload of this item.
     *
     * @return Whatever was passed to {@link #setUserData(Object)} or {@code null}.
     */
    public final @Nullable Object getUserData() {
        return userData;
    }

    /**
     * Attaches an application defined payload to this item. The drawer never reads it.
     *
     * @param value Payload or {@code null} to remove it.
     */
    public final void setUserData(final @Nullable Object value) {
        this.userData = value;
    }

}
