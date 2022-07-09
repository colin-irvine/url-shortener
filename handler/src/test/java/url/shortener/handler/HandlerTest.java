package url.shortener.handler;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class HandlerTest {

    @Test
    void testExecuteReturns400WhenRequestBodyIsMalformed() {
        assertFalse(true);
    }

    @Test
    void testExecuteReturns401WhenBearerTokenIsMissing() {
        assertFalse(true);
    }

    @Test
    void testExecuteReturns403WhenTokenFailsVerification() {
        assertFalse(true);
    }

    @Test
    void testExecuteReturns404WhenLongNameNotFound() {
        assertFalse(true);
    }

    @Test
    void testExecuteReturns500WhenUnableToStoreShortName() {
        assertFalse(true);
    }

    @Test
    void testExecuteReturns200WhenLongNameFound() {
        assertFalse(true);
    }
}
