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
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * A captioned block of the drawer - "Operations", "Master data" - that can be folded away.
 * <p>
 * A section owns the items below its caption rather than merely announcing them, which is what lets it
 * collapse them. It holds {@link NavigationDestination}s and {@link NavigationGroup}s, so a section cannot
 * contain a section: one level of captions and one level of sub menus below it, both enforced by the type of
 * {@link #getItems()} rather than by a rule someone has to remember.
 * <p>
 * Unlike a {@link NavigationGroup} a section starts open, and unlike a group it has no icon - it is a
 * caption, and there is nothing sensible to draw beside it.
 * <p>
 * A section is not selectable. Clicking the caption folds it, nothing else.
 * <p>
 * While the drawer is {@link DrawerSize#COLLAPSED} the caption is hidden, because a caption without an icon
 * has nowhere to go on the rail. The items are then shown whatever {@link #expandedProperty()} says: with no
 * caption left to click there would be no way to get a folded section back, and those destinations would be
 * stranded until the drawer is expanded again.
 */
public final class NavigationSection extends NavigationItem {

    private final StringProperty text;

    private final ObservableList<NavigationLabeledItem> items = FXCollections.observableArrayList();

    private final BooleanProperty collapsible = new SimpleBooleanProperty(this, "collapsible", true);

    private final BooleanProperty expanded = new SimpleBooleanProperty(this, "expanded", true);

    /**
     * Constructor with caption.
     *
     * @param text Caption, never {@code null}.
     */
    public NavigationSection(final String text) {
        super();
        this.text = new SimpleStringProperty(this, "text", Objects.requireNonNull(text, "text==null"));
    }

    /**
     * Constructor with caption and items.
     *
     * @param text     Caption, never {@code null}.
     * @param children Destinations and groups of this section, never {@code null}.
     */
    public NavigationSection(final String text, final NavigationLabeledItem... children) {
        this(text);
        items.addAll(children);
    }

    /**
     * Caption shown above the items.
     *
     * @return Property, never {@code null}.
     */
    public StringProperty textProperty() {
        return text;
    }

    /**
     * Returns the caption.
     *
     * @return Caption, never {@code null}.
     */
    public String getText() {
        return text.get();
    }

    /**
     * Sets the caption.
     *
     * @param value Caption, never {@code null}.
     */
    public void setText(final String value) {
        text.set(Objects.requireNonNull(value, "value==null"));
    }

    /**
     * The destinations and groups of this section, in the order they are shown.
     *
     * @return Modifiable live list, never {@code null}.
     */
    public ObservableList<NavigationLabeledItem> getItems() {
        return items;
    }

    /**
     * Whether the caption reacts to clicks and shows a twisty. Switch it off for a section that is only a
     * heading.
     *
     * @return Property, defaults to {@code true}.
     */
    public BooleanProperty collapsibleProperty() {
        return collapsible;
    }

    /**
     * Returns whether the section can be folded.
     *
     * @return {@code true} if collapsible.
     */
    public boolean isCollapsible() {
        return collapsible.get();
    }

    /**
     * Sets whether the section can be folded.
     *
     * @param value {@code false} to make the caption a plain heading.
     */
    public void setCollapsible(final boolean value) {
        collapsible.set(value);
    }

    /**
     * Whether the items are shown. Ignored while the drawer is {@link DrawerSize#COLLAPSED}, where the items
     * are always shown - see the class documentation.
     *
     * @return Property, defaults to {@code true}.
     */
    public BooleanProperty expandedProperty() {
        return expanded;
    }

    /**
     * Returns whether the items are shown.
     *
     * @return {@code true} if expanded.
     */
    public boolean isExpanded() {
        return expanded.get();
    }

    /**
     * Sets whether the items are shown.
     *
     * @param value {@code true} to show them.
     */
    public void setExpanded(final boolean value) {
        expanded.set(value);
    }

    /**
     * Opens the section if it is folded and folds it otherwise.
     */
    public void toggleExpanded() {
        setExpanded(!isExpanded());
    }

    @Override
    public String toString() {
        return "NavigationSection[" + getText() + "]";
    }

}
