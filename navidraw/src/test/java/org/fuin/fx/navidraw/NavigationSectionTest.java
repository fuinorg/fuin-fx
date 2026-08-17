package org.fuin.fx.navidraw;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

/**
 * Tests for {@link NavigationSection} and the way the drawer treats its items.
 */
class NavigationSectionTest extends AbstractFxTest {

    @Test
    void testDefaults() {
        onFxThread(() -> {

            // TEST
            final NavigationSection testee = new NavigationSection("Operations");

            // VERIFY - unlike a group, a section starts open
            assertThat(testee.getText()).isEqualTo("Operations");
            assertThat(testee.getItems()).isEmpty();
            assertThat(testee.isExpanded()).isTrue();
            assertThat(testee.isCollapsible()).isTrue();
            assertThat(testee.isVisible()).isTrue();
            assertThat(testee).hasToString("NavigationSection[Operations]");
        });
    }

    @Test
    void testConstructorWithItems() {
        onFxThread(() -> {

            // PREPARE
            final NavigationDestination orders = new NavigationDestination("orders", "Orders");
            final NavigationGroup reports = new NavigationGroup("Reports");

            // TEST - destinations and groups, and by the type of the list nothing else
            final NavigationSection testee = new NavigationSection("Operations", orders, reports);

            // VERIFY
            assertThat(testee.getItems()).containsExactly(orders, reports);
        });
    }

    @Test
    void testToggleExpanded() {
        onFxThread(() -> {

            // PREPARE
            final NavigationSection testee = new NavigationSection("Operations");

            // TEST & VERIFY
            testee.toggleExpanded();
            assertThat(testee.isExpanded()).isFalse();
            testee.toggleExpanded();
            assertThat(testee.isExpanded()).isTrue();
        });
    }

    @Test
    void testRejectsNull() {
        // TEST & VERIFY
        assertThatThrownBy(() -> new NavigationSection(null))
                .isInstanceOf(NullPointerException.class).hasMessage("text==null");
    }

    @Test
    void testItemsAreSelectableInVisualOrder() {
        onFxThread(() -> {

            // PREPARE
            final NavigationDrawer drawer = newDrawer();
            final NavigationDestination dashboard = new NavigationDestination("dashboard", "Dashboard");
            final NavigationDestination orders = new NavigationDestination("orders", "Orders");
            final NavigationDestination daily = new NavigationDestination("daily", "Daily");
            final NavigationDestination weekly = new NavigationDestination("weekly", "Weekly");
            final NavigationDestination customers = new NavigationDestination("customers", "Customers");
            final NavigationGroup reports = new NavigationGroup("Reports", null, daily, weekly);

            // TEST - two levels of nesting at once
            drawer.getItems().addAll(dashboard,
                    new NavigationSection("Operations", orders, reports),
                    new NavigationSection("Master data", customers));

            // VERIFY - one flat list, in the order the rows appear on screen
            assertThat(drawer.getDestinations())
                    .containsExactly(dashboard, orders, daily, weekly, customers);
            assertThat(drawer.findDestination("weekly")).isSameAs(weekly);
        });
    }

    @Test
    void testAddingAnItemLaterIsPickedUp() {
        onFxThread(() -> {

            // PREPARE
            final NavigationDrawer drawer = newDrawer();
            final NavigationSection section = new NavigationSection("Operations");
            drawer.getItems().add(section);
            final NavigationDestination orders = new NavigationDestination("orders", "Orders");

            // TEST
            section.getItems().add(orders);

            // VERIFY
            assertThat(drawer.getDestinations()).containsExactly(orders);

            // TEST
            section.getItems().remove(orders);

            // VERIFY
            assertThat(drawer.getDestinations()).isEmpty();
        });
    }

    @Test
    void testAddingAGroupToASectionLaterIsPickedUp() {
        onFxThread(() -> {

            // PREPARE
            final NavigationDrawer drawer = newDrawer();
            final NavigationSection section = new NavigationSection("Operations");
            drawer.getItems().add(section);
            final NavigationDestination daily = new NavigationDestination("daily", "Daily");
            final NavigationGroup reports = new NavigationGroup("Reports", null, daily);

            // TEST
            section.getItems().add(reports);

            // VERIFY - the group inside the section has to be watched as well
            assertThat(drawer.getDestinations()).containsExactly(daily);

            // TEST
            reports.getItems().add(new NavigationDestination("weekly", "Weekly"));

            // VERIFY
            assertThat(drawer.getDestinations()).hasSize(2);
        });
    }

    @Test
    void testRemovingTheSectionStopsWatchingItsItems() {
        onFxThread(() -> {

            // PREPARE
            final NavigationDrawer drawer = newDrawer();
            final NavigationSection section = new NavigationSection("Operations");
            drawer.getItems().add(section);
            drawer.getItems().remove(section);

            // TEST
            section.getItems().add(new NavigationDestination("orders", "Orders"));

            // VERIFY
            assertThat(drawer.getDestinations()).isEmpty();
        });
    }

    @Test
    void testSelectingAnItemOpensItsSection() {
        onFxThread(() -> {

            // PREPARE
            final NavigationDrawer drawer = newDrawer();
            final NavigationDestination orders = new NavigationDestination("orders", "Orders");
            final NavigationSection section = new NavigationSection("Operations", orders);
            section.setExpanded(false);
            drawer.getItems().add(section);

            // TEST
            drawer.getSelectionModel().selectById("orders");

            // VERIFY
            assertThat(section.isExpanded()).isTrue();
        });
    }

    @Test
    void testSelectingAnItemOpensBothItsGroupAndItsSection() {
        onFxThread(() -> {

            // PREPARE
            final NavigationDrawer drawer = newDrawer();
            final NavigationDestination weekly = new NavigationDestination("weekly", "Weekly");
            final NavigationGroup reports = new NavigationGroup("Reports", null, weekly);
            final NavigationSection section = new NavigationSection("Operations", reports);
            section.setExpanded(false);
            drawer.getItems().add(section);

            // VERIFY both folded to begin with
            assertThat(section.isExpanded()).isFalse();
            assertThat(reports.isExpanded()).isFalse();

            // TEST
            drawer.getSelectionModel().select(weekly);

            // VERIFY - either one left folded would hide the selection
            assertThat(section.isExpanded()).isTrue();
            assertThat(reports.isExpanded()).isTrue();
        });
    }

}
