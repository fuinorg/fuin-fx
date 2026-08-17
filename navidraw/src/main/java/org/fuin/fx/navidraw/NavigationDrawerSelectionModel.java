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

import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.control.SingleSelectionModel;
import org.jspecify.annotations.Nullable;

/**
 * Tracks the one destination that is currently shown, over the drawer's destinations in the order they
 * appear - the main items first, the footer items after them.
 * <p>
 * The model follows its item: when destinations are inserted or removed the selected index is corrected so
 * that the same object stays selected, and the selection is cleared when that object is removed.
 */
public class NavigationDrawerSelectionModel extends SingleSelectionModel<NavigationDestination> {

    private final ObservableList<NavigationDestination> destinations;

    /**
     * Constructor with the list to select from.
     *
     * @param destinations Live list of destinations, never {@code null}. The model registers a listener on
     *                     it and follows every change.
     */
    public NavigationDrawerSelectionModel(final ObservableList<NavigationDestination> destinations) {
        super();
        this.destinations = Objects.requireNonNull(destinations, "destinations==null");
        this.destinations.addListener((ListChangeListener<NavigationDestination>) change -> onDestinationsChanged());
    }

    @Override
    protected @Nullable NavigationDestination getModelItem(final int index) {
        if (index < 0 || index >= destinations.size()) {
            return null;
        }
        return destinations.get(index);
    }

    @Override
    protected int getItemCount() {
        return destinations.size();
    }

    /**
     * Selects the destination with the given identifier, or clears the selection when there is no such
     * destination.
     *
     * @param id Identifier of a {@link NavigationDestination}, never {@code null}.
     *
     * @return {@code true} if a destination was found and selected.
     */
    public boolean selectById(final String id) {
        Objects.requireNonNull(id, "id==null");
        for (int i = 0; i < destinations.size(); i++) {
            if (destinations.get(i).getId().equals(id)) {
                select(i);
                return true;
            }
        }
        clearSelection();
        return false;
    }

    private void onDestinationsChanged() {
        final NavigationDestination selected = getSelectedItem();
        if (selected == null) {
            return;
        }
        final int index = destinations.indexOf(selected);
        if (index < 0) {
            clearSelection();
        } else if (index != getSelectedIndex()) {
            select(index);
        }
    }

}
