package erik.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class TestApp {

    public static void main(String[] args) throws Exception {

        SpringApplication.run(Application.class, args);
        //App.sendSubscribeRequest("http://127.0.0.1:85/callback", "subscribe","12345","test-chart");

    }
}
