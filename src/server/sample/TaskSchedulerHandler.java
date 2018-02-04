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

    private int minTime;
    private int minID;
    private String result;

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {

        Headers headers = httpExchange.getRequestHeaders();
        int currentClientID = clientCounter++;
        //Prepare needed values
        String hardDeadline = null;
        String softDeadline = null;
        int deadline = 0;
        int tArr = 0;
        int taskRange = 0;
        String taskType = null;
        boolean isRt; // real time task flag
        boolean isHard = false; //  hard/soft deadline flag
        if(headers.containsKey("Task-range"))
            taskRange = Integer.parseInt(headers.get("Task-range").get(0));
        if(headers.containsKey("Task-type"))
            taskType = headers.get("Task-type").get(0);
        int fibAvTime = 1800;
        int strongAvTime = 2100;
        int averageSolvingTime;
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
            System.out.println(String.format(template, String.valueOf(currentClientID), method + "RT").concat(String.format(" Hard Deadline: %s", deadline)));
        } else if (softDeadline != null){
            System.out.println(String.format(template, String.valueOf(currentClientID), method + "RT").concat(String.format(" Soft Deadline: %s", deadline)));
        } else {
            System.out.println(String.format(template, String.valueOf(currentClientID), method));
        }

            /**
             * Algorytm LLF
             */
            Task t = new Task();
            int laxity;
            if(isRt){
                laxity = deadline - averageSolvingTime;
                t.setLaxity(laxity);
                if(laxity<0) sendResponse(420, httpExchange, currentClientID);
                else {
                    if(isHard){
                        System.out.println("Identified Hard-deadline");
                        System.out.println("Looking for best server...");
                        findCurrentMinFinishTimeServer();
                        System.out.println("Best Fog Server ID: " + this.minID + " finish time: " + this.minTime);
                        if(this.minTime - tArr <= laxity){
                            Server.fogServersFinishTimeMap.put(
                                    this.minID,
                                    Server.fogServersFinishTimeMap.get(this.minID) + averageSolvingTime
                            );
                            System.out.println("Server is ok!");
                            System.out.println("Adding task to RT List...");
                            Server.rtHardTasksList.add(t);
                            System.out.println("Sorting RT List...");
                            Collections.sort(Server.rtHardTasksList, new Task.LaxityComparator());
                            System.out.println("Current HARD tasks...");
                            for(Task tt : Server.rtHardTasksList){
                                System.out.print(tt.getLaxity() + ", ");
                            }
//                            if current task has the min laxity
                            while(true){
                                int minIdx = Server.rtHardTasksList.indexOf(Collections.min(Server.rtHardTasksList, new Task.LaxityComparator()));
                                if(Server.rtHardTasksList.get(minIdx).getLaxity() == t.getLaxity()){
                                    System.out.println("Task from client no. " + currentClientID + " (HARD) has min Laxity now!");
                                    break;
                                }
                            }

                            HttpURLConnection connection = (HttpURLConnection) Server.fogServersURLsMap.get(this.minID).openConnection();
                            connection.setRequestProperty("request-id", String.valueOf(currentClientID));
                            connection.setRequestProperty("Task-type", taskType);
                            if(taskRange !=0)
                                connection.setRequestProperty("Task-range", String.valueOf(taskRange));

                            //reading response from fog
                            System.out.println("Request " + currentClientID + " (HARD) waiting for FOG response...");
                            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                            StringBuilder stringBuilder = new StringBuilder();
                            String line = reader.readLine();
                            stringBuilder.append(line);
                            this.result = String.valueOf(stringBuilder);
                            //remove current task from list
                            Server.rtHardTasksList.remove(t);

                            System.out.println("Request no. " + currentClientID + " computed by Fog Server " + this.minID + "! Sending response to client.");
                            //send response to actual client
                            sendResponse(220, httpExchange, currentClientID);
                        } else sendResponse(120, httpExchange, currentClientID); //can't compute this hard type request
                    } else { //soft deadline
                        System.out.println("Identified Soft-deadline");
                        System.out.println("Looking for best server...");
                        findCurrentMinFinishTimeServer();
                        System.out.println("Best Fog Server ID: " + this.minID + " finish time: " + this.minTime);
                        Server.fogServersFinishTimeMap.put(
                                this.minID,
                                Server.fogServersFinishTimeMap.get(this.minID) + averageSolvingTime
                        );
                        System.out.println("Adding task to RT List...");
                        Server.rtSoftTasksList.add(t);
                        System.out.println("Sorting RT List...");
                        Collections.sort(Server.rtSoftTasksList, new Task.LaxityComparator());
                        System.out.println("SOFT tasks laxities...");
                        for(Task tt : Server.rtSoftTasksList){
                            System.out.print(tt.getLaxity() + ", ");
                        }
                        //if current task has the min laxity
                        if(Server.rtHardTasksList.isEmpty()){
                            System.out.println("No Hard-deadlines for now.");
                            while(true){
                                int minIdx = Server.rtSoftTasksList.indexOf(Collections.min(Server.rtSoftTasksList, new Task.LaxityComparator()));
                                if(Server.rtSoftTasksList.get(minIdx).getLaxity() == t.getLaxity()){
                                    System.out.println("Task from client no. " + currentClientID + " (SOFT) has min Laxity now!");
                                    break;
                                }
                            }
                            HttpURLConnection connection = (HttpURLConnection) Server.fogServersURLsMap.get(this.minID).openConnection();
                            connection.setRequestProperty("request-id", String.valueOf(currentClientID));
                            connection.setRequestProperty("Task-type", taskType);
                            if(taskRange !=0)
                                connection.setRequestProperty("Task-range", String.valueOf(taskRange));

                            //reading response from fog
                            System.out.println("Request " + currentClientID + " (SOFT) waiting for FOG response...");
                            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                            StringBuilder stringBuilder = new StringBuilder();
                            String line = reader.readLine();
                            stringBuilder.append(line);
                            this.result = String.valueOf(stringBuilder);
                            //remove current task from list
                            Server.rtSoftTasksList.remove(t);
                            System.out.println("Request no. " + currentClientID + " computed by Fog Server " + this.minID + "! Sending response to client.");

                            //send response to actual client
                            sendResponse(220, httpExchange, currentClientID);
                        }
                    }
                }
            } else {
                //standard requests here
                System.out.println("Identified Standard request");

                Server.normalTasksList.add(t);
                if(Server.rtHardTasksList.isEmpty() && Server.rtSoftTasksList.isEmpty()){
                    System.out.println("No RT Requests now.");
                    findCurrentMinFinishTimeServer();
                    System.out.println("Best Fog Server ID: " + this.minID + " finish time: " + this.minTime);
                    Server.fogServersFinishTimeMap.put(
                            this.minID,
                            Server.fogServersFinishTimeMap.get(this.minID) + averageSolvingTime
                    );
                    HttpURLConnection connection = (HttpURLConnection) Server.fogServersURLsMap.get(this.minID).openConnection();
                    connection.setRequestProperty("request-id", String.valueOf(currentClientID));
                    connection.setRequestProperty("Task-type", taskType);
                    if(taskRange !=0)
                        connection.setRequestProperty("Task-range", String.valueOf(taskRange));

                    //reading response from fog
                    System.out.println("Request " + currentClientID + " (STANDARD) waiting for FOG response...");
                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    StringBuilder stringBuilder = new StringBuilder();
                    String line = reader.readLine();
                    stringBuilder.append(line);
                    this.result = String.valueOf(stringBuilder);
                    //remove current task from list
                    Server.rtHardTasksList.remove(t);
                    System.out.println("Request no. " + currentClientID + " computed by Fog Server " + this.minID + "! Sending response to client.");
                    //send response to actual client
                    sendResponse(220, httpExchange, currentClientID);
                }


            }
    }

    private void sendResponse(int responseCode, HttpExchange httpExchange, int no) throws IOException {
        String type;
        if(responseCode == 120){
            type = "120 Server Timeout";
        } else if (responseCode == 220){
            type = "220 Constraint Satisfied; result: [ " + result + " ]; computed by Fog Server ID: " + this.minID;
        } else if (responseCode == 420){
          type = "420 Wrong deadline";
        } else {
            System.out.println("Wrong response!");
            return;
        }

        String response = type + " for client no. " + no;
        httpExchange.sendResponseHeaders(200, response.length());
        OutputStream os = httpExchange.getResponseBody();
        os.write(response.getBytes());
        os.close();
    }

    private void findCurrentMinFinishTimeServer(){
        this.minTime = Server.fogServersFinishTimeMap.get(1);
        this.minID = 1;
        for(Map.Entry<Integer, Integer> entry : Server.fogServersFinishTimeMap.entrySet()){
            if(entry.getValue() < this.minTime ){
                this.minTime = entry.getValue();
                this.minID = entry.getKey();
            }
        }
    }
}
