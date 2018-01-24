package server.sample;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.*;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by Bartosz on 09.01.2018.
 */
public class EchoHeaderHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        Headers headers = httpExchange.getRequestHeaders();

        Set<Map.Entry<String, List<String>>> entries = headers.entrySet();
        String response = "";
        for(Map.Entry<String, List<String>> entry : entries) {
            response += entry.toString() + "\n";
            System.out.println();
        }
        httpExchange.sendResponseHeaders(200, response.length());
        OutputStream os = httpExchange.getResponseBody();
        os.write(response.getBytes());
        os.close();
    }
}
