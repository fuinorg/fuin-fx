package org.fuin.fx.navidraw;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.atomic.AtomicInteger;

import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link DestinationCell}, the node a destination is drawn with.
 */
class DestinationCellTest extends AbstractFxTest {

    @Test
    void testClickSelectsAndRunsTheAction() {
        onFxThread(() -> {

            // PREPARE
            final NavigationDrawer drawer = newDrawer();
            final AtomicInteger calls = new AtomicInteger();
            final NavigationDestination orders = new NavigationDestination("orders", "Orders");
            orders.setOnActivate(calls::incrementAndGet);
            drawer.getItems().addAll(new NavigationDestination("home", "Home"), orders);
            layout(new StackPane(drawer), 400, 600);
            final DestinationCell testee = cells(drawer).get(1);

            // TEST
            testee.fireEvent(mouseClicked(MouseButton.PRIMARY));

            // VERIFY
            assertThat(drawer.getSelectionModel().getSelectedItem()).isSameAs(orders);
            assertThat(calls.get()).isEqualTo(1);
        });
    }

    @Test
    void testSecondaryClickIsIgnored() {
        onFxThread(() -> {

            // PREPARE
            final NavigationDrawer drawer = newDrawer();
            drawer.getItems().add(new NavigationDestination("home", "Home"));
            layout(new StackPane(drawer), 400, 600);
            final DestinationCell testee = cells(drawer).get(0);

            // TEST
            testee.fireEvent(mouseClicked(MouseButton.SECONDARY));

            // VERIFY
            assertThat(drawer.getSelectionModel().getSelectedItem()).isNull();
        });
    }

    @Test
    void testEnterAndSpaceSelect() {
        onFxThread(() -> {

            // PREPARE
            final NavigationDrawer drawer = newDrawer();
            final NavigationDestination home = new NavigationDestination("home", "Home");
            final NavigationDestination orders = new NavigationDestination("orders", "Orders");
            drawer.getItems().addAll(home, orders);
            layout(new StackPane(drawer), 400, 600);

            // TEST
            cells(drawer).get(0).fireEvent(keyPressed(KeyCode.ENTER));

            // VERIFY
            assertThat(drawer.getSelectionModel().getSelectedItem()).isSameAs(home);

            // TEST
            cells(drawer).get(1).fireEvent(keyPressed(KeyCode.SPACE));

            // VERIFY
            assertThat(drawer.getSelectionModel().getSelectedItem()).isSameAs(orders);
        });
    }

    @Test
    void testDisabledDestinationCannotBeSelected() {
        onFxThread(() -> {

            // PREPARE
            final NavigationDrawer drawer = newDrawer();
            final NavigationDestination home = new NavigationDestination("home", "Home");
            home.setDisable(true);
            drawer.getItems().add(home);
            layout(new StackPane(drawer), 400, 600);
            final DestinationCell testee = cells(drawer).get(0);

            // VERIFY the disabled state made it to the node
            assertThat(testee.isDisabled()).isTrue();

            // TEST
            testee.fireEvent(mouseClicked(MouseButton.PRIMARY));

            // VERIFY
            assertThat(drawer.getSelectionModel().getSelectedItem()).isNull();
        });
    }

    @Test
    void testLabelAndAccessibleTextFollowTheDestination() {
        onFxThread(() -> {

            // PREPARE
            final NavigationDrawer drawer = newDrawer();
            final NavigationDestination home = new NavigationDestination("home", "Home");
            drawer.getItems().add(home);
            layout(new StackPane(drawer), 400, 600);
            final DestinationCell testee = cells(drawer).get(0);

            // TEST
            home.setText("Start");

            // VERIFY - the accessible text stays the full label, which is what a collapsed drawer relies on
            assertThat(testee.getLabel().getText()).isEqualTo("Start");
            assertThat(testee.getAccessibleText()).isEqualTo("Start");
        });
    }

    @Test
    void testGraphicIsShownAndReplaced() {
        onFxThread(() -> {

            // PREPARE
            final NavigationDrawer drawer = newDrawer();
            final Label first = new Label("1");
            final Label second = new Label("2");
            final NavigationDestination home = new NavigationDestination("home", "Home", first);
            drawer.getItems().add(home);
            layout(new StackPane(drawer), 400, 600);

            // VERIFY
            assertThat(first.getParent()).isNotNull();
            assertThat(first.getParent().getStyleClass()).contains("icon");

            // TEST
            home.setGraphic(second);

            // VERIFY
            assertThat(first.getParent()).isNull();
            assertThat(second.getParent()).isNotNull();

            // TEST
            home.setGraphic(null);

            // VERIFY
            assertThat(second.getParent()).isNull();
        });
    }

    private static java.util.List<DestinationCell> cells(final NavigationDrawer drawer) {
        return ((NavigationDrawerSkin) drawer.getSkin()).getDestinationCells();
    }

    private static MouseEvent mouseClicked(final MouseButton button) {
        return new MouseEvent(MouseEvent.MOUSE_CLICKED, 0, 0, 0, 0, button, 1, false, false, false, false,
                true, false, false, false, false, false, null);
    }

    private static KeyEvent keyPressed(final KeyCode code) {
        return new KeyEvent(KeyEvent.KEY_PRESSED, "", "", code, false, false, false, false);
    }

}
