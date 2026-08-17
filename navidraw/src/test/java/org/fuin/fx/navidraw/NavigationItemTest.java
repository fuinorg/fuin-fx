package org.fuin.fx.navidraw;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import javafx.scene.control.Label;
import org.junit.jupiter.api.Test;

/**
 * Tests for the item model: {@link NavigationDestination} and {@link NavigationSeparator}. Groups and
 * sections have tests of their own.
 */
class NavigationItemTest extends AbstractFxTest {

    @Test
    void testDestinationDefaults() {
        onFxThread(() -> {

            // TEST
            final NavigationDestination testee = new NavigationDestination("home", "Home");

            // VERIFY
            assertThat(testee.getId()).isEqualTo("home");
            assertThat(testee.getText()).isEqualTo("Home");
            assertThat(testee.getGraphic()).isNull();
            assertThat(testee.getTooltipText()).isEmpty();
            assertThat(testee.getBadgeText()).isEmpty();
            assertThat(testee.isDisable()).isFalse();
            assertThat(testee.isVisible()).isTrue();
            assertThat(testee.getOnActivate()).isNull();
            assertThat(testee.getUserData()).isNull();
            assertThat(testee).hasToString("NavigationDestination[home]");
        });
    }

    @Test
    void testDestinationSetters() {
        onFxThread(() -> {

            // PREPARE
            final NavigationDestination testee = new NavigationDestination("home", "Home");
            final Label graphic = new Label();
            final Runnable action = () -> {
                // Nothing to do
            };

            // TEST
            testee.setText("Start");
            testee.setGraphic(graphic);
            testee.setTooltipText("Back to the start page");
            testee.setBadgeText("3");
            testee.setDisable(true);
            testee.setVisible(false);
            testee.setOnActivate(action);
            testee.setUserData("payload");

            // VERIFY
            assertThat(testee.getText()).isEqualTo("Start");
            assertThat(testee.getGraphic()).isSameAs(graphic);
            assertThat(testee.getTooltipText()).isEqualTo("Back to the start page");
            assertThat(testee.getBadgeText()).isEqualTo("3");
            assertThat(testee.isDisable()).isTrue();
            assertThat(testee.isVisible()).isFalse();
            assertThat(testee.getOnActivate()).isSameAs(action);
            assertThat(testee.getUserData()).isEqualTo("payload");
        });
    }

    @Test
    void testDestinationConstructorWithGraphic() {
        onFxThread(() -> {

            // PREPARE
            final Label graphic = new Label();

            // TEST
            final NavigationDestination testee = new NavigationDestination("home", "Home", graphic);

            // VERIFY
            assertThat(testee.getGraphic()).isSameAs(graphic);
        });
    }

    @Test
    void testDestinationRejectsNull() {
        // TEST & VERIFY
        assertThatThrownBy(() -> new NavigationDestination(null, "Home"))
                .isInstanceOf(NullPointerException.class).hasMessage("id==null");
        assertThatThrownBy(() -> new NavigationDestination("home", null))
                .isInstanceOf(NullPointerException.class).hasMessage("text==null");
    }

    @Test
    void testSeparator() {
        onFxThread(() -> {

            // TEST
            final NavigationSeparator testee = new NavigationSeparator();

            // VERIFY
            assertThat(testee.isVisible()).isTrue();
            assertThat(testee).hasToString("NavigationSeparator");
        });
    }

    @Test
    void testDrawerSizeToggled() {
        // TEST & VERIFY
        assertThat(DrawerSize.EXPANDED.toggled()).isEqualTo(DrawerSize.COLLAPSED);
        assertThat(DrawerSize.COLLAPSED.toggled()).isEqualTo(DrawerSize.EXPANDED);
    }

}
