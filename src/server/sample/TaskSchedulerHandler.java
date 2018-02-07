package server.sample;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import task.implementation.Task;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.net.HttpURLConnection;
import java.util.AbstractMap;
import java.util.Collections;
import java.util.Map;

/**
 * Created by Bartosz on 17.01.2018.
 */
public class TaskSchedulerHandler implements HttpHandler {

    private static int clientCounter = 1;
    private static int hardCounter = 0;
    private static int hardSuccesCounter = 0;
    private static int softCounter = 0;
    private String result;

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {

        Headers headers = httpExchange.getRequestHeaders();
        int currentClientID = clientCounter++;
        int fibAvTime = 2300;
        int strongAvTime = 2100;
        RuntimeMXBean rmb = ManagementFactory.getRuntimeMXBean();
        long tArr = rmb.getUptime();
        int averageSolvingTime;
        Map.Entry<Integer, Integer> currentMinTimeServer = Server.fogServersFinishTimeMap.entrySet().iterator().next();

        String hardDeadline = null;
        String softDeadline = null;
        int deadline = 0;
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
                t.setId(currentClientID);
                t.setArrivalt(tArr);
                if(laxity<0){ sendResponse(420, httpExchange, currentClientID, 0);  }
                else {
                    if(isHard){
                        System.out.println("Identified Hard-deadline");
                        System.out.println("Looking for best server...");
                        currentMinTimeServer = findCurrentMinFinishTimeServer(currentMinTimeServer);
                        System.out.println("Best Fog Server ID: " + currentMinTimeServer.getKey() + " finish time: " + currentMinTimeServer.getValue());
                        System.out.println("Hard counter: " + ++hardCounter);
                        if(currentMinTimeServer.getValue() - tArr <= laxity){
                            Server.fogServersFinishTimeMap.put(
                                    currentMinTimeServer.getKey(),
                                    Server.fogServersFinishTimeMap.get(currentMinTimeServer.getKey()) + averageSolvingTime
                            );
                            System.out.println("Server is ok!");
                            System.out.println("Adding task to RT List...");
                            Server.rtHardTasksList.add(t);
                            System.out.println("Sorting RT List...");
                            Collections.sort(Server.rtHardTasksList, new Task.LaxityComparator());

                            while(true){
                                int laxMin = Server.rtHardTasksList.get(0).getLaxity();
                                System.out.print("");
                                if(laxity == laxMin){
                                    System.out.println("Task from client no. " + currentClientID + " (HARD) has min Laxity now!");
                                    synchronized (this){
                                        HttpURLConnection connection = (HttpURLConnection) Server.fogServersURLsMap.get(currentMinTimeServer.getKey()).openConnection();
                                        connection.setRequestProperty("request-id", String.valueOf(currentClientID));
                                        connection.setRequestProperty("Task-type", taskType);
                                        if(taskRange !=0)
                                            connection.setRequestProperty("Task-range", String.valueOf(taskRange));
                                        connection.setReadTimeout(0);
                                        connection.setConnectTimeout(0);

                                        //reading response from fog
                                        System.out.println("Request " + currentClientID + " (HARD) waiting for FOG response...");
                                        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                                        StringBuilder stringBuilder = new StringBuilder();
                                        String line = reader.readLine();
                                        stringBuilder.append(line);
                                        this.result = String.valueOf(stringBuilder);
                                        System.out.println("Request no. " + currentClientID + " computed by Fog Server " + currentMinTimeServer.getKey() + "! Sending response to client.");
                                        //remove current task from list
                                        Server.rtHardTasksList.remove(t);
                                        System.out.println("Hard success counter: " + ++hardSuccesCounter);
                                        //send response to actual client
                                        sendResponse(220, httpExchange, currentClientID, currentMinTimeServer.getKey());
                                    }
                                    break;
                                }
                            }
                        } else sendResponse(120, httpExchange, currentClientID, currentMinTimeServer.getKey()); //can't compute this hard type request
                    } // Hard-deadlines requests
                    else { //soft deadline
                        System.out.println("Identified Soft-deadline");
                        System.out.println("Looking for best server...");
                        currentMinTimeServer = findCurrentMinFinishTimeServer(currentMinTimeServer);
                        System.out.println("Best Fog Server ID: " + currentMinTimeServer.getKey() + " finish time: " + currentMinTimeServer.getValue());
                        Server.fogServersFinishTimeMap.put(
                                currentMinTimeServer.getKey(),
                                Server.fogServersFinishTimeMap.get(currentMinTimeServer.getKey()) + averageSolvingTime
                        );
                        Server.rtSoftTasksList.add(t);
                        System.out.println("Soft counter: " + ++softCounter);
//                        System.out.println("Added to RT List");
                        Collections.sort(Server.rtSoftTasksList, new Task.LaxityComparator());
                        System.out.println("RT list sorted");

//                        print laxities after sorting
                        for(Task tt : Server.rtSoftTasksList){
                            System.out.print("id: " + tt.getId() + " arr: " + tt.getArrivalt() + " lax: " + tt.getLaxity() + "\n");
                        }
                        System.out.println();

                        while(true){
                            int laxMin = Server.rtSoftTasksList.get(0).getLaxity();
                            System.out.print("");
                            if(Server.rtHardTasksList.isEmpty() && laxity == laxMin){
                                System.out.println("NO HARD-Deadlines queue");
                                System.out.println("Request no. " + currentClientID + " (SOFT) has min Laxity ( " + t.getLaxity() + " ) now!");
                                synchronized (this){
                                    HttpURLConnection connection = (HttpURLConnection) Server.fogServersURLsMap.get(currentMinTimeServer.getKey()).openConnection();
                                    connection.setRequestProperty("request-id", String.valueOf(currentClientID));
                                    connection.setRequestProperty("Task-type", taskType);
                                    if(taskRange !=0)
                                        connection.setRequestProperty("Task-range", String.valueOf(taskRange));
                                    connection.setReadTimeout(0);
                                    connection.setConnectTimeout(0);

                                    //reading response from fog
                                    System.out.println("Request " + currentClientID + " (SOFT) waiting for FOG response...");
                                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                                    StringBuilder stringBuilder = new StringBuilder();
                                    String line = reader.readLine();
                                    stringBuilder.append(line);
                                    this.result = String.valueOf(stringBuilder);

                                    System.out.println("Request no. " + currentClientID + " computed by Fog Server " + currentMinTimeServer.getKey() + "!");
                                    System.out.println("Sending response to client...");

                                    //remove rt from list
                                    Server.rtSoftTasksList.remove(t);
                                    //send response to actual client
                                    sendResponse(220, httpExchange, currentClientID, currentMinTimeServer.getKey());
                                }
                                break;
                            }
                        }
                    } // Soft-deadlines requests
                }
            } else {
                //standard requests here
                System.out.println("Identified Standard request");
                Server.normalTasksList.add(t);
                while (true){
                    System.out.print("");
                    if(Server.rtHardTasksList.isEmpty() && Server.rtSoftTasksList.isEmpty()){
                        System.out.println("No RT Requests in queues.");
                        currentMinTimeServer = findCurrentMinFinishTimeServer(currentMinTimeServer);
                        System.out.println("Best Fog Server ID: " + currentMinTimeServer.getKey() + " finish time: " + currentMinTimeServer.getValue());
                        Server.fogServersFinishTimeMap.put(
                                currentMinTimeServer.getKey(),
                                Server.fogServersFinishTimeMap.get(currentMinTimeServer.getKey()) + averageSolvingTime
                        );
                        synchronized (this){
                            HttpURLConnection connection = (HttpURLConnection) Server.fogServersURLsMap.get(currentMinTimeServer.getKey()).openConnection();
                            connection.setRequestProperty("request-id", String.valueOf(currentClientID));
                            connection.setRequestProperty("Task-type", taskType);
                            if(taskRange !=0)
                                connection.setRequestProperty("Task-range", String.valueOf(taskRange));
                            connection.setReadTimeout(0);
                            connection.setConnectTimeout(0);

                            //reading response from fog
                            System.out.println("Request " + currentClientID + " (STANDARD) waiting for FOG response...");
                            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                            StringBuilder stringBuilder = new StringBuilder();
                            String line = reader.readLine();
                            stringBuilder.append(line);
                            this.result = String.valueOf(stringBuilder);
                            //remove current task from list
                            System.out.println("Request no. " + currentClientID + " computed by Fog Server " + currentMinTimeServer.getKey() + "! Sending response to client.");
                            //send response to actual client
                            Server.normalTasksList.remove(t);
                            sendResponse(220, httpExchange, currentClientID, currentMinTimeServer.getKey());
                        }
                        break;
                    }
                }
            } // standard requests
    }

    private void sendResponse(int responseCode, HttpExchange httpExchange, int no, int id) throws IOException {
        String type;
        if(responseCode == 120){
            type = "120 Server Timeout";
        } else if (responseCode == 220){
            type = "220 Constraint Satisfied; result: [ " + result + " ]; computed by Fog Server ID: " + id;
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

    private Map.Entry findCurrentMinFinishTimeServer(Map.Entry<Integer, Integer> current){
        int id = current.getKey();
        int time = current.getValue();

        for(Map.Entry<Integer, Integer> entry : Server.fogServersFinishTimeMap.entrySet()){
            if(entry.getValue() < current.getValue() ){
                time = entry.getValue();
                id = entry.getKey();
            }
        }
        return new AbstractMap.SimpleEntry<>(id, time);
    }
}
