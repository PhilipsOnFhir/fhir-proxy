package com.philips.research.philipsonfhir.fhirproxy.support.bulkdata.operations;

import com.philips.research.philipsonfhir.fhirproxy.support.bulkdata.fhir.BundleRetriever;
import com.philips.research.philipsonfhir.fhirproxy.support.proxy.operation.FhirOperationCall;
import com.philips.research.philipsonfhir.fhirproxy.support.proxy.service.IFhirServer;
import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.OperationOutcome;
import org.hl7.fhir.dstu3.model.Resource;
import org.hl7.fhir.instance.model.api.IBaseResource;

import java.util.*;
import java.util.logging.Logger;

public class ExportAllFhirOperationCall implements FhirOperationCall {
    private final IFhirServer fhirServer;
    private final Map<String, String> queryParams;
    private Logger logger = Logger.getLogger( this.getClass().getName() );
    private Bundle resultBundle = null;
    private String progressDescription = "undefined";
    private Status status = Status.UNKNOWN;
    private Map<String, OperationOutcome> errorMap = new TreeMap<>();

    public ExportAllFhirOperationCall(IFhirServer fhirServer, Map<String, String> queryParams) {
        this.fhirServer = fhirServer;
        this.queryParams = queryParams;
    }


    @Override
    public String getDescription() {
        return this.progressDescription += ".";
    }

    @Override
    public Map<String, OperationOutcome> getErrors() {
        return Collections.unmodifiableMap( errorMap );
    }

    public IBaseResource getResult() {
        if ( resultBundle == null ) {
            performOperation();
        }
        return resultBundle;
    }

    private void performOperation() {
        this.status = Status.PROCESSING;
        String type = queryParams.get( "_type" );
        String since = queryParams.get( "_since" );

        Set<String> allresources = new TreeSet<>();
        HashMap<String, IBaseResource> resultHashMap = new HashMap<>();

        this.progressDescription = "Retrieving resource list";
        fhirServer.getCapabilityStatement().getRest().stream()
            .forEach( capabilityStatementRestComponent -> capabilityStatementRestComponent.getResource().stream()
                .map( capabilityStatementRestResourceComponent -> capabilityStatementRestResourceComponent.getType() )
                .filter( rType -> (type == null || type.contains( rType )) )
                .forEach( rType -> allresources.add( rType ) ) );

        this.progressDescription = "Processing resources";
        allresources.stream().forEach( resourceName -> {
            try {
                this.progressDescription = "Processing resource " + resourceName;
                IBaseResource result = fhirServer.searchResource( resourceName, null );
                if ( result instanceof Bundle ) {
                    BundleRetriever bundleRetriever = new BundleRetriever( fhirServer, (Bundle) result );
                    bundleRetriever.retrieveAllResources().stream().forEach( resource ->
                        resultHashMap.put( resource.fhirType() + "/" + resource.getId(), resource ) );
                }
                resultHashMap.put( resourceName, result );
            } catch ( Exception e ) {
                logger.info( resourceName + " error" );
                OperationOutcome operationOutcome = new OperationOutcome()
                    .addIssue( new OperationOutcome.OperationOutcomeIssueComponent()
                        .setSeverity( OperationOutcome.IssueSeverity.ERROR )
                        .setDiagnostics( e.getMessage() )
                        .addLocation( resourceName )
                    );
                this.errorMap.put( resourceName, operationOutcome );
            }
        } );

        this.progressDescription = "Creating result";

        Bundle resultBundle = new Bundle()
            .setType( Bundle.BundleType.SEARCHSET );

        resultHashMap.values().stream()
            .filter( iBaseResource -> iBaseResource instanceof Resource )
            .map( iBaseResource -> (Resource) iBaseResource )
            .forEach( resource -> resultBundle.addEntry(
                new Bundle.BundleEntryComponent().setResource( resource )
            ) );

        resultBundle.setTotal( resultBundle.getEntry().size() );

        this.resultBundle = resultBundle;
        this.progressDescription = "finished";
        this.status = Status.DONE;
    }

    public enum Status {UNKNOWN, PROCESSING, DONE}
}
