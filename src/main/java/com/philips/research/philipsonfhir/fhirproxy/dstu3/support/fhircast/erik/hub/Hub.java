package com.philips.research.philipsonfhir.fhirproxy.dstu3.support.fhircast.erik.hub;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Hub {

    static ArrayList<HubData> Subscribers = new ArrayList<>();

    public static String calculateHMAC(String secret) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        String magicKey = "hellothere";
        mac.init(new SecretKeySpec(magicKey.getBytes(), "HmacSHA256"));

        byte[] hash = mac.doFinal(secret.getBytes());
        return DatatypeConverter.printHexBinary(hash);
    }

    static String generateChallenge() {
        SecureRandom generator = new SecureRandom();
        return String.valueOf(generator.nextInt() & Integer.MAX_VALUE);
    }

    static boolean sendNotification(String receivedEvent) throws IOException {
        boolean worked = false;
        for (HubData subscriber : Subscribers) {
            if (subscriber.getEvents().equals(receivedEvent)) {
                //X-HUB SIGN. CAN BE GENERATED USING generateHMAC(subscriber.getSecret())
                //CHANGE TO POST METHOD WITH X-HUB SIGNATURE AND SEND EVENT AS A JSON OBJECT.
                worked = true;
                URL callbackUri = new URL(subscriber.getCallback() + String.format("?mode=%s&topic=%s&events=%s&challenge=%s", subscriber.getMode(), subscriber.getTopic(), subscriber.getEvents(), Hub.generateChallenge()));
                HttpURLConnection con = (HttpURLConnection) callbackUri.openConnection();
                con.setRequestMethod("GET");

                BufferedReader resultReader = new BufferedReader(new InputStreamReader(con.getInputStream()));
                String result = resultReader.lines().collect(Collectors.joining(""));
                System.out.println("(APP): " + result);
            } else {
                worked = false;
            }
        }
        return(worked);
    }

    static void runVerification() throws IOException, InterruptedException {
        boolean isRunning = true;
        List<HubData> toRemove = new ArrayList<>();
        while(isRunning) {
            for (HubData subscriber : Subscribers) {
                    if (!subscriber.getVerified()) {
                        String challenge = Hub.generateChallenge();
                        URL callbackUri = new URL(subscriber.getCallback() + String.format("/verify?mode=%s&topic=%s&events=%s&challenge=%s", subscriber.getMode(), subscriber.getTopic(), subscriber.getEvents(), challenge));
                        HttpURLConnection con = (HttpURLConnection) callbackUri.openConnection();
                        con.setRequestMethod("GET");


                        BufferedReader resultReader = new BufferedReader(new InputStreamReader(con.getInputStream()));
                        String result = resultReader.lines().collect(Collectors.joining(""));
                        if (result.equals(challenge)) {
                            System.out.println("Verified.");
                            subscriber.setVerified(true);
                        } else if (result.equals("unsubscribe")) {
                            System.out.println("Verification at app failed, removing.");
                            toRemove.add(subscriber);
                        } else {
                            System.out.println("Verification at hub failed, removing.");
                            toRemove.add(subscriber);
                        }
                    }
            }
            Subscribers.removeAll(toRemove);
            Thread.sleep(100);
        }
    }
}
