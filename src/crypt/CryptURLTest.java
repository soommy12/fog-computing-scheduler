package crypt;

import java.net.URL;
import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;

class OurURLStreamHandlerFactory implements URLStreamHandlerFactory {
    public URLStreamHandler createURLStreamHandler(String protocol) {
        if ( protocol.equalsIgnoreCase("crypt") )
            return new Handler();
        else
            return null;
    }
}

class CryptURLTest {


    public static void main(String argv[] ) throws Exception {

        URL.setURLStreamHandlerFactory(
                new OurURLStreamHandlerFactory());

        URL url = new URL("crypt:rot13//localhost:8080/");
        System.out.println( url.getContent() );
    }
}