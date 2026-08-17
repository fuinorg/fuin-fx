package org.fuin.fx.navidraw;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import javafx.css.PseudoClass;
import javafx.scene.control.Label;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link GroupCell}, the node a sub menu is drawn with.
 */
class GroupCellTest extends AbstractFxTest {

    private static final PseudoClass SELECTED = PseudoClass.getPseudoClass("selected");

    private static final PseudoClass CONTAINS_SELECTED = PseudoClass.getPseudoClass("contains-selected");

    private static final PseudoClass EXPANDED = PseudoClass.getPseudoClass("expanded");

    @Test
    void testClickingTheHeaderOpensAndClosesTheGroup() {
        onFxThread(() -> {

            // PREPARE
            final Fixture fixture = new Fixture();

            // VERIFY closed
            assertThat(fixture.group.isExpanded()).isFalse();
            assertThat(fixture.cell.getChildBox().isVisible()).isFalse();
            assertThat(fixture.cell.getChildBox().isManaged()).isFalse();

            // TEST
            fixture.clickHeader();

            // VERIFY open
            assertThat(fixture.group.isExpanded()).isTrue();
            assertThat(fixture.cell.getChildBox().isVisible()).isTrue();
            assertThat(fixture.cell.getChildBox().isManaged()).isTrue();
            assertThat(fixture.cell.getHeaderRow().getPseudoClassStates()).contains(EXPANDED);

            // TEST
            fixture.clickHeader();

            // VERIFY closed again
            assertThat(fixture.group.isExpanded()).isFalse();
            assertThat(fixture.cell.getChildBox().isManaged()).isFalse();
        });
    }

    @Test
    void testTheGroupItselfIsNeverSelected() {
        onFxThread(() -> {

            // PREPARE
            final Fixture fixture = new Fixture();

            // TEST
            fixture.clickHeader();

            // VERIFY - a group has no page behind it
            assertThat(fixture.drawer.getSelectionModel().getSelectedItem()).isNull();
        });
    }

    @Test
    void testTwistyTurns() {
        onFxThread(() -> {

            // PREPARE
            final Fixture fixture = new Fixture();

            // VERIFY closed - pointing at the children it would reveal
            assertThat(fixture.cell.getHeaderRow().getTwisty().getRotate()).isEqualTo(-90);

            // TEST
            fixture.group.setExpanded(true);

            // VERIFY
            assertThat(fixture.cell.getHeaderRow().getTwisty().getRotate()).isZero();
        });
    }

    @Test
    void testChildrenAreDrawnAndSelectable() {
        onFxThread(() -> {

            // PREPARE
            final Fixture fixture = new Fixture();
            fixture.group.setExpanded(true);
            relayout(fixture.root);

            // VERIFY
            final List<DestinationCell> cells = fixture.cell.getChildCells();
            assertThat(cells).hasSize(2);
            assertThat(cells.get(0).getLabel().getText()).isEqualTo("Daily");

            // TEST
            cells.get(1).fireEvent(mouseClicked());

            // VERIFY
            assertThat(fixture.drawer.getSelectionModel().getSelectedItem()).isSameAs(fixture.weekly);
            assertThat(cells.get(1).getPseudoClassStates()).contains(SELECTED);
            assertThat(cells.get(0).getPseudoClassStates()).doesNotContain(SELECTED);
        });
    }

    @Test
    void testHeaderIsMarkedWhileAChildIsSelected() {
        onFxThread(() -> {

            // PREPARE
            final Fixture fixture = new Fixture();

            // TEST
            fixture.drawer.getSelectionModel().select(fixture.daily);

            // VERIFY - the header carries the mark, the group itself is still not "selected"
            assertThat(fixture.cell.getHeaderRow().getPseudoClassStates()).contains(CONTAINS_SELECTED);
            assertThat(fixture.cell.getHeaderRow().getPseudoClassStates()).doesNotContain(SELECTED);

            // TEST
            fixture.drawer.getSelectionModel().clearSelection();

            // VERIFY
            assertThat(fixture.cell.getHeaderRow().getPseudoClassStates()).doesNotContain(CONTAINS_SELECTED);
        });
    }

    @Test
    void testChildrenStayHiddenInTheMiniSize() {
        onFxThread(() -> {

            // PREPARE
            final Fixture fixture = new Fixture();
            fixture.group.setExpanded(true);
            relayout(fixture.root);

            // TEST
            fixture.drawer.setSize(DrawerSize.COLLAPSED);
            relayout(fixture.root);

            // VERIFY - an indented sub tree has nowhere to go on a 64 pixel rail
            assertThat(fixture.group.isExpanded()).isTrue();
            assertThat(fixture.cell.getChildBox().isVisible()).isFalse();
            assertThat(fixture.cell.getChildBox().isManaged()).isFalse();
            assertThat(fixture.cell.getHeaderRow().getLabel().isVisible()).isFalse();
            assertThat(fixture.cell.getHeaderRow().getTwisty().isVisible()).isFalse();

            // TEST
            fixture.drawer.setSize(DrawerSize.EXPANDED);
            relayout(fixture.root);

            // VERIFY - back to what the group said all along
            assertThat(fixture.cell.getChildBox().isManaged()).isTrue();
        });
    }

    @Test
    void testHeaderOpensTheFlyoutInsteadOfExpandingInTheMiniSize() {
        onFxThread(() -> {

            // PREPARE
            final Fixture fixture = new Fixture();
            fixture.drawer.setSize(DrawerSize.COLLAPSED);
            relayout(fixture.root);

            // TEST - no window here, so the flyout cannot actually be shown
            fixture.clickHeader();

            // VERIFY - and above all it must not have expanded in place
            assertThat(fixture.group.isExpanded()).isFalse();
            assertThat(fixture.cell.getChildBox().isManaged()).isFalse();
        });
    }

    @Test
    void testFlyoutContent() {
        onFxThread(() -> {

            // PREPARE
            final Fixture fixture = new Fixture();

            // TEST
            final VBox content = fixture.cell.buildFlyoutContent();

            // VERIFY - caption plus one row per child
            assertThat(content.getStyleClass()).containsExactly("navigation-flyout");
            assertThat(content.getChildren()).hasSize(3);
            assertThat(((Label) content.getChildren().get(0)).getText()).isEqualTo("Reports");
            assertThat(fixture.cell.getFlyoutCells()).hasSize(2);
            // A popup has a scene of its own and inherits nothing, so it carries the stylesheet itself
            assertThat(content.getStylesheets()).containsExactly(NavigationDrawer.defaultStylesheet());
        });
    }

    @Test
    void testFlyoutCellsSelectAndShowTheSelection() {
        onFxThread(() -> {

            // PREPARE
            final Fixture fixture = new Fixture();
            fixture.cell.buildFlyoutContent();
            final List<DestinationCell> flyoutCells = fixture.cell.getFlyoutCells();

            // TEST
            flyoutCells.get(0).fireEvent(mouseClicked());

            // VERIFY - the same selection model, and both copies of the row agree about it
            assertThat(fixture.drawer.getSelectionModel().getSelectedItem()).isSameAs(fixture.daily);
            assertThat(flyoutCells.get(0).getPseudoClassStates()).contains(SELECTED);
            assertThat(fixture.cell.getChildCells().get(0).getPseudoClassStates()).contains(SELECTED);
        });
    }

    @Test
    void testFlyoutIsRebuiltWhenTheChildrenChange() {
        onFxThread(() -> {

            // PREPARE
            final Fixture fixture = new Fixture();
            fixture.cell.buildFlyoutContent();
            assertThat(fixture.cell.getFlyoutCells()).hasSize(2);

            // TEST
            fixture.group.getItems().add(new NavigationDestination("monthly", "Monthly"));

            // VERIFY - the stale cells are gone and the next flyout is built from the new children
            assertThat(fixture.cell.getFlyoutCells()).isEmpty();
            assertThat(fixture.cell.buildFlyoutContent().getChildren()).hasSize(4);
        });
    }

    @Test
    void testShowFlyoutWithoutAWindowDoesNothing() {
        onFxThread(() -> {

            // PREPARE
            final Fixture fixture = new Fixture();

            // TEST & VERIFY - must not throw
            fixture.cell.showFlyout();
            assertThat(fixture.cell.isFlyoutShowing()).isFalse();
        });
    }

    private static MouseEvent mouseClicked() {
        return new MouseEvent(MouseEvent.MOUSE_CLICKED, 0, 0, 0, 0, MouseButton.PRIMARY, 1, false, false,
                false, false, true, false, false, false, false, false, null);
    }

    /** A drawer holding a single group of two destinations, laid out in a scene. */
    private static final class Fixture {

        private final NavigationDrawer drawer = newDrawer();

        private final NavigationDestination daily = new NavigationDestination("daily", "Daily");

        private final NavigationDestination weekly = new NavigationDestination("weekly", "Weekly");

        private final NavigationGroup group = new NavigationGroup("Reports", null, daily, weekly);

        private final StackPane root;

        private final GroupCell cell;

        private Fixture() {
            drawer.getItems().add(group);
            root = new StackPane(drawer);
            layout(root, 400, 600);
            cell = ((NavigationDrawerSkin) drawer.getSkin()).getGroupCells().get(0);
        }

        private void clickHeader() {
            cell.getHeaderRow().fireEvent(mouseClicked());
            relayout(root);
        }

    }

}
