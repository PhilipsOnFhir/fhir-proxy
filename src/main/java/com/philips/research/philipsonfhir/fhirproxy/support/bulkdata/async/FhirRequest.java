package com.philips.research.philipsonfhir.fhirproxy.support.bulkdata.async;

import com.philips.research.philipsonfhir.fhirproxy.support.proxy.operation.FhirOperationCall;
import com.philips.research.philipsonfhir.fhirproxy.support.proxy.service.IFhirServer;
import lombok.Getter;
import lombok.Setter;
import org.hl7.fhir.exceptions.FHIRException;
import org.hl7.fhir.instance.model.api.IBaseResource;

import java.util.Map;

@Getter
@Setter
public class FhirRequest {
    private FhirOperationCall fhirOperationCall = null;
    private String callUrl;
    private IFhirServer fhirServer;
    private String resourceType;
    private String id;
    private String params;
    private Map<String, String> queryParams;

    public FhirRequest(String callUrl, FhirOperationCall fhirOperationCall) {
        this.fhirOperationCall = fhirOperationCall;
        this.callUrl = callUrl;
    }

    public FhirRequest(String callUrl, IFhirServer fhirServer, String resourceType, String id, String params, Map<String, String> queryParams) {
        this.callUrl = callUrl;
        this.fhirServer = fhirServer;
        this.resourceType = resourceType;
        this.id = id;
        this.params = params;
        this.queryParams = queryParams;
    }

    public IBaseResource getResource() throws FHIRException {
        if ( this.fhirOperationCall != null ) {
            return this.fhirOperationCall.getResult();
        } else {
            return getResourceOld();
        }
    }

    private IBaseResource getResourceOld() throws FHIRException {
        if ( resourceType == null ) {
            throw new FHIRException("resource type not set");
        }
        if ( id!=null){
            if ( params!=null ) {
                return fhirServer.getResource(resourceType, id, params, queryParams);
            } else{
                return fhirServer.getResource(resourceType, id, queryParams);
            }
        } else{
            if ( params!=null ) {
                throw new FHIRException("params on search not supported.");
            } else{
                return fhirServer.searchResource( resourceType, queryParams);
            }
        }
    }

    public boolean returnNdJson() {
        if ( queryParams == null ) {
            return true;
        }

        String outputFormat = queryParams.get( "_outputFormat" );
        return outputFormat == null ||
            (outputFormat != null && outputFormat.equals( "application/fhir+ndjson" ));
    }
}
