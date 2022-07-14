package url.shortener.handler;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import url.shortener.authentication.Authentication;
import url.shortener.shortener.Shortener;

import java.util.Map;

public class LambdaHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {
    private APIGatewayProxyResponseEvent responseEvent;
    private APIGatewayProxyRequestEvent requestEvent;
    private Shortener shortener;
    private Authentication authentication;

    public LambdaHandler(Shortener shortener, Authentication authentication) {
        this.responseEvent = new APIGatewayProxyResponseEvent();
        this.shortener = shortener;
        this.authentication = authentication;
    }

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent event, Context context) {
        setRequestEvent(event);
        try {
            checkRequest();
            checkAuthentication();

            if (isShortenRequest()) {
                handleShortenRequest();
            }
            if (isLengthenRequest()) {
                handleLengthenRequest();
            }

        }
        catch (HttpException exc) {
            createErrorResponse(exc);
        }
        catch (Exception exc) {
            createErrorResponse(exc);
        }
        finally {
            return responseEvent;
        }
    }

    private void setRequestEvent(APIGatewayProxyRequestEvent event) {
        this.requestEvent = event;
    }

    private void checkRequest() throws HttpException {
        if (isHttpMethodNotAllowed()) {
            throw new HttpException("HTTP Method not allowed", 405);
        }

        if (isEventBodyBad()) {
            throw new HttpException("Bad request", 400);
        }

        if (isEventPathBad()) {
            throw new HttpException("Bad url path", 400);
        }
    }

    private boolean isHttpMethodNotAllowed() {
        String method = this.requestEvent.getHttpMethod();

        return (method.equals("GET") || method.equals("POST")) == false;
    }

    private boolean isEventBodyBad() {
        if (this.requestEvent.getHttpMethod() == "GET") {
            return false;
        }
        if (this.requestEvent.getBody().isBlank()) {
            return true;
        }

        return false;
    }

    private boolean isEventPathBad() {
        return this.extractShortNameFromPath().isBlank();
    }

    private void checkAuthentication() throws HttpException {
        if (isAuthorizationHeaderNotPresent()) {
            throw new HttpException("No Authorization header present", 401);
        }

        if (isNotAuthorized()) {
            throw new HttpException("Unauthorized client", 403);
        }
    }

    private boolean isAuthorizationHeaderNotPresent() {
        Map<String, String> headers = this.requestEvent.getHeaders();

        return headers.containsKey("Authorization") == false;
    }

    private void handleShortenRequest() throws Exception {
        String shortName, longName = requestEvent.getBody();
        shortName = shortener.getShortName(longName);
        responseEvent.setBody(shortName);
        responseEvent.setStatusCode(200);
    }

    private void handleLengthenRequest() throws Exception {
        String longName, shortname = extractShortNameFromPath();
        longName = shortener.getLongName(shortname);
        responseEvent.setBody(longName);
        responseEvent.setStatusCode(200);

        if (longName == null) {
            throw new HttpException("Long name not found", 404);
        }

    }

    private boolean isShortenRequest() {
        return this.requestEvent.getHttpMethod().equals("POST");
    }

    private boolean isLengthenRequest() {
        return this.requestEvent.getHttpMethod().equals("GET");
    }

    private String extractShortNameFromPath() {
        String path = this.requestEvent.getPath();
        if (path.lastIndexOf('/') != -1)
            return path.substring(path.lastIndexOf('/') + 1);

        return path;
    }

    private boolean isNotAuthorized() {
        return this.authentication.verifyToken() == false;
    }

    private class HttpException extends Exception {
        private String errorMessage;
        private int statusCode;
        public HttpException(String errorMessage, int statusCode) {
            super(errorMessage);
            this.errorMessage = errorMessage;
            this.statusCode = statusCode;
        }
    }

    private APIGatewayProxyResponseEvent createErrorResponse(HttpException exc) {
        this.responseEvent.setStatusCode(exc.statusCode);
        this.responseEvent.setBody(exc.errorMessage);

        return this.responseEvent;
    }

    private APIGatewayProxyResponseEvent createErrorResponse(Exception exc) {
        this.responseEvent.setBody(exc.getMessage());
        this.responseEvent.setStatusCode(500);
        return this.responseEvent;
    }
}
