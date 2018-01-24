package crypt;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.Socket;
import java.net.URL;
import java.net.URLConnection;

/**
 * Created by Bartosz on 15.01.2018.
 */
public class CryptURLConnection extends URLConnection {

    CryptInputStream cis;
    static int defaultPort = 80;

    CryptURLConnection(URL url, String cryptype) {
        super(url);
        try {
            String name = "crypt." + cryptype + "CryptInputStream";
            cis = (CryptInputStream) Class.forName(name).newInstance();
        } catch ( Exception e) {}
    }

    @Override
    synchronized public void connect() throws IOException {
        int port;
        if ( cis == null) throw new IOException("Crypt class not found");
        if( (port = url.getPort()) == -1) port = defaultPort;
        Socket s = new Socket( url.getHost(), port);

        // Sned the filename in plainttext
        OutputStream server = s.getOutputStream();
        new PrintStream( server).println( "GET" + url.getFile());

        //Initialize the CrtpyInputStream
        cis.set(s.getInputStream(), server);
        connected = true;
        }

    synchronized public InputStream getInputStream() throws IOException {
        if(!connected)
            connect();
        return (cis);
    }

    public String getContentType() {
        return guessContentTypeFromName( url.getFile());
    }

}


