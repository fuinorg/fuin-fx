package org.fuin.fx.navidraw;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import javafx.application.Platform;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

/**
 * Starts the JavaFX toolkit once for the whole test run and offers a way to execute code on the JavaFX
 * application thread and wait for it.
 * <p>
 * No robot and no X server are involved: the build selects JavaFX's own headless Glass with
 * {@code glass.platform=Headless} (see the surefire configuration in the POM), so the toolkit starts on a
 * machine without a display.
 */
public final class FxToolkitExtension implements BeforeAllCallback {

    private static final AtomicBoolean STARTED = new AtomicBoolean(false);

    private static final long TIMEOUT_SECONDS = 30;

    @Override
    public void beforeAll(final ExtensionContext context) throws InterruptedException {
        if (STARTED.compareAndSet(false, true)) {
            final CountDownLatch latch = new CountDownLatch(1);
            Platform.startup(latch::countDown);
            if (!latch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                throw new IllegalStateException("The JavaFX toolkit did not start within "
                        + TIMEOUT_SECONDS + " seconds");
            }
            Platform.setImplicitExit(false);
        }
    }

    /**
     * Runs the given code on the JavaFX application thread and returns after it finished.
     *
     * @param runnable Code to run, never {@code null}.
     */
    public static void runAndWait(final Runnable runnable) {
        if (Platform.isFxApplicationThread()) {
            runnable.run();
            return;
        }
        final CountDownLatch latch = new CountDownLatch(1);
        final AtomicReference<RuntimeException> unchecked = new AtomicReference<>();
        final AtomicReference<Error> error = new AtomicReference<>();
        Platform.runLater(() -> {
            try {
                runnable.run();
            } catch (final RuntimeException ex) {
                unchecked.set(ex);
            } catch (final Error ex) {
                error.set(ex);
            } finally {
                latch.countDown();
            }
        });
        try {
            if (!latch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                throw new IllegalStateException("Code on the JavaFX application thread did not finish within "
                        + TIMEOUT_SECONDS + " seconds");
            }
        } catch (final InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while waiting for the JavaFX application thread", ex);
        }
        if (error.get() != null) {
            throw error.get();
        }
        if (unchecked.get() != null) {
            throw unchecked.get();
        }
    }

}
