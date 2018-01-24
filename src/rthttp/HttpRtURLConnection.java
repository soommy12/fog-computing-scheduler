package rthttp;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.HttpURLConnection;
import java.net.Socket;
import java.net.URL;

/**
 * Created by Bartosz on 14.01.2018.
 */
public class HttpRtURLConnection extends HttpURLConnection {

//    private HttpClient httpClient;

    private RtInputStream ris;


    /**
     * Constructor for the HttpURLConnection.
     * HttpRtURLConnection
     * @param u the URL
     */
    protected HttpRtURLConnection(URL u) throws IOException {
        super(u);
        String name = "rthttp.rotRtInputStream";
        try {
            ris = (RtInputStream) Class.forName(name).newInstance();
        } catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
            e.printStackTrace();
        }
//        this.httpClient = HttpClient.New(u, null, -1, false);
    }

    @Override
    public void disconnect() {

    }

    @Override
    public boolean usingProxy() {
        return false;
    }

    @Override
    public void connect() throws IOException {
        System.out.println("Connected from RT HTTP!");
//        this.in = this.httpClient.getInputStream();
        if(ris == null){
            throw new IOException("RT Class Not Found");
        }
        Socket s = new Socket(url.getHost(), url.getPort());

        //tutaj output stream do serwera?
        OutputStream server = s.getOutputStream();
        new PrintStream(server).println( "GET" + url.getHost());

        //ustawiamy input stream zeby pobrac to co wysyla serwer
        ris.set(s.getInputStream(), server);

        connected = true;
    }


    private static final String[] methods = {
            "GET", "POST", "HEAD", "OPTIONS", "PUT", "DELETE", "TRACE",
            "GETRT", "POSTRT"
    };

    @Override
    public InputStream getInputStream() throws IOException {
        System.out.println("Trying to get input...");
//        if(this.in != null){
//            System.out.println("done");
//            return this.in;
//        } else {
//            System.out.println("Cos nie dziala");
//            return null;
//        }
        if(!connected)
            connect();
        return ris;
    }
}
