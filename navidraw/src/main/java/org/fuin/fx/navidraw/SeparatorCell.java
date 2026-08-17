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

import javafx.scene.layout.Region;

/**
 * The node a {@link NavigationSeparator} is drawn with. Height and colour come from the stylesheet.
 */
final class SeparatorCell extends Region {

    SeparatorCell(final NavigationSeparator separator) {
        super();
        getStyleClass().setAll("navigation-separator");
        setMaxWidth(Double.MAX_VALUE);
        visibleProperty().bind(separator.visibleProperty());
        managedProperty().bind(visibleProperty());
    }

    /**
     * Detaches the cell from its item. Called before the cell is thrown away.
     */
    void dispose() {
        visibleProperty().unbind();
        managedProperty().unbind();
    }

}
