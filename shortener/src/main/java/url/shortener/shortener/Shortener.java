package url.shortener.shortener;

public interface Shortener {
    String getShortName(String longName);
    String getLongName(String shortName);
}
