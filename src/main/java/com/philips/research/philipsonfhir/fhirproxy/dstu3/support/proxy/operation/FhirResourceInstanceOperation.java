package com.philips.research.philipsonfhir.fhirproxy.dstu3.support.proxy.operation;

import com.philips.research.philipsonfhir.fhirproxy.dstu3.support.NotImplementedException;
import com.philips.research.philipsonfhir.fhirproxy.dstu3.support.proxy.service.FhirServer;
import lombok.Getter;
import org.hl7.fhir.dstu3.model.*;
import org.hl7.fhir.exceptions.FHIRException;
import org.hl7.fhir.instance.model.api.IBaseResource;

import java.util.Map;

@Getter
public abstract class FhirResourceInstanceOperation {


    protected final String resourceType;
    protected final String operationName;

    public FhirResourceInstanceOperation(String resourceType, String operationName) {
        this.resourceType = resourceType;
        this.operationName = operationName;
    }

    public FhirOperationCall createGetOperationCall(FhirServer fhirServer, String resourceId, Map<String, String> queryparams) throws NotImplementedException {
        throw new NotImplementedException();
    }

    public FhirOperationCall createPostOperationCall(FhirServer fhirServer, String resourceId, IBaseResource parseResource, Map<String, String> queryParams) throws NotImplementedException {
        throw new NotImplementedException();
    }

    protected void populateParameter(Parameters parameters, String name, boolean mandatory, Enumerations.FHIRAllTypes base, Map<String, String> queryParams ) throws FHIRException, NotImplementedException {
        if (queryParams.containsKey(name)){
            Parameters.ParametersParameterComponent parametersParameterComponent = new Parameters.ParametersParameterComponent();
            parametersParameterComponent.setName(name);
            switch( base ){
                case REFERENCE:
                    parametersParameterComponent.setValue( new Reference(queryParams.get(name)) );
                    break;
                default:
                    throw new NotImplementedException("Parameters of type "+ base.getDisplay()+ " are not yet supported.");
            }
            parameters.addParameter(  parametersParameterComponent );

        } else if ( mandatory ){
            throw new FHIRException("Missing required parameter "+name);
        }
    }
}
