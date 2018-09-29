package com.philips.research.philipsonfhir.fhirproxy.support.proxy.operation;

import com.philips.research.philipsonfhir.fhirproxy.support.proxy.service.FhirServer;

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

    public FhirOperationCall getGetOperation(FhirServer fhirServer, String resourceType, String resourceId, String operationName, Map<String, String> queryparams) {
        FhirResourceInstanceOperation operation = getFhirResourceInstanceOperation( resourceType, operationName );
        if ( operation != null ) {
            return operation.createOperationCall( fhirServer, resourceId, queryparams );
        }
        return null;
    }

//    public FhirOperationCall getPostOperation(FhirServer fhirServer, String resourceType, String resourceId, String operationName, IBaseResource parseResource, Map<String, String> queryParams) {
//        FhirResourceInstanceOperation operation = getFhirResourceInstanceOperation( resourceType, operationName );
//        if ( operation != null ) {
//            return operation.createOperationCall( fhirServer, resourceId, parseResource, queryParams );
//        }
//        return null;
//    }
}
