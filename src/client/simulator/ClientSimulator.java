package client.simulator;

import task.implementation.Task;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Random;

/**
 *
 * A complete Java class that shows how to open a URL, then read data (text) from that URL,
 * HttpURLConnection class (in combination with an InputStreamReader and BufferedReader).
 *
 * CLIENT SIMULATOR
 *
 */
public class ClientSimulator {

    private Random random;
    private static int clientCounter = 1;

//    private final static ScheduledExecutorService exec = Executors.newScheduledThreadPool(20);

    public static void main(String[] args) throws Exception {


//        //new Client in each 3 secs
//        ScheduledExecutorService exec = Executors.newSingleThreadScheduledExecutor();
//        final Random rTime = new Random();
//        int rSleepTime = rTime.nextInt(2000-1000)+1000;
////        exec.scheduleAtFixedRate(ClientSimulator::new, 0, rSleepTime, TimeUnit.MILLISECONDS);
//        exec.scheduleAtFixedRate(ClientSimulator::new, 0, 3, TimeUnit.MILLISECONDS);

        //fixed client number with random arrival time!
//        final Random rTime = new Random();
//        final long maxSleepTime = 3000L;
//        for(int i = 0; i < 30; i++){
//            int rSleepTime = rTime.nextInt((int)maxSleepTime);
//            Runnable simulator = ClientSimulator::new;
//            exec.scheduleAtFixedRate(simulator, rSleepTime, rSleepTime, TimeUnit.MILLISECONDS);
//        }

        new ClientSimulator();
        new ClientSimulator();
        new ClientSimulator();
    }

    private ClientSimulator() {

        this.random = new Random();
        RuntimeMXBean rmb = ManagementFactory.getRuntimeMXBean();
        long arrivalTime = rmb.getUptime();

        System.out.println("thread no. " + clientCounter++ + " arrival time: " + arrivalTime);
        try {
            String schedulerUrl = "http://localhost:8080/taskScheduler";
            String serverResponse = createClient(schedulerUrl);
            System.out.println(serverResponse);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String createClient(String schedulerUrl) throws Exception {
        URL url;
        BufferedReader reader = null;
        StringBuilder stringBuilder;

        try {
            //Standard HTTP connection
            url = new URL(schedulerUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.setDoOutput(true);


            // setting request method
            int[] arr = {
                    random.nextInt(10-1)+1,
                    random.nextInt(10-1)+1,
                    random.nextInt(10-1)+1,
                    random.nextInt(10-1)+1,
                    random.nextInt(10-1)+1,
            };
            Task t = new Task(arr);
            if(getRandBool()) connection.setRequestMethod("GET");
            else connection.setRequestMethod("POST");

//            int deadline = random.nextInt(3000-1000)+1000;
//            connection.setRequestProperty("Hard-deadline", String.valueOf(deadline));

            // make request RT or NOT
            if(getRandBool()){
                int deadline = random.nextInt(3000-1000)+1000;
                if(getRandBool()) connection.setRequestProperty("Hard-deadline", String.valueOf(deadline));
                else connection.setRequestProperty("Soft-deadline", String.valueOf(deadline));
            }

            ObjectOutputStream oos = new ObjectOutputStream(connection.getOutputStream());
            oos.writeObject(t);
            oos.close();

            // read the output from the server
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
            // close the reader; this can throw an exception too, so
            // wrap it in another try/catch block.
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