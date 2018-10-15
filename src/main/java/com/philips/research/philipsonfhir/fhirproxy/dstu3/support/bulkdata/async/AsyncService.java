package com.philips.research.philipsonfhir.fhirproxy.dstu3.support.bulkdata.async;

import com.philips.research.philipsonfhir.fhirproxy.dstu3.support.bulkdata.operations.ExportAllFhirOperationCall;
import com.philips.research.philipsonfhir.fhirproxy.dstu3.support.proxy.service.IFhirServer;
import org.hl7.fhir.dstu3.model.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@Controller
public class AsyncService {
    final static Logger logger = LoggerFactory.getLogger( AsyncService.class );
    private Map<String, AsyncSession> sessionMap = new TreeMap<>();

    public List<Resource> getResources(String sessionId, String resourceName) {
        AsyncSession asyncSession = sessionMap.get( sessionId );
        List<Resource> resources = asyncSession.getResultResources( resourceName );

        return resources;
    }
    public List<String> getSessionResultResourceNames(String sessionId) {
        AsyncSession asyncSession = sessionMap.get( sessionId );
        List<String> resourceNameList = asyncSession.getResultResourceNames();

        return resourceNameList;
    }

    public Date getSessionTransActionTime(String sessionId) {
        AsyncSession bulkDataSession = sessionMap.get( sessionId );
        return (bulkDataSession!=null ? bulkDataSession.getTransActionTime(): null );
    }

    public void deleteSession(String bulkdataserviceId) {
        sessionMap.remove(bulkdataserviceId);
    }

    public String getRequestUrl(String sessionId) {
        AsyncSession session = sessionMap.get( sessionId );
        return (session!=null? session.getCallUrl() : null );
    }

    public enum Status {PROCESSING,UNKNOWN,READY}

    public String newAyncGetSession(String callUrl, IFhirServer fhirServer, String resourceType, String id, String params, Map<String, String> queryParams ) {
        String sessionId = ""+ System.currentTimeMillis();

        FhirRequest fhirRequest = new FhirRequest( callUrl, fhirServer, resourceType, id, params, queryParams );
        AsyncSession asyncSession = new AsyncSession( fhirRequest );
        sessionMap.put( sessionId, asyncSession );

        return sessionId;
    }

    public String newAyncGetSession(String callUrl, ExportAllFhirOperationCall exportAllFhirOperationCall) {
        String sessionId = "" + System.currentTimeMillis();

        FhirRequest fhirRequest = new FhirRequest( callUrl, exportAllFhirOperationCall );
        AsyncSession asyncSession = new AsyncSession( fhirRequest );
        sessionMap.put( sessionId, asyncSession );

        return sessionId;
    }

    public AsyncSession getAsyncSession(String sessionId) {
        AsyncSession session = sessionMap.get( sessionId );
        return session;
    }

    public Status getSessionStatus(String bulkdataserviceId) {
        AsyncSession bulkDataSession = sessionMap.get( bulkdataserviceId );
        if ( bulkDataSession==null){
            return Status.UNKNOWN;
        }
        if ( bulkDataSession.isReady()){
            return Status.READY;
        } else
        {
            return Status.PROCESSING;
        }

    }

}
