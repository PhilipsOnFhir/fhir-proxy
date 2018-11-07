package com.philips.research.philipsonfhir.fhirproxy.dstu3.support.bulkdata.fhir;

import com.philips.research.philipsonfhir.fhirproxy.dstu3.support.NotImplementedException;
import com.philips.research.philipsonfhir.fhirproxy.dstu3.support.proxy.service.IFhirServer;
import org.hl7.fhir.dstu3.model.*;
import org.hl7.fhir.exceptions.FHIRException;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class PatientExportServer {

    private final Logger logger = LoggerFactory.getLogger( this.getClass().getName() );
    private final IFhirServer fhirServer;

    PatientExportServer(IFhirServer fhirServer ){
        this.fhirServer = fhirServer;
    }


    public Bundle exportAllPatientData(
            String outputFormat,
            String since,
            String type) throws FHIRException, NotImplementedException {

        Bundle bundle = (Bundle) this.fhirServer.doGet("Patient","$everything",null);
        BundleRetriever bulkDataHelper = new BundleRetriever( this.fhirServer, bundle );
        List<Resource> resources = bulkDataHelper.retrieveAllResources();

        List<Resource> result = new ArrayList<>();
        resources.stream()
            .filter( resource -> (type == null || type.contains( resource.fhirType() )) )
            //TODO since
            .forEach( resource -> result.add( resource ) );

        Bundle resultBundle = new Bundle()
            .setTotal( result.size() )
            .setType( Bundle.BundleType.SEARCHSET );

        result.stream().forEach( resource -> {
            resultBundle.addEntry( new Bundle.BundleEntryComponent()
                .setResource( resource )
            );
        } );

        return resultBundle;
    }

    public Bundle exportAllGroupData(String id, String outputFormat, String since, String type) throws FHIRException, NotImplementedException {
        Group group = (Group) this.fhirServer.doGet( "Group", id, null );

        Bundle resultBundle = new Bundle()
            .setType( Bundle.BundleType.SEARCHSET );

        group.getMember().stream().forEach( groupMemberComponent -> {
            Reference reference = groupMemberComponent.getEntity();
            IdType idType = new IdType( reference.getReference() );
            if ( idType.getResourceType().equals( "Patient" ) ) {
                try {
                    Bundle patientBundle = exportPatientData( idType.getIdPart(), outputFormat, since, type );
                    patientBundle.getEntry().stream()
                        .filter( bundleEntryComponent -> (type == null || type.contains( bundleEntryComponent.getResource().fhirType() )) )
                        //TODO since
                        .forEach( bundleEntryComponent -> resultBundle.addEntry( bundleEntryComponent ) );
                } catch ( FHIRException e ) {
                    e.printStackTrace();
                }
            }
        } );

        return resultBundle;
    }

    public Bundle exportPatientData(String id, String outputFormat, String since, String type) throws FHIRException, NotImplementedException {
        Bundle bundle = (Bundle) this.fhirServer.doGet( "Patient", id, "$everything", null );
        BundleRetriever bulkDataHelper = new BundleRetriever( this.fhirServer, bundle );
        List<Resource> resources = bulkDataHelper.retrieveAllResources();

        List<Resource> result = new ArrayList<>();
        resources.stream()
            .filter( resource -> (type == null || type.contains( resource.fhirType() )) )
            //TODO since
            .forEach( resource -> result.add(resource));

        Bundle resultBundle = new Bundle()
            .setTotal(result.size())
            .setType(Bundle.BundleType.SEARCHSET);

        result.stream().forEach( resource -> {
            resultBundle.addEntry( new Bundle.BundleEntryComponent()
                .setResource(resource)
            );
        });

        return resultBundle;
    }

    public Bundle exportAllData(String type) throws NotImplementedException {
        Set<String> allresources = new TreeSet<>();
        HashMap<String, IBaseResource> resultHashMap = new HashMap<>();
        fhirServer.getCapabilityStatement().getRest().stream()
            .forEach( capabilityStatementRestComponent -> capabilityStatementRestComponent.getResource().stream()
                .map( capabilityStatementRestResourceComponent -> capabilityStatementRestResourceComponent.getType() )
                .filter( rType -> (type == null || type.contains( rType )) )
                .forEach( rType -> allresources.add( rType ) ) );

        allresources.stream().forEach( resourceName -> {
            try {
                IBaseResource result = fhirServer.doSearch( resourceName, null );
                if ( result instanceof Bundle ) {
                    BundleRetriever bundleRetriever = new BundleRetriever( fhirServer, (Bundle) result );
                    bundleRetriever.retrieveAllResources().stream().forEach( resource ->
                        resultHashMap.put( resource.fhirType() + "/" + resource.getId(), resource ) );
                }
                resultHashMap.put( resourceName, result );
            } catch ( Exception e ) {
                logger.info( resourceName + " error" );
            }
        } );


        Bundle resultBundle = new Bundle()
            .setType( Bundle.BundleType.SEARCHSET );

        resultHashMap.values().stream()
            .filter( iBaseResource -> iBaseResource instanceof Resource )
            .map( iBaseResource -> (Resource) iBaseResource )
            .forEach( resource -> resultBundle.addEntry(
                new Bundle.BundleEntryComponent().setResource( resource )
            ) );

        resultBundle.setTotal( resultBundle.getEntry().size() );
        return resultBundle;
    }
}
