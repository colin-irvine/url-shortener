package url.shortener.handler;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import url.shortener.authentication.DummyAuthentication;
import url.shortener.shortener.DummyShortener;
import url.shortener.shortener.Shortener;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class HandlerTest {
    private DummyShortener shortener;
    private LambdaHandler lambdaHandler;
    private DummyAuthentication authentication;
    private APIGatewayProxyRequestEvent requestEvent;
    private APIGatewayProxyResponseEvent actual;

    @BeforeAll
    void setup() {
        this.shortener = new DummyShortener();
        this.authentication = new DummyAuthentication();
        this.lambdaHandler = new LambdaHandler(this.shortener, this.authentication);
        this.requestEvent = new APIGatewayProxyRequestEvent();
    }

    @Test
    void testExecuteReturns405WhenHttpMethodIsNotAllowed() {
        this.requestEvent.setHttpMethod("ANY");

        this.actual = this.lambdaHandler.handleRequest(this.requestEvent, null);

        assertEquals(405, this.actual.getStatusCode());
        assertEquals("HTTP Method not allowed", this.actual.getBody());
    }

    @Test
    void testExecuteReturns400WhenRequestBodyIsMalformed() {
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "test");
        this.authentication.setVerify(true);
        this.requestEvent.setHttpMethod("POST");
        this.requestEvent.setHeaders(headers);
        this.requestEvent.setBody("");
        this.requestEvent.setPath("www.57ad656d.execute.api/prod/request");
        // TODO: what should the body contain?
        // TODO: what headers are expected? Authorization and ?
        // TODO: Research Gson or other JSON usage

        this.actual = this.lambdaHandler.handleRequest(this.requestEvent, null);
        assertEquals(400, this.actual.getStatusCode());
        assertEquals("Bad request", this.actual.getBody());

    }

    @Test
    void testExecuteReturns400WhenRequestPathIsMalformed() {
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "test");
        this.authentication.setVerify(true);
        this.requestEvent.setHttpMethod("GET");
        this.requestEvent.setHeaders(headers);
        this.requestEvent.setBody("{\"test\": \"key\"}");
        this.requestEvent.setPath("");

        this.actual = this.lambdaHandler.handleRequest(this.requestEvent, null);
        assertEquals(400, this.actual.getStatusCode());
        assertEquals("Bad url path", this.actual.getBody());

    }

    @Test
    void testExecuteReturns401WhenBearerTokenIsMissing() {
        APIGatewayProxyResponseEvent actual;
        Map<String, String> headers = new HashMap<>();
        this.requestEvent.setHttpMethod("GET");
        this.requestEvent.setHeaders(headers);
        actual = this.lambdaHandler.handleRequest(this.requestEvent, null);

        assertEquals(401, actual.getStatusCode());
        assertEquals("No Authorization header present", actual.getBody());
    }

    @Test
    void testExecuteReturns403WhenTokenFailsVerification() {
        APIGatewayProxyResponseEvent actual;
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "test");
        this.authentication.setVerify(false);
        this.requestEvent.setHttpMethod("GET");
        this.requestEvent.setHeaders(headers);
        this.requestEvent.setPath("www.short.url/not present");
        actual = this.lambdaHandler.handleRequest(this.requestEvent, null);

        assertEquals(403, actual.getStatusCode());
        assertEquals("Unauthorized client", actual.getBody());
    }

    @Test
    void testExecuteReturns404WhenLongNameNotFound() {
        APIGatewayProxyResponseEvent actual;
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "test");
        this.authentication.setVerify(true);
        this.requestEvent.setHttpMethod("GET");
        this.requestEvent.setHeaders(headers);
        this.requestEvent.setPath("www.short.url/not present");

        actual = this.lambdaHandler.handleRequest(this.requestEvent, null);

        assertEquals(404, actual.getStatusCode());
        assertEquals("Long name not found",actual.getBody());
    }

    @Test
    void testExecuteReturns500WhenUnableToStoreShortName() {
        // storage should probably throw an exception for this if
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "test");
        this.authentication.setVerify(true);
        this.requestEvent.setHttpMethod("POST");
        this.requestEvent.setHeaders(headers);
        this.requestEvent.setPath("www.short.url/not present");
        this.requestEvent.setBody("shorten me please");
        this.shortener.setUnableToShortenException(true);

        this.actual = this.lambdaHandler.handleRequest(this.requestEvent, null);

        assertEquals(500, this.actual.getStatusCode());
        assertEquals("Unable to store Short Name", this.actual.getBody());
    }

    @Test
    void testExecuteReturns200WhenLongNameFound() {
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "test");
        this.authentication.setVerify(true);
        this.shortener.setUnableToShortenException(false);
        this.requestEvent.setHttpMethod("GET");
        this.requestEvent.setHeaders(headers);
        this.requestEvent.setPath("www.short.url/test");

        this.actual = this.lambdaHandler.handleRequest(this.requestEvent, null);

        assertEquals(200, this.actual.getStatusCode());
        assertEquals("value", this.actual.getBody());
    }
}
