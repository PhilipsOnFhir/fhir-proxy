package erik.app;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
public class AppController {

    @RequestMapping("/callback")
    public String sendCallback(@RequestParam(value="mode") String mode, @RequestParam(value="topic") String topic, @RequestParam(value="events") String events, @RequestParam(value="challenge") String challenge) throws IOException, InterruptedException {
        return "Notification received: " + events;
        //Do something with the notifcation
    }

    @RequestMapping("/callback/verify")
    public String sendVerification(@RequestParam("mode") String mode, @RequestParam("topic") String topic, @RequestParam("events") String events, @RequestParam("challenge") String challenge) {
        if(App.storedData.getTopic().equals(topic)) {
            return challenge;
        } else {
            return "unsubscribe";
        }
    }
}
