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

import javafx.scene.Node;
import org.kordamp.ikonli.javafx.FontIcon;

/**
 * Builds drawer graphics from Ikonli icon literals.
 * <p>
 * <b>Ikonli is an optional dependency of this library.</b> Nothing else in the package references it, so a
 * consumer that builds its own graphic nodes never needs it. Calling any method here without
 * {@code org.kordamp.ikonli:ikonli-javafx} and an icon pack on the classpath fails with a
 * {@link NoClassDefFoundError}. Add them yourself:
 *
 * <pre>
 * &lt;dependency&gt;
 *     &lt;groupId&gt;org.kordamp.ikonli&lt;/groupId&gt;
 *     &lt;artifactId&gt;ikonli-javafx&lt;/artifactId&gt;
 *     &lt;version&gt;12.4.0&lt;/version&gt;
 * &lt;/dependency&gt;
 * &lt;dependency&gt;
 *     &lt;groupId&gt;org.kordamp.ikonli&lt;/groupId&gt;
 *     &lt;artifactId&gt;ikonli-materialdesign2-pack&lt;/artifactId&gt;
 *     &lt;version&gt;12.4.0&lt;/version&gt;
 * &lt;/dependency&gt;
 * </pre>
 *
 * The size and colour of the returned node are left to the stylesheet, which styles
 * {@code .navigation-item > .icon .ikonli-font-icon}.
 */
public final class IkonliGraphics {

    private IkonliGraphics() {
        throw new UnsupportedOperationException("It is not allowed to create an instance of a utility class");
    }

    /**
     * Creates an icon node from an Ikonli icon literal.
     *
     * @param iconLiteral Literal such as {@code mdi2h-home}, never {@code null}.
     *
     * @return Icon node, never {@code null}.
     *
     * @throws UnsupportedOperationException The literal cannot be resolved, which usually means the icon
     *                                       pack it belongs to is not on the classpath.
     */
    public static Node of(final String iconLiteral) {
        Objects.requireNonNull(iconLiteral, "iconLiteral==null");
        return new FontIcon(iconLiteral);
    }

    /**
     * Creates a destination with an icon built from an Ikonli icon literal.
     *
     * @param id          Identifier that is unique within the drawer, never {@code null}.
     * @param text        Label, never {@code null}.
     * @param iconLiteral Literal such as {@code mdi2h-home}, never {@code null}.
     *
     * @return Destination, never {@code null}.
     *
     * @throws UnsupportedOperationException The literal cannot be resolved, which usually means the icon
     *                                       pack it belongs to is not on the classpath.
     */
    public static NavigationDestination destination(final String id, final String text,
            final String iconLiteral) {
        return new NavigationDestination(id, text, of(iconLiteral));
    }

}
