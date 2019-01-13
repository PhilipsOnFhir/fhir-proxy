package erik.hub;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
public class HubController {

    @ResponseStatus(HttpStatus.ACCEPTED)
    @RequestMapping("/Hub")
    public String subscription(@RequestParam(value="callback") String callback, @RequestParam(value="mode") String mode, @RequestParam(value="topic") String topic, @RequestParam(value="secret") String secret, @RequestParam(value="events") String events) throws Exception {
        if(callback.equals("") || mode.equals("") || topic.equals("") || secret.equals("") || events.equals("") ||  secret.length() < 64) {
            String reason = "Something went wrong";
            return String.format("{\"mode\":\"denied\",\"topic\":\"%s\",\"events\":\"%s\",\"reason\":\"%s\"}", topic, events, reason);
        } else {
            HubData subscriber = new HubData(callback, mode, topic, events, Hub.generateChallenge(), false);
            //CHECK IF USER EXISTS ALREADY, IF SO DELETE OLD SUBSCRIPTION AND RE-ADD.
            Hub.Subscribers.add(subscriber);
            System.out.println("New subscriber added");
            return "Request received";
        }
    }

    @ResponseStatus(HttpStatus.OK)
    @RequestMapping("/event")
    public String notify(@RequestParam(value="timestamp") String timestamp, @RequestParam(value="id") String id, @RequestParam(value="event") String event) throws IOException {
        if(Hub.sendNotification(event)){
            return "Notification sent successfully.";
        } else {
            return "Sending notification either failed, or there are no subscribers listening for \"" + event + "\"";
        }
    }

    //Prints out the current list,
    @RequestMapping("/printlist")
    public String printSublist() {
        StringBuilder result = new StringBuilder();
        for(HubData subscriber : Hub.Subscribers) {
            result.append(subscriber.getVerified() + "," + subscriber.getEvents() + "\n");
        }
       return result.toString();
    }
}