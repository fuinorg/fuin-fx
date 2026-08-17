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
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import org.jspecify.annotations.Nullable;

/**
 * A collapsible sub menu: an item that holds destinations of its own instead of being one.
 * <p>
 * Clicking a group opens and closes it and never changes the selection - there is no page behind a group, so
 * a click that both navigated somewhere and expanded something would be two answers to one question.
 * <p>
 * Nesting is one level deep by construction: {@link #getItems()} holds {@link NavigationDestination}s, so a
 * group cannot contain another group. That is what real navigation drawers do, and it keeps indentation,
 * keyboard handling and the selection order predictable.
 * <p>
 * While the drawer is {@link DrawerSize#COLLAPSED} the children have nowhere to go, so a click on the group
 * opens them in a flyout beside the rail instead of expanding in place. Selecting a child of a collapsed
 * group expands it, so the selection is never hidden.
 */
public final class NavigationGroup extends NavigationLabeledItem {

    private final ObservableList<NavigationDestination> items = FXCollections.observableArrayList();

    private final BooleanProperty expanded = new SimpleBooleanProperty(this, "expanded", false);

    /**
     * Constructor with text.
     *
     * @param text Label, never {@code null}.
     */
    public NavigationGroup(final String text) {
        super(text);
    }

    /**
     * Constructor with text and graphic.
     *
     * @param text    Label, never {@code null}.
     * @param graphic Icon node or {@code null} for none.
     */
    public NavigationGroup(final String text, final @Nullable Node graphic) {
        this(text);
        setGraphic(graphic);
    }

    /**
     * Constructor with text, graphic and children.
     *
     * @param text     Label, never {@code null}.
     * @param graphic  Icon node or {@code null} for none.
     * @param children Destinations of this group, never {@code null}.
     */
    public NavigationGroup(final String text, final @Nullable Node graphic,
            final NavigationDestination... children) {
        this(text, graphic);
        items.addAll(children);
    }

    /**
     * The destinations of this group, in the order they are shown.
     *
     * @return Modifiable live list, never {@code null}.
     */
    public ObservableList<NavigationDestination> getItems() {
        return items;
    }

    /**
     * Whether the children are shown. Ignored while the drawer is {@link DrawerSize#COLLAPSED}, where the
     * children appear in a flyout instead.
     *
     * @return Property, defaults to {@code false}.
     */
    public BooleanProperty expandedProperty() {
        return expanded;
    }

    /**
     * Returns whether the children are shown.
     *
     * @return {@code true} if expanded.
     */
    public boolean isExpanded() {
        return expanded.get();
    }

    /**
     * Sets whether the children are shown.
     *
     * @param value {@code true} to show them.
     */
    public void setExpanded(final boolean value) {
        expanded.set(value);
    }

    /**
     * Opens the group if it is closed and closes it otherwise.
     */
    public void toggleExpanded() {
        setExpanded(!isExpanded());
    }

    @Override
    public String toString() {
        return "NavigationGroup[" + getText() + "]";
    }

}
