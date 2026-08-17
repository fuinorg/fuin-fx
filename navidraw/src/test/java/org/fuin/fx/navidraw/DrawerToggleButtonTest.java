package org.fuin.fx.navidraw;

import static org.assertj.core.api.Assertions.assertThat;

import javafx.scene.Node;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.StackPane;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link DrawerToggleButton}.
 */
class DrawerToggleButtonTest extends AbstractFxTest {

    @Test
    void testModalArrangementOpensAndCloses() {
        onFxThread(() -> {

            // PREPARE
            final NavigationDrawerPane pane = new NavigationDrawerPane(newDrawer(), new StackPane());
            pane.setDisplayMode(DrawerDisplayMode.MODAL);
            final DrawerToggleButton testee = new DrawerToggleButton(pane);
            layout(pane, 600, 600);

            // VERIFY - closed, so the button offers to open
            assertThat(testee.isChevronShown()).isFalse();

            // TEST
            testee.fire();

            // VERIFY
            assertThat(pane.isDrawerOpen()).isTrue();
            assertThat(testee.isChevronShown()).isTrue();

            // TEST
            testee.fire();

            // VERIFY
            assertThat(pane.isDrawerOpen()).isFalse();
            assertThat(testee.isChevronShown()).isFalse();
        });
    }

    @Test
    void testPersistentArrangementExpandsAndCollapses() {
        onFxThread(() -> {

            // PREPARE
            final NavigationDrawer drawer = newDrawer();
            final NavigationDrawerPane pane = new NavigationDrawerPane(drawer, new StackPane());
            pane.setDisplayMode(DrawerDisplayMode.PERSISTENT);
            final DrawerToggleButton testee = new DrawerToggleButton(pane);
            layout(pane, 1200, 600);

            // VERIFY - expanded, so the button offers to collapse
            assertThat(testee.isChevronShown()).isTrue();

            // TEST
            testee.fire();

            // VERIFY - the drawer shrinks instead of disappearing
            assertThat(drawer.getSize()).isEqualTo(DrawerSize.COLLAPSED);
            assertThat(pane.isDrawerOpen()).isFalse();
            assertThat(testee.isChevronShown()).isFalse();

            // TEST
            testee.fire();

            // VERIFY
            assertThat(drawer.getSize()).isEqualTo(DrawerSize.EXPANDED);
            assertThat(testee.isChevronShown()).isTrue();
        });
    }

    @Test
    void testIconFollowsAChangeMadeElsewhere() {
        onFxThread(() -> {

            // PREPARE
            final NavigationDrawer drawer = newDrawer();
            final NavigationDrawerPane pane = new NavigationDrawerPane(drawer, new StackPane());
            pane.setDisplayMode(DrawerDisplayMode.PERSISTENT);
            final DrawerToggleButton testee = new DrawerToggleButton(pane);
            layout(pane, 1200, 600);

            // TEST - somebody else collapses the drawer
            drawer.setSize(DrawerSize.COLLAPSED);

            // VERIFY
            assertThat(testee.isChevronShown()).isFalse();
        });
    }

    @Test
    void testIconFollowsTheArrangement() {
        onFxThread(() -> {

            // PREPARE
            final NavigationDrawerPane pane = new NavigationDrawerPane(newDrawer(), new StackPane());
            pane.setModalBreakpoint(900);
            final DrawerToggleButton testee = new DrawerToggleButton(pane);
            layout(pane, 1200, 600);

            // VERIFY - persistent and expanded
            assertThat(testee.isChevronShown()).isTrue();

            // TEST - narrowing turns it into a closed overlay
            pane.resize(600, 600);
            relayout(pane);

            // VERIFY
            assertThat(testee.isChevronShown()).isFalse();
        });
    }

    @Test
    void testDrawerCanBeSwappedOut() {
        onFxThread(() -> {

            // PREPARE
            final NavigationDrawer first = newDrawer();
            final NavigationDrawerPane pane = new NavigationDrawerPane(first, new StackPane());
            pane.setDisplayMode(DrawerDisplayMode.PERSISTENT);
            final DrawerToggleButton testee = new DrawerToggleButton(pane);
            layout(pane, 1200, 600);
            final NavigationDrawer second = newDrawer();
            second.setSize(DrawerSize.COLLAPSED);

            // TEST
            pane.setDrawer(second);

            // VERIFY - the button now reports and drives the new drawer
            assertThat(testee.isChevronShown()).isFalse();
            testee.fire();
            assertThat(second.getSize()).isEqualTo(DrawerSize.EXPANDED);
            assertThat(first.getSize()).isEqualTo(DrawerSize.EXPANDED);
        });
    }

    @Test
    void testIconIsStyledInsideTheApplicationsOwnToolBar() {
        onFxThread(() -> {

            // PREPARE - the button belongs in a tool bar, which is neither inside the drawer nor, in
            // general, inside the pane. A user agent stylesheet only reaches its own subtree, so this is
            // exactly where the icon silently collapsed to nothing before.
            final NavigationDrawerPane pane = new NavigationDrawerPane(newDrawer(), new StackPane());
            final DrawerToggleButton testee = new DrawerToggleButton(pane);
            final ToolBar toolBar = new ToolBar(testee);

            // TEST
            layout(toolBar, 400, 60);

            // VERIFY - 18 pixels of icon plus 9 pixels of padding on either side
            assertThat(testee.getWidth()).isGreaterThanOrEqualTo(36);
            assertThat(testee.getHeight()).isGreaterThanOrEqualTo(34);
        });
    }

    @Test
    void testIconReachesItsFinalStateWithoutAnimation() {
        onFxThread(() -> {

            // PREPARE - a drawer with animations switched off must not leave the icon half faded
            final NavigationDrawer drawer = newDrawer();
            final NavigationDrawerPane pane = new NavigationDrawerPane(drawer, new StackPane());
            pane.setDisplayMode(DrawerDisplayMode.PERSISTENT);
            final DrawerToggleButton testee = new DrawerToggleButton(pane);
            layout(new StackPane(testee), 400, 100);

            // TEST
            drawer.setSize(DrawerSize.COLLAPSED);

            // VERIFY - bars fully opaque, chevron fully gone
            assertThat(testee.isChevronShown()).isFalse();
            assertThat(barsOpacity(testee)).isEqualTo(1);
            assertThat(chevronOpacity(testee)).isZero();
        });
    }

    @Test
    void testStylesheetIsAppliedToTheButtonItself() {
        onFxThread(() -> {

            // TEST & VERIFY
            assertThat(new DrawerToggleButton().getUserAgentStylesheet())
                    .isEqualTo(NavigationDrawer.defaultStylesheet());
        });
    }

    @Test
    void testWithoutAPaneTheButtonIsInert() {
        onFxThread(() -> {

            // PREPARE
            final DrawerToggleButton testee = new DrawerToggleButton();

            // TEST & VERIFY - must not throw
            testee.fire();
            assertThat(testee.getPane()).isNull();
        });
    }

    private static double barsOpacity(final DrawerToggleButton button) {
        return graphicChild(button, "bars").getOpacity();
    }

    private static double chevronOpacity(final DrawerToggleButton button) {
        return graphicChild(button, "chevron").getOpacity();
    }

    private static Node graphicChild(final DrawerToggleButton button, final String styleClass) {
        return ((StackPane) button.getGraphic()).getChildren().stream()
                .filter(node -> node.getStyleClass().contains(styleClass))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No child with style class " + styleClass));
    }

}
