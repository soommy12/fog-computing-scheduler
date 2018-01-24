package server.sample;

import com.sun.net.httpserver.HttpServer;
import task.implementation.Task;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Bartosz on 07.01.2018.
 */
public class Server {

    //to serwer ma trzymać kolejki z zadaniami..
    public static List<Task> rtTasksList = new LinkedList<>();
    public static List<Task> nTasksList = new LinkedList<>();

    public static void main(String[] args) {
        int port = 8080;
        try {
            HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
            System.out.println("Server started at " + port + " port");
            server.createContext("/", new RootHandler()); // root handler to know if server started
            server.createContext("/echoHeader", new EchoHeaderHandler()); // test header to check additional headers
            server.createContext("/echoGet", new EchoGetHandler()); // don't need atm
            server.createContext("/echoPost"); // don't need atm
            server.createContext("/taskScheduler", new TaskSchedulerHandler()); // todo implement scheduler with LLF algorithm
            server.setExecutor(null); // don't know what is that atm
            server.start(); // actual start of the server
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
