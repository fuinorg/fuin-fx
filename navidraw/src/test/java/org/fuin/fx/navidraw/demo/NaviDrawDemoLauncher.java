package org.fuin.fx.navidraw.demo;

import javafx.application.Application;

/**
 * Starts {@link NaviDrawDemo}.
 * <p>
 * A separate class on purpose: the demo runs from the classpath, and JavaFX refuses to start when the class
 * named on the command line is itself an {@link Application} subclass.
 */
public final class NaviDrawDemoLauncher {

    private NaviDrawDemoLauncher() {
        throw new UnsupportedOperationException("It is not allowed to create an instance of a utility class");
    }

    /**
     * Starts the demo application.
     *
     * @param args Ignored.
     */
    public static void main(final String[] args) {
        Application.launch(NaviDrawDemo.class, args);
    }

}
