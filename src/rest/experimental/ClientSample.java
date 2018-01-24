package rest.experimental;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;

/**
 * Created by Bartosz on 09.01.2018.
 */
public class ClientSample {

    public static void main(String[] args) throws IOException {
        /*URL url = new URL("http://localhost:8080/");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setDoOutput(true);
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Hard deadline", "3000");
        connection.setConnectTimeout(16000);
        connection.setReadTimeout(16000);*/

//        OutputStreamWriter out = new OutputStreamWriter(connection.getOutputStream());
//        out.write("Dapadam!");
//        out.close();
        InetAddress host = InetAddress.getLocalHost();
//        URL url = new URL(host.toString());
        URL url = new URL("http://localhost:8080/echoGet");
//        Socket socket = new Socket(host.getHostName(), 8080);
//        OutputStreamWriter os = new OutputStreamWriter(socket.getOutputStream());
////        os.write("Klient wita serwer..");
//        BufferedWriter bf = new BufferedWriter(os);
//        bf.write("Klient wita serwer..");
//        bf.close();
//        os.close();
//        Socket clientSocket = new Socket(host, 8080);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setDoOutput(true);
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Hard deadline", "3000");
        connection.setConnectTimeout(16000);
        connection.setReadTimeout(16000);

    }
}
