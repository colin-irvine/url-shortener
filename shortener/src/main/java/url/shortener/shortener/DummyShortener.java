package url.shortener.shortener;

import java.util.HashMap;
import java.util.Map;

public class DummyShortener implements Shortener {
    // All this has to do is...
    // return short URLs
    // return long URLs
    // failed to get long URL
    // get short URL but fail to save
    private Map<String, String> urlMap;
    private boolean throwShortenException;

    public DummyShortener() {
        this.urlMap = new HashMap<>();
        this.urlMap.put("test", "value");
        this.throwShortenException = false;
    }

    @Override
    public String getShortName(String longName) throws Exception {
        String shortName = "short";
        if (this.throwShortenException) {
            throw new Exception("Unable to store Short Name");
        }

        this.urlMap.put(shortName, longName);

        return shortName;
    }

    @Override
    public String getLongName(String shortName) {
        return this.urlMap.get(shortName);
    }

    public void setUnableToShortenException(boolean flag) {
        this.throwShortenException = flag;
    }
}
