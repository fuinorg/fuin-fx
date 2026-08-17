package org.fuin.fx.navidraw;

import static org.assertj.core.api.Assertions.assertThat;

import javafx.scene.control.Label;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link NavigationGroup} and the way the drawer treats its children.
 */
class NavigationGroupTest extends AbstractFxTest {

    @Test
    void testDefaults() {
        onFxThread(() -> {

            // TEST
            final NavigationGroup testee = new NavigationGroup("Reports");

            // VERIFY
            assertThat(testee.getText()).isEqualTo("Reports");
            assertThat(testee.getGraphic()).isNull();
            assertThat(testee.getItems()).isEmpty();
            assertThat(testee.isExpanded()).isFalse();
            assertThat(testee.isVisible()).isTrue();
            assertThat(testee.isDisable()).isFalse();
            assertThat(testee).hasToString("NavigationGroup[Reports]");
        });
    }

    @Test
    void testConstructorWithChildren() {
        onFxThread(() -> {

            // PREPARE
            final Label graphic = new Label();
            final NavigationDestination daily = new NavigationDestination("daily", "Daily");
            final NavigationDestination weekly = new NavigationDestination("weekly", "Weekly");

            // TEST
            final NavigationGroup testee = new NavigationGroup("Reports", graphic, daily, weekly);

            // VERIFY
            assertThat(testee.getGraphic()).isSameAs(graphic);
            assertThat(testee.getItems()).containsExactly(daily, weekly);
        });
    }

    @Test
    void testToggleExpanded() {
        onFxThread(() -> {

            // PREPARE
            final NavigationGroup testee = new NavigationGroup("Reports");

            // TEST & VERIFY
            testee.toggleExpanded();
            assertThat(testee.isExpanded()).isTrue();
            testee.toggleExpanded();
            assertThat(testee.isExpanded()).isFalse();
        });
    }

    @Test
    void testChildrenAreSelectableInVisualOrder() {
        onFxThread(() -> {

            // PREPARE
            final NavigationDrawer drawer = newDrawer();
            final NavigationDestination home = new NavigationDestination("home", "Home");
            final NavigationDestination daily = new NavigationDestination("daily", "Daily");
            final NavigationDestination weekly = new NavigationDestination("weekly", "Weekly");
            final NavigationDestination settings = new NavigationDestination("settings", "Settings");
            final NavigationGroup reports = new NavigationGroup("Reports", null, daily, weekly);

            // TEST
            drawer.getItems().addAll(home, reports);
            drawer.getFooterItems().add(settings);

            // VERIFY - a group contributes its children where the group itself sits
            assertThat(drawer.getDestinations()).containsExactly(home, daily, weekly, settings);
            assertThat(drawer.findDestination("weekly")).isSameAs(weekly);
        });
    }

    @Test
    void testAddingAChildLaterIsPickedUp() {
        onFxThread(() -> {

            // PREPARE
            final NavigationDrawer drawer = newDrawer();
            final NavigationGroup reports = new NavigationGroup("Reports");
            drawer.getItems().add(reports);
            final NavigationDestination daily = new NavigationDestination("daily", "Daily");

            // TEST
            reports.getItems().add(daily);

            // VERIFY
            assertThat(drawer.getDestinations()).containsExactly(daily);

            // TEST
            reports.getItems().remove(daily);

            // VERIFY
            assertThat(drawer.getDestinations()).isEmpty();
        });
    }

    @Test
    void testRemovingTheGroupStopsWatchingItsChildren() {
        onFxThread(() -> {

            // PREPARE
            final NavigationDrawer drawer = newDrawer();
            final NavigationGroup reports = new NavigationGroup("Reports");
            drawer.getItems().add(reports);
            drawer.getItems().remove(reports);

            // TEST
            reports.getItems().add(new NavigationDestination("daily", "Daily"));

            // VERIFY - a group that is no longer in the drawer must not feed it destinations
            assertThat(drawer.getDestinations()).isEmpty();
        });
    }

    @Test
    void testSelectingAChildOpensItsGroup() {
        onFxThread(() -> {

            // PREPARE
            final NavigationDrawer drawer = newDrawer();
            final NavigationDestination weekly = new NavigationDestination("weekly", "Weekly");
            final NavigationGroup reports =
                    new NavigationGroup("Reports", null, new NavigationDestination("daily", "Daily"), weekly);
            drawer.getItems().add(reports);

            // VERIFY closed to begin with
            assertThat(reports.isExpanded()).isFalse();

            // TEST
            drawer.getSelectionModel().selectById("weekly");

            // VERIFY - a selection nobody can see would be worse than a group that opened by itself
            assertThat(reports.isExpanded()).isTrue();
            assertThat(drawer.getSelectionModel().getSelectedItem()).isSameAs(weekly);
        });
    }

    @Test
    void testSelectionSurvivesAChildBeingInserted() {
        onFxThread(() -> {

            // PREPARE
            final NavigationDrawer drawer = newDrawer();
            final NavigationDestination weekly = new NavigationDestination("weekly", "Weekly");
            final NavigationGroup reports = new NavigationGroup("Reports", null, weekly);
            drawer.getItems().add(reports);
            drawer.getSelectionModel().select(weekly);

            // TEST
            reports.getItems().add(0, new NavigationDestination("daily", "Daily"));

            // VERIFY
            assertThat(drawer.getSelectionModel().getSelectedItem()).isSameAs(weekly);
            assertThat(drawer.getSelectionModel().getSelectedIndex()).isEqualTo(1);
        });
    }

}
