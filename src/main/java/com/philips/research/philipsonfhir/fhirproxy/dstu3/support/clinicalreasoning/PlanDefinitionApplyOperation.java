package com.philips.research.philipsonfhir.fhirproxy.dstu3.support.clinicalreasoning;

import com.philips.research.philipsonfhir.fhirproxy.dstu3.support.NotImplementedException;
import com.philips.research.philipsonfhir.fhirproxy.dstu3.support.clinicalreasoning.processor.ActivityDefinitionProcessor;
import com.philips.research.philipsonfhir.fhirproxy.dstu3.support.proxy.operation.FhirOperationCall;
import com.philips.research.philipsonfhir.fhirproxy.dstu3.support.proxy.operation.FhirResourceInstanceOperation;
import com.philips.research.philipsonfhir.fhirproxy.dstu3.support.proxy.service.FhirServer;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.OperationOutcome;
import org.hl7.fhir.exceptions.FHIRException;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.opencds.cqf.cql.data.fhir.BaseFhirDataProvider;
import org.opencds.cqf.cql.data.fhir.FhirDataProviderStu3;

import java.util.Map;

public class PlanDefinitionApplyOperation extends FhirResourceInstanceOperation {

    private final String url;

    public PlanDefinitionApplyOperation(String url ) {
        super( "ActivityDefinition", "$apply" );
        this.url = url;
    }

    @Override
    public FhirOperationCall createGetOperationCall(FhirServer fhirServer, String resourceId, Map<String, String> queryParams) {
        return new FhirOperationCall() {
            @Override
            public IBaseResource getResult() throws FHIRException, NotImplementedException {
                BaseFhirDataProvider baseFhirDataProvider = new FhirDataProviderStu3().setEndpoint( url );
                IdType idType = new IdType( ).setValue( resourceType+"/"+resourceId );

                String patientId = queryParams.get( "patient" );
                String encounterId = queryParams.get( "encounter");
                String practitionerId = queryParams.get( "practitioner");
                String organizationId = queryParams.get( "organization");
                String userType       = queryParams.get( "userType");
                String userLanguage   = queryParams.get("userLanguage");
                String userTaskContext = queryParams.get( "userTaskComtext");
                String setting         = queryParams.get("setting");
                String settingContext  = queryParams.get("settingContext");

                ActivityDefinitionProcessor activityDefinitionProcessor = new ActivityDefinitionProcessor(
                    baseFhirDataProvider, idType
                    , patientId, encounterId, practitionerId, organizationId, userType, userLanguage, userTaskContext, setting, settingContext );
                return activityDefinitionProcessor.getResult();
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
