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

import java.util.Objects;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.Node;
import org.jspecify.annotations.Nullable;

/**
 * A selectable entry in a {@link NavigationDrawer} - one of the application's top-level destinations, or one
 * entry of a {@link NavigationGroup}.
 */
public final class NavigationDestination extends NavigationLabeledItem {

    private final String id;

    private final StringProperty badgeText = new SimpleStringProperty(this, "badgeText", "");

    private final ObjectProperty<@Nullable Runnable> onActivate = new SimpleObjectProperty<>(this, "onActivate", null);

    /**
     * Constructor with identifier and text.
     *
     * @param id   Identifier that is unique within the drawer, used by
     *             {@link NavigationDrawer#findDestination(String)}. Never {@code null}.
     * @param text Label shown next to the graphic. Never {@code null}.
     */
    public NavigationDestination(final String id, final String text) {
        super(text);
        this.id = Objects.requireNonNull(id, "id==null");
    }

    /**
     * Constructor with identifier, text and graphic.
     *
     * @param id      Identifier that is unique within the drawer. Never {@code null}.
     * @param text    Label shown next to the graphic. Never {@code null}.
     * @param graphic Icon node or {@code null} for none.
     */
    public NavigationDestination(final String id, final String text, final @Nullable Node graphic) {
        this(id, text);
        setGraphic(graphic);
    }

    /**
     * Returns the identifier that is unique within the drawer.
     *
     * @return Identifier, never {@code null}.
     */
    public String getId() {
        return id;
    }

    /**
     * Short text shown in a badge at the trailing edge of the item, for example an unread count. An empty
     * string hides the badge.
     *
     * @return Property, defaults to an empty string.
     */
    public StringProperty badgeTextProperty() {
        return badgeText;
    }

    /**
     * Returns the badge text.
     *
     * @return Badge text, may be empty but never {@code null}.
     */
    public String getBadgeText() {
        return badgeText.get();
    }

    /**
     * Sets the badge text.
     *
     * @param value Badge text or an empty string to hide the badge.
     */
    public void setBadgeText(final String value) {
        badgeText.set(Objects.requireNonNull(value, "value==null"));
    }

    /**
     * Runs after the destination became the selected one. It is not called when the selection is changed
     * through the {@link NavigationDrawer#getSelectionModel() selection model} directly, only when the user
     * activated the item.
     *
     * @return Property, defaults to {@code null}.
     */
    public ObjectProperty<@Nullable Runnable> onActivateProperty() {
        return onActivate;
    }

    /**
     * Returns the action that runs when the user activates the destination.
     *
     * @return Action or {@code null}.
     */
    public @Nullable Runnable getOnActivate() {
        return onActivate.get();
    }

    /**
     * Sets the action that runs when the user activates the destination.
     *
     * @param value Action or {@code null} for none.
     */
    public void setOnActivate(final @Nullable Runnable value) {
        onActivate.set(value);
    }

    @Override
    public String toString() {
        return "NavigationDestination[" + id + "]";
    }

}
