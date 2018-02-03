package server.sample;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import task.implementation.Task;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Map;

/**
 * Created by Bartosz on 17.01.2018.
 */
public class TaskSchedulerHandler implements HttpHandler {

    private static int clientCounter = 1;
    private static int averageSolvingTime = 0;

    private int fs1FinishTime = 0;
    private int fs2FinishTime = 0;

    private int fibAvTime = 1100;
    private int strongAvTime = 1400;
    private int minTime;
    private int minID;


    //Urls...
    private String fogServ1URL = "http://192.168.1.116:8080/"; // host w mieszkaniu
    private String fogServ2URL = "http://192.168.1.109:8080/"; // host w mieszkaniu

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {

        Headers headers = httpExchange.getRequestHeaders();

        //Prepare needed values
        String hardDeadline = null;
        String softDeadline = null;
        int deadline = 0;
        int tFinish = 0;
        int tArr = 0;
        int taskRange = 0;
        String taskType = null;
        boolean isRt; // real time task flag
        boolean isHard = false; //  hard/soft deadline flag
        if(headers.containsKey("Task-range"))
            taskRange = Integer.parseInt(headers.get("Task-range").get(0));
        if(headers.containsKey("Task-type"))
            taskType = headers.get("Task-type").get(0);
        if(taskType.equals("fibo")) averageSolvingTime = fibAvTime;
        else averageSolvingTime = strongAvTime;
        if(headers.containsKey("Arrival-time"))
            tArr = Integer.parseInt(headers.get("Arrival-time").get(0));
        if(headers.containsKey("Hard-deadline")){
            isRt = true;
            isHard = true;
            hardDeadline = headers.get("Hard-deadline").get(0);
            deadline = Integer.parseInt(hardDeadline);
        } else if(headers.containsKey("Soft-deadline")) {
            isRt = true;
            softDeadline = headers.get("Soft-deadline").get(0);
            deadline = Integer.parseInt(softDeadline);
        } else isRt = false;

        String method = httpExchange.getRequestMethod();
        String template = "\nClient no. %s connected!   method type: %s ";

        //Printing all client requests
        if(hardDeadline != null){
            System.out.println(String.format(template, String.valueOf(clientCounter), method + "RT").concat(String.format(" Hard Deadline: %s", deadline)));
        } else if (softDeadline != null){
            System.out.println(String.format(template, String.valueOf(clientCounter), method + "RT").concat(String.format(" Soft Deadline: %s", deadline)));
        } else {
            System.out.println(String.format(template, String.valueOf(clientCounter), method));
        }

        //Testing responses
        String response;
        if(isRt){
            if(isHard) response = "Response for client no. " + clientCounter + " identified as HARD";
            else response = "Response for client no. " + clientCounter + " identified as SOFT";
        } else response = "Response for client no. " + clientCounter + " identified as NORMAL";
        httpExchange.sendResponseHeaders(200, response.length());
        OutputStream os = httpExchange.getResponseBody();
        os.write(response.getBytes());
        os.close();
        clientCounter++;


        //tak odbieramy taska - nowy sposób, nie potrzeba tworzyc przesylac instancji taska
//        Task t;
//        try (ObjectInputStream ois = new ObjectInputStream(httpExchange.getRequestBody())) {
//        try {

            //Testy Object Mappera
//            InputStream is = httpExchange.getRequestBody();
//            ObjectMapper mapper = new ObjectMapper();
//            t = mapper.readValue(is, Task.class);
//            is.close();
//            System.out.println("odebrano: " + t.toString());
//
//            sendResponse(220,httpExchange);
//            System.out.print("Recieved object: ");
//            t = (Task) ois.readObject();
//            t.setDeadline(deadline);
//            t.setHard(isHard);
//            System.out.print(" not sorted array: ");
//            int[] arr = (int[]) t.getData();
//            for (int anArr : arr) {
//                System.out.print(anArr + " ");
//            }
//            ois.close();
//            sendResponse(220,httpExchange);
            /**
             * Algorytm LLF
             */

            Task t = new Task();
            int laxity;
            if(isRt){
                laxity = deadline - averageSolvingTime;
                t.setLaxity(laxity);
                if(laxity<0) sendResponse(420, httpExchange);
                else {
                    if(isHard){
                        if(this.minTime - tArr <= laxity){
                            Server.rtHardTasksList.add(t);
                            //szereguje
                            Collections.sort(Server.rtHardTasksList);
                            //jezeli w liscie jest to akutlany najmniejszy
                            findCurrentMinFinishTimeServer();
                            Server.fogServersFinishTimeMap.put(
                                    this.minID,
                                    Server.fogServersFinishTimeMap.get(this.minID) + averageSolvingTime
                            );
                            HttpURLConnection connection = (HttpURLConnection) Server.fogServersURLsMap.get(this.minID).openConnection();
                            connection.setRequestProperty("Task-type", taskType);
                            if(taskRange !=0)
                                connection.setRequestProperty("Task-range", String.valueOf(taskRange));
                            //odczytac responsa z tego connection
                            //wyslac responsa do klienta
                            Server.rtHardTasksList.remove(t);
                        } else sendResponse(120, httpExchange);
                    } else {
                        //tutaj requesty soft
                    }
                }
            } else {
                //tutaj requesty normalne
                if(Server.rtHardTasksList.isEmpty() && Server.rtSoftTasksList.isEmpty()){
                    Server.fogServersFinishTimeMap.put(
                            this.minID,
                            Server.fogServersFinishTimeMap.get(this.minID) + averageSolvingTime
                    );
                }


            }
//            if (isRt) {
//                //na razie bez obliczania czasu zadania
//                averageSolvingTime = 1200;  //ale to trzeba jakoś obliczyć
////                sendResponse(220, httpExchange);
//                t.setLaxity(deadline - averageSolvingTime); //obliczanie laxity
//                Server.rtTasksList.add(t);
//                System.out.println("Added to RT list");
//
//                System.out.println("Headery:");
//                Set<Map.Entry<String, List<String>>> entries = headers.entrySet();
//                String headerstoPrint = "";
//                for(Map.Entry<String, List<String>> entry : entries)
//                    headerstoPrint += entry.toString() + "\n";
//                System.out.println(headerstoPrint);
//                sendResponse(220,httpExchange);
//
//                //przesylam do serwera obliczeniowego
////                HttpURLConnection avilableFogServer = (HttpURLConnection) new URL(fogServ1URL).openConnection();
////                avilableFogServer.setDoOutput(true);
////                avilableFogServer.setDoInput(true);
////                avilableFogServer.setRequestMethod(method); // czy potrzebne?
////                ObjectOutputStream oos = new ObjectOutputStream(avilableFogServer.getOutputStream());
////                oos.writeObject(t);
////                oos.close();
////                System.out.println("4");
//////                avilableFogServer.getInputStream();
////                int i = avilableFogServer.getResponseCode(); //pobrac responsa NA SAMYM KONCU!!!
////                System.out.println("Response code: " + i);
//            } else {
//                // jezeli zadanie nie jest typu RT to wrzucamy do zwyklej kolejki
//                // i w responsie tez oznajmiamy ze uda sie nam obliczyć zadanie
//                System.out.println("Scheduler putting normal task to list..");
//                sendResponse(220, httpExchange);
//                Server.nTasksList.add(t); //placeholder
//            }
////            ois.close();
//        } catch (ClassNotFoundException e) {
////        } catch (Exception e) {
//            e.printStackTrace();
//        }


        //Kiedy i jak wykonywac zadania? juz wiem w teorii
    }

    private void sendResponse(int responseCode, HttpExchange httpExchange) throws IOException {
        String type;
        if(responseCode == 120){
            type = "Server Timeout";
        } else if (responseCode == 220){
            type = "Constraint Satisfied";
        } else if (responseCode == 420){
          type = "Wrong deadline";
        } else {
            System.out.println("Wrong responsee");
            return;
        }

        String response = type + " for client no. " + clientCounter;
        httpExchange.sendResponseHeaders(responseCode, response.length());
        OutputStream os = httpExchange.getResponseBody();
        os.write(response.getBytes());
        os.close();
        clientCounter++;
    }

    private void findCurrentMinFinishTimeServer(){
        this.minTime = Collections.min(Server.fogServersFinishTimeMap.values());
        Map.Entry<Integer, Integer> min = null;
        for(Map.Entry<Integer, Integer> entry : Server.fogServersFinishTimeMap.entrySet()){
            if(min == null || min.getValue() > entry.getValue()){
                this.minTime = min.getValue();
                this.minID = min.getKey();
            }
        }
    }

    private Comparable findMindLax(LinkedList list){
        return Collections.min(list);
    }
}
