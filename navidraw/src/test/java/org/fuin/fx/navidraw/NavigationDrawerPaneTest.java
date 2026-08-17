package org.fuin.fx.navidraw;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.atomic.AtomicBoolean;

import javafx.css.PseudoClass;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link NavigationDrawerPane}.
 */
class NavigationDrawerPaneTest extends AbstractFxTest {

    private static final PseudoClass PERSISTENT = PseudoClass.getPseudoClass("persistent");

    private static final PseudoClass MODAL = PseudoClass.getPseudoClass("modal");

    private static final PseudoClass OPEN = PseudoClass.getPseudoClass("open");

    @Test
    void testAutoModeFlipsAtTheBreakpoint() {
        onFxThread(() -> {

            // PREPARE
            final NavigationDrawer drawer = newDrawer();
            final NavigationDrawerPane testee = new NavigationDrawerPane(drawer, new StackPane());
            testee.setModalBreakpoint(900);
            layout(testee, 1200, 800);

            // VERIFY wide
            assertThat(testee.getEffectiveDisplayMode()).isEqualTo(DrawerDisplayMode.PERSISTENT);
            assertThat(testee.getPseudoClassStates()).contains(PERSISTENT).doesNotContain(MODAL);

            // TEST
            testee.resize(600, 800);
            relayout(testee);

            // VERIFY narrow
            assertThat(testee.getEffectiveDisplayMode()).isEqualTo(DrawerDisplayMode.MODAL);
            assertThat(testee.getPseudoClassStates()).contains(MODAL).doesNotContain(PERSISTENT);

            // TEST
            testee.resize(1000, 800);
            relayout(testee);

            // VERIFY wide again
            assertThat(testee.getEffectiveDisplayMode()).isEqualTo(DrawerDisplayMode.PERSISTENT);
        });
    }

    @Test
    void testExplicitModeIgnoresTheBreakpoint() {
        onFxThread(() -> {

            // PREPARE
            final NavigationDrawerPane testee = new NavigationDrawerPane(newDrawer(), new StackPane());
            testee.setDisplayMode(DrawerDisplayMode.PERSISTENT);

            // TEST - far below the breakpoint
            layout(testee, 400, 800);

            // VERIFY
            assertThat(testee.getEffectiveDisplayMode()).isEqualTo(DrawerDisplayMode.PERSISTENT);
        });
    }

    @Test
    void testPersistentLaysContentOutBesideTheDrawer() {
        onFxThread(() -> {

            // PREPARE
            final NavigationDrawer drawer = newDrawer();
            drawer.setExpandedWidth(280);
            final Label content = new Label("content");
            content.setMaxWidth(Double.MAX_VALUE);
            final NavigationDrawerPane testee = new NavigationDrawerPane(drawer, content);
            testee.setDisplayMode(DrawerDisplayMode.PERSISTENT);

            // TEST
            layout(testee, 1000, 600);

            // VERIFY - no overlap, the drawer is fully in view and the content starts behind it
            assertThat(drawer.getLayoutX()).isZero();
            assertThat(drawer.getTranslateX()).isZero();
            assertThat(drawer.getWidth()).isEqualTo(280);
            assertThat(content.getLayoutX()).isEqualTo(280);
            assertThat(content.getWidth()).isEqualTo(720);
        });
    }

    @Test
    void testModalPushesTheDrawerOutOfViewUntilItIsOpened() {
        onFxThread(() -> {

            // PREPARE
            final NavigationDrawer drawer = newDrawer();
            drawer.setExpandedWidth(280);
            final Label content = new Label("content");
            final NavigationDrawerPane testee = new NavigationDrawerPane(drawer, content);
            testee.setDisplayMode(DrawerDisplayMode.MODAL);
            layout(testee, 600, 600);

            // VERIFY closed - the drawer sits completely to the left of the pane
            assertThat(drawer.getTranslateX()).isEqualTo(-280);
            assertThat(testee.getScrim().isVisible()).isFalse();
            assertThat(content.getLayoutX()).isZero();

            // TEST
            testee.open();
            relayout(testee);

            // VERIFY open
            assertThat(drawer.getTranslateX()).isZero();
            assertThat(testee.getScrim().isVisible()).isTrue();
            assertThat(testee.getScrim().getOpacity()).isEqualTo(1);
            assertThat(testee.getPseudoClassStates()).contains(OPEN);
            // The content is not pushed aside, the drawer floats above it
            assertThat(content.getLayoutX()).isZero();
        });
    }

    @Test
    void testNarrowingClosesTheDrawer() {
        onFxThread(() -> {

            // PREPARE
            final NavigationDrawerPane testee = new NavigationDrawerPane(newDrawer(), new StackPane());
            testee.setModalBreakpoint(900);
            layout(testee, 1200, 800);
            testee.setDrawerOpen(true);

            // TEST
            testee.resize(600, 800);
            relayout(testee);

            // VERIFY - an overlay left over from the wide layout would cover the content
            assertThat(testee.getEffectiveDisplayMode()).isEqualTo(DrawerDisplayMode.MODAL);
            assertThat(testee.isDrawerOpen()).isFalse();
            assertThat(testee.getScrim().isVisible()).isFalse();
        });
    }

    @Test
    void testScrimIsNeverVisibleWhilePersistent() {
        onFxThread(() -> {

            // PREPARE
            final NavigationDrawerPane testee = new NavigationDrawerPane(newDrawer(), new StackPane());
            testee.setDisplayMode(DrawerDisplayMode.PERSISTENT);
            layout(testee, 1200, 800);

            // TEST - even when someone sets the flag by hand
            testee.setDrawerOpen(true);
            relayout(testee);

            // VERIFY
            assertThat(testee.getScrim().isVisible()).isFalse();
            assertThat(testee.getScrim().isMouseTransparent()).isTrue();
        });
    }

    @Test
    void testEscapeClosesTheModalDrawer() {
        onFxThread(() -> {

            // PREPARE
            final NavigationDrawerPane testee = new NavigationDrawerPane(newDrawer(), new StackPane());
            testee.setDisplayMode(DrawerDisplayMode.MODAL);
            layout(testee, 600, 600);
            testee.open();
            final AtomicBoolean reachedApplication = new AtomicBoolean();
            testee.addEventHandler(KeyEvent.KEY_PRESSED, event -> reachedApplication.set(true));

            // TEST
            testee.fireEvent(escapePressed());

            // VERIFY - the filter consumed it, so the application's own handler never saw it
            assertThat(testee.isDrawerOpen()).isFalse();
            assertThat(reachedApplication.get()).isFalse();
        });
    }

    @Test
    void testEscapeIsIgnoredWhilePersistent() {
        onFxThread(() -> {

            // PREPARE
            final NavigationDrawerPane testee = new NavigationDrawerPane(newDrawer(), new StackPane());
            testee.setDisplayMode(DrawerDisplayMode.PERSISTENT);
            layout(testee, 1200, 600);
            final AtomicBoolean reachedApplication = new AtomicBoolean();
            testee.addEventHandler(KeyEvent.KEY_PRESSED, event -> reachedApplication.set(true));

            // TEST
            testee.fireEvent(escapePressed());

            // VERIFY - nothing to dismiss, so the event has to stay available to the application
            assertThat(reachedApplication.get()).isTrue();
        });
    }

    @Test
    void testClickingTheScrimCloses() {
        onFxThread(() -> {

            // PREPARE
            final NavigationDrawerPane testee = new NavigationDrawerPane(newDrawer(), new StackPane());
            testee.setDisplayMode(DrawerDisplayMode.MODAL);
            layout(testee, 600, 600);
            testee.open();

            // TEST
            testee.getScrim().fireEvent(mouseClicked());

            // VERIFY
            assertThat(testee.isDrawerOpen()).isFalse();
        });
    }

    @Test
    void testScrimIsPaintedByTheStylesheet() {
        onFxThread(() -> {

            // PREPARE - the scrim is a child of the pane, not of the drawer, so it is only styled because
            // the pane declares the stylesheet as well
            final NavigationDrawerPane testee = new NavigationDrawerPane(newDrawer(), new StackPane());
            testee.setDisplayMode(DrawerDisplayMode.MODAL);
            layout(testee, 600, 600);

            // TEST
            testee.open();
            relayout(testee);

            // VERIFY - an unstyled scrim would be an invisible overlay that swallows clicks
            assertThat(testee.getUserAgentStylesheet()).isEqualTo(NavigationDrawer.defaultStylesheet());
            assertThat(testee.getScrim().getBackground()).isNotNull();
            assertThat(testee.getScrim().getBackground().getFills()).isNotEmpty();
        });
    }

    @Test
    void testToggleSwitchesTheOpenState() {
        onFxThread(() -> {

            // PREPARE
            final NavigationDrawerPane testee = new NavigationDrawerPane(newDrawer(), new StackPane());
            testee.setDisplayMode(DrawerDisplayMode.MODAL);
            layout(testee, 600, 600);

            // TEST & VERIFY
            testee.toggle();
            assertThat(testee.isDrawerOpen()).isTrue();
            testee.toggle();
            assertThat(testee.isDrawerOpen()).isFalse();
        });
    }

    private static KeyEvent escapePressed() {
        return new KeyEvent(KeyEvent.KEY_PRESSED, "", "", KeyCode.ESCAPE, false, false, false, false);
    }

    private static MouseEvent mouseClicked() {
        return new MouseEvent(MouseEvent.MOUSE_CLICKED, 0, 0, 0, 0, MouseButton.PRIMARY, 1, false, false,
                false, false, true, false, false, false, false, false, null);
    }

}
