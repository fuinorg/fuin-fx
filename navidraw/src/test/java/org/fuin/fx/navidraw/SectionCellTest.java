package org.fuin.fx.navidraw;

import static org.assertj.core.api.Assertions.assertThat;

import javafx.css.PseudoClass;
import javafx.scene.AccessibleRole;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link SectionCell}, the node a collapsible section is drawn with.
 */
class SectionCellTest extends AbstractFxTest {

    private static final PseudoClass SELECTED = PseudoClass.getPseudoClass("selected");

    private static final PseudoClass COLLAPSIBLE = PseudoClass.getPseudoClass("collapsible");

    private static final PseudoClass EXPANDED = PseudoClass.getPseudoClass("expanded");

    @Test
    void testClickingTheCaptionFoldsAndUnfoldsTheSection() {
        onFxThread(() -> {

            // PREPARE
            final Fixture fixture = new Fixture();

            // VERIFY open to begin with
            assertThat(fixture.section.isExpanded()).isTrue();
            assertThat(fixture.cell.getItemBox().isManaged()).isTrue();
            assertThat(fixture.cell.getCaptionRow().getPseudoClassStates()).contains(EXPANDED);

            // TEST
            fixture.clickCaption();

            // VERIFY
            assertThat(fixture.section.isExpanded()).isFalse();
            assertThat(fixture.cell.getItemBox().isVisible()).isFalse();
            assertThat(fixture.cell.getItemBox().isManaged()).isFalse();
            assertThat(fixture.cell.getCaptionRow().getPseudoClassStates()).doesNotContain(EXPANDED);

            // TEST
            fixture.clickCaption();

            // VERIFY
            assertThat(fixture.section.isExpanded()).isTrue();
            assertThat(fixture.cell.getItemBox().isManaged()).isTrue();
        });
    }

    @Test
    void testTwistyTurns() {
        onFxThread(() -> {

            // PREPARE
            final Fixture fixture = new Fixture();

            // VERIFY open
            assertThat(fixture.cell.getCaptionRow().getTwisty().getRotate()).isZero();

            // TEST
            fixture.section.setExpanded(false);

            // VERIFY
            assertThat(fixture.cell.getCaptionRow().getTwisty().getRotate()).isEqualTo(-90);
        });
    }

    @Test
    void testCaptionShowsTheSectionText() {
        onFxThread(() -> {

            // PREPARE
            final Fixture fixture = new Fixture();

            // VERIFY
            assertThat(fixture.cell.getCaptionRow().getLabel().getText()).isEqualTo("Operations");

            // TEST
            fixture.section.setText("Logistics");

            // VERIFY
            assertThat(fixture.cell.getCaptionRow().getLabel().getText()).isEqualTo("Logistics");
        });
    }

    @Test
    void testANonCollapsibleSectionIsAPlainHeading() {
        onFxThread(() -> {

            // PREPARE
            final Fixture fixture = new Fixture();
            fixture.section.setCollapsible(false);
            relayout(fixture.root);
            final SectionCaptionRow caption = fixture.cell.getCaptionRow();

            // VERIFY - nothing to press, so no twisty, no focus stop and no button role
            assertThat(caption.getTwisty().isVisible()).isFalse();
            assertThat(caption.isFocusTraversable()).isFalse();
            assertThat(caption.getAccessibleRole()).isEqualTo(AccessibleRole.TEXT);
            assertThat(caption.getPseudoClassStates()).doesNotContain(COLLAPSIBLE);

            // TEST
            fixture.clickCaption();

            // VERIFY
            assertThat(fixture.section.isExpanded()).isTrue();
            assertThat(fixture.cell.getItemBox().isManaged()).isTrue();
        });
    }

    @Test
    void testACollapsibleSectionIsAControl() {
        onFxThread(() -> {

            // PREPARE
            final Fixture fixture = new Fixture();
            final SectionCaptionRow caption = fixture.cell.getCaptionRow();

            // VERIFY
            assertThat(caption.getTwisty().isVisible()).isTrue();
            assertThat(caption.isFocusTraversable()).isTrue();
            assertThat(caption.getAccessibleRole()).isEqualTo(AccessibleRole.BUTTON);
            assertThat(caption.getPseudoClassStates()).contains(COLLAPSIBLE);
            assertThat(caption.getAccessibleText()).isEqualTo("Operations");
        });
    }

    @Test
    void testMiniSizeHidesTheCaptionButKeepsAFoldedSectionsItems() {
        onFxThread(() -> {

            // PREPARE - folded, so in the full size its items are away
            final Fixture fixture = new Fixture();
            fixture.section.setExpanded(false);
            relayout(fixture.root);
            assertThat(fixture.cell.getItemBox().isManaged()).isFalse();

            // TEST
            fixture.drawer.setSize(DrawerSize.COLLAPSED);
            relayout(fixture.root);

            // VERIFY - with the caption gone there would be no way to unfold it, and those destinations
            // would be stranded on the rail
            assertThat(fixture.cell.getCaptionRow().isVisible()).isFalse();
            assertThat(fixture.cell.getCaptionRow().isManaged()).isFalse();
            assertThat(fixture.cell.getItemBox().isVisible()).isTrue();
            assertThat(fixture.cell.getItemBox().isManaged()).isTrue();
            assertThat(fixture.section.isExpanded()).isFalse();

            // TEST
            fixture.drawer.setSize(DrawerSize.EXPANDED);
            relayout(fixture.root);

            // VERIFY - back to what the section said all along
            assertThat(fixture.cell.getCaptionRow().isVisible()).isTrue();
            assertThat(fixture.cell.getItemBox().isManaged()).isFalse();
        });
    }

    @Test
    void testMiniSizeReachesTheCellsInsideTheSection() {
        onFxThread(() -> {

            // PREPARE
            final Fixture fixture = new Fixture();

            // TEST
            fixture.drawer.setSize(DrawerSize.COLLAPSED);
            relayout(fixture.root);

            // VERIFY - the destination and the group inside have to shrink too
            assertThat(fixture.cell.getDestinationCells().get(0).getLabel().isVisible()).isFalse();
            assertThat(fixture.cell.getDestinationCells().get(0).isTooltipInstalled()).isTrue();
            final GroupCell group = fixture.cell.getGroupCells().get(0);
            assertThat(group.getHeaderRow().getLabel().isVisible()).isFalse();
            assertThat(group.getChildBox().isManaged()).isFalse();
        });
    }

    @Test
    void testSelectionReachesDestinationsAndNestedGroups() {
        onFxThread(() -> {

            // PREPARE
            final Fixture fixture = new Fixture();

            // TEST
            fixture.drawer.getSelectionModel().select(fixture.orders);

            // VERIFY
            assertThat(fixture.cell.getDestinationCells().get(0).getPseudoClassStates()).contains(SELECTED);

            // TEST - two levels down
            fixture.drawer.getSelectionModel().select(fixture.weekly);
            relayout(fixture.root);

            // VERIFY
            assertThat(fixture.cell.getDestinationCells().get(0).getPseudoClassStates())
                    .doesNotContain(SELECTED);
            final GroupCell group = fixture.cell.getGroupCells().get(0);
            assertThat(group.getChildCells().get(0).getPseudoClassStates()).doesNotContain(SELECTED);
            assertThat(group.getChildCells().get(1).getPseudoClassStates()).contains(SELECTED);
        });
    }

    @Test
    void testItemsAddedLaterAreDrawn() {
        onFxThread(() -> {

            // PREPARE
            final Fixture fixture = new Fixture();

            // TEST
            fixture.section.getItems().add(new NavigationDestination("returns", "Returns"));
            relayout(fixture.root);

            // VERIFY
            assertThat(fixture.cell.getDestinationCells()).hasSize(2);
            assertThat(fixture.cell.getItemBox().getChildren()).hasSize(3);
        });
    }

    private static MouseEvent mouseClicked() {
        return new MouseEvent(MouseEvent.MOUSE_CLICKED, 0, 0, 0, 0, MouseButton.PRIMARY, 1, false, false,
                false, false, true, false, false, false, false, false, null);
    }

    /** A drawer holding one section with a destination and a group of two destinations. */
    private static final class Fixture {

        private final NavigationDrawer drawer = newDrawer();

        private final NavigationDestination orders = new NavigationDestination("orders", "Orders");

        private final NavigationDestination daily = new NavigationDestination("daily", "Daily");

        private final NavigationDestination weekly = new NavigationDestination("weekly", "Weekly");

        private final NavigationGroup reports = new NavigationGroup("Reports", null, daily, weekly);

        private final NavigationSection section = new NavigationSection("Operations", orders, reports);

        private final StackPane root;

        private final SectionCell cell;

        private Fixture() {
            drawer.getItems().add(section);
            root = new StackPane(drawer);
            layout(root, 400, 600);
            cell = ((NavigationDrawerSkin) drawer.getSkin()).getSectionCells().get(0);
        }

        private void clickCaption() {
            cell.getCaptionRow().fireEvent(mouseClicked());
            relayout(root);
        }

    }

}
