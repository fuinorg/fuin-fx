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

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.Node;
import org.jspecify.annotations.Nullable;

/**
 * An item drawn as a row of icon and label - a {@link NavigationDestination} or a {@link NavigationGroup}.
 * <p>
 * Both are clickable, both shrink to their icon in the mini size and both then show their text as a tooltip
 * instead, which is why all of that lives here.
 */
public abstract sealed class NavigationLabeledItem extends NavigationItem
        permits NavigationDestination, NavigationGroup {

    private final StringProperty text;

    private final ObjectProperty<@Nullable Node> graphic = new SimpleObjectProperty<>(this, "graphic", null);

    private final StringProperty tooltipText = new SimpleStringProperty(this, "tooltipText", "");

    private final BooleanProperty disable = new SimpleBooleanProperty(this, "disable", false);

    /**
     * Constructor with text.
     *
     * @param text Label, never {@code null}.
     */
    NavigationLabeledItem(final String text) {
        super();
        this.text = new SimpleStringProperty(this, "text", Objects.requireNonNull(text, "text==null"));
    }

    /**
     * Label shown next to the graphic. It is also the accessible text and the tooltip fallback, so it stays
     * meaningful when the drawer is {@link DrawerSize#COLLAPSED}.
     *
     * @return Property, never {@code null}.
     */
    public final StringProperty textProperty() {
        return text;
    }

    /**
     * Returns the label.
     *
     * @return Label, never {@code null}.
     */
    public final String getText() {
        return text.get();
    }

    /**
     * Sets the label.
     *
     * @param value Label, never {@code null}.
     */
    public final void setText(final String value) {
        text.set(Objects.requireNonNull(value, "value==null"));
    }

    /**
     * Icon node shown before the label. Any node works - an Ikonli {@code FontIcon}, an {@code ImageView} or
     * an {@code SVGPath}. See {@link IkonliGraphics} for a shortcut.
     *
     * @return Property, defaults to {@code null}.
     */
    public final ObjectProperty<@Nullable Node> graphicProperty() {
        return graphic;
    }

    /**
     * Returns the icon node.
     *
     * @return Icon node or {@code null}.
     */
    public final @Nullable Node getGraphic() {
        return graphic.get();
    }

    /**
     * Sets the icon node.
     *
     * @param value Icon node or {@code null} for none.
     */
    public final void setGraphic(final @Nullable Node value) {
        graphic.set(value);
    }

    /**
     * Tooltip text. When empty the {@link #textProperty() text} is used instead, which is why a collapsed
     * drawer stays readable without any extra setup.
     *
     * @return Property, defaults to an empty string.
     */
    public final StringProperty tooltipTextProperty() {
        return tooltipText;
    }

    /**
     * Returns the tooltip text.
     *
     * @return Tooltip text, may be empty but never {@code null}.
     */
    public final String getTooltipText() {
        return tooltipText.get();
    }

    /**
     * Sets the tooltip text.
     *
     * @param value Tooltip text or an empty string to fall back to the label.
     */
    public final void setTooltipText(final String value) {
        tooltipText.set(Objects.requireNonNull(value, "value==null"));
    }

    /**
     * Whether the item is greyed out and does not react to clicks.
     *
     * @return Property, defaults to {@code false}.
     */
    public final BooleanProperty disableProperty() {
        return disable;
    }

    /**
     * Returns whether the item is disabled.
     *
     * @return {@code true} if disabled.
     */
    public final boolean isDisable() {
        return disable.get();
    }

    /**
     * Sets whether the item is disabled.
     *
     * @param value {@code true} to disable.
     */
    public final void setDisable(final boolean value) {
        disable.set(value);
    }

}
