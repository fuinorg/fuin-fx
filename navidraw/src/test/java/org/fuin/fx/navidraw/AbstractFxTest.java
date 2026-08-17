package org.fuin.fx.navidraw;

import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.Region;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * Base class for tests that create JavaFX nodes.
 */
@ExtendWith(FxToolkitExtension.class)
abstract class AbstractFxTest {

    /**
     * Runs the given code on the JavaFX application thread and returns after it finished.
     *
     * @param runnable Code to run, never {@code null}.
     */
    static void onFxThread(final Runnable runnable) {
        FxToolkitExtension.runAndWait(runnable);
    }

    /**
     * Puts the node into a scene of the given size and lays it out.
     * <p>
     * The scene is never shown. That is enough for everything asserted here - skins are created, CSS is
     * applied and layout runs - and it keeps the tests free of a window and of animation timing, because
     * both the drawer and the pane skip their transitions while there is no scene to drive them.
     *
     * @param root   Node to lay out, never {@code null}.
     * @param width  Scene width in pixels.
     * @param height Scene height in pixels.
     *
     * @return The created scene, never {@code null}.
     */
    static Scene layout(final Region root, final double width, final double height) {
        final Scene scene = new Scene(root, width, height);
        // A scene that is never shown gets no pulse, so the root has to be sized by hand.
        root.resize(width, height);
        root.applyCss();
        root.layout();
        return scene;
    }

    /**
     * Applies CSS and lays the node out again, after something changed.
     *
     * @param root Node to lay out, never {@code null}.
     */
    static void relayout(final Parent root) {
        root.applyCss();
        root.layout();
    }

    /**
     * Creates a drawer with animations switched off, so that a size change is final immediately.
     *
     * @return New drawer, never {@code null}.
     */
    static NavigationDrawer newDrawer() {
        final NavigationDrawer drawer = new NavigationDrawer();
        drawer.setAnimated(false);
        return drawer;
    }

}
