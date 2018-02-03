package server.sample;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by Bartosz on 07.01.2018.
 */
public class RootHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
//        String response = "<h1>Serwer wystartowal pomyslnie!</h1>";
//        httpExchange.sendResponseHeaders(200, response.getBytes().length);
        System.out.println("User connected from web browser");
        Headers headers = httpExchange.getRequestHeaders();
         Set<Map.Entry<String, List<String>>> entries = headers.entrySet();
        String response = "";
        for(Map.Entry<String, List<String>> entry : entries) {
            response += entry.toString() + "\n";
            System.out.println(response);
        }
        httpExchange.sendResponseHeaders(200, response.length());
        OutputStream os = httpExchange.getResponseBody();
        os.write(response.getBytes());
        os.close();
//        OutputStream os = httpExchange.getResponseBody();
//        os.write(response.getBytes());
//        os.close();
    }
}
