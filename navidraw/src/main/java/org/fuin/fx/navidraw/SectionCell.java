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

import javafx.beans.InvalidationListener;
import javafx.collections.ListChangeListener;
import javafx.scene.Node;
import javafx.scene.layout.VBox;
import org.jspecify.annotations.Nullable;

/**
 * The node a {@link NavigationSection} is drawn with: a caption and, below it, the items that fold away with
 * it.
 * <p>
 * The same shape as {@link GroupCell} one level up, and it forwards to its children the same way - the mini
 * size and the current selection both have to reach the destinations inside, and the groups inside have to
 * pass them on again to their own children.
 */
final class SectionCell extends VBox {

    private final NavigationDrawer drawer;

    private final NavigationSection section;

    private final SectionCaptionRow caption;

    private final CollapsibleBox itemBox = new CollapsibleBox("section-items");

    private final List<DestinationCell> destinationCells = new ArrayList<>();

    private final List<GroupCell> groupCells = new ArrayList<>();

    private final ListChangeListener<NavigationLabeledItem> itemsListener = change -> rebuildChildren();

    private final InvalidationListener expandedListener = observable -> updateExpansion(true);

    private boolean collapsed;

    SectionCell(final NavigationDrawer drawer, final NavigationSection section) {
        super();
        this.drawer = drawer;
        this.section = section;

        getStyleClass().setAll("navigation-section");
        setFillWidth(true);
        setMaxWidth(Double.MAX_VALUE);
        visibleProperty().bind(section.visibleProperty());
        managedProperty().bind(visibleProperty());

        caption = new SectionCaptionRow(drawer, section, section::toggleExpanded);
        getChildren().addAll(caption, itemBox);

        section.getItems().addListener(itemsListener);
        section.expandedProperty().addListener(expandedListener);

        rebuildChildren();
    }

    /**
     * Returns the section this cell shows.
     *
     * @return Section, never {@code null}.
     */
    NavigationSection getSection() {
        return section;
    }

    /**
     * Returns the caption row.
     *
     * @return Caption, never {@code null}.
     */
    SectionCaptionRow getCaptionRow() {
        return caption;
    }

    /**
     * Returns the box holding the items.
     *
     * @return Item box, never {@code null}.
     */
    CollapsibleBox getItemBox() {
        return itemBox;
    }

    /**
     * Returns the cells of the destinations that sit directly in this section.
     *
     * @return Unmodifiable list, never {@code null}.
     */
    List<DestinationCell> getDestinationCells() {
        return List.copyOf(destinationCells);
    }

    /**
     * Returns the cells of the groups in this section.
     *
     * @return Unmodifiable list, never {@code null}.
     */
    List<GroupCell> getGroupCells() {
        return List.copyOf(groupCells);
    }

    /**
     * Switches the cell between the full and the mini presentation.
     *
     * @param value {@code true} for the mini presentation.
     */
    void setCollapsed(final boolean value) {
        collapsed = value;
        caption.setCollapsed(value);
        for (final DestinationCell cell : destinationCells) {
            cell.setCollapsed(value);
        }
        for (final GroupCell cell : groupCells) {
            cell.setCollapsed(value);
        }
        updateExpansion(false);
    }

    /**
     * Marks whichever cell in this section shows the selected destination.
     *
     * @param selected Currently selected destination or {@code null}.
     */
    void updateSelection(final @Nullable NavigationDestination selected) {
        for (final DestinationCell cell : destinationCells) {
            cell.setSelectedState(cell.getDestination() == selected);
        }
        for (final GroupCell cell : groupCells) {
            cell.updateSelection(selected);
        }
    }

    /**
     * Detaches the cell from its section. Called before the cell is thrown away.
     */
    void dispose() {
        section.getItems().removeListener(itemsListener);
        section.expandedProperty().removeListener(expandedListener);
        visibleProperty().unbind();
        managedProperty().unbind();
        caption.dispose();
        itemBox.dispose();
        disposeChildCells();
    }

    private void rebuildChildren() {
        disposeChildCells();
        final List<Node> nodes = new ArrayList<>();
        for (final NavigationLabeledItem item : section.getItems()) {
            switch (item) {
                case NavigationDestination destination -> {
                    final DestinationCell cell = new DestinationCell(drawer, destination);
                    destinationCells.add(cell);
                    nodes.add(cell);
                }
                case NavigationGroup group -> {
                    final GroupCell cell = new GroupCell(drawer, group);
                    groupCells.add(cell);
                    nodes.add(cell);
                }
            }
        }
        itemBox.getChildren().setAll(nodes);
        setCollapsed(collapsed);
        updateSelection(drawer.getSelectionModel().getSelectedItem());
    }

    private void updateExpansion(final boolean animate) {
        // In the mini size the caption is gone, so nothing could reopen a folded section - its items would
        // be stranded. The rail therefore shows them whatever the section says.
        final boolean show = collapsed || section.isExpanded();
        // Without a scene there is no pulse to drive a timeline, and a drawer with animations switched off
        // expects every state to be final the moment it is set.
        final boolean animated = animate && drawer.isAnimated() && getScene() != null;
        caption.setExpandedState(section.isExpanded(), animated && !collapsed);
        itemBox.setOpen(show, animated, drawer.getTransitionDuration());
    }

    private void disposeChildCells() {
        for (final DestinationCell cell : destinationCells) {
            cell.dispose();
        }
        for (final GroupCell cell : groupCells) {
            cell.dispose();
        }
        destinationCells.clear();
        groupCells.clear();
    }

}
