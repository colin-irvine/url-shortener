package url.shortener.authentication;

public class DummyAuthentication implements Authentication {
    private boolean verify;

    public DummyAuthentication() {
        this.verify = false;
    }

    @Override
    public boolean verifyToken() {
        return this.verify;
    }

    public void setVerify(boolean verify) {
        this.verify = verify;
    }
}
