//package com.philips.research.philipsonfhir.fhirproxy.dstu3.support;
//
//import ca.uhn.fhir.context.FhirContext;
//import ca.uhn.fhir.rest.client.api.IGenericClient;
//import com.philips.research.philipsonfhir.fhirproxy.dstu3.applications.memoryserver.servlet.MemoryFhirServer;
//import org.junit.Test;
//
//public class MemoryFhirServerTest {
//
//    @Test()
//    public void testMemoryFhirServerCreation() {
//        int port = 9999;
//        MemoryFhirServer memoryFhirServer = new MemoryFhirServer( port );
//
//        FhirContext ourCtx = FhirContext.forDstu3();
//        IGenericClient ourClient = ourCtx.newRestfulGenericClient( "http:localhost:"+port+"/baseDstu3");
//
//        memoryFhirServer.stop();
//
//    }
//
//}