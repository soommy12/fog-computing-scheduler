package crypt;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;

/**
 * Created by Bartosz on 15.01.2018.
 */
public class Handler extends URLStreamHandler {

    String cryptype;

    protected void parseURL(URL u, String spec, int start, int end) {
        int slash = spec.indexOf('/');
        cryptype = spec.substring(start, slash);
        start = slash;
        super.parseURL(u, spec, start, end);
    }
    @Override
    protected URLConnection openConnection(URL u) throws IOException {
        return new CryptURLConnection(u, cryptype);
    }
}
