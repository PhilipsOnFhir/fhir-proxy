package org.github.philipsonfhir.fhirproxy.clinicalreasoning.questionnaire;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.api.EncodingEnum;
import ca.uhn.fhir.rest.client.api.ServerValidationModeEnum;
import org.github.philipsonfhir.fhirproxy.common.FhirProxyException;
import org.github.philipsonfhir.fhirproxy.common.FhirProxyNotImplementedException;
import org.github.philipsonfhir.fhirproxy.common.fhircall.FhirCall;
import org.github.philipsonfhir.fhirproxy.common.fhircall.FhirRequest;
import org.github.philipsonfhir.fhirproxy.common.operation.FhirResourceInstanceOperation;
import org.github.philipsonfhir.fhirproxy.common.operation.FhirResourceOperation;
import org.github.philipsonfhir.fhirproxy.common.util.ParametersUtil;
import org.github.philipsonfhir.fhirproxy.controller.service.FhirServer;
import org.hl7.fhir.dstu3.model.*;
import org.hl7.fhir.dstu3.model.Parameters.ParametersParameterComponent;
import org.hl7.fhir.exceptions.FHIRException;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.opencds.cqf.cql.data.fhir.BaseFhirDataProvider;
import org.opencds.cqf.cql.data.fhir.FhirDataProviderStu3;
import org.opencds.cqf.cql.terminology.fhir.FhirTerminologyProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class QuestionnairePopulateOperation implements FhirResourceInstanceOperation, FhirResourceOperation {
    Logger logger = LoggerFactory.getLogger( this.getClass() );
    private String statusDescription;

    @Override
    public FhirCall createFhirCall(FhirServer fhirServer, FhirRequest fhirRequest) throws FhirProxyException {
        return new FhirCall() {
            private Resource result;
            Map<String, OperationOutcome> errors = new HashMap<>();

            @Override
            public void execute() throws FhirProxyException {
                BaseFhirDataProvider baseFhirDataProvider = new FhirDataProviderStu3().setEndpoint(fhirServer.getServerUrl());
                FhirContext fhirContext = baseFhirDataProvider.getFhirContext();
                fhirContext.getRestfulClientFactory().setServerValidationMode(ServerValidationModeEnum.NEVER);
                baseFhirDataProvider.setFhirContext(fhirContext);
                baseFhirDataProvider.getFhirClient().setEncoding(EncodingEnum.JSON);

                FhirTerminologyProvider terminologyProvider = new FhirTerminologyProvider();
                terminologyProvider.setEndpoint(fhirServer.getServerUrl(), false );

                baseFhirDataProvider.setTerminologyProvider(terminologyProvider);

                Parameters parameters = (Parameters) fhirRequest.getBodyResource();

                populateParameter(parameters,"identifier", false, Enumerations.FHIRAllTypes.URI, fhirRequest.getQueryMap());
                populateParameter(parameters,"questionnaire", false, Enumerations.FHIRAllTypes.URI, fhirRequest.getQueryMap());
                populateParameter(parameters,"questionnaireRef", false, Enumerations.FHIRAllTypes.REFERENCE, fhirRequest.getQueryMap());
                populateParameter(parameters,"subject", true, Enumerations.FHIRAllTypes.REFERENCE, fhirRequest.getQueryMap());
                populateParameter(parameters,"content", false, Enumerations.FHIRAllTypes.REFERENCE, fhirRequest.getQueryMap());
                populateParameter(parameters,"local", false, Enumerations.FHIRAllTypes.BOOLEAN, fhirRequest.getQueryMap());

                ParametersParameterComponent qResource = ParametersUtil.getParameter(parameters, "questionnaire");
                ParametersParameterComponent qResourceRef = ParametersUtil.getParameter(parameters, "questionnaireRef");

                if ( ParametersUtil.holdsParameter(parameters, "questionnaire") || ParametersUtil.holdsParameter(parameters, "questionnaire") ){
                } else {
                    if ( fhirRequest.getResourceId()==null ){
                        throw new FhirProxyException(String.format( "No questionnaire indicated in %s", getOperationName()));
                    }
                    parameters.addParameter( new ParametersParameterComponent()
                            .setName("questionnaireRef")
                            .setValue( new Reference().setReference( fhirRequest.getResourceType()+"/"+fhirRequest.getResourceId()))
                    );
                }


                QuestionnairePopulateProcessor questionnairePopulateProcessor =
                        new QuestionnairePopulateProcessor(baseFhirDataProvider, fhirRequest.getResourceId(), parameters);
                this.result = questionnairePopulateProcessor.getQuestionnaireResponse();
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
                return errors;
            }

            private void populateParameter(Parameters parameters, String name, boolean mandatory, Enumerations.FHIRAllTypes base, Map<String, String> queryParams ) throws FhirProxyNotImplementedException {
                Optional<ParametersParameterComponent> opt = parameters.getParameter().stream()
                        .filter(p -> p.getName().equals(name))
                        .findFirst();

                if (queryParams.containsKey(name)){
                    ParametersParameterComponent parametersParameterComponent;
                    if ( opt.isPresent() ) {
                        parametersParameterComponent = opt.get();
                    } else {
                        parametersParameterComponent = new ParametersParameterComponent();
                        parametersParameterComponent.setName(name);
                        parameters.addParameter(parametersParameterComponent);
                    }

                    switch (base) {
                        case REFERENCE:
                            parametersParameterComponent.setValue(new Reference(queryParams.get(name)));
                            break;
                        default:
                            throw new FhirProxyNotImplementedException("Parameters of type " + base.getDisplay() + " are not yet supported.");
                    }

                } else if ( !opt.isPresent() && mandatory ){
                    throw new FHIRException("Missing required parameter "+name);
                }
            }
        };
    }

    @Override
    public String getResourceType() {
        return "Questionnaire";
    }

    @Override
    public String getOperationName() {
        return "$populate";
    }

    @Override
    public OperationDefinition getOperation() {
        return (OperationDefinition) new OperationDefinition()
                .setUrl("http://hl7.org/fhir/OperationDefinition/Questionnaire-populate")
                .setName("Populate Questionnaire")
                .setStatus(Enumerations.PublicationStatus.DRAFT)
                .setKind(OperationDefinition.OperationKind.OPERATION)
                .setPublisher( "HL7 (FHIR Project)" )
                .setDescription( "Generates a [QuestionnaireResponse](questionnaireresponse.html) instance  based on a specified [Questionnaire](questionnaire.html), filling in answers to questions where possible based on information provided as part of the operation or already known by the server about the subject of the [Questionnaire](questionnaire.html).  If the operation is not called at the instance level, one and only one of the identifier, questionnaire or questionnaireRef 'in' parameters must be provided. If called at the instance level, these parameters will be ignored.  The response will contain a [QuestionnaireResponse](questionnaireresponse.html) instance based on the specified [Questionnaire](questionnaire.html) and/or an [OperationOutcome](operationoutcome.html) resource with errors or warnings.  The [QuestionnaireResponse](questionnaireresponse.html) instance will be populated with an unanswered set of questions following the group and question structure of the specified [Questionnaire](questionnaire.html).  If  *content* parameters were specified or the *local* parameter was set to true, some of the questions may have answers filled in as well.  In the case of repeating questions or groups, typically only one repetition will be provided unless answer values exist that would support populating multiple repetitions.  Population of the [QuestionnaireResponse](questionnaireresponse.html) with appropriate data is dependent on the questions and/or groups in the [Questionnaire](questionnaire.html) having metadata that allows the server to recognize the questions, e.g. through Questionnaire.item.definition or through use of the [ConceptMap](conceptmap.html) resource.  Regardless of the mechanism used to link the questions in a questionnaire to a \\\"known\\\" mappable concept, solutions using this operation should ensure that the details of the question and associated linkage element are sufficiently similar as to safely allow auto-population; i.e. the question text and context must be sufficiently the same, the value set for the question must fall within the value set for the mapped element, the data types must be the same or convertible, etc.\",\n" +
                        "  \"code\": \"populate\",\n" +
                        "  \"comment\": \"While it is theoretically possible for a [QuestionnaireResponse](questionnaireresponse.html) instance to be completely auto-populated and submitted without human review, the intention of this transaction is merely to reduce redundant data entry.  A client **SHOULD** ensure that a human submitter has an opportunity to review the auto-populated answers to confirm correctness as well as to complete or expand on information provided by the auto-population process.  When creating an \\\"empty\\\" questionnaire, the general algorithm is to create a QuestionnaireResponse with one item for every item in the source Questionnaire, including items with \\\"enableWhen\\\", \\\"display\\\" items, etc.  If a question has a default, the default answer should be populated.  And the QuestionnaireResponse should point back to the originating Questionnaire.  Repeating items will typically only include a single repetition.  Other extensions and/or elements may also be populated if the system is aware of appropriate values.  Complex form designs with conditional logic or tight constraints on cardinalities may be challenging to auto-populate.  A server MAY choose to traverse the questionnaire as if it were a human respondent, answering only those questions that are enabled based on previously answered questions.  However, doing so may result in minimal population.  Alternatively, systems may choose to populate all known answers, independent of dependencies and other constraints.  This may cause questions to be answered that should not be answered.  It will be up to the client to appropriately prune the final populated questionnaire once human review has taken place.  Invoking this operation with the ''content'' parameter may involve the disclosure of personally identifiable healthcare information to the system which is performing the population process.  No such disclosures should be made unless the system on which the operation is being invoked is a \\\"trusted\\\" system and appropriate agreements are in place to protect the confidentiality of any information shared with that system." )
                .setCode("populate")
                .setComment("The result of this operation is a CarePlan resource, with activities for each of the applicable actions based on evaluating the applicability condition in context. For each applicable action, the activitydefinition is applied as described in the $apply operation of the ActivityDefinition resource, and the resulting resource is added as an activity to the CarePlan. If the ActivityDefinition includes library references, those libraries will be available to the evaluated expressions. If those libraries have parameters, those parameters will be bound by name to the parameters given to the operation")
                .addResource("Questionnaire")
                .setSystem(false)
                .setType(true)
                .setInstance(true)
                .addParameter( new OperationDefinition.OperationDefinitionParameterComponent()
                        .setName("identifier")
                        .setUse(OperationDefinition.OperationParameterUse.IN)
                        .setMin(1)
                        .setMax("1")
                        .setDocumentation("A logical questionnaire identifier (i.e. ''Questionnaire.identifier''). The server must know the questionnaire or be able to retrieve it from other known repositories.")
                        .setType("uri")
                )
                .addParameter( new OperationDefinition.OperationDefinitionParameterComponent()
                        .setName("questionnaire")
                        .setUse(OperationDefinition.OperationParameterUse.IN)
                        .setMin(0)
                        .setMax("1")
                        .setDocumentation("The [Questionnaire](questionnaire.html) is provided directly as part of the request. Servers may choose not to accept questionnaires in this fashion")
                        .setType("Questionnaire")
                )
                .addParameter( new OperationDefinition.OperationDefinitionParameterComponent()
                        .setName("questionnaireRef")
                        .setUse(OperationDefinition.OperationParameterUse.IN)
                        .setMin(0)
                        .setMax("1")
                        .setDocumentation("The [Questionnaire](questionnaire.html) is provided as a resource reference. Servers may choose not to accept questionnaires in this fashion or may fail if they cannot resolve or access the referenced questionnaire.")
                        .setType("Reference")
                        .setProfile(new Reference().setReference("http://hl7.org/fhir/StructureDefinition/Questionnaire"))
                )
                .addParameter( new OperationDefinition.OperationDefinitionParameterComponent()
                        .setName("subject")
                        .setUse(OperationDefinition.OperationParameterUse.IN)
                        .setMin(1)
                        .setMax("1")
                        .setDocumentation("The resource that is to be the *QuestionnaireResponse.subject*. The [QuestionnaireResponse](questionnaireresponse.html) instance will reference the provided subject.  In addition, if the *local* parameter is set to true, server information about the specified subject will be used to populate the instance.")
                        .setType("Reference")
                )
                .addParameter( new OperationDefinition.OperationDefinitionParameterComponent()
                        .setName("content")
                        .setUse(OperationDefinition.OperationParameterUse.IN)
                        .setMin(0)
                        .setMax("*")
                        .setDocumentation("Resources containing information to be used to help populate the [QuestionnaireResponse](questionnaireresponse.html).  These may be FHIR resources or may be binaries containing FHIR documents, CDA documents or other source materials.  Servers might not support all possible source materials and may ignore materials they do not recognize.  (They MAY provide warnings if ignoring submitted resources.)")
                        .setType("Reference")
                )
                .addParameter( new OperationDefinition.OperationDefinitionParameterComponent()
                        .setName("local")
                        .setUse(OperationDefinition.OperationParameterUse.IN)
                        .setMin(0)
                        .setMax("1")
                        .setDocumentation("If specified and set to 'true' (and the server is capable), the server should use what resources and other knowledge it has about the referenced subject when pre-populating answers to questions.")
                        .setType("boolean")
                )
                .addParameter( new OperationDefinition.OperationDefinitionParameterComponent()
                        .setName("questionnaire")
                        .setUse(OperationDefinition.OperationParameterUse.OUT)
                        .setMin(1)
                        .setMax("1")
                        .setDocumentation("The partially (or fully)-populated set of answers for the specified Questionnaire")
                        .setType("QuestionnaireResponse")
                )
                .addParameter( new OperationDefinition.OperationDefinitionParameterComponent()
                        .setName("issues")
                        .setUse(OperationDefinition.OperationParameterUse.IN)
                        .setMin(0)
                        .setMax("1")
                        .setDocumentation("A list of hints and warnings about problems encountered while populating the questionnaire. These might be show to the user as an advisory note. Note: if the questionnaire cannot be populated at all, then the operation should fail, and an OperationOutcome is returned directly with the failure, rather than using this parameter")
                        .setType("OperationOutcome")
                )
                .setId("Questionnaire-populate");
    }
}
