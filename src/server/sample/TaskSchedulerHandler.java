package server.sample;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import task.implementation.Task;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Bartosz on 17.01.2018.
 */
public class TaskSchedulerHandler implements HttpHandler {

    private static int clientCounter = 1;
    private static int averageSolvingTime = 0;

    //Urls...
//        String fogServ1URL = "http://192.168.1.109:8080/";
//        String fogServ2URL = "http://192.168.1.109:8080/";
//        String fogServ1URL = "http://10.0.8.124:8080/"; // host w pracy
//        String fogServ2URL = "http://10.0.8.124:8080/"; // host w pracy
//    private String fogServ1URL = "http://192.168.43.98:8080/"; // host na tel
//    private String fogServ2URL = "http://192.168.43.244:8080/"; // host na tel
    private String fogServ1URL = "http://192.168.1.115:8080/"; // host w mieszkaniu
    private String fogServ2URL = "http://192.168.1.109:8080/"; // host w mieszkaniu
    private String isFogServ1Busy = "http://192.168.43.98:8080/isBusy"; //busy na tel
    private String isFogServ2Busy = "http://192.168.43.244:8080/isBusy"; // busy na tel

    //Fog server urls list...
    private List<URL> fogServerURLsList = new ArrayList<>();


    public TaskSchedulerHandler() throws IOException {
        //chyba lepiej zostac przy Liscie URL
        fogServerURLsList.add(new URL(fogServ1URL));
        fogServerURLsList.add(new URL(fogServ2URL));
    }

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {

        Headers headers = httpExchange.getRequestHeaders();

        //Prepare to print
        String hardDeadline = null;
        String softDeadline = null;
        int deadline = 0;
        boolean isRt; // real time task flag
        boolean isHard = false; //  hard/soft deadline flag
        if(headers.containsKey("Hard-deadline")){
            hardDeadline = headers.get("Hard-deadline").get(0);
            isRt = true;
        } else if(headers.containsKey("Soft-deadline")) {
            softDeadline = headers.get("Soft-deadline").get(0);
            isRt = true;
        } else isRt = false;

        String method = httpExchange.getRequestMethod();
        String template = "\nClient no. %s connected!   method type: %s ";

        //Printing all client requests
        if(hardDeadline != null){
            deadline = Integer.valueOf(hardDeadline);
            method = method.concat("RT");
            isHard = true;
            System.out.println(String.format(template, String.valueOf(clientCounter), method).concat(String.format(" Hard Deadline: %s", hardDeadline)));
        } else if (softDeadline != null){
            deadline = Integer.valueOf(softDeadline);
            method = method.concat("RT");
            System.out.println(String.format(template, String.valueOf(clientCounter), method).concat(String.format(" Soft Deadline: %s", softDeadline)));
        } else {
            System.out.println(String.format(template, String.valueOf(clientCounter), method));
        }

        //tak odbieramy taska
        Task t;
        ObjectInputStream ois = new ObjectInputStream(httpExchange.getRequestBody());
        try {
            System.out.print("Recieved object:");
            t = (Task) ois.readObject();
            t.setDeadline(deadline);
            t.setHard(isHard);
            System.out.print(" not sorted array: ");
            int[] arr = (int[]) t.getData();
            for (int anArr : arr) {
                System.out.print(anArr + " ");
            }
            ois.close();

            sendResponse(220, httpExchange);

            HttpURLConnection test = (HttpURLConnection) new URL(fogServ1URL).openConnection();
            test.setDoOutput(true);
            System.out.println("test__1");
            ObjectOutputStream stream = new ObjectOutputStream(test.getOutputStream());
            stream.flush();
            System.out.println("test__2");
            stream.writeObject(t);
            System.out.println("test__3");
            stream.close();
            System.out.println("test__4");
            test.getResponseCode();
            System.out.println("test__5");

//            /**
//             * Algorytm LLF
//             */
//            if(isRt){
//                if(Server.rtTasksList.isEmpty()){ // jezeli kolejka RT jest pusta to na pewno uda sie w deadline obliczyc zadanie
//                    System.out.print("\n!! RT Tasks list EMPTY! ");
//                    System.out.println("Need to compound average solving time...");
//                    sendResponse(220, httpExchange);
//
//                    //na potrzeby wstepnej implementacji przyjmuje
//                    this.averageSolvingTime = 1200;  //ale to trzeba jakoś obliczyć
//                    //a teraz obliczam laxity
//                    t.setLaxity(deadline - averageSolvingTime);
//                    Server.rtTasksList.add(t);
//
//                    HttpURLConnection avilableFogServer = (HttpURLConnection)new URL(fogServ1URL).openConnection();
//                    avilableFogServer.setDoOutput(true);
//                    ObjectOutputStream oos = new ObjectOutputStream(avilableFogServer.getOutputStream());
//                    oos.writeObject(t);
//                    oos.close();
//                    int i = avilableFogServer.getResponseCode(); //pobrac responsa NA SAMYM KONCU!!!
//                    System.out.println("Response code: " + i);
//                }
//               /* else {
//                    System.out.println("\nNext RT Task...");
//                    //kolejne zadania RT
//                    t.setLaxity(deadline - averageSolvingTime);
//                    System.out.println("next lax: " + t.getLaxity());
//                    Server.rtTasksList.add(t);
//                    Server.rtTasksList.sort(new Task.laxityComparator());
//                    //dla kazdej harda w liscie sprawdzamy czy da sie wykonac zadanie
//                    for(Task currentRtTask : Server.rtTasksList){
//                        System.out.println("first for");
//                        while(!currentRtTask.isHard()){
//                            System.out.println("in while");
//                            HttpURLConnection avilableFogServer;
//                            for(URL url : fogServerURLsList){
//                                avilableFogServer = (HttpURLConnection) url.openConnection();
//                                //jezeli jest wolny serwer i i laxity jest wieksze to wysylamy zadanie
//                                boolean isFree = InetAddress.getByName(url.toString()).isReachable(1);
//                                if(InetAddress.getByName(url.toString()).isReachable(1)){
////                                    if(t.getLaxity() > )
//                                }
//                                if( isFree && t.getLaxity() > 0){
//                                    avilableFogServer.setDoOutput(true);
//                                    ObjectOutputStream oos = new ObjectOutputStream(avilableFogServer.getOutputStream());
//                                    oos.writeObject(t);
//                                    oos.close();
//                                    //pobrac responsa NA SAMYM KONCU!!!
//                                    avilableFogServer.getResponseCode();
//                                    break;
//                                } else { // jezeli nie da sie w w tym czasie obliczyc to
//                                    if(t.isHard()){ // jezeli to jest hard deadline to nic nie mozemy zrobic, wysylamy odpowiedniego responsa i usuwamy go z listy
//                                        Server.rtTasksList.remove(currentRtTask);
//                                        sendResponse(120, httpExchange);
//                                    }
//                                }
//                            }
//                        }
//                    }
//                }*/
//            } /*else {
//                // jezeli zadanie nie jest typu RT to wrzucamy do zwyklej kolejki
//                // i w responsie tez oznajmiamy ze uda sie nam obliczyć zadanie
//                sendResponse(220, httpExchange);
//                Server.nTasksList.add(t); //placeholder
//            }*/
//
//
////            ois.close();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        //Kiedy i jak wykonywac zadania?

//        /**
//         * Connection tests
//         */
//        HttpURLConnection avilableFogServer;
//        for(URL url : fogServerURLsList){
//            avilableFogServer = (HttpURLConnection) url.openConnection();
//            avilableFogServer.getResponseCode();
//        }

        //Wypisanie headerów do klienta; zamiast headerów trzeba bedzie odpowiedni response zwrocic

        /*Set<Map.Entry<String, List<String>>> entries = headers.entrySet();
        String response = "";
        for(Map.Entry<String, List<String>> entry : entries) {
            response += entry.toString() + "\n";
            System.out.println();
        }
        httpExchange.sendResponseHeaders(200, response.length());
        OutputStream os = httpExchange.getResponseBody();
        os.write(response.getBytes());
        os.close();*/
    }

    private void sendResponse(int responseCode, HttpExchange httpExchange) throws IOException {
        String type;
        if(responseCode == 120){
            type = "Server Timeout";
        } else if (responseCode == 220){
            type = "Constraint Satisfied";
        } else {
            System.out.println("Wrong responsee");
            return;
        }
//        String response = "Constraint Satisfied for client no. " + clientCounter;
        String response = type + " for client no. " + clientCounter;
        httpExchange.sendResponseHeaders(responseCode, response.length());
        OutputStream os = httpExchange.getResponseBody();
        os.write(response.getBytes());
        os.close();
        clientCounter++;
    }
}
