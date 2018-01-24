package rthttp;

import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;

/**
 * Created by Bartosz on 14.01.2018.
 */
public class RtURLStreamHandlerFactory implements URLStreamHandlerFactory {
    @Override
    public URLStreamHandler createURLStreamHandler(String protocol) {
        if("rt-http".equals(protocol)){
            return new RtURLStreamHandler();
        }
        return null;
    }
}
