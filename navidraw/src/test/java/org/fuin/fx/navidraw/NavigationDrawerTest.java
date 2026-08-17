package org.fuin.fx.navidraw;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import javafx.css.PseudoClass;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link NavigationDrawer}.
 */
class NavigationDrawerTest extends AbstractFxTest {

    private static final PseudoClass EXPANDED = PseudoClass.getPseudoClass("expanded");

    private static final PseudoClass COLLAPSED = PseudoClass.getPseudoClass("collapsed");

    private static final PseudoClass SELECTED = PseudoClass.getPseudoClass("selected");

    @Test
    void testDestinationsFollowItemsAndFooterItems() {
        onFxThread(() -> {

            // PREPARE
            final NavigationDrawer testee = newDrawer();
            final NavigationDestination home = new NavigationDestination("home", "Home");
            final NavigationDestination orders = new NavigationDestination("orders", "Orders");
            final NavigationDestination settings = new NavigationDestination("settings", "Settings");

            // TEST
            testee.getItems().addAll(new NavigationSection("Work"), home, new NavigationSeparator(), orders);
            testee.getFooterItems().add(settings);

            // VERIFY - main items first, footer items after them, non selectable items skipped
            assertThat(testee.getDestinations()).containsExactly(home, orders, settings);
            assertThat(testee.findDestination("orders")).isSameAs(orders);
            assertThat(testee.findDestination("nope")).isNull();
        });
    }

    @Test
    void testToggleSizeSwitchesPseudoClasses() {
        onFxThread(() -> {

            // PREPARE
            final NavigationDrawer testee = newDrawer();

            // VERIFY initial state
            assertThat(testee.getSize()).isEqualTo(DrawerSize.EXPANDED);
            assertThat(testee.getPseudoClassStates()).contains(EXPANDED).doesNotContain(COLLAPSED);

            // TEST
            testee.toggleSize();

            // VERIFY
            assertThat(testee.getSize()).isEqualTo(DrawerSize.COLLAPSED);
            assertThat(testee.getPseudoClassStates()).contains(COLLAPSED).doesNotContain(EXPANDED);

            // TEST
            testee.toggleSize();

            // VERIFY
            assertThat(testee.getSize()).isEqualTo(DrawerSize.EXPANDED);
        });
    }

    @Test
    void testWidthFollowsSize() {
        onFxThread(() -> {

            // PREPARE
            final NavigationDrawer testee = newDrawer();
            testee.setExpandedWidth(300);
            testee.setCollapsedWidth(56);
            testee.getItems().add(new NavigationDestination("home", "Home"));
            layout(new StackPane(testee), 400, 600);

            // VERIFY
            assertThat(testee.prefWidth(-1)).isEqualTo(300);

            // TEST
            testee.setSize(DrawerSize.COLLAPSED);

            // VERIFY
            assertThat(testee.prefWidth(-1)).isEqualTo(56);
            assertThat(testee.minWidth(-1)).isEqualTo(56);
            assertThat(testee.maxWidth(-1)).isEqualTo(56);

            // TEST - changing the width of the current size takes effect right away
            testee.setCollapsedWidth(72);

            // VERIFY
            assertThat(testee.prefWidth(-1)).isEqualTo(72);
        });
    }

    @Test
    void testCollapsedHidesLabelsAndShowsTooltips() {
        onFxThread(() -> {

            // PREPARE
            final NavigationDrawer testee = newDrawer();
            final NavigationDestination home = new NavigationDestination("home", "Home");
            final NavigationSection section = new NavigationSection("Work");
            testee.getItems().addAll(section, home);
            layout(new StackPane(testee), 400, 600);
            final DestinationCell cell = cells(testee).get(0);

            // VERIFY expanded
            assertThat(cell.getLabel().isVisible()).isTrue();
            assertThat(cell.isTooltipInstalled()).isFalse();

            // TEST
            testee.setSize(DrawerSize.COLLAPSED);
            relayout(testee);

            // VERIFY - the label is gone and its text is offered as a tooltip instead
            assertThat(cell.getLabel().isVisible()).isFalse();
            assertThat(cell.getLabel().isManaged()).isFalse();
            assertThat(cell.isTooltipInstalled()).isTrue();
            assertThat(cell.getCellTooltip().getText()).isEqualTo("Home");

            // VERIFY - a caption without an icon has nothing left to show
            assertThat(testee.lookupAll(".navigation-section-header")).allSatisfy(
                    node -> assertThat(node.isVisible()).isFalse());
        });
    }

    @Test
    void testTooltipTextOverridesTheLabel() {
        onFxThread(() -> {

            // PREPARE
            final NavigationDrawer testee = newDrawer();
            final NavigationDestination home = new NavigationDestination("home", "Home");
            home.setTooltipText("Back to the start page");
            testee.getItems().add(home);
            layout(new StackPane(testee), 400, 600);

            // TEST
            testee.setSize(DrawerSize.COLLAPSED);

            // VERIFY
            assertThat(cells(testee).get(0).getCellTooltip().getText()).isEqualTo("Back to the start page");
        });
    }

    @Test
    void testSelectionShowsUpAsPseudoClassOnOneCell() {
        onFxThread(() -> {

            // PREPARE
            final NavigationDrawer testee = newDrawer();
            final NavigationDestination home = new NavigationDestination("home", "Home");
            final NavigationDestination orders = new NavigationDestination("orders", "Orders");
            testee.getItems().addAll(home, orders);
            layout(new StackPane(testee), 400, 600);

            // TEST
            testee.getSelectionModel().select(orders);

            // VERIFY
            final List<DestinationCell> cells = cells(testee);
            assertThat(cells.get(0).getPseudoClassStates()).doesNotContain(SELECTED);
            assertThat(cells.get(1).getPseudoClassStates()).contains(SELECTED);
        });
    }

    @Test
    void testBadgeIsShownOnlyWhenThereIsTextAndRoomForIt() {
        onFxThread(() -> {

            // PREPARE
            final NavigationDrawer testee = newDrawer();
            final NavigationDestination orders = new NavigationDestination("orders", "Orders");
            testee.getItems().add(orders);
            layout(new StackPane(testee), 400, 600);
            final DestinationCell cell = cells(testee).get(0);

            // VERIFY - no badge text, no badge
            assertThat(cell.getBadge().isVisible()).isFalse();

            // TEST
            orders.setBadgeText("7");

            // VERIFY
            assertThat(cell.getBadge().isVisible()).isTrue();
            assertThat(cell.getBadge().getText()).isEqualTo("7");

            // TEST - the mini size has no room for it
            testee.setSize(DrawerSize.COLLAPSED);

            // VERIFY
            assertThat(cell.getBadge().isVisible()).isFalse();
        });
    }

    @Test
    void testHiddenItemIsNeitherVisibleNorManaged() {
        onFxThread(() -> {

            // PREPARE
            final NavigationDrawer testee = newDrawer();
            final NavigationDestination home = new NavigationDestination("home", "Home");
            testee.getItems().add(home);
            layout(new StackPane(testee), 400, 600);
            final DestinationCell cell = cells(testee).get(0);

            // TEST
            home.setVisible(false);

            // VERIFY
            assertThat(cell.isVisible()).isFalse();
            assertThat(cell.isManaged()).isFalse();
        });
    }

    @Test
    void testHeaderNodeIsAddedAndRemoved() {
        onFxThread(() -> {

            // PREPARE
            final NavigationDrawer testee = newDrawer();
            final Label header = new Label("Acme");
            layout(new StackPane(testee), 400, 600);

            // TEST
            testee.setHeader(header);
            relayout(testee);

            // VERIFY
            assertThat(header.getParent()).isNotNull();
            assertThat(header.getParent().getStyleClass()).contains("header");

            // TEST
            testee.setHeader(null);
            relayout(testee);

            // VERIFY
            assertThat(header.getParent()).isNull();
        });
    }

    @Test
    void testStylesheetIsFound() {
        onFxThread(() -> {

            // TEST & VERIFY
            assertThat(newDrawer().getUserAgentStylesheet()).endsWith("navidraw.css");
        });
    }

    private static List<DestinationCell> cells(final NavigationDrawer drawer) {
        return ((NavigationDrawerSkin) drawer.getSkin()).getDestinationCells();
    }

}
