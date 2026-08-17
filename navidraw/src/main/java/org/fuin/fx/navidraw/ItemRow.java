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

import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.css.PseudoClass;
import javafx.geometry.Pos;
import javafx.scene.AccessibleRole;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import org.jspecify.annotations.Nullable;

/**
 * The clickable row shared by a destination and a group header: icon, label, and whatever the subclass puts
 * at the trailing edge.
 * <p>
 * Deliberately an {@link HBox} rather than a {@code Button}: the row has to place an icon, a label that
 * disappears in the mini size and a trailing node pushed to the far edge, and a button lays its graphic out
 * at the graphic's own width. Everything a button contributes is added back here - focus traversal, the
 * {@code ENTER}/{@code SPACE} keys and the accessible role.
 */
abstract class ItemRow extends HBox {

    private static final PseudoClass SELECTED_PSEUDO_CLASS = PseudoClass.getPseudoClass("selected");

    private final StackPane iconHolder = new StackPane();

    private final Label label = new Label();

    private final Region spacer = new Region();

    private final Tooltip tooltip = new Tooltip();

    private final BooleanProperty collapsed = new SimpleBooleanProperty(this, "collapsed", false);

    private final NavigationLabeledItem item;

    private final ChangeListener<@Nullable Node> graphicListener;

    private boolean tooltipInstalled;

    ItemRow(final NavigationLabeledItem item) {
        super();
        this.item = item;

        getStyleClass().setAll("navigation-item");
        setAlignment(Pos.CENTER_LEFT);
        setMaxWidth(Double.MAX_VALUE);
        setFocusTraversable(true);
        setAccessibleRole(AccessibleRole.BUTTON);

        iconHolder.getStyleClass().setAll("icon");
        label.getStyleClass().setAll("label");
        HBox.setHgrow(spacer, Priority.ALWAYS);
        getChildren().addAll(iconHolder, label, spacer);

        label.textProperty().bind(item.textProperty());
        label.visibleProperty().bind(collapsed.not());
        label.managedProperty().bind(label.visibleProperty());

        accessibleTextProperty().bind(item.textProperty());
        disableProperty().bind(item.disableProperty());
        visibleProperty().bind(item.visibleProperty());
        managedProperty().bind(visibleProperty());

        // An empty tooltip text falls back to the label, so a collapsed drawer is readable without the
        // application setting anything.
        tooltip.textProperty().bind(Bindings.when(item.tooltipTextProperty().isEmpty())
                .then(item.textProperty())
                .otherwise(item.tooltipTextProperty()));

        collapsed.addListener((obs, old, value) -> updateForSize(value));
        graphicListener = (obs, old, value) -> updateGraphic(value);
        item.graphicProperty().addListener(graphicListener);
        updateGraphic(item.getGraphic());
        updateForSize(collapsed.get());

        setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.PRIMARY) {
                activateIfEnabled();
            }
        });
        addEventHandler(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.ENTER || event.getCode() == KeyCode.SPACE) {
                activateIfEnabled();
                event.consume();
            }
        });
    }

    /**
     * Called when the row was clicked or activated with the keyboard, and only when it is enabled.
     */
    protected abstract void activate();

    /**
     * Adds a node at the trailing edge of the row, after the spacer.
     *
     * @param node Node to add, never {@code null}.
     */
    protected final void addTrailing(final Node node) {
        getChildren().add(node);
    }

    /**
     * Marks the row as the selected one, which shows up as the {@code :selected} pseudo class.
     *
     * @param value {@code true} if this row is the selected one.
     */
    final void setSelectedState(final boolean value) {
        pseudoClassStateChanged(SELECTED_PSEUDO_CLASS, value);
    }

    /**
     * Switches the row between the full and the mini presentation.
     *
     * @param value {@code true} to hide the label and show a tooltip instead.
     */
    void setCollapsed(final boolean value) {
        collapsed.set(value);
    }

    /**
     * Returns whether the row is in the mini presentation.
     *
     * @return {@code true} if the label is hidden.
     */
    final boolean isCollapsed() {
        return collapsed.get();
    }

    /**
     * Whether the row is in the mini presentation, for subclasses that bind to it.
     *
     * @return Property, never {@code null}.
     */
    protected final BooleanProperty collapsedProperty() {
        return collapsed;
    }

    /**
     * Returns the label node, for tests.
     *
     * @return Label, never {@code null}.
     */
    final Label getLabel() {
        return label;
    }

    /**
     * Returns the tooltip, whether it is currently installed or not.
     *
     * @return Tooltip, never {@code null}.
     */
    final Tooltip getCellTooltip() {
        return tooltip;
    }

    /**
     * Returns whether the tooltip is currently attached to the row. There is no getter for the tooltip of a
     * plain node, so the row keeps track of it itself.
     *
     * @return {@code true} while the drawer is collapsed.
     */
    final boolean isTooltipInstalled() {
        return tooltipInstalled;
    }

    /**
     * Detaches the row from its item. Called before the row is thrown away, so that a rebuilt drawer does
     * not leave listeners of dead nodes behind on a model that outlives them.
     */
    void dispose() {
        label.textProperty().unbind();
        label.visibleProperty().unbind();
        label.managedProperty().unbind();
        accessibleTextProperty().unbind();
        disableProperty().unbind();
        visibleProperty().unbind();
        managedProperty().unbind();
        tooltip.textProperty().unbind();
        item.graphicProperty().removeListener(graphicListener);
        if (tooltipInstalled) {
            Tooltip.uninstall(this, tooltip);
            tooltipInstalled = false;
        }
    }

    private void activateIfEnabled() {
        if (isDisabled()) {
            return;
        }
        requestFocus();
        activate();
    }

    private void updateGraphic(final @Nullable Node graphic) {
        if (graphic == null) {
            iconHolder.getChildren().clear();
        } else {
            iconHolder.getChildren().setAll(graphic);
        }
    }

    private void updateForSize(final boolean value) {
        setAlignment(value ? Pos.CENTER : Pos.CENTER_LEFT);
        if (value == tooltipInstalled) {
            return;
        }
        if (value) {
            Tooltip.install(this, tooltip);
        } else {
            Tooltip.uninstall(this, tooltip);
        }
        tooltipInstalled = value;
    }

}
