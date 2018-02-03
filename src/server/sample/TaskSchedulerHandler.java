package server.sample;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import task.implementation.Task;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.util.Collections;
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
    private String result;


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
                            Collections.sort(Server.rtHardTasksList, new Task.LaxityComparator());
                            //if current task has the min laxity
                            while(true){
                                int minIdx = Server.rtHardTasksList.indexOf(Collections.min(Server.rtHardTasksList, new Task.LaxityComparator()));
                                if(Server.rtHardTasksList.get(minIdx).getLaxity() == t.getLaxity())
                                    break;
                            }
                            findCurrentMinFinishTimeServer();
                            Server.fogServersFinishTimeMap.put(
                                    this.minID,
                                    Server.fogServersFinishTimeMap.get(this.minID) + averageSolvingTime
                            );
                            HttpURLConnection connection = (HttpURLConnection) Server.fogServersURLsMap.get(this.minID).openConnection();
                            connection.setRequestProperty("Task-type", taskType);
                            if(taskRange !=0)
                                connection.setRequestProperty("Task-range", String.valueOf(taskRange));

                            //reading response from fog
                            System.out.println("Request " + clientCounter + " (HARD) waiting for FOG response...");
                            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                            StringBuilder stringBuilder = new StringBuilder();
                            String line = reader.readLine();
                            stringBuilder.append(line);
                            this.result = String.valueOf(stringBuilder);
                            //remove current task from list
                            Server.rtHardTasksList.remove(t);

                            //send response to actual client
                            sendResponse(220, httpExchange);
                        } else sendResponse(120, httpExchange); //can't compute this hard type request
                    } else { //soft deadline
                        //tutaj requesty soft, CHYBA powinny byc jednak w jednej liscie z HARD
                        Server.rtSoftTasksList.add(t);
                        Collections.sort(Server.rtSoftTasksList, new Task.LaxityComparator());
                        //if current task has the min laxity
                        if(Server.rtHardTasksList.isEmpty()){
                            while(true){
                                int minIdx = Server.rtSoftTasksList.indexOf(Collections.min(Server.rtSoftTasksList, new Task.LaxityComparator()));
                                if(Server.rtSoftTasksList.get(minIdx).getLaxity() == t.getLaxity())
                                    break;
                            }
                            findCurrentMinFinishTimeServer();
                            Server.fogServersFinishTimeMap.put(
                                    this.minID,
                                    Server.fogServersFinishTimeMap.get(this.minID) + averageSolvingTime
                            );
                            HttpURLConnection connection = (HttpURLConnection) Server.fogServersURLsMap.get(this.minID).openConnection();
                            connection.setRequestProperty("Task-type", taskType);
                            if(taskRange !=0)
                                connection.setRequestProperty("Task-range", String.valueOf(taskRange));

                            //reading response from fog
                            System.out.println("Request " + clientCounter + " (SOFT) waiting for FOG response...");
                            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                            StringBuilder stringBuilder = new StringBuilder();
                            String line = reader.readLine();
                            stringBuilder.append(line);
                            this.result = String.valueOf(stringBuilder);
                            //remove current task from list
                            Server.rtSoftTasksList.remove(t);

                            //send response to actual client
                            sendResponse(220, httpExchange);
                        }
                    }
                }
            } else {
                //tutaj requesty normalne
                Server.rtNormalTasksList.add(t);
                if(Server.rtNormalTasksList.isEmpty() && Server.rtSoftTasksList.isEmpty()){
                    Server.fogServersFinishTimeMap.put(
                            this.minID,
                            Server.fogServersFinishTimeMap.get(this.minID) + averageSolvingTime
                    );
                    HttpURLConnection connection = (HttpURLConnection) Server.fogServersURLsMap.get(this.minID).openConnection();
                    connection.setRequestProperty("Task-type", taskType);
                    if(taskRange !=0)
                        connection.setRequestProperty("Task-range", String.valueOf(taskRange));

                    //reading response from fog
                    System.out.println("Request " + clientCounter + " waiting for FOG response...");
                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    StringBuilder stringBuilder = new StringBuilder();
                    String line = reader.readLine();
                    stringBuilder.append(line);
                    this.result = String.valueOf(stringBuilder);
                    //remove current task from list
                    Server.rtHardTasksList.remove(t);

                    //send response to actual client
                    sendResponse(220, httpExchange);
                }


            }
    }

    private void sendResponse(int responseCode, HttpExchange httpExchange) throws IOException {
        String type;
        if(responseCode == 120){
            type = "120 Server Timeout";
        } else if (responseCode == 220){
            type = "220 Constraint Satisfied; result: [ " + result + " ]";
        } else if (responseCode == 420){
          type = "420 Wrong deadline";
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
}
