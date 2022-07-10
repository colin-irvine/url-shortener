package url.shortener.handler;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import url.shortener.authentication.Authentication;
import url.shortener.authentication.DummyAuthentication;
import url.shortener.shortener.DummyShortener;
import url.shortener.shortener.Shortener;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class HandlerTest {
    private Shortener shortener;
    private LambdaHandler lambdaHandler;
    private DummyAuthentication authentication;

    @BeforeAll
    void setup() {
        this.shortener = new DummyShortener();
        this.authentication = new DummyAuthentication();
        this.lambdaHandler = new LambdaHandler(this.shortener, this.authentication);
    }

    @Test
    void testExecuteReturns405WhenHttpMethodIsNotAllowed() {
        APIGatewayProxyRequestEvent event = new APIGatewayProxyRequestEvent();
        APIGatewayProxyResponseEvent actual;
        event.setHttpMethod("ANY");

        actual = this.lambdaHandler.handleRequest(event, null);
        assertEquals(405, actual.getStatusCode());
        assertEquals("HTTP Method \'ANY\' is not allowed.", actual.getBody());
    }

    @Test
    void testExecuteReturns400WhenRequestBodyIsMalformed() {
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "test");
        APIGatewayProxyRequestEvent event = new APIGatewayProxyRequestEvent();
        APIGatewayProxyResponseEvent expected, actual;
        expected = new APIGatewayProxyResponseEvent();
        this.authentication.setVerify(true);
        event.setHttpMethod("POST");
        event.setHeaders(headers);
        event.setBody(null);
        // what should the body contain?
        // what headers are expected? Authorization
        // what is the process for turning a body into an object?

        actual = this.lambdaHandler.handleRequest(event, null);
        assertEquals(400, actual.getStatusCode());

    }

    @Test
    void testExecuteReturns401WhenBearerTokenIsMissing() {
        APIGatewayProxyRequestEvent event = new APIGatewayProxyRequestEvent();
        APIGatewayProxyResponseEvent actual;
        Map<String, String> headers = new HashMap<>();
        //headers.put("Authorization", "test");
        event.setHttpMethod("GET");
        event.setHeaders(headers);
        actual = this.lambdaHandler.handleRequest(event, null);

        assertEquals(401, actual.getStatusCode());
        assertEquals("Authorization header missing.", actual.getBody());
    }

    @Test
    void testExecuteReturns403WhenTokenFailsVerification() {
        APIGatewayProxyRequestEvent event = new APIGatewayProxyRequestEvent();
        APIGatewayProxyResponseEvent actual;
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "test");
        this.authentication.setVerify(false);
        event.setHttpMethod("GET");
        event.setHeaders(headers);
        event.setPath("www.short.url/not present");
        actual = this.lambdaHandler.handleRequest(event, null);

        assertEquals(403, actual.getStatusCode());
    }

    @Test
    void testExecuteReturns404WhenLongNameNotFound() {
        APIGatewayProxyResponseEvent actual;
        APIGatewayProxyRequestEvent event = new APIGatewayProxyRequestEvent();
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "test");
        this.authentication.setVerify(true);
        event.setHttpMethod("GET");
        event.setHeaders(headers);
        event.setPath("www.short.url/not present");

        actual = this.lambdaHandler.handleRequest(event, null);

        assertEquals(404, actual.getStatusCode());
    }

    @Test
    void testExecuteReturns500WhenUnableToStoreShortName() {
        assertFalse(true);
    }

    @Test
    void testExecuteReturns200WhenLongNameFound() {
        APIGatewayProxyResponseEvent actual;
        APIGatewayProxyRequestEvent event = new APIGatewayProxyRequestEvent();
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "test");
        this.authentication.setVerify(true);
        event.setHttpMethod("GET");
        event.setHeaders(headers);
        event.setPath("www.short.url/test");

        actual = this.lambdaHandler.handleRequest(event, null);

        assertEquals(200, actual.getStatusCode());
        assertEquals("value", actual.getBody());
    }
}
