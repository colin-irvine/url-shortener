package url.shortener.shortener;

public interface Shortener {
    String getShortName(String longName) throws Exception;
    String getLongName(String shortName);
}
