package org.fuin.fx.navidraw;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/**
 * Tests for {@link NavigationDrawerSelectionModel}.
 */
class NavigationDrawerSelectionModelTest extends AbstractFxTest {

    @Test
    void testSelectByItemAndByIndex() {
        onFxThread(() -> {

            // PREPARE
            final NavigationDrawer drawer = newDrawer();
            final NavigationDestination home = new NavigationDestination("home", "Home");
            final NavigationDestination orders = new NavigationDestination("orders", "Orders");
            drawer.getItems().addAll(home, orders);
            final NavigationDrawerSelectionModel testee = drawer.getSelectionModel();

            // VERIFY nothing is selected to begin with
            assertThat(testee.getSelectedIndex()).isEqualTo(-1);
            assertThat(testee.getSelectedItem()).isNull();

            // TEST
            testee.select(orders);

            // VERIFY
            assertThat(testee.getSelectedItem()).isSameAs(orders);
            assertThat(testee.getSelectedIndex()).isEqualTo(1);

            // TEST
            testee.select(0);

            // VERIFY
            assertThat(testee.getSelectedItem()).isSameAs(home);
        });
    }

    @Test
    void testSelectById() {
        onFxThread(() -> {

            // PREPARE
            final NavigationDrawer drawer = newDrawer();
            final NavigationDestination orders = new NavigationDestination("orders", "Orders");
            drawer.getItems().addAll(new NavigationDestination("home", "Home"), orders);
            final NavigationDrawerSelectionModel testee = drawer.getSelectionModel();

            // TEST & VERIFY
            assertThat(testee.selectById("orders")).isTrue();
            assertThat(testee.getSelectedItem()).isSameAs(orders);

            // TEST & VERIFY - an unknown identifier clears the selection instead of leaving a stale one
            assertThat(testee.selectById("nope")).isFalse();
            assertThat(testee.getSelectedItem()).isNull();
        });
    }

    @Test
    void testSelectionFollowsItsItemWhenTheListChanges() {
        onFxThread(() -> {

            // PREPARE
            final NavigationDrawer drawer = newDrawer();
            final NavigationDestination home = new NavigationDestination("home", "Home");
            final NavigationDestination orders = new NavigationDestination("orders", "Orders");
            drawer.getItems().addAll(home, orders);
            final NavigationDrawerSelectionModel testee = drawer.getSelectionModel();
            testee.select(orders);

            // TEST - insert before the selected one
            drawer.getItems().add(0, new NavigationDestination("new", "New"));

            // VERIFY - the same object stays selected, at its new index
            assertThat(testee.getSelectedItem()).isSameAs(orders);
            assertThat(testee.getSelectedIndex()).isEqualTo(2);
        });
    }

    @Test
    void testSelectionIsClearedWhenItsItemIsRemoved() {
        onFxThread(() -> {

            // PREPARE
            final NavigationDrawer drawer = newDrawer();
            final NavigationDestination home = new NavigationDestination("home", "Home");
            final NavigationDestination orders = new NavigationDestination("orders", "Orders");
            drawer.getItems().addAll(home, orders);
            final NavigationDrawerSelectionModel testee = drawer.getSelectionModel();
            testee.select(orders);

            // TEST
            drawer.getItems().remove(orders);

            // VERIFY
            assertThat(testee.getSelectedItem()).isNull();
            assertThat(testee.getSelectedIndex()).isEqualTo(-1);
        });
    }

    @Test
    void testFooterDestinationsAreSelectableToo() {
        onFxThread(() -> {

            // PREPARE
            final NavigationDrawer drawer = newDrawer();
            final NavigationDestination settings = new NavigationDestination("settings", "Settings");
            drawer.getItems().add(new NavigationDestination("home", "Home"));
            drawer.getFooterItems().add(settings);
            final NavigationDrawerSelectionModel testee = drawer.getSelectionModel();

            // TEST
            testee.select(settings);

            // VERIFY - footer destinations come after the main ones
            assertThat(testee.getSelectedIndex()).isEqualTo(1);
            assertThat(testee.getSelectedItem()).isSameAs(settings);
        });
    }

}
