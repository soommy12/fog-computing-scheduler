package client.simulator;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
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
        for(int i = 0; i<50 ; i++){
            exec.submit(ClientSimulator::new);
            exec.awaitTermination(rTime.nextInt(100-50)+50, TimeUnit.MILLISECONDS);
        }
    }

    private ClientSimulator() {

        this.random = new Random();
        RuntimeMXBean rmb = ManagementFactory.getRuntimeMXBean();
        long arrivalTime = rmb.getUptime();

        try {
            String schedulerUrl = "http://localhost:8080/taskScheduler";
            String serverResponse = createClient(schedulerUrl, arrivalTime);
            System.out.println(serverResponse);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String createClient(String schedulerUrl, long arrTime) throws Exception {
        URL url;
        BufferedReader reader = null;
        StringBuilder stringBuilder;
        try {
            //Standard HTTP connection
            url = new URL(schedulerUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.setDoOutput(true);
            connection.setRequestProperty("Arrival-time", String.valueOf(arrTime));

            //request type
            if(getRandBool()) connection.setRequestMethod("GET");
            else {
                connection.setRequestMethod("POST");
                int taskRange = random.nextInt(10-1)+1;
                connection.setRequestProperty("Task-range", String.valueOf(taskRange));
            }
            //request task
            if(getRandBool()) connection.setRequestProperty("Task-type", "fibo");
            else connection.setRequestProperty("Task-type", "strong");



            //make request RT or NOT
            if(getRandBool()){
                int deadline = random.nextInt(3000-1500)+1500;
                if(getRandBool()) connection.setRequestProperty("Hard-deadline", String.valueOf(deadline));
                else connection.setRequestProperty("Soft-deadline", String.valueOf(deadline));
            }
            connection.setConnectTimeout(1000);

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