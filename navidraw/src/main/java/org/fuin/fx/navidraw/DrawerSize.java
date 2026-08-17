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

/**
 * How much of a {@link NavigationDrawer} is shown.
 */
public enum DrawerSize {

    /** Full width: icon and label of every item are visible. */
    EXPANDED,

    /**
     * Mini width: only the icons are visible, labels and section headers are hidden and every item shows its
     * text as a tooltip instead.
     */
    COLLAPSED;

    /**
     * Returns the other value.
     *
     * @return {@link #COLLAPSED} for {@link #EXPANDED} and the other way around.
     */
    public DrawerSize toggled() {
        return this == EXPANDED ? COLLAPSED : EXPANDED;
    }

}
