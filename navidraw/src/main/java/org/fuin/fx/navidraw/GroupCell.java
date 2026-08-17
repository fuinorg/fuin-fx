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
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Popup;
import org.jspecify.annotations.Nullable;

/**
 * The node a {@link NavigationGroup} is drawn with: a header row and, below it, the children.
 * <p>
 * Opening and closing animates the height of the child box, which is clipped to its own bounds so the rows
 * slide out of view instead of spilling over the items below.
 * <p>
 * In the mini size there is no room to expand anything, so the header opens a flyout beside the rail
 * instead. The flyout is a {@link Popup} with a scene of its own, which inherits nothing from the
 * application's scene - so it gets its own copy of the stylesheets, including whatever palette the
 * application added.
 */
final class GroupCell extends VBox {

    private final NavigationDrawer drawer;

    private final NavigationGroup group;

    private final GroupHeaderRow header;

    private final CollapsibleBox childBox = new CollapsibleBox("group-items");

    private final List<DestinationCell> childCells = new ArrayList<>();

    private final List<DestinationCell> flyoutCells = new ArrayList<>();

    private final ListChangeListener<NavigationDestination> childListener = change -> rebuildChildren();

    private final InvalidationListener expandedListener = observable -> updateExpansion(true);

    private @Nullable Popup flyout;

    private boolean collapsed;

    GroupCell(final NavigationDrawer drawer, final NavigationGroup group) {
        super();
        this.drawer = drawer;
        this.group = group;

        getStyleClass().setAll("navigation-group");
        setFillWidth(true);
        setMaxWidth(Double.MAX_VALUE);
        visibleProperty().bind(group.visibleProperty());
        managedProperty().bind(visibleProperty());

        header = new GroupHeaderRow(drawer, group, this::onHeaderActivated);

        getChildren().addAll(header, childBox);

        group.getItems().addListener(childListener);
        group.expandedProperty().addListener(expandedListener);

        rebuildChildren();
    }

    /**
     * Returns the group this cell shows.
     *
     * @return Group, never {@code null}.
     */
    NavigationGroup getGroup() {
        return group;
    }

    /**
     * Returns the clickable header row.
     *
     * @return Header row, never {@code null}.
     */
    GroupHeaderRow getHeaderRow() {
        return header;
    }

    /**
     * Returns the cells of the children as they appear inside the drawer.
     *
     * @return Unmodifiable list, never {@code null}.
     */
    List<DestinationCell> getChildCells() {
        return List.copyOf(childCells);
    }

    /**
     * Returns the cells of the children as they appear inside the flyout, empty until the flyout was built.
     *
     * @return Unmodifiable list, never {@code null}.
     */
    List<DestinationCell> getFlyoutCells() {
        return List.copyOf(flyoutCells);
    }

    /**
     * Returns the box holding the children.
     *
     * @return Child box, never {@code null}.
     */
    CollapsibleBox getChildBox() {
        return childBox;
    }

    /**
     * Switches the cell between the full and the mini presentation.
     *
     * @param value {@code true} for the mini presentation.
     */
    void setCollapsed(final boolean value) {
        collapsed = value;
        header.setCollapsed(value);
        hideFlyout();
        updateExpansion(false);
    }

    /**
     * Marks whichever of this group's cells shows the selected destination.
     *
     * @param selected Currently selected destination or {@code null}.
     */
    void updateSelection(final @Nullable NavigationDestination selected) {
        boolean containsSelected = false;
        for (final DestinationCell cell : childCells) {
            final boolean isSelected = cell.getDestination() == selected;
            cell.setSelectedState(isSelected);
            containsSelected = containsSelected || isSelected;
        }
        for (final DestinationCell cell : flyoutCells) {
            cell.setSelectedState(cell.getDestination() == selected);
        }
        header.setContainsSelected(containsSelected);
        if (containsSelected) {
            // Picking something from the flyout has answered the question the flyout was asking.
            hideFlyout();
        }
    }

    /**
     * Opens the flyout beside the rail. Does nothing while the cell is not in a window.
     */
    void showFlyout() {
        final Scene scene = getScene();
        if (scene == null || scene.getWindow() == null) {
            return;
        }
        final Bounds bounds = localToScreen(getBoundsInLocal());
        if (bounds == null) {
            return;
        }
        final Popup popup = flyoutPopup();
        syncFlyoutStylesheets(scene);
        popup.show(scene.getWindow(), bounds.getMaxX(), bounds.getMinY());
    }

    /**
     * Closes the flyout if it is open.
     */
    void hideFlyout() {
        final Popup popup = flyout;
        if (popup != null && popup.isShowing()) {
            popup.hide();
        }
    }

    /**
     * Returns whether the flyout is currently open.
     *
     * @return {@code true} if open.
     */
    boolean isFlyoutShowing() {
        final Popup popup = flyout;
        return popup != null && popup.isShowing();
    }

    /**
     * Builds the flyout content, creating a second set of cells for the same destinations.
     *
     * @return Content node, never {@code null}.
     */
    VBox buildFlyoutContent() {
        disposeFlyoutCells();
        final VBox box = new VBox();
        box.getStyleClass().setAll("navigation-flyout");
        final Label caption = new Label();
        caption.getStyleClass().setAll("caption");
        caption.textProperty().bind(group.textProperty());
        box.getChildren().add(caption);
        for (final NavigationDestination destination : group.getItems()) {
            final DestinationCell cell = new DestinationCell(drawer, destination);
            flyoutCells.add(cell);
            box.getChildren().add(cell);
        }
        box.getStylesheets().add(NavigationDrawer.defaultStylesheet());
        updateSelection(drawer.getSelectionModel().getSelectedItem());
        return box;
    }

    /**
     * Detaches the cell from its group. Called before the cell is thrown away.
     */
    void dispose() {
        childBox.dispose();
        hideFlyout();
        flyout = null;
        group.getItems().removeListener(childListener);
        group.expandedProperty().removeListener(expandedListener);
        visibleProperty().unbind();
        managedProperty().unbind();
        header.dispose();
        disposeChildCells();
        disposeFlyoutCells();
    }

    private void onHeaderActivated() {
        if (collapsed) {
            if (isFlyoutShowing()) {
                hideFlyout();
            } else {
                showFlyout();
            }
        } else {
            group.toggleExpanded();
        }
    }

    private void rebuildChildren() {
        disposeChildCells();
        final List<Node> nodes = new ArrayList<>();
        for (final NavigationDestination destination : group.getItems()) {
            final DestinationCell cell = new DestinationCell(drawer, destination);
            childCells.add(cell);
            nodes.add(cell);
        }
        childBox.getChildren().setAll(nodes);
        // The flyout shows the same destinations and is rebuilt from scratch next time it is needed.
        hideFlyout();
        flyout = null;
        disposeFlyoutCells();
        updateExpansion(false);
        updateSelection(drawer.getSelectionModel().getSelectedItem());
    }

    private void updateExpansion(final boolean animate) {
        final boolean expanded = group.isExpanded();
        final boolean show = expanded && !collapsed;
        // Without a scene there is no pulse to drive a timeline, and a drawer with animations switched off
        // expects every state to be final the moment it is set.
        final boolean animated = animate && drawer.isAnimated() && getScene() != null;
        header.setExpandedState(expanded, animated && !collapsed);
        childBox.setOpen(show, animated, drawer.getTransitionDuration());
    }

    private Popup flyoutPopup() {
        Popup popup = flyout;
        if (popup == null) {
            popup = new Popup();
            popup.setAutoHide(true);
            popup.setAutoFix(true);
            popup.getContent().add(buildFlyoutContent());
            flyout = popup;
        }
        return popup;
    }

    /**
     * A popup has a scene of its own and inherits none of the application's stylesheets, so a dark palette
     * added to the main scene would stop at the flyout's edge.
     *
     * @param scene Scene the cell lives in, never {@code null}.
     */
    private void syncFlyoutStylesheets(final Scene scene) {
        final Popup popup = flyout;
        if (popup == null || popup.getContent().isEmpty()) {
            return;
        }
        if (popup.getContent().get(0) instanceof Parent root) {
            final List<String> stylesheets = new ArrayList<>();
            stylesheets.add(NavigationDrawer.defaultStylesheet());
            stylesheets.addAll(scene.getStylesheets());
            root.getStylesheets().setAll(stylesheets);
        }
    }

    private void disposeChildCells() {
        for (final DestinationCell cell : childCells) {
            cell.dispose();
        }
        childCells.clear();
    }

    private void disposeFlyoutCells() {
        for (final DestinationCell cell : flyoutCells) {
            cell.dispose();
        }
        flyoutCells.clear();
    }


}
