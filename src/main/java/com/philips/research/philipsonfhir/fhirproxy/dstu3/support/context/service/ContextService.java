package com.philips.research.philipsonfhir.fhirproxy.dstu3.support.context.service;

import com.philips.research.philipsonfhir.fhirproxy.dstu3.support.proxy.service.FhirServer;

import java.util.*;

public class ContextService {
    Map< String,ContextSession> contextSessionMap = new TreeMap<>();

    public ContextService() {
    }

    public ContextSession createContextSession( FhirServer fhirServer ) {
        String id = "CS"+java.lang.System.currentTimeMillis();
        ContextSession contextSession = new ContextSession(id, fhirServer);
        contextSessionMap.put(id, contextSession );
        return contextSession;
    }

    public Collection<ContextSession> getActiveContextSessions() {
        return Collections.unmodifiableCollection( contextSessionMap.values() );
    }

    public ContextSession getContextSession(String contextId) {
        return contextSessionMap.get(contextId);
    }

    public void deleteContextSession(String sessionId) {
        contextSessionMap.remove(sessionId);
    }
}
