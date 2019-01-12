package com.philips.research.philipsonfhir.fhirproxy.dstu3.support.fhircast.erik.app;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class App {

    public static AppData storedData = null;

    public static String generateSecret(){
        Random rand = new Random();
        IntStream generatedInts = rand.ints(20);
        int[] generatedIntArray = generatedInts.toArray();
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < generatedIntArray.length; i++) {
            stringBuilder.append(generatedIntArray[i] & Integer.MAX_VALUE);
        }
        return stringBuilder.substring(0, 64);
    }

    public static boolean sendSubscribeRequest(String callback, String mode, String topic, String events) throws Exception {
        System.out.println(" (APP) Sending subscription request");
        String secret = new String(generateSecret());
        URL FHIRServer = new URL("http://127.0.0.1:8080/Hub");
        HttpURLConnection con = (HttpURLConnection) FHIRServer.openConnection();
        String Params = String.format("callback=%s&mode=%s&topic=%s&secret=%s&events=%s", callback, mode, topic, secret, events );
        con.setDoOutput(true);
        con.setRequestMethod("POST");
        OutputStreamWriter os = new OutputStreamWriter(con.getOutputStream());
        os.write(Params);
        os.flush();
        os.close();

        BufferedReader resultReader = new BufferedReader(new InputStreamReader(con.getInputStream()));
        String result = resultReader.lines().collect(Collectors.joining(""));
        System.out.println("(HUB): " + result);

        con.disconnect();
        storedData = new AppData(callback, mode, topic, secret, events);
        return (con.getResponseCode() == 200 && storedData.getTopic() != null );
    }
}
