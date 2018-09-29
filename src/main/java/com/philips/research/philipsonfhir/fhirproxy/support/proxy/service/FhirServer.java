package com.philips.research.philipsonfhir.fhirproxy.support.proxy.service;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.model.primitive.IdDt;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import com.philips.research.philipsonfhir.fhirproxy.support.NotImplementedException;
import com.philips.research.philipsonfhir.fhirproxy.support.proxy.operation.FhirOperationCall;
import com.philips.research.philipsonfhir.fhirproxy.support.proxy.operation.FhirOperationRepository;
import org.hl7.fhir.dstu3.model.*;
import org.hl7.fhir.exceptions.FHIRException;
import org.hl7.fhir.instance.model.api.IBaseBundle;
import org.hl7.fhir.instance.model.api.IBaseOperationOutcome;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.springframework.stereotype.Controller;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

@Controller
public class FhirServer implements IFhirServer {
    final FhirContext ourCtx;
    final IGenericClient ourClient;
    private Logger logger = Logger.getLogger( this.getClass().getName());
    private final String fhirUrl;
    private FhirOperationRepository fhirOperationRepository = new FhirOperationRepository();

    public FhirServer(String fhirUrl){
        this.fhirUrl = fhirUrl;
        ourCtx = FhirContext.forDstu3();
        ourClient = ourCtx.newRestfulGenericClient(fhirUrl );
    }

        @Override
        public CapabilityStatement getCapabilityStatement() {
        CapabilityStatement capabilityStatement =
                ourClient.capabilities().ofType(CapabilityStatement.class).execute();
        return capabilityStatement;
    }

    public FhirOperationRepository getFhirOperationRepository(){return fhirOperationRepository;};

    @Override
    public IBaseResource searchResource(String resourceType, Map<String, String> queryParams) {
        String url = getUrl( resourceType, null, null, queryParams );
        logger.info( "GET " + fhirUrl + url );

        IBaseBundle iBaseBundle = ourClient.search()
                .byUrl( url )
                .execute();
        return iBaseBundle;
    }

    @Override
    public IBaseResource getResource(String resourceType, String resourceId, Map<String, String> queryParams) throws FHIRException {
        String url = getUrl( resourceType, resourceId, null, queryParams );
        logger.info( "GET " + fhirUrl + url );

        IBaseResource iBaseResource = null;
        if ( resourceId.startsWith("$") ){
            return getResourceOperation( resourceType, resourceId, queryParams );
        } else {
            if ( queryParams != null && queryParams.size() != 0 ) {
                logger.severe( "queryParams on get resourcetype/id is not supported" );
            }

            iBaseResource = ourClient
                .read()
                .resource( resourceType )
                .withId( resourceId )
                .execute();
        }
        return iBaseResource;
    }

    @Override
    public IBaseResource getResourceOperation(String resourceType, String operationName, Map<String, String> queryParams) throws FHIRException {
        IBaseResource iBaseResource = null;
        try {
            Class resourceClass = Class.forName( "org.hl7.fhir.dstu3.model." + resourceType );
            // TODO parameters
            Parameters parameters = new Parameters();
            if ( queryParams != null ) {
                queryParams.entrySet().stream().forEach( stringStringEntry -> {
                    parameters.addParameter( new Parameters.ParametersParameterComponent()
                        .setName( stringStringEntry.getKey() )
                        .setValue( new StringType( stringStringEntry.getValue() ) ) );
                } );
            }
            // operation on resource and not a retrieval
            IBaseResource result = ourClient.operation()
                .onType( resourceClass )
                .named( operationName )
                .withParameters( parameters )
                .useHttpGet()
                .execute();
            iBaseResource = ((Parameters) result).getParameterFirstRep().getResource();
        } catch ( ClassNotFoundException e ) {
            e.printStackTrace();
            throw new FHIRException( "Unknown resource type " + resourceType );
        }
        return iBaseResource;
    }

    @Override
    public IBaseResource getResource(String resourceType, String resourceId, String params, Map<String, String> queryParams) throws FHIRException, NotImplementedException {
        String url = getUrl( resourceType, resourceId, params, queryParams );
        logger.info( "GET " + fhirUrl + url );

        FhirOperationCall operation =
            fhirOperationRepository.getGetOperation( this, resourceType, resourceId, params, queryParams );

        if ( operation!=null ){
            return  operation.getResult();
        }
        // operation not found, call sever
        Parameters parameters = new Parameters();
        if ( queryParams != null ) {
            queryParams.entrySet().stream().forEach( stringStringEntry -> {
                parameters.addParameter()
                    .setName( stringStringEntry.getKey() )
                    .setValue( new StringType( stringStringEntry.getValue() ) );
            } );
        }

        Parameters outParams = ourClient
                .operation()
                .onInstance(new IdDt(resourceType, resourceId ))
                .named(params)
                .withParameters( parameters )
                .useHttpGet()
                .execute();

        List<Parameters.ParametersParameterComponent> response = outParams.getParameter();

        Resource resource = response.get(0).getResource();
        return resource;
    }

    @Override
    public Bundle loadPage(Bundle resultBundle) {
        return  ourClient.loadPage().next( resultBundle ).execute();
    }

    private String getUrl(String resourceType, String id, String params, Map<String, String> queryParams) {
        String url = resourceType;
        url += ( id!=null ? "/"+id : "" );
        url += ( params!=null ? "/"+params : "" );

        if ( queryParams!=null ) {
            Iterator<Map.Entry<String, String>> iterator = queryParams.entrySet().iterator();
            if (iterator.hasNext()) {
                Map.Entry<String, String> entry = iterator.next();
                url += "?" + entry.getKey() + "=" + entry.getValue();
            }
            while (iterator.hasNext()) {
                Map.Entry<String, String> entry = iterator.next();
                url += "&" + entry.getKey() + "=" + entry.getValue();
            }
        }
        logger.info( "GET " + fhirUrl + url );

        return url;
    }

    @Override
    public IBaseOperationOutcome putResource(IBaseResource iBaseResource) {
        return ourClient.update().resource(iBaseResource).execute().getOperationOutcome();
    }

    @Override
    public IBaseOperationOutcome postResource(IBaseResource iBaseResource) {
        return ourClient.create().resource(iBaseResource).execute().getOperationOutcome();
    }

    @Override
    public IBaseResource postResource(
            String resourceType,
            String resourceId,
            IBaseResource parseResource,
            String params,
            Map<String, String> queryParams
    ) throws FHIRException, NotImplementedException {
        String url = getUrl( resourceType, resourceId, params, queryParams );
        logger.info( "POST " + fhirUrl + "/"+ url );

        if ( params.startsWith("$") ){
//            FhirOperationCall operation =
//                fhirOperationRepository.getPostOperation( this, resourceType, resourceId, params, parseResource, queryParams );
//
//            if ( operation!=null ){
//                return  operation.getResult();
//            }
            return getResourceOperation( resourceType, resourceId, queryParams );
        } else {

            String xml = ourCtx.newXmlParser().encodeResourceToString( parseResource );
            Parameters outParams = ourClient
                .operation()
                .onInstance( new IdDt( resourceType, resourceId ) )
                .named( params )
                .withNoParameters( Parameters.class )
                .execute();

            List<Parameters.ParametersParameterComponent> response = outParams.getParameter();

            Resource resource = response.get( 0 ).getResource();

            return resource;
        }
    }

    @Override
    public FhirContext getCtx() {
        return ourCtx;
    }

}
