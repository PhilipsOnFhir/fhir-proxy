package com.philips.research.philipsonfhir.fhirproxy.dstu3.support.proxy.operation;

import com.philips.research.philipsonfhir.fhirproxy.dstu3.support.NotImplementedException;
import com.philips.research.philipsonfhir.fhirproxy.dstu3.support.proxy.service.FhirServer;
import org.hl7.fhir.instance.model.api.IBaseResource;

import java.util.Map;
import java.util.TreeMap;

public class FhirOperationRepository {
    private TreeMap<String, Map<String, FhirResourceInstanceOperation>> fhirResourceInstanceOperationMap = new TreeMap<>(  );

    public void registerOperation(FhirResourceInstanceOperation fhirOperation ) {
        Map<String, FhirResourceInstanceOperation> map =
            this.fhirResourceInstanceOperationMap.get( fhirOperation.getResourceType());
        if ( map==null ){
            map = new TreeMap<>(  );
            this.fhirResourceInstanceOperationMap.put( fhirOperation.getResourceType(), map );
        }
        map.put( fhirOperation.getOperationName(), fhirOperation );
    }

    private FhirResourceInstanceOperation getFhirResourceInstanceOperation(String resourceType, String operationName ) {
        Map<String, FhirResourceInstanceOperation> map = this.fhirResourceInstanceOperationMap.get( resourceType );
        if ( map!=null ){
            return map.get( operationName );
        }
        return null;
    }

    public FhirOperationCall doGetOperation(FhirServer fhirServer, String resourceType, String operationName, Map<String, String> queryparams) throws NotImplementedException {
        FhirResourceInstanceOperation operation = getFhirResourceInstanceOperation( resourceType, operationName );
        if ( operation != null ) {
            return operation.createGetOperationCall( fhirServer, queryparams );
        }
        return null;
    }

    public FhirOperationCall doGetOperation(FhirServer fhirServer, String resourceType, String resourceId, String operationName, Map<String, String> queryparams) throws NotImplementedException {
        FhirResourceInstanceOperation operation = getFhirResourceInstanceOperation( resourceType, operationName );
        if ( operation != null ) {
            return operation.createGetOperationCall( fhirServer, resourceId, queryparams );
        }
        return null;
    }

    public FhirOperationCall doPostOperation(FhirServer fhirServer, String resourceType, String resourceId, String operationName, IBaseResource parseResource, Map<String, String> queryParams) throws NotImplementedException {
        FhirResourceInstanceOperation operation = getFhirResourceInstanceOperation( resourceType, operationName );
        if ( operation != null ) {
            return operation.createPostOperationCall( fhirServer, resourceId, parseResource, queryParams );
        }
        return null;
    }

    public FhirOperationCall doPostOperation(FhirServer fhirServer, String resourceType, String operationName, IBaseResource parseResource, Map<String, String> queryParams) throws NotImplementedException {
        FhirResourceInstanceOperation operation = getFhirResourceInstanceOperation( resourceType, operationName );
        if ( operation != null ) {
            return operation.createPostOperationCall( fhirServer, parseResource, queryParams );
        }
        return null;
    }
}
