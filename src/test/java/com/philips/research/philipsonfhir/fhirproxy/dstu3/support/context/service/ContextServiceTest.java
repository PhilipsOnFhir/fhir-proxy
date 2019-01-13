//package com.philips.research.philipsonfhir.fhirproxy.dstu3.support.context.service;
//
//import com.philips.research.philipsonfhir.fhirproxy.dstu3.support.proxy.service.FhirServer;
//import org.junit.Test;
//
//import java.util.Collection;
//import java.util.List;
//
//import static org.junit.Assert.*;
//
//public class ContextServiceTest {
//
//    @Test
//    public void createUpdateDelete(){
//        FhirServer fhirServer = new FhirServer("http://doesnotexist.com");
//        ContextService contextService = new ContextService();
//
//        ContextSession contextSession = contextService.createContextSession( fhirServer );
//
//        // check precense in list
//        Collection<ContextSession> contextSessionList = contextService.getActiveContextSessions();
//        assertEquals( 1, contextSessionList.size() );
//        contextSessionList.stream().forEach( contextSession1 -> assertEquals( contextSession.getSessionId(), contextSession1.getSessionId() ));
//
//        assertEquals( contextSession.getSessionId(), contextService.getContextSession( contextSession.getSessionId() ).getSessionId());
//
//
//        contextService.deleteContextSession( contextSession.getSessionId() );
//        assertEquals( null, contextService.getContextSession( contextSession.getSessionId() ));
//
//    }
//
//}