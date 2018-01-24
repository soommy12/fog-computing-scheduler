package rthttp;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;

/**
 * Created by Bartosz on 14.01.2018.
 */
public class RtURLStreamHandler extends URLStreamHandler {

    @Override
    protected URLConnection openConnection(URL u) throws IOException {
        return new HttpRtURLConnection(u);
    }
}
