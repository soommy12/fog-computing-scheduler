package client.simulator;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * CLIENT SIMULATOR
 */
public class ClientSimulator {

    private Random random;
    private static int clientCounter = 1;
    private final static ExecutorService exec = Executors.newCachedThreadPool();

    public static void main(String[] args) throws Exception {
        final Random rTime = new Random();
        for(int i = 0; i<100 ; i++){
            exec.submit(ClientSimulator::new);
            exec.awaitTermination(rTime.nextInt(400-200)+200, TimeUnit.MILLISECONDS);
        }
    }

    private ClientSimulator() {
        this.random = new Random();
        try {
            String schedulerUrl = "http://localhost:8080/taskScheduler";
            String serverResponse = createClient(schedulerUrl);
            System.out.println(serverResponse);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    synchronized private String createClient(String schedulerUrl) throws Exception {
        URL url;
        BufferedReader reader = null;
        StringBuilder stringBuilder;

        try {
            //Standard HTTP connection
            url = new URL(schedulerUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.setDoOutput(true);
            //request type
            if(getRandBool()) connection.setRequestMethod("GET");
            else {
                connection.setRequestMethod("POST");
                int taskRange = random.nextInt(45-43)+43;
                connection.setRequestProperty("Task-range", String.valueOf(taskRange));
            }
            //request task
            if(getRandBool()) connection.setRequestProperty("Task-type", "fibo");
            else connection.setRequestProperty("Task-type", "strong");

            if(getRandBool()){  //make request RT or NOT
                int deadline = random.nextInt(3000-2200)+2200;
                if(getRandBool()) connection.setRequestProperty("Hard-deadline", String.valueOf(deadline));
                else connection.setRequestProperty("Soft-deadline", String.valueOf(deadline));
            }
            connection.setConnectTimeout(0);
            connection.setReadTimeout(0);

            // read the output from the server
            System.out.println("Client " + clientCounter++ + " waiting for response...");
            reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            stringBuilder = new StringBuilder();
            //print the response
            String line = reader.readLine();
            stringBuilder.append(line);

            return stringBuilder.toString();
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        } finally {
            // closing reader
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                }
            }
        }
    }

    private boolean getRandBool(){
        return this.random.nextBoolean();
    }
}