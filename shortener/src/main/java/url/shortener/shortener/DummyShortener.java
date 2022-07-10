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

    public DummyShortener() {
        this.urlMap = new HashMap<>();
        this.urlMap.put("test", "value");
    }

    @Override
    public String getShortName(String longName) {
        String shortName = "short";
        this.urlMap.put(shortName, longName);

        return null;
    }

    @Override
    public String getLongName(String shortName) {
        return this.urlMap.get(shortName);
    }
}
