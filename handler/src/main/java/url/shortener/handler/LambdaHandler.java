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
        // TODO: parse event and make sure it is good
        // TODO: for an event to be good, it must have a good path
        // TODO: handle auth

        this.setRequestEvent(event);

        if (isHttpMethodNotAllowed()) {
            return this.create405Response();
        }

        if (isAuthorizationHeaderNotPresent()) {
            return this.create401Response();
        }

        if (isNotAuthorized()) {
            return this.create403Response();
        }

        if (isShortenRequest()) {
            // method and auth are good, now let's try and shorten something!
            return this.handleShortenRequest();
        }

        if (isLengthenRequest()) {
            return this.handleLengthenRequest();
        }

        return responseEvent;
    }

    private void setRequestEvent(APIGatewayProxyRequestEvent event) {
        this.requestEvent = event;
    }

    private boolean isHttpMethodNotAllowed() {
        String method = this.requestEvent.getHttpMethod();

        return (method.equals("GET") || method.equals("POST")) == false;
    }

    private boolean isAuthorizationHeaderNotPresent() {
        Map<String, String> headers = this.requestEvent.getHeaders();

        return headers.containsKey("Authorization") == false;
    }

    private APIGatewayProxyResponseEvent create405Response() {
        String method = this.requestEvent.getHttpMethod();
        String body = String.format("HTTP Method \'%s\' is not allowed.", method);
        this.responseEvent.setStatusCode(405);
        this.responseEvent.setBody(body);

        return this.responseEvent;
    }

    private APIGatewayProxyResponseEvent create401Response() {
        String body = "Authorization header missing.";
        this.responseEvent.setStatusCode(401);
        this.responseEvent.setBody(body);

        return this.responseEvent;
    }

    private boolean isShortenRequest() {
        return this.requestEvent.getHttpMethod().equals("POST");
    }

    private boolean isLengthenRequest() {
        return this.requestEvent.getHttpMethod().equals("GET");
    }

    private APIGatewayProxyResponseEvent handleShortenRequest() {
        // make sure request is good (event body)
        // shorten Url
        // save to storage
        // could this be as simple as:
        // try {
        //      shortName = this.shortener.getShortName(longName);
        //
        // } catch (Something exc) { // handle state changes here }

        return null;
    }

    private APIGatewayProxyResponseEvent handleLengthenRequest() {
        // make sure request is good (event path)
        // retrieve from storage
        // this.shortener.getLongName(shortName);
        String shortName = this.extractShortNameFromPath();
        String longName = this.shortener.getLongName(shortName);

        if (longName == null) {
            this.responseEvent.setStatusCode(404);
            //this.responseEvent.setBody("body");
            return this.responseEvent;
        }

        this.responseEvent.setBody(longName);
        this.responseEvent.setStatusCode(200);

        return this.responseEvent;
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

    private APIGatewayProxyResponseEvent create403Response() {
        this.responseEvent.setStatusCode(403);

        return this.responseEvent;
    }
}
