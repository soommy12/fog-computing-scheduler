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

//    private final static ScheduledExecutorService exec = Executors.newScheduledThreadPool(5);
    private final static ExecutorService exec = Executors.newCachedThreadPool();

    public static void main(String[] args) throws Exception {

        //tworzymy randomowe zadania w losowych odstepach czasu
//        ScheduledExecutorService ex = Executors.newSingleThreadScheduledExecutor();
//        ex.scheduleAtFixedRate(ClientSimulator::new, 0, rTime.nextInt(2000-1000)+1000, TimeUnit.MILLISECONDS);


        final Random rTime = new Random();
        for(int i = 0; i<20 ; i++){
            exec.submit(ClientSimulator::new);
            exec.awaitTermination(rTime.nextInt(2000-1000)+1000, TimeUnit.MILLISECONDS);
        }
//        new ClientSimulator();
//        new ClientSimulator();
//        new ClientSimulator();
    }

    private ClientSimulator() {

        this.random = new Random();
        RuntimeMXBean rmb = ManagementFactory.getRuntimeMXBean();
        long arrivalTime = rmb.getUptime();

        System.out.println("thread no. " + clientCounter + " arrival time: " + arrivalTime);
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
//        boolean isPost = false;
        try {
            //Standard HTTP connection
            url = new URL(schedulerUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.setDoOutput(true);
            connection.setRequestProperty("Arrival-time", String.valueOf(arrTime));
            // setting request method
//            int[] arr = {
//                    random.nextInt(10-1)+1,
//                    random.nextInt(10-1)+1,
//                    random.nextInt(10-1)+1,
//                    random.nextInt(10-1)+1,
//                    random.nextInt(10-1)+1,
//            };
//            Task t = new Task(arr);

            //request type
            if(getRandBool()) connection.setRequestMethod("GET");
            else {
                connection.setRequestMethod("POST");
                int taskRange = random.nextInt(10-1)+1;
                connection.setRequestProperty("Task-range", String.valueOf(taskRange));
//                isPost = true;
            }
            //request task
            if(getRandBool()) connection.setRequestProperty("Task-type", "fibo");
            else connection.setRequestProperty("Task-type", "strong");

            //test only RT requests
//            int deadline = random.nextInt(3000-1000)+1000;
//            System.out.println("Deadline to send: " + deadline);
//            connection.setRequestProperty("Hard-deadline", String.valueOf(deadline));

            // make request RT or NOT
            if(getRandBool()){
                int deadline = random.nextInt(3000-1000)+1000;
                if(getRandBool()) connection.setRequestProperty("Hard-deadline", String.valueOf(deadline));
                else connection.setRequestProperty("Soft-deadline", String.valueOf(deadline));
            }
            //send fresh task to the scheduler

            //nowa wersja z jacksonem
//            ObjectMapper mapper = new ObjectMapper();
//            String strTask = mapper.writeValueAsString(t);
//            OutputStream os = connection.getOutputStream();
//            os.write(strTask.getBytes());
//            os.close();

//            stara wersja
//            if(isPost){
//                ObjectOutputStream oos = new ObjectOutputStream(connection.getOutputStream());
//                oos.writeObject(t);
//                oos.close();
//            }

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