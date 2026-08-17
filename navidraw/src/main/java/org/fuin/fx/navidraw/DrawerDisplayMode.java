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
 * How a {@link NavigationDrawer} is placed inside a {@link NavigationDrawerPane}.
 */
public enum DrawerDisplayMode {

    /** The drawer takes space of its own and the content is laid out beside it. */
    PERSISTENT,

    /** The drawer floats above the content, dimming it, and can be dismissed. */
    MODAL,

    /**
     * {@link #PERSISTENT} while the pane is at least
     * {@link NavigationDrawerPane#modalBreakpointProperty() modalBreakpoint} wide, {@link #MODAL} below that.
     * This is the default and never the <em>effective</em> mode - see
     * {@link NavigationDrawerPane#effectiveDisplayModeProperty()}.
     */
    AUTO

}
