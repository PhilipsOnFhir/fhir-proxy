package org.github.philipsonfhir.fhirproxy.clinicalreasoning.activitydefinition;

import org.github.philipsonfhir.fhirproxy.common.FhirProxyException;
import org.github.philipsonfhir.fhirproxy.common.fhircall.FhirCall;
import org.github.philipsonfhir.fhirproxy.common.fhircall.FhirRequest;
import org.github.philipsonfhir.fhirproxy.common.operation.FhirResourceInstanceOperation;
import org.github.philipsonfhir.fhirproxy.controller.service.FhirServer;
import org.hl7.fhir.dstu3.model.*;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.opencds.cqf.cql.data.fhir.BaseFhirDataProvider;
import org.opencds.cqf.cql.data.fhir.FhirDataProviderStu3;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class ActivityDefinitionApplyOperation implements FhirResourceInstanceOperation {
    Logger logger = LoggerFactory.getLogger( this.getClass() );
    private String statusDescription;

    @Override
    public FhirCall createFhirCall(FhirServer fhirServer, FhirRequest fhirRequest) throws FhirProxyException {
        return new FhirCall() {
            private Resource result;

            @Override
            public void execute() throws FhirProxyException {
                BaseFhirDataProvider baseFhirDataProvider = new FhirDataProviderStu3().setEndpoint( fhirServer.getServerUrl() );
                IdType idType = new IdType( ).setValue( getResourceType()+"/"+fhirRequest.getResourceId() );

                Map<String, String> queryParams = fhirRequest.getQueryMap();
                String patientId       = queryParams.get( "patient" );
                String encounterId     = queryParams.get( "encounter");
                String practitionerId  = queryParams.get( "practitioner");
                String organizationId  = queryParams.get( "organization");
                String userType        = queryParams.get( "userType");
                String userLanguage    = queryParams.get( "userLanguage");
                String userTaskContext = queryParams.get( "userTaskComtext");
                String setting         = queryParams.get("setting");
                String settingContext  = queryParams.get("settingContext");

                ActivityDefinitionProcessor activityDefinitionProcessor = new ActivityDefinitionProcessor(
                        baseFhirDataProvider, idType
                        , patientId, encounterId, practitionerId, organizationId, userType, userLanguage, userTaskContext, setting, settingContext );
                result =  activityDefinitionProcessor.getResult();
            }

            @Override
            public String getStatusDescription() {
                return statusDescription;
            }

            @Override
            public IBaseResource getResource() throws FhirProxyException {
                return result;
            }

            @Override
            public FhirServer getFhirServer() {
                return fhirServer;
            }

            @Override
            public Map<String, OperationOutcome> getErrors() {
                return new HashMap<>();
            }
        };

    }
    private void updateStatus(String str) {
        logger.info(str);
        this.statusDescription = str;
    }

    @Override
    public String getOperationName() {
        return "$apply";
    }

    @Override
    public OperationDefinition getOperation() {

        OperationDefinition operationDefinition = (OperationDefinition) new OperationDefinition()
                .setUrl("http://hl7.org/fhir/OperationDefinition/ActivityDefinition-apply")
                .setName("Apply")
                .setStatus(Enumerations.PublicationStatus.DRAFT)
                .setKind(OperationDefinition.OperationKind.OPERATION)
                .setPublisher( "HL7 (FHIR Project)" )
                .setDescription( "The apply operation realizes a definition in a specific context" )
                .setCode("apply")
                .setComment("The result of invoking this operation is a resource of the type specified by the activity definition, with all the definitions resolved as appropriate for the type of resource. Any dynamicValue elements will be evaluated and the resulting value used for the values of the appropriate elements of the returned resource.  If the ActivityDefinition includes library references, those libraries will be available to the evaluated expressions. If those libraries have parameters, those parameters will be bound by name to the parameters given to the operation")
                .addResource("ActivityDefinition")
                .setSystem(false)
                .setType(false)
                .setInstance(true)
                .addParameter( new OperationDefinition.OperationDefinitionParameterComponent()
                        .setName("patient")
                        .setUse(OperationDefinition.OperationParameterUse.IN)
                        .setMin(1)
                        .setMax("1")
                        .setDocumentation("The patient that is the target of the activity to be applied")
                        .setType("Reference")
                        .setProfile(new Reference().setReference("http://hl7.org/fhir/StructureDefinition/Patient"))
                )
                .addParameter( new OperationDefinition.OperationDefinitionParameterComponent()
                        .setName("encounter")
                        .setUse(OperationDefinition.OperationParameterUse.IN)
                        .setMin(0)
                        .setMax("1")
                        .setDocumentation("The encounter in context, if any")
                        .setType("Reference")
                        .setProfile(new Reference().setReference("http://hl7.org/fhir/StructureDefinition/Encounter"))
                )
                .addParameter( new OperationDefinition.OperationDefinitionParameterComponent()
                        .setName("practitioner")
                        .setUse(OperationDefinition.OperationParameterUse.IN)
                        .setMin(0)
                        .setMax("1")
                        .setDocumentation("The practitioner in context")
                        .setType("Reference")
                        .setProfile(new Reference().setReference("http://hl7.org/fhir/StructureDefinition/Practitioner"))
                )
                .addParameter( new OperationDefinition.OperationDefinitionParameterComponent()
                        .setName("organization")
                        .setUse(OperationDefinition.OperationParameterUse.IN)
                        .setMin(0)
                        .setMax("1")
                        .setDocumentation("The organization  in context")
                        .setType("Reference")
                        .setProfile(new Reference().setReference("http://hl7.org/fhir/StructureDefinition/Organization "))
                )
                .addParameter( new OperationDefinition.OperationDefinitionParameterComponent()
                        .setName("userType")
                        .setUse(OperationDefinition.OperationParameterUse.IN)
                        .setMin(0)
                        .setMax("1")
                        .setDocumentation("The type of user initiating the request, e.g. patient, healthcare provider, or specific type of healthcare provider (physician, nurse, etc.)")
                        .setType("CodeableConcept")
                )
                .addParameter( new OperationDefinition.OperationDefinitionParameterComponent()
                        .setName("userLanguage")
                        .setUse(OperationDefinition.OperationParameterUse.IN)
                        .setMin(0)
                        .setMax("1")
                        .setDocumentation("Preferred language of the person using the system")
                        .setType("CodeableConcept")
                )
                .addParameter( new OperationDefinition.OperationDefinitionParameterComponent()
                        .setName("setting")
                        .setUse(OperationDefinition.OperationParameterUse.IN)
                        .setMin(0)
                        .setMax("1")
                        .setDocumentation("The current setting of the request (inpatient, outpatient, etc)")
                        .setType("CodeableConcept")
                )
                .addParameter( new OperationDefinition.OperationDefinitionParameterComponent()
                        .setName("settingContext")
                        .setUse(OperationDefinition.OperationParameterUse.IN)
                        .setMin(0)
                        .setMax("1")
                        .setDocumentation("Additional detail about the setting of the request, if any")
                        .setType("CodeableConcept")
                )
                .addParameter( new OperationDefinition.OperationDefinitionParameterComponent()
                        .setName("return")
                        .setUse(OperationDefinition.OperationParameterUse.OUT)
                        .setMin(1)
                        .setMax("1")
                        .setDocumentation("The resource that is the result of applying the definition")
                        .setType("Any")
                )
                .setId("ActivityDefinition-apply");


        return operationDefinition;
    }

    @Override
    public String getResourceType() {
        return "ActivityDefinition";
    }
}
