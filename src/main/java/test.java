import Client.WhiteBoardClient;
import Server.WhiteBoardServer;

import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
public class test {

    public static void main(String[] args) {
        setupPolling();

    }
    private static void setupPolling() {
        Timer timer;
        int delay = 1000;


        // Get the current date object

        timer = new Timer("Shapes Update Checker");
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                Calendar calendar = Calendar.getInstance();
                Date currentDate = calendar.getTime();
                System.out.println(currentDate+" ping");

            }
        }, 0, delay); // Delay and period in milliseconds (e.g., 5000 ms)
    }
}
