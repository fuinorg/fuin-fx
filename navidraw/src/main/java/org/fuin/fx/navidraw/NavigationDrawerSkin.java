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

import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.InvalidationListener;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.collections.ListChangeListener;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.control.SkinBase;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.jspecify.annotations.Nullable;

/**
 * Draws a {@link NavigationDrawer}: an optional header at the top, the scrollable items in the middle and
 * the footer items pinned to the bottom.
 * <p>
 * The width is the skin's business. Rather than writing the drawer's own {@code prefWidth} property - which
 * would take that property away from the application - the skin reports the current width from
 * {@code computeMinWidth}, {@code computePrefWidth} and {@code computeMaxWidth} and requests a layout pass
 * whenever it changes. A size change animates that one value, so the parent re-lays out on every frame and
 * the content beside the drawer follows along.
 */
final class NavigationDrawerSkin extends SkinBase<NavigationDrawer> {

    private final BorderPane root = new BorderPane();

    private final StackPane headerBox = new StackPane();

    private final VBox itemBox = new VBox();

    private final VBox footerBox = new VBox();

    private final ScrollPane scrollPane = new ScrollPane();

    private final List<DestinationCell> destinationCells = new ArrayList<>();

    private final List<GroupCell> groupCells = new ArrayList<>();

    private final List<SectionCell> sectionCells = new ArrayList<>();

    private final List<SeparatorCell> separatorCells = new ArrayList<>();

    private final DoubleProperty currentWidth = new SimpleDoubleProperty(this, "currentWidth");

    private final ListChangeListener<NavigationItem> itemsListener = change -> rebuildCells();

    private final InvalidationListener sizeListener = observable -> onSizeChanged();

    private final InvalidationListener widthListener = observable -> updateWidth(getSkinnable().isAnimated());

    private final InvalidationListener headerListener = observable -> updateHeader();

    private final InvalidationListener selectionListener = observable -> updateSelection();

    private final InvalidationListener currentWidthListener = observable -> getSkinnable().requestLayout();

    private @Nullable Timeline transition;

    NavigationDrawerSkin(final NavigationDrawer control) {
        super(control);

        headerBox.getStyleClass().setAll("header");
        itemBox.getStyleClass().setAll("items");
        footerBox.getStyleClass().setAll("footer");

        scrollPane.getStyleClass().add("items-scroll");
        scrollPane.setContent(itemBox);
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollBarPolicy.AS_NEEDED);
        scrollPane.setMinWidth(0);

        root.getStyleClass().add("drawer-body");
        root.setTop(headerBox);
        root.setCenter(scrollPane);
        root.setBottom(footerBox);
        root.setMinWidth(0);
        getChildren().add(root);

        currentWidth.set(control.getTargetWidth());
        currentWidth.addListener(currentWidthListener);

        control.getItems().addListener(itemsListener);
        control.getFooterItems().addListener(itemsListener);
        control.sizeProperty().addListener(sizeListener);
        control.expandedWidthProperty().addListener(widthListener);
        control.collapsedWidthProperty().addListener(widthListener);
        control.headerProperty().addListener(headerListener);
        control.getSelectionModel().selectedItemProperty().addListener(selectionListener);

        rebuildCells();
        updateHeader();
    }

    @Override
    protected double computeMinWidth(final double height, final double topInset, final double rightInset,
            final double bottomInset, final double leftInset) {
        return currentWidth.get();
    }

    @Override
    protected double computePrefWidth(final double height, final double topInset, final double rightInset,
            final double bottomInset, final double leftInset) {
        return currentWidth.get();
    }

    @Override
    protected double computeMaxWidth(final double height, final double topInset, final double rightInset,
            final double bottomInset, final double leftInset) {
        return currentWidth.get();
    }

    @Override
    public void dispose() {
        final NavigationDrawer control = getSkinnable();
        if (control != null) {
            control.getItems().removeListener(itemsListener);
            control.getFooterItems().removeListener(itemsListener);
            control.sizeProperty().removeListener(sizeListener);
            control.expandedWidthProperty().removeListener(widthListener);
            control.collapsedWidthProperty().removeListener(widthListener);
            control.headerProperty().removeListener(headerListener);
            control.getSelectionModel().selectedItemProperty().removeListener(selectionListener);
        }
        currentWidth.removeListener(currentWidthListener);
        stopTransition();
        disposeCells();
        super.dispose();
    }

    /**
     * Returns the cells of the destinations that sit directly in the drawer, main list first. Children of a
     * group are reached through {@link #getGroupCells()}.
     *
     * @return Unmodifiable list, never {@code null}.
     */
    List<DestinationCell> getDestinationCells() {
        return List.copyOf(destinationCells);
    }

    /**
     * Returns the cells of the groups, main list first.
     *
     * @return Unmodifiable list, never {@code null}.
     */
    List<GroupCell> getGroupCells() {
        return List.copyOf(groupCells);
    }

    /**
     * Returns the cells of the sections, main list first.
     *
     * @return Unmodifiable list, never {@code null}.
     */
    List<SectionCell> getSectionCells() {
        return List.copyOf(sectionCells);
    }

    /**
     * Returns the width the drawer currently reports, which is somewhere between the collapsed and the
     * expanded width while a transition is running.
     *
     * @return Width in pixels.
     */
    double getCurrentWidth() {
        return currentWidth.get();
    }

    private void rebuildCells() {
        disposeCells();
        itemBox.getChildren().setAll(createCells(getSkinnable().getItems()));
        footerBox.getChildren().setAll(createCells(getSkinnable().getFooterItems()));
        applySizeToCells();
        updateSelection();
    }

    private List<Node> createCells(final List<NavigationItem> source) {
        final List<Node> nodes = new ArrayList<>();
        for (final NavigationItem item : source) {
            switch (item) {
                case NavigationDestination destination -> {
                    final DestinationCell cell = new DestinationCell(getSkinnable(), destination);
                    destinationCells.add(cell);
                    nodes.add(cell);
                }
                case NavigationGroup group -> {
                    final GroupCell cell = new GroupCell(getSkinnable(), group);
                    groupCells.add(cell);
                    nodes.add(cell);
                }
                case NavigationSection section -> {
                    final SectionCell cell = new SectionCell(getSkinnable(), section);
                    sectionCells.add(cell);
                    nodes.add(cell);
                }
                case NavigationSeparator separator -> {
                    final SeparatorCell cell = new SeparatorCell(separator);
                    separatorCells.add(cell);
                    nodes.add(cell);
                }
            }
        }
        return nodes;
    }

    private void disposeCells() {
        for (final DestinationCell cell : destinationCells) {
            cell.dispose();
        }
        for (final GroupCell cell : groupCells) {
            cell.dispose();
        }
        for (final SectionCell cell : sectionCells) {
            cell.dispose();
        }
        for (final SeparatorCell cell : separatorCells) {
            cell.dispose();
        }
        destinationCells.clear();
        groupCells.clear();
        sectionCells.clear();
        separatorCells.clear();
    }

    private void onSizeChanged() {
        applySizeToCells();
        updateWidth(getSkinnable().isAnimated());
    }

    private void applySizeToCells() {
        final boolean collapsed = getSkinnable().getSize() == DrawerSize.COLLAPSED;
        for (final DestinationCell cell : destinationCells) {
            cell.setCollapsed(collapsed);
        }
        for (final GroupCell cell : groupCells) {
            cell.setCollapsed(collapsed);
        }
        for (final SectionCell cell : sectionCells) {
            cell.setCollapsed(collapsed);
        }
    }

    private void updateSelection() {
        final NavigationDestination selected = getSkinnable().getSelectionModel().getSelectedItem();
        for (final DestinationCell cell : destinationCells) {
            cell.setSelectedState(cell.getDestination() == selected);
        }
        for (final GroupCell cell : groupCells) {
            cell.updateSelection(selected);
        }
        for (final SectionCell cell : sectionCells) {
            cell.updateSelection(selected);
        }
    }

    private void updateHeader() {
        final Node node = getSkinnable().getHeader();
        if (node == null) {
            headerBox.getChildren().clear();
        } else {
            headerBox.getChildren().setAll(node);
        }
        headerBox.setVisible(node != null);
        headerBox.setManaged(node != null);
    }

    private void updateWidth(final boolean animate) {
        final NavigationDrawer control = getSkinnable();
        final double target = control.getTargetWidth();
        stopTransition();
        // Without a scene there is no pulse to drive the timeline, so the value would stay at its start.
        if (!animate || control.getScene() == null) {
            currentWidth.set(target);
            return;
        }
        final Timeline timeline = new Timeline(new KeyFrame(control.getTransitionDuration(),
                new KeyValue(currentWidth, target, Interpolator.EASE_BOTH)));
        timeline.setOnFinished(event -> transition = null);
        transition = timeline;
        timeline.play();
    }

    private void stopTransition() {
        final Timeline running = transition;
        if (running != null) {
            running.stop();
            transition = null;
        }
    }

}
