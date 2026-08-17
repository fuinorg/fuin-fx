package org.fuin.fx.navidraw;

import static org.assertj.core.api.Assertions.assertThat;

import javafx.scene.layout.StackPane;
import org.junit.jupiter.api.Test;

/**
 * Tests showing and hiding items of every kind, at any time.
 * <p>
 * Hiding is not only a matter of the node: a destination nobody can see must not be selectable either, and
 * that goes for a destination inside a hidden group or a hidden section just as much.
 */
class VisibilityTest extends AbstractFxTest {

    @Test
    void testHidingADestinationRemovesItFromTheSelectableList() {
        onFxThread(() -> {

            // PREPARE
            final Fixture fixture = new Fixture();

            // TEST
            fixture.orders.setVisible(false);

            // VERIFY
            assertThat(fixture.drawer.getDestinations())
                    .containsExactly(fixture.dashboard, fixture.daily, fixture.weekly, fixture.customers);

            // TEST
            fixture.orders.setVisible(true);

            // VERIFY - back in its old place, not appended at the end
            assertThat(fixture.drawer.getDestinations()).containsExactly(fixture.dashboard, fixture.orders,
                    fixture.daily, fixture.weekly, fixture.customers);
        });
    }

    @Test
    void testHidingAGroupRemovesItsChildren() {
        onFxThread(() -> {

            // PREPARE
            final Fixture fixture = new Fixture();

            // TEST
            fixture.reports.setVisible(false);

            // VERIFY - a child of a hidden group is as unreachable as a hidden child
            assertThat(fixture.drawer.getDestinations())
                    .containsExactly(fixture.dashboard, fixture.orders, fixture.customers);

            // TEST
            fixture.reports.setVisible(true);

            // VERIFY
            assertThat(fixture.drawer.getDestinations()).hasSize(5);
        });
    }

    @Test
    void testHidingASectionRemovesEverythingInIt() {
        onFxThread(() -> {

            // PREPARE
            final Fixture fixture = new Fixture();

            // TEST
            fixture.operations.setVisible(false);

            // VERIFY - the destination and the whole group inside it are gone
            assertThat(fixture.drawer.getDestinations())
                    .containsExactly(fixture.dashboard, fixture.customers);

            // TEST
            fixture.operations.setVisible(true);

            // VERIFY
            assertThat(fixture.drawer.getDestinations()).hasSize(5);
        });
    }

    @Test
    void testAChildStaysHiddenWhenItsSectionIsShownAgain() {
        onFxThread(() -> {

            // PREPARE
            final Fixture fixture = new Fixture();
            fixture.orders.setVisible(false);

            // TEST
            fixture.operations.setVisible(false);
            fixture.operations.setVisible(true);

            // VERIFY - showing the section must not undo what was said about the item inside it
            assertThat(fixture.drawer.getDestinations()).doesNotContain(fixture.orders);
            assertThat(fixture.orders.isVisible()).isFalse();
        });
    }

    @Test
    void testHidingTheSelectedDestinationClearsTheSelection() {
        onFxThread(() -> {

            // PREPARE
            final Fixture fixture = new Fixture();
            fixture.drawer.getSelectionModel().select(fixture.orders);

            // TEST
            fixture.orders.setVisible(false);

            // VERIFY
            assertThat(fixture.drawer.getSelectionModel().getSelectedItem()).isNull();
            assertThat(fixture.drawer.getSelectionModel().getSelectedIndex()).isEqualTo(-1);
        });
    }

    @Test
    void testHidingASectionClearsASelectionInsideIt() {
        onFxThread(() -> {

            // PREPARE
            final Fixture fixture = new Fixture();
            fixture.drawer.getSelectionModel().select(fixture.weekly);

            // TEST - two levels above the selected destination
            fixture.operations.setVisible(false);

            // VERIFY
            assertThat(fixture.drawer.getSelectionModel().getSelectedItem()).isNull();
        });
    }

    @Test
    void testAHiddenDestinationCannotBeSelected() {
        onFxThread(() -> {

            // PREPARE
            final Fixture fixture = new Fixture();
            fixture.orders.setVisible(false);

            // TEST & VERIFY
            assertThat(fixture.drawer.getSelectionModel().selectById("orders")).isFalse();
            assertThat(fixture.drawer.getSelectionModel().getSelectedItem()).isNull();
        });
    }

    @Test
    void testAHiddenDestinationCanStillBeFound() {
        onFxThread(() -> {

            // PREPARE
            final Fixture fixture = new Fixture();
            fixture.weekly.setVisible(false);

            // TEST - otherwise there would be no way to get hold of it and show it again
            final NavigationDestination found = fixture.drawer.findDestination("weekly");

            // VERIFY
            assertThat(found).isSameAs(fixture.weekly);

            // TEST
            found.setVisible(true);

            // VERIFY
            assertThat(fixture.drawer.getDestinations()).contains(fixture.weekly);
        });
    }

    @Test
    void testFindReachesIntoAHiddenSection() {
        onFxThread(() -> {

            // PREPARE
            final Fixture fixture = new Fixture();
            fixture.operations.setVisible(false);

            // TEST & VERIFY
            assertThat(fixture.drawer.findDestination("orders")).isSameAs(fixture.orders);
            assertThat(fixture.drawer.findDestination("weekly")).isSameAs(fixture.weekly);
            assertThat(fixture.drawer.findDestination("nope")).isNull();
        });
    }

    @Test
    void testFooterItemsCanBeHiddenToo() {
        onFxThread(() -> {

            // PREPARE
            final Fixture fixture = new Fixture();
            final NavigationDestination settings = new NavigationDestination("settings", "Settings");
            fixture.drawer.getFooterItems().add(settings);
            assertThat(fixture.drawer.getDestinations()).contains(settings);

            // TEST
            settings.setVisible(false);

            // VERIFY
            assertThat(fixture.drawer.getDestinations()).doesNotContain(settings);
            assertThat(fixture.drawer.findDestination("settings")).isSameAs(settings);
        });
    }

    @Test
    void testTheNodesFollowAtAnyTime() {
        onFxThread(() -> {

            // PREPARE - laid out, so the cells exist
            final Fixture fixture = new Fixture();
            final StackPane root = new StackPane(fixture.drawer);
            layout(root, 400, 700);
            final NavigationDrawerSkin skin = (NavigationDrawerSkin) fixture.drawer.getSkin();
            final SectionCell sectionCell = skin.getSectionCells().get(0);
            final GroupCell groupCell = sectionCell.getGroupCells().get(0);

            // VERIFY everything showing
            assertThat(sectionCell.isVisible()).isTrue();
            assertThat(groupCell.isVisible()).isTrue();

            // TEST
            fixture.reports.setVisible(false);
            relayout(root);

            // VERIFY - a hidden item takes no space either
            assertThat(groupCell.isVisible()).isFalse();
            assertThat(groupCell.isManaged()).isFalse();
            assertThat(sectionCell.isVisible()).isTrue();

            // TEST
            fixture.operations.setVisible(false);
            relayout(root);

            // VERIFY
            assertThat(sectionCell.isVisible()).isFalse();
            assertThat(sectionCell.isManaged()).isFalse();

            // TEST
            fixture.operations.setVisible(true);
            fixture.reports.setVisible(true);
            relayout(root);

            // VERIFY
            assertThat(sectionCell.isVisible()).isTrue();
            assertThat(groupCell.isVisible()).isTrue();
        });
    }

    /** Dashboard, then an "Operations" section holding Orders and a "Reports" group, then Customers. */
    private static final class Fixture {

        private final NavigationDrawer drawer = newDrawer();

        private final NavigationDestination dashboard = new NavigationDestination("dashboard", "Dashboard");

        private final NavigationDestination orders = new NavigationDestination("orders", "Orders");

        private final NavigationDestination daily = new NavigationDestination("daily", "Daily");

        private final NavigationDestination weekly = new NavigationDestination("weekly", "Weekly");

        private final NavigationDestination customers = new NavigationDestination("customers", "Customers");

        private final NavigationGroup reports = new NavigationGroup("Reports", null, daily, weekly);

        private final NavigationSection operations = new NavigationSection("Operations", orders, reports);

        private Fixture() {
            drawer.getItems().addAll(dashboard, operations, new NavigationSection("Master data", customers));
        }

    }

}
