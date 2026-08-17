package org.fuin.fx.navidraw;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.kordamp.ikonli.javafx.FontIcon;

/**
 * Tests for {@link IkonliGraphics}. The icon pack is a test dependency here, exactly as it is an optional
 * one for consumers.
 */
class IkonliGraphicsTest extends AbstractFxTest {

    @Test
    void testIconFromLiteral() {
        onFxThread(() -> {

            // TEST
            final FontIcon icon = (FontIcon) IkonliGraphics.of("mdi2h-home");

            // VERIFY
            assertThat(icon.getIconLiteral()).isEqualTo("mdi2h-home");
            assertThat(icon.getStyleClass()).contains("ikonli-font-icon");
        });
    }

    @Test
    void testDestinationFromLiteral() {
        onFxThread(() -> {

            // TEST
            final NavigationDestination testee = IkonliGraphics.destination("home", "Home", "mdi2h-home");

            // VERIFY
            assertThat(testee.getId()).isEqualTo("home");
            assertThat(testee.getText()).isEqualTo("Home");
            assertThat(testee.getGraphic()).isInstanceOf(FontIcon.class);
        });
    }

    @Test
    void testUnknownLiteralFails() {
        onFxThread(() -> {
            // TEST & VERIFY
            assertThatThrownBy(() -> IkonliGraphics.of("no-such-icon-at-all"))
                    .isInstanceOf(UnsupportedOperationException.class)
                    .hasMessageContaining("no-such-icon-at-all");
        });
    }

}
