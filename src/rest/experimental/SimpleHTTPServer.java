package rest.experimental;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by Bartosz on 09.01.2018.
 */

/**
 * First attemption; server done using sockets only
 */
public class SimpleHTTPServer {

    public static void main(String[] args) throws IOException {
        final ServerSocket server = new ServerSocket(8080);
        System.out.println("Listening for connection on port 8080...");
        while (true) {

            final Socket client = server.accept();
            System.out.println("New connection!");
            InputStreamReader isr = new InputStreamReader(client.getInputStream());
            BufferedReader reader = new BufferedReader(isr);
            String line = reader.readLine();
//            System.out.println(line);
//            while (!line.isEmpty()){
            while (line != null){
                System.out.println(line);
                line = reader.readLine();
            }
        }
    }

}
