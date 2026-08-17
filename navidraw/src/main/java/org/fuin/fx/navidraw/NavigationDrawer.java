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
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import javafx.beans.InvalidationListener;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.css.CssMetaData;
import javafx.css.PseudoClass;
import javafx.css.SimpleStyleableDoubleProperty;
import javafx.css.Styleable;
import javafx.css.StyleableDoubleProperty;
import javafx.css.StyleableProperty;
import javafx.css.converter.SizeConverter;
import javafx.scene.Node;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;
import javafx.util.Duration;
import org.jspecify.annotations.Nullable;

/**
 * A panel holding an application's top-level destinations.
 * <p>
 * The control is usable on its own - put it into a {@code BorderPane}'s left slot and it behaves as a
 * permanent side panel. To get the responsive behaviour (a panel on wide windows, an overlay on narrow ones)
 * put it into a {@link NavigationDrawerPane} instead.
 * <p>
 * Two independent states describe how the drawer looks. {@link #sizeProperty()} switches between the full
 * width and the mini presentation, and is owned by the drawer itself. Whether the drawer floats above the
 * content is not its business at all - that is the enclosing {@link NavigationDrawerPane}.
 * <p>
 * Items nest at most two levels deep: a {@link NavigationSection} holds destinations and
 * {@link NavigationGroup}s, and a group holds destinations. Whatever the nesting,
 * {@link #getDestinations()} and the selection model see one flat list in the order the destinations appear
 * on screen.
 * <p>
 * The default stylesheet is returned by {@link #getUserAgentStylesheet()}, so the drawer is styled without
 * the application adding anything. Restyle it by overriding the looked-up colours documented in
 * {@code navidraw.css}, or add {@code navidraw-dark.css} to the scene for the dark palette.
 * <p>
 * Instances must be created and used on the JavaFX application thread, like every other JavaFX control.
 */
public class NavigationDrawer extends Control {

    private static final String DEFAULT_STYLE_CLASS = "navigation-drawer";

    private static final String STYLESHEET = "navidraw.css";

    private static final String DARK_STYLESHEET = "navidraw-dark.css";

    private static final PseudoClass EXPANDED_PSEUDO_CLASS = PseudoClass.getPseudoClass("expanded");

    private static final PseudoClass COLLAPSED_PSEUDO_CLASS = PseudoClass.getPseudoClass("collapsed");

    private static final double DEFAULT_EXPANDED_WIDTH = 280;

    private static final double DEFAULT_COLLAPSED_WIDTH = 64;

    private static final CssMetaData<NavigationDrawer, Number> EXPANDED_WIDTH_META =
            new CssMetaData<>("-fx-expanded-width", SizeConverter.getInstance(), DEFAULT_EXPANDED_WIDTH) {
                @Override
                public boolean isSettable(final NavigationDrawer drawer) {
                    return !drawer.expandedWidth.isBound();
                }

                @Override
                public StyleableProperty<Number> getStyleableProperty(final NavigationDrawer drawer) {
                    return drawer.expandedWidth;
                }
            };

    private static final CssMetaData<NavigationDrawer, Number> COLLAPSED_WIDTH_META =
            new CssMetaData<>("-fx-collapsed-width", SizeConverter.getInstance(), DEFAULT_COLLAPSED_WIDTH) {
                @Override
                public boolean isSettable(final NavigationDrawer drawer) {
                    return !drawer.collapsedWidth.isBound();
                }

                @Override
                public StyleableProperty<Number> getStyleableProperty(final NavigationDrawer drawer) {
                    return drawer.collapsedWidth;
                }
            };

    private static final List<CssMetaData<? extends Styleable, ?>> CSS_META_DATA;

    static {
        final List<CssMetaData<? extends Styleable, ?>> list = new ArrayList<>(Control.getClassCssMetaData());
        list.add(EXPANDED_WIDTH_META);
        list.add(COLLAPSED_WIDTH_META);
        CSS_META_DATA = Collections.unmodifiableList(list);
    }

    private final ObservableList<NavigationItem> items = FXCollections.observableArrayList();

    private final ObservableList<NavigationItem> footerItems = FXCollections.observableArrayList();

    /** The list the walk rebuilds. Only {@link #destinations} is handed out. */
    private final ObservableList<NavigationDestination> modifiableDestinations =
            FXCollections.observableArrayList();

    /** Groups currently in the drawer, so their children can be watched and revealed. */
    private final List<NavigationGroup> observedGroups = new ArrayList<>();

    /** Sections currently in the drawer, for the same reason. */
    private final List<NavigationSection> observedSections = new ArrayList<>();

    /** Every item the last walk touched, so that showing or hiding any of them redoes the walk. */
    private final List<NavigationItem> observedItems = new ArrayList<>();

    private final InvalidationListener visibilityListener = observable -> rebuildDestinations();

    private final ListChangeListener<NavigationDestination> groupItemsListener =
            change -> rebuildDestinations();

    private final ListChangeListener<NavigationLabeledItem> sectionItemsListener =
            change -> rebuildDestinations();

    private final ObservableList<NavigationDestination> destinations =
            FXCollections.unmodifiableObservableList(modifiableDestinations);

    private final NavigationDrawerSelectionModel selectionModel =
            new NavigationDrawerSelectionModel(modifiableDestinations);

    private final ObjectProperty<@Nullable Node> header = new SimpleObjectProperty<>(this, "header", null);

    private final ObjectProperty<DrawerSize> size =
            new SimpleObjectProperty<>(this, "size", DrawerSize.EXPANDED) {
                @Override
                protected void invalidated() {
                    updateSizePseudoClasses();
                }
            };

    private final StyleableDoubleProperty expandedWidth =
            new SimpleStyleableDoubleProperty(EXPANDED_WIDTH_META, this, "expandedWidth", DEFAULT_EXPANDED_WIDTH);

    private final StyleableDoubleProperty collapsedWidth =
            new SimpleStyleableDoubleProperty(COLLAPSED_WIDTH_META, this, "collapsedWidth", DEFAULT_COLLAPSED_WIDTH);

    private final BooleanProperty animated = new SimpleBooleanProperty(this, "animated", true);

    private final ObjectProperty<Duration> transitionDuration =
            new SimpleObjectProperty<>(this, "transitionDuration", Duration.millis(200));

    /**
     * Default constructor creating an empty drawer.
     */
    public NavigationDrawer() {
        super();
        getStyleClass().setAll(DEFAULT_STYLE_CLASS);
        items.addListener((ListChangeListener<NavigationItem>) change -> rebuildDestinations());
        footerItems.addListener((ListChangeListener<NavigationItem>) change -> rebuildDestinations());
        selectionModel.selectedItemProperty().addListener((obs, old, selected) -> revealSelected(selected));
        updateSizePseudoClasses();
    }

    /**
     * The items shown from the top of the drawer downwards.
     *
     * @return Modifiable live list, never {@code null}.
     */
    public final ObservableList<NavigationItem> getItems() {
        return items;
    }

    /**
     * The items pinned to the bottom of the drawer - typically settings, help and sign out. They stay in
     * place while the main list scrolls.
     *
     * @return Modifiable live list, never {@code null}.
     */
    public final ObservableList<NavigationItem> getFooterItems() {
        return footerItems;
    }

    /**
     * Every reachable {@link NavigationDestination} of {@link #getItems()} followed by every one of
     * {@link #getFooterItems()}, in the order they appear. This is what the selection model works on.
     * <p>
     * Reachable means visible: a destination that is hidden, or that sits inside a hidden group or a hidden
     * section, is not in this list and cannot be selected. Hiding the selected destination therefore clears
     * the selection. Use {@link #findDestination(String)} to get hold of a hidden one, for example to show
     * it again.
     *
     * @return Unmodifiable live list, never {@code null}.
     */
    public final ObservableList<NavigationDestination> getDestinations() {
        return destinations;
    }

    /**
     * Returns the destination with the given identifier, wherever it is nested and whether it is currently
     * visible or not.
     *
     * @param id Identifier to look for, never {@code null}.
     *
     * @return Destination or {@code null} if there is no such destination.
     */
    public final @Nullable NavigationDestination findDestination(final String id) {
        Objects.requireNonNull(id, "id==null");
        final NavigationDestination found = findInItems(items, id);
        return found == null ? findInItems(footerItems, id) : found;
    }

    private static @Nullable NavigationDestination findInItems(final List<NavigationItem> source,
            final String id) {
        for (final NavigationItem item : source) {
            switch (item) {
                case NavigationDestination destination -> {
                    if (destination.getId().equals(id)) {
                        return destination;
                    }
                }
                case NavigationGroup group -> {
                    final NavigationDestination found = findInDestinations(group.getItems(), id);
                    if (found != null) {
                        return found;
                    }
                }
                case NavigationSection section -> {
                    final NavigationDestination found = findInSection(section, id);
                    if (found != null) {
                        return found;
                    }
                }
                case NavigationSeparator _ -> {
                    // Nothing to find
                }
            }
        }
        return null;
    }

    private static @Nullable NavigationDestination findInSection(final NavigationSection section,
            final String id) {
        for (final NavigationLabeledItem child : section.getItems()) {
            if (child instanceof NavigationDestination destination && destination.getId().equals(id)) {
                return destination;
            }
            if (child instanceof NavigationGroup group) {
                final NavigationDestination found = findInDestinations(group.getItems(), id);
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }

    private static @Nullable NavigationDestination findInDestinations(
            final List<NavigationDestination> source, final String id) {
        for (final NavigationDestination destination : source) {
            if (destination.getId().equals(id)) {
                return destination;
            }
        }
        return null;
    }

    /**
     * Tracks the destination that is currently shown.
     *
     * @return Selection model, never {@code null}.
     */
    public final NavigationDrawerSelectionModel getSelectionModel() {
        return selectionModel;
    }

    /**
     * Node shown above the items, for example a product name or a user tile. It is laid out at the drawer's
     * width, so it has to cope with the mini width as well.
     *
     * @return Property, defaults to {@code null}.
     */
    public final ObjectProperty<@Nullable Node> headerProperty() {
        return header;
    }

    /**
     * Returns the header node.
     *
     * @return Header node or {@code null}.
     */
    public final @Nullable Node getHeader() {
        return header.get();
    }

    /**
     * Sets the header node.
     *
     * @param value Header node or {@code null} for none.
     */
    public final void setHeader(final @Nullable Node value) {
        header.set(value);
    }

    /**
     * Whether the drawer shows labels ({@link DrawerSize#EXPANDED}) or only icons
     * ({@link DrawerSize#COLLAPSED}). Reflected as the {@code :expanded} and {@code :collapsed} pseudo
     * classes.
     *
     * @return Property, defaults to {@link DrawerSize#EXPANDED}.
     */
    public final ObjectProperty<DrawerSize> sizeProperty() {
        return size;
    }

    /**
     * Returns the current size.
     *
     * @return Size, never {@code null}.
     */
    public final DrawerSize getSize() {
        return size.get();
    }

    /**
     * Sets the current size.
     *
     * @param value Size, never {@code null}.
     */
    public final void setSize(final DrawerSize value) {
        size.set(Objects.requireNonNull(value, "value==null"));
    }

    /**
     * Switches between {@link DrawerSize#EXPANDED} and {@link DrawerSize#COLLAPSED}.
     */
    public final void toggleSize() {
        setSize(getSize().toggled());
    }

    /**
     * Width of the drawer while {@link DrawerSize#EXPANDED}. Styleable as {@code -fx-expanded-width}.
     *
     * @return Property, defaults to 280.
     */
    public final StyleableDoubleProperty expandedWidthProperty() {
        return expandedWidth;
    }

    /**
     * Returns the expanded width.
     *
     * @return Width in pixels.
     */
    public final double getExpandedWidth() {
        return expandedWidth.get();
    }

    /**
     * Sets the expanded width.
     *
     * @param value Width in pixels.
     */
    public final void setExpandedWidth(final double value) {
        expandedWidth.set(value);
    }

    /**
     * Width of the drawer while {@link DrawerSize#COLLAPSED}. Styleable as {@code -fx-collapsed-width}.
     *
     * @return Property, defaults to 64.
     */
    public final StyleableDoubleProperty collapsedWidthProperty() {
        return collapsedWidth;
    }

    /**
     * Returns the collapsed width.
     *
     * @return Width in pixels.
     */
    public final double getCollapsedWidth() {
        return collapsedWidth.get();
    }

    /**
     * Sets the collapsed width.
     *
     * @param value Width in pixels.
     */
    public final void setCollapsedWidth(final double value) {
        collapsedWidth.set(value);
    }

    /**
     * Whether a size change is animated. Switch it off in tests so that the width is the final one as soon
     * as the size changes.
     *
     * @return Property, defaults to {@code true}.
     */
    public final BooleanProperty animatedProperty() {
        return animated;
    }

    /**
     * Returns whether size changes are animated.
     *
     * @return {@code true} if animated.
     */
    public final boolean isAnimated() {
        return animated.get();
    }

    /**
     * Sets whether size changes are animated.
     *
     * @param value {@code true} to animate.
     */
    public final void setAnimated(final boolean value) {
        animated.set(value);
    }

    /**
     * How long a size change takes while {@link #animatedProperty()} is set.
     *
     * @return Property, defaults to 200 milliseconds.
     */
    public final ObjectProperty<Duration> transitionDurationProperty() {
        return transitionDuration;
    }

    /**
     * Returns the duration of a size change.
     *
     * @return Duration, never {@code null}.
     */
    public final Duration getTransitionDuration() {
        return transitionDuration.get();
    }

    /**
     * Sets the duration of a size change.
     *
     * @param value Duration, never {@code null}.
     */
    public final void setTransitionDuration(final Duration value) {
        transitionDuration.set(Objects.requireNonNull(value, "value==null"));
    }

    /**
     * Returns the width the drawer has in its current size.
     *
     * @return Expanded or collapsed width, in pixels.
     */
    public final double getTargetWidth() {
        return getSize() == DrawerSize.EXPANDED ? getExpandedWidth() : getCollapsedWidth();
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new NavigationDrawerSkin(this);
    }

    @Override
    public String getUserAgentStylesheet() {
        return defaultStylesheet();
    }

    /**
     * Returns the stylesheet the drawer uses unless the application says otherwise.
     * <p>
     * Every control of this package applies it to its own subtree already, so there is normally nothing to
     * do. It is public for the case where the application styles nodes of its own to match - a tool bar
     * around the {@link DrawerToggleButton}, for example.
     *
     * @return External form of the stylesheet URL, never {@code null}.
     */
    public static String defaultStylesheet() {
        return resource(STYLESHEET);
    }

    /**
     * Returns the dark palette. Add it to the scene to switch the drawer, the scrim and the toggle button
     * over at once - it redefines the looked-up colours and nothing else.
     *
     * <pre>
     * scene.getStylesheets().add(NavigationDrawer.darkStylesheet());
     * </pre>
     *
     * @return External form of the stylesheet URL, never {@code null}.
     */
    public static String darkStylesheet() {
        return resource(DARK_STYLESHEET);
    }

    private static String resource(final String name) {
        return Objects.requireNonNull(NavigationDrawer.class.getResource(name),
                "Stylesheet not found: " + name).toExternalForm();
    }

    @Override
    public List<CssMetaData<? extends Styleable, ?>> getControlCssMetaData() {
        return getClassCssMetaData();
    }

    /**
     * Returns the CSS properties this control adds to those of {@link Control}.
     *
     * @return Unmodifiable list, never {@code null}.
     */
    public static List<CssMetaData<? extends Styleable, ?>> getClassCssMetaData() {
        return CSS_META_DATA;
    }

    private void updateSizePseudoClasses() {
        final DrawerSize current = getSize();
        pseudoClassStateChanged(EXPANDED_PSEUDO_CLASS, current == DrawerSize.EXPANDED);
        pseudoClassStateChanged(COLLAPSED_PSEUDO_CLASS, current == DrawerSize.COLLAPSED);
    }

    private void rebuildDestinations() {
        for (final NavigationGroup group : observedGroups) {
            group.getItems().removeListener(groupItemsListener);
        }
        for (final NavigationSection section : observedSections) {
            section.getItems().removeListener(sectionItemsListener);
        }
        for (final NavigationItem item : observedItems) {
            item.visibleProperty().removeListener(visibilityListener);
        }
        observedGroups.clear();
        observedSections.clear();
        observedItems.clear();
        final List<NavigationDestination> found = new ArrayList<>();
        collectDestinations(items, found);
        collectDestinations(footerItems, found);
        modifiableDestinations.setAll(found);
    }

    /**
     * Collects the reachable destinations in the order they are shown: a group or a section contributes its
     * children where it sits itself, so the selection index and the visual order stay the same thing.
     * <p>
     * A hidden item contributes nothing and is not descended into - a destination inside a hidden section is
     * as unreachable as a hidden destination. Every item that is walked past gets a listener on its
     * visibility, so showing or hiding anything redoes the walk.
     *
     * @param source Items to walk, never {@code null}.
     * @param target List to fill, never {@code null}.
     */
    private void collectDestinations(final List<NavigationItem> source,
            final List<NavigationDestination> target) {
        for (final NavigationItem item : source) {
            if (!observeAndIsVisible(item)) {
                continue;
            }
            switch (item) {
                case NavigationDestination destination -> target.add(destination);
                case NavigationGroup group -> collectGroup(group, target);
                case NavigationSection section -> collectSection(section, target);
                case NavigationSeparator _ -> {
                    // Nothing to select
                }
            }
        }
    }

    private void collectSection(final NavigationSection section,
            final List<NavigationDestination> target) {
        section.getItems().addListener(sectionItemsListener);
        observedSections.add(section);
        for (final NavigationLabeledItem child : section.getItems()) {
            if (!observeAndIsVisible(child)) {
                continue;
            }
            if (child instanceof NavigationDestination destination) {
                target.add(destination);
            } else if (child instanceof NavigationGroup group) {
                collectGroup(group, target);
            }
        }
    }

    private void collectGroup(final NavigationGroup group, final List<NavigationDestination> target) {
        group.getItems().addListener(groupItemsListener);
        observedGroups.add(group);
        for (final NavigationDestination child : group.getItems()) {
            if (observeAndIsVisible(child)) {
                target.add(child);
            }
        }
    }

    private boolean observeAndIsVisible(final NavigationItem item) {
        item.visibleProperty().addListener(visibilityListener);
        observedItems.add(item);
        return item.isVisible();
    }

    /**
     * Opens whatever a newly selected destination is folded inside - its group, its section, or both. A
     * selection nobody can see is worse than something that opened by itself.
     *
     * @param selected Newly selected destination or {@code null}.
     */
    private void revealSelected(final @Nullable NavigationDestination selected) {
        if (selected == null) {
            return;
        }
        for (final NavigationGroup group : observedGroups) {
            if (group.getItems().contains(selected)) {
                group.setExpanded(true);
            }
        }
        for (final NavigationSection section : observedSections) {
            if (contains(section, selected)) {
                section.setExpanded(true);
            }
        }
    }

    private static boolean contains(final NavigationSection section,
            final NavigationDestination destination) {
        for (final NavigationLabeledItem item : section.getItems()) {
            if (item == destination) {
                return true;
            }
            if (item instanceof NavigationGroup group && group.getItems().contains(destination)) {
                return true;
            }
        }
        return false;
    }

}
