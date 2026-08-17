package org.fuin.fx.navidraw.demo;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.fuin.fx.navidraw.DrawerDisplayMode;
import org.fuin.fx.navidraw.DrawerToggleButton;
import org.fuin.fx.navidraw.IkonliGraphics;
import org.fuin.fx.navidraw.NavigationDestination;
import org.fuin.fx.navidraw.NavigationDrawer;
import org.fuin.fx.navidraw.NavigationDrawerPane;
import org.fuin.fx.navidraw.NavigationGroup;
import org.fuin.fx.navidraw.NavigationSection;
import org.fuin.fx.navidraw.NavigationSeparator;

/**
 * Shows the drawer in a real window.
 * <p>
 * Start it with {@code ./mvnw -s settings.xml -pl navidraw test-compile exec:exec@demo} and try it out:
 * drag the window across 900 pixels to see the arrangement flip, use the toggle button in the tool bar,
 * hover an item while the drawer is mini, and switch the palette with the check box.
 */
public class NaviDrawDemo extends Application {

    private static final double BREAKPOINT = 900;

    private final Label headline = new Label();

    private final Label description = new Label();

    @Override
    public void start(final Stage stage) {

        final NavigationDrawer drawer = createDrawer();
        final NavigationDrawerPane pane = new NavigationDrawerPane();
        pane.setDrawer(drawer);
        pane.setModalBreakpoint(BREAKPOINT);
        pane.setContent(createContent(pane));

        drawer.getSelectionModel().selectedItemProperty().addListener(
                (obs, old, selected) -> showSelection(pane, selected));
        drawer.getSelectionModel().selectById("dashboard");

        final Scene scene = new Scene(pane, 1100, 700);
        stage.setTitle("fuin-fx navigation drawer");
        stage.setScene(scene);
        stage.show();
    }

    private NavigationDrawer createDrawer() {

        final NavigationDrawer drawer = new NavigationDrawer();

        final Label brand = new Label("Acme Logistics");
        brand.setStyle("-fx-font-size: 15px; -fx-font-weight: bold;");
        drawer.setHeader(brand);

        final NavigationDestination orders = IkonliGraphics.destination("orders", "Orders", "mdi2c-clipboard-list-outline");
        orders.setBadgeText("12");

        final NavigationDestination archive = IkonliGraphics.destination("archive", "Archive", "mdi2a-archive-outline");
        archive.setDisable(true);
        archive.setTooltipText("Nothing archived yet");

        final NavigationGroup reports = new NavigationGroup("Reports",
                IkonliGraphics.of("mdi2c-chart-line"),
                IkonliGraphics.destination("daily", "Daily", "mdi2c-calendar-today"),
                IkonliGraphics.destination("weekly", "Weekly", "mdi2c-calendar-week"),
                IkonliGraphics.destination("monthly", "Monthly", "mdi2c-calendar-month"));

        final NavigationSection operations = new NavigationSection("Operations",
                orders,
                IkonliGraphics.destination("shipments", "Shipments", "mdi2t-truck-outline"),
                IkonliGraphics.destination("warehouse", "Warehouse", "mdi2w-warehouse"),
                reports);

        final NavigationSection masterData = new NavigationSection("Master data",
                IkonliGraphics.destination("customers", "Customers", "mdi2a-account-group-outline"),
                IkonliGraphics.destination("articles", "Articles", "mdi2p-package-variant-closed"),
                archive);
        // Folded to begin with, so the feature is visible without touching anything first.
        masterData.setExpanded(false);

        drawer.getItems().addAll(
                IkonliGraphics.destination("dashboard", "Dashboard", "mdi2v-view-dashboard-outline"),
                operations,
                new NavigationSeparator(),
                masterData);

        drawer.getFooterItems().addAll(
                IkonliGraphics.destination("settings", "Settings", "mdi2c-cog-outline"),
                IkonliGraphics.destination("help", "Help", "mdi2h-help-circle-outline"));

        return drawer;
    }

    private Region createContent(final NavigationDrawerPane pane) {

        final DrawerToggleButton toggle = new DrawerToggleButton(pane);

        final Label title = new Label();
        title.textProperty().bind(pane.effectiveDisplayModeProperty().asString());
        title.setStyle("-fx-opacity: 0.6;");

        final ComboBox<DrawerDisplayMode> mode = new ComboBox<>();
        mode.getItems().setAll(DrawerDisplayMode.values());
        mode.valueProperty().bindBidirectional(pane.displayModeProperty());

        final CheckBox dark = new CheckBox("Dark");
        dark.selectedProperty().addListener((obs, old, selected) -> applyDarkPalette(pane, selected));

        final Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        final ToolBar toolBar = new ToolBar(toggle, title, spacer, new Label("Display mode"), mode, dark);

        headline.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");
        final VBox body = new VBox(8, headline, description);
        body.setAlignment(Pos.CENTER);
        body.setPadding(new Insets(32));

        final BorderPane content = new BorderPane();
        content.setTop(toolBar);
        content.setCenter(new StackPane(body));
        return content;
    }

    private void showSelection(final NavigationDrawerPane pane, final NavigationDestination selected) {
        if (selected == null) {
            headline.setText("Nothing selected");
            description.setText("");
            return;
        }
        headline.setText(selected.getText());
        description.setText("Destination \"" + selected.getId() + "\" would be shown here.");
        if (pane.getEffectiveDisplayMode() == DrawerDisplayMode.MODAL) {
            // An overlay has done its job once something was picked.
            pane.close();
        }
    }

    private void applyDarkPalette(final NavigationDrawerPane pane, final boolean dark) {
        final Scene scene = pane.getScene();
        if (scene == null) {
            return;
        }
        final String stylesheet = NavigationDrawer.darkStylesheet();
        if (dark) {
            if (!scene.getStylesheets().contains(stylesheet)) {
                scene.getStylesheets().add(stylesheet);
            }
        } else {
            scene.getStylesheets().remove(stylesheet);
        }
    }

}
