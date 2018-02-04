package server.sample;

import com.sun.net.httpserver.HttpServer;
import task.implementation.Task;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URL;
import java.util.*;
import java.util.concurrent.Executors;

/**
 * Created by Bartosz on 07.01.2018.
 */
public class Server {

    //Server keeps all tasks list
    static List<Task> rtHardTasksList = new LinkedList<>();
    static List<Task> rtSoftTasksList = new LinkedList<>();
    static List<Task> normalTasksList = new LinkedList<>();

    static Map<Integer, URL> fogServersURLsMap = new HashMap<>(); //Fog servers urls map
    static Map<Integer, Integer> fogServersFinishTimeMap = new HashMap<>(); //Fog servers finish time map

    public static void main(String[] args) {
        int port = 8080;
        try {

            //urls to edge nodes
            String fogServ1URL = "http://192.168.1.107:8080/";
            String fogServ2URL = "http://192.168.1.111:8080/";

            //filling maps
            fogServersURLsMap.put(1, new URL(fogServ1URL));
            fogServersURLsMap.put(2, new URL(fogServ2URL));
            fogServersFinishTimeMap.put(1, 0);
            fogServersFinishTimeMap.put(2, 0);

            HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
            System.out.println("Server started at " + port + " port");
            server.createContext("/", new RootHandler()); // root handler to know if server started
            server.createContext("/taskScheduler", new TaskSchedulerHandler()); // task scheduler handler
            server.setExecutor(Executors.newCachedThreadPool()); // let server deal with more than 1 request
            server.start();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
