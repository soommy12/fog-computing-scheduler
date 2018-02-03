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

    //to serwer ma trzymać kolejki z zadaniami.. NIE! Każdy serwer posiada osobne kolejki...
    static List<Task> rtHardTasksList = new LinkedList<>();
    static List<Task> rtSoftTasksList = new LinkedList<>();
    static List<Task> rtNormalTasksList = new LinkedList<>();

    //Fog server urls list... ale moze sie okazac ze trzeba mape!
    static List<URL> fogServerURLsList = new ArrayList<>();
    // jednak mapa
    static Map<Integer, URL> fogServersURLsMap = new HashMap<>();
    static Map<Integer, Integer> fogServersFinishTimeMap = new HashMap<>();

    public static void main(String[] args) {
        int port = 8080;
        boolean isServerFree;
        try {

            //urls to edge nodes
            String fogServ1URL = "http://192.168.1.116:8080/";
            String fogServ2URL = "http://192.168.1.109:8080/";
            //filling list
            fogServerURLsList.add(new URL(fogServ1URL));
            fogServerURLsList.add(new URL(fogServ2URL));
            //filling maps
            fogServersURLsMap.put(1, new URL(fogServ1URL));
            fogServersURLsMap.put(2, new URL(fogServ2URL));
            fogServersFinishTimeMap.put(1, 0);
            fogServersFinishTimeMap.put(2, 0);

            HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
            System.out.println("Server started at " + port + " port");
            server.createContext("/", new RootHandler()); // root handler to know if server started
            server.setExecutor(Executors.newCachedThreadPool()); // let server deal with more than 1 request
            server.start(); // actual start of the server

            //tu bedziemy wrzucac zadania do wykonywania
//            while(true){
//                System.out.println("n size: " + nTasksList.size());
//                if(!rtTasksList.isEmpty()){
//                    System.out.println("RT lista nie jest pusta!");
//                } else if (!nTasksList.isEmpty()){
//                    System.out.println("FIFO lista nie jest pusta!");
//                    for(int i = 0; i < nTasksList.size(); i++){
//                        for(URL url : fogServerURLsList){
//                            HttpURLConnection availableServer = (HttpURLConnection) url.openConnection();
//                            if(InetAddress.getByName(url.toString()).isReachable(0)){
//                                availableServer.setDoOutput(true);
//                                ObjectOutputStream oos = new ObjectOutputStream(availableServer.getOutputStream());
//                                oos.writeObject(nTasksList.get(i));
//                                oos.close();
//                                nTasksList.remove(i);
//                                break;
//                            }
//
//                        }
//                    }
//                }
//            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
