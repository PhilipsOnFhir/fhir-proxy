package com.philips.research.philipsonfhir.fhirproxy.dstu3.support.bulkdata.async;

import ca.uhn.fhir.context.FhirContext;
import com.philips.research.philipsonfhir.fhirproxy.dstu3.support.NotImplementedException;
import com.philips.research.philipsonfhir.fhirproxy.dstu3.support.bulkdata.fhir.BundleRetriever;
import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.OperationOutcome;
import org.hl7.fhir.dstu3.model.Resource;
import org.hl7.fhir.exceptions.FHIRException;
import org.hl7.fhir.instance.model.api.IBaseResource;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

public class AsyncSession {
    private static FhirContext ourCtx = FhirContext.forDstu3();
    private final Date transActionTime;
    private final AsyncSessionProcessor processor;
    private final FhirRequest fhirRequest;
    private AsyncResult asyncResult;

    public AsyncSession(FhirRequest fhirRequest) {
        this.transActionTime = new Date();
        this.fhirRequest = fhirRequest;

        processor = new AsyncSessionProcessor();
        (new Thread(processor)).start();
    }


    public boolean isReady() {
        return processor.isDone;
    }

    public Date getTransActionTime() {
        return transActionTime;
    }

//    public static void processBundle(AsyncResult asyncResult, Bundle bundle, IFhirServer fhirServer) throws FHIRException {
//        AsyncService.logger.debug( "process Bundle" );
//
//        Bundle currentBundle = bundle;
//        while ( currentBundle.getLink( Bundle.LINK_NEXT ) != null ) {
//            AsyncService.logger.debug( "process Bundle - load next" );
//            Bundle nextPage = fhirServer.loadPage( currentBundle );
//            asyncResult.addBundle( nextPage );
//            currentBundle = nextPage;
//        }
//        AsyncService.logger.debug( "process Bundle  done" );
//
//    }

    public List<Resource> getResultResources(String resourceName) {
        return processor.getResult().resultTreeMap.get(resourceName).values()
            .stream().collect(Collectors.toList());
    }

    public List<String> getResultResourceNames() {
        return  processor.getResult().resultTreeMap.keySet().stream()
            .collect(Collectors.toList());
    }


    public String getCallUrl() {
        return fhirRequest.getCallUrl();
    }

    public int getResourceCount(String resourceName) {
        return processor.getResult().resultTreeMap.get( resourceName ).size();
    }

    public boolean returnNdJson() {
        return this.fhirRequest.returnNdJson();
    }

    public String getProcessDescription() {
        if ( this.fhirRequest.getFhirOperationCall() != null ) {
            return this.fhirRequest.getFhirOperationCall().getDescription();
        } else {
            return "processing";
        }
    }

    public Map<String, OperationOutcome> getErrorMap() {
        if ( this.fhirRequest.getFhirOperationCall() != null ) {
            return this.fhirRequest.getFhirOperationCall().getErrors();
        }
        return new TreeMap<>();
    }


    private class AsyncSessionProcessor implements Runnable {
        private boolean isDone = false;

        @Override
        public synchronized void run() {
            AsyncService.logger.info( "Async processing start" );
            try {
                IBaseResource iBaseResource = fhirRequest.getResource();

                asyncResult = new AsyncResult();
                if ( iBaseResource instanceof Bundle) {
                    Bundle bundle = (Bundle)iBaseResource;
                    asyncResult.addBundle( bundle );
                    BundleRetriever bundleRetriever = new BundleRetriever( fhirRequest.getFhirServer(), bundle );
                    asyncResult.addResources( bundleRetriever.retrieveAllResources() );
//                    processBundle( asyncResult, bundle, fhirRequest.getFhirServer() );
                } else if ( iBaseResource instanceof Resource) {
                    asyncResult.addResource( (Resource)iBaseResource );
                }

            } catch (FHIRException e ) {
                e.printStackTrace();
            }

            AsyncService.logger.info( "Async processing done" );
            isDone = true;
        }




        public AsyncResult getResult() {
            return asyncResult;
        }

    }


}

