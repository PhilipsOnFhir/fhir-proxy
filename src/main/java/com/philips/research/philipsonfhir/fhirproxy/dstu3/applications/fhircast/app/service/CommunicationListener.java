package com.philips.research.philipsonfhir.fhirproxy.dstu3.applications.fhircast.app.service;

import java.io.*;
import java.net.MalformedURLException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Logger;

public class CommunicationListener implements Runnable {
    private final int port;
    Logger logger = Logger.getLogger(this.getClass().getName());
    private String headerData;
    private boolean continueListening = true;

    CommunicationListener(int port ){
        this.port = port;
    }

    @Override
    public void run() {
        try {
            logger.info( "listener started" );

            ServerSocket server = new ServerSocket( port );

            while ( continueListening ) {
                Socket client = server.accept();
                logger.info( "callback received" );


                try {
                    InputStream raw = client.getInputStream(); // ARM
                    headerData = getHeaderToArray( raw );

                    String parameters = headerData.substring( headerData.indexOf( "?" ) + 1, headerData.indexOf( "HTTP" ) );
                    String str = parameters;
                    String[] params = str.split( "&" );
                    Map<String, String> queryParams = new TreeMap<>();
                    for ( String param : params ) {
                        String[] parts = param.split( "=" );
                        queryParams.put( parts[0], parts[1] );
                    }


                    Date today = new Date();
                    String httpResponse = "HTTP/1.1 200 OK\r\n\r\n" + queryParams.get( "hub.challenge" );
                    client.getOutputStream().write( httpResponse.getBytes( "UTF-8" ) );
                    client.close();
                } catch ( MalformedURLException ex ) {
                    System.err.println( client.getLocalAddress() + " is not a parseable URL" );

                } catch ( IOException ex ) {
                    System.err.println( ex.getMessage() );
                }
            }
        } catch ( IOException e ) {
            e.printStackTrace();
        }
    }

    public String getHeaderToArray(InputStream inputStream) {

        String headerTempData = "";

        // chain the InputStream to a Reader
        Reader reader = new InputStreamReader(inputStream);
        try {
            int c;
            while ((c = reader.read()) != -1) {
                System.out.print((char) c);
                headerTempData += (char) c;

                if (headerTempData.contains("\r\n\r\n"))
                    break;
            }
        } catch (IOException ex) {
            System.err.println(ex.getMessage());
        }
        headerData = headerTempData;

        return headerTempData;
    }
}
