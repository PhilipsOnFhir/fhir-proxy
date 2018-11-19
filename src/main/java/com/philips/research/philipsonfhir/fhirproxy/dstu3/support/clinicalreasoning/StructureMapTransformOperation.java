package com.philips.research.philipsonfhir.fhirproxy.dstu3.support.clinicalreasoning;

import ca.uhn.fhir.rest.client.api.IGenericClient;
import com.philips.research.philipsonfhir.fhirproxy.dstu3.support.NotImplementedException;
import com.philips.research.philipsonfhir.fhirproxy.dstu3.support.clinicalreasoning.processor.StructureMapTransformServer;
import com.philips.research.philipsonfhir.fhirproxy.dstu3.support.proxy.operation.FhirOperationCall;
import com.philips.research.philipsonfhir.fhirproxy.dstu3.support.proxy.operation.FhirResourceInstanceOperation;
import com.philips.research.philipsonfhir.fhirproxy.dstu3.support.proxy.service.FhirServer;
import org.hl7.fhir.dstu3.model.*;
import org.hl7.fhir.exceptions.FHIRException;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.opencds.cqf.cql.data.fhir.BaseFhirDataProvider;
import org.opencds.cqf.cql.data.fhir.FhirDataProviderStu3;

import java.util.Map;
import java.util.Optional;

public class StructureMapTransformOperation extends FhirResourceInstanceOperation {
    private final IGenericClient client;
    private final String url;
    private StructureMapTransformServer structureMapTransformServer;

    public StructureMapTransformOperation(String url, IGenericClient client) throws FHIRException {
        super( ResourceType.StructureMap.name(), "$transform" );
        this.client = client;
        this.url = url;
        this.structureMapTransformServer = new StructureMapTransformServer( client.getFhirContext() );
    }

    @Override
    public FhirOperationCall createGetOperationCall(FhirServer fhirServer, Map<String, String> queryparams) throws NotImplementedException {
        throw new NotImplementedException();
    }

    @Override
    public FhirOperationCall createGetOperationCall(FhirServer fhirServer, String resourceId, Map<String, String> queryparams) throws NotImplementedException {
        throw new NotImplementedException();
    }

    @Override
    public FhirOperationCall createPostOperationCall(FhirServer fhirServer, String resourceId, IBaseResource parameters, Map<String, String> queryParams) {
        return new FhirOperationCall() {
            @Override
            public IBaseResource getResult() throws FHIRException, NotImplementedException {
                BaseFhirDataProvider baseFhirDataProvider = new FhirDataProviderStu3().setEndpoint( url );
                IdType idType = new IdType().setValue( resourceType + "/" + resourceId );

                String source = queryParams.get( "source" );

                Optional<Resource> optSourceResource = ((Parameters) parameters).getParameter().stream()
                        .filter( parameter -> parameter.getName().equals( "source" ) )
                        .map( parameter -> parameter.getResource() )
                        .findFirst();

                Optional<Resource> optContentResource = ((Parameters) parameters).getParameter().stream()
                        .filter( parameter -> parameter.getName().equals( "content" ) )
                        .map( parameter -> parameter.getResource() )
                        .findFirst();


                if ( !optContentResource.isPresent() ) {
                    throw new FHIRException( "missing content parameter" );
                }
                Resource contentResource = optContentResource.get();

                StructureMap structuredMap =
                       client.read().resource( StructureMap.class ).withId( resourceId ).execute();
                if ( structuredMap==null ){
                    throw new FHIRException( "StructureMap "+resourceId+" can not be found" );
                }

                IBaseResource result = structureMapTransformServer.doTransform( structuredMap, contentResource, null );
                return result;
            }

            @Override
            public String getDescription() {
                return null;
            }

            @Override
            public Map<String, OperationOutcome> getErrors() {
                return null;
            }
        };
    }

    @Override
    public FhirOperationCall createPostOperationCall(FhirServer fhirServer, IBaseResource parameters, Map<String, String> queryParams) throws NotImplementedException {
        return new FhirOperationCall() {
            @Override
            public IBaseResource getResult() throws FHIRException, NotImplementedException {
                BaseFhirDataProvider baseFhirDataProvider = new FhirDataProviderStu3().setEndpoint( url );

                Optional<Resource> optSourceResource = ((Parameters) parameters).getParameter().stream()
                        .filter( parameter -> parameter.getName().equals( "source" ) )
                        .map( parameter -> parameter.getResource() )
                        .findFirst();
                if ( !optSourceResource.isPresent() ) {
                    throw new FHIRException( "missing source parameter" );
                }

                Optional<Resource> optContentResource = ((Parameters) parameters).getParameter().stream()
                        .filter( parameter -> parameter.getName().equals( "content" ) )
                        .map( parameter -> parameter.getResource() )
                        .findFirst();
                if ( !optContentResource.isPresent() ) {
                    throw new FHIRException( "missing content parameter" );
                }

                Resource contentResource = optContentResource.get();
                Resource sourceResource  = optSourceResource.get();

                if ( sourceResource==null || !(sourceResource instanceof StructureMap)){
                    throw new FHIRException( "Body should contain a valid structureMap" );
                }
                StructureMap structuredMap = (StructureMap) sourceResource;

                IBaseResource result = structureMapTransformServer.doTransform( structuredMap, contentResource, null );
                return result;
            }

            @Override
            public String getDescription() {
                return null;
            }

            @Override
            public Map<String, OperationOutcome> getErrors() {
                return null;
            }
        };
    }

}
