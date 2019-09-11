package org.github.philipsonfhir.fhirproxy.clinicalreasoning.structuremap;

import org.github.philipsonfhir.fhirproxy.common.FhirProxyException;
import org.github.philipsonfhir.fhirproxy.common.fhircall.FhirCall;
import org.github.philipsonfhir.fhirproxy.common.fhircall.FhirRequest;
import org.github.philipsonfhir.fhirproxy.common.operation.FhirResourceInstanceOperation;
import org.github.philipsonfhir.fhirproxy.common.operation.FhirResourceOperation;
import org.github.philipsonfhir.fhirproxy.common.util.ReferenceUtil;
import org.github.philipsonfhir.fhirproxy.controller.service.FhirServer;
import org.hl7.fhir.dstu3.model.*;
import org.hl7.fhir.exceptions.FHIRException;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class StructureMapTransformOperation implements FhirResourceInstanceOperation, FhirResourceOperation {
    private StructureMapTransformWorker structureMapTransformServer;
    Logger logger = LoggerFactory.getLogger( this.getClass() );

    public StructureMapTransformOperation() throws FHIRException {
        this.structureMapTransformServer = new StructureMapTransformWorker();
    }


    @Override
    public FhirCall createFhirCall(FhirServer fhirServer, FhirRequest fhirRequest) throws FhirProxyException {

        return new FhirCall() {
            IBaseResource result = null;
            String statusDescription = "unknown";


            @Override
            public void execute() throws FhirProxyException {
                updateStatus("processing");
                String source = fhirRequest.getQueryMap().get( "source" );

                IBaseResource body = fhirRequest.getBodyResource();
                if (!( body instanceof Parameters)) {
                    throw new FhirProxyException("Body does not contain resource of type Parameters");
                }
                Parameters parameters = (Parameters)body;

                //////////////////////////////////////////////////
                Optional<Resource> optContent = ((Parameters) parameters).getParameter().stream()
                        .filter( parameter -> parameter.getName().equals( "content" ) )
                        .map( parameter -> parameter.getResource() )
                        .findFirst();

                if ( !optContent.isPresent() ) {
                    throw new FHIRException( "missing content parameter" );
                }
                Resource contentResource = optContent.get();

                //////////////////////////////////////////////////
                updateStatus("retrieve structure map");

                String structureMapId = ( source!=null? source: fhirRequest.getResourceId() );
                if (structureMapId==null){
                    Optional<Type> optSource = ((Parameters) parameters).getParameter().stream()
                            .filter( parameter -> parameter.getName().equals( "source" ) )
                            .map( parameter -> parameter.getValue() )
                            .findFirst();
                    if( optSource.isPresent() ) {
                        Type type = optSource.get();
                        Reference reference = (Reference) type;
                        ReferenceUtil.ParsedReference pr = ReferenceUtil.parseReference(reference);
                        structureMapId = pr.getResourceId();
                    }
                }
                StructureMap structuredMap =
                        (StructureMap) fhirServer.doGet( "StructureMap", structureMapId, null);
//                        client.read().resource( StructureMap.class ).withId( structureMapId ).execute();
                if ( structuredMap==null ){
                    throw new FHIRException( "StructureMap "+structureMapId+" can not be found" );
                }

                updateStatus("doing transform");

                result = structureMapTransformServer.doTransform( structuredMap, contentResource, null );
                updateStatus("ready");
            }

            private void updateStatus(String str) {
                logger.info(str);
                this.statusDescription = str;
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
//        {
//            @Override
//            public IBaseResource getResult() throws FHIRException, NotImplementedException {
//                BaseFhirDataProvider baseFhirDataProvider = new FhirDataProviderStu3().setEndpoint( url );
//                IdType idType = new IdType().setValue( resourceType + "/" + resourceId );
//
//                String source = queryParams.get( "source" );
//
//                Optional<Resource> optResource = ((Parameters) parameters).getParameter().stream()
//                    .filter( parameter -> parameter.getName().equals( "content" ) )
//                    .map( parameter -> parameter.getResource() )
//                    .findFirst();
//
//                if ( !optResource.isPresent() ) {
//                    throw new FHIRException( "missing content parameter" );
//                }
//                Resource contentRsource = optResource.get();
//
//                StructureMap structuredMap =
//                       client.read().resource( StructureMap.class ).withId( resourceId ).execute();
//                if ( structuredMap==null ){
//                    throw new FHIRException( "StructureMap "+resourceId+" can not be found" );
//                }
//
//                IBaseResource result = structureMapTransformServer.doTransform( structuredMap, contentRsource, null );
//
//            }
//
//            @Override
//            public String getDescription() {
//                return null;
//            }
//
//            @Override
//            public Map<String, OperationOutcome> getErrors() {
//                return null;
//            }
//        };
    }

    @Override
    public String getResourceType() {
        return "StructureMap";
    }


    @Override
    public String getOperationName() {
        return "$transform";
    }

    @Override
    public OperationDefinition getOperation() {

        OperationDefinition operationDefinition = (OperationDefinition) new OperationDefinition()
                .setUrl("http://hl7.org/fhir/OperationDefinition/StructureMap-transform")
                .setName("Model Instance Transformation")
                .setStatus(Enumerations.PublicationStatus.DRAFT)
                .setKind(OperationDefinition.OperationKind.OPERATION)
                .setPublisher( "HL7 (FHIR Project)" )
                .setDescription( "The transform operation takes input content, applies a structure map transform, and then returns the output." )
                .setCode("transform")
                .setComment("The input and return are specified as 'Resources'. In most usage of the $transform operation, either the input or return content is not a valid FHIR resource. In these cases, the return type is actually a [Binary](binary.html) resource. For this operation, the Binary resources may be encoded directly, using a mime-type, as shown in the example. Note: this specification does not yet address the means by which the servers may know the correct mime types for the various content involved")
                .addResource("StructureMap")
                .setSystem(false)
                .setType(true)
                .setInstance(true)
                .addParameter( new OperationDefinition.OperationDefinitionParameterComponent()
                        .setName("source")
                        .setUse(OperationDefinition.OperationParameterUse.IN)
                        .setMin(0)
                        .setMax("1")
                        .setDocumentation("The structure map to apply. This is only needed if the operation is invoked at the resource level. If the $transform operation is invoked on a particular structure map, this will be ignored by the server")
                        .setType("uri")
                )
                .addParameter( new OperationDefinition.OperationDefinitionParameterComponent()
                        .setName("content")
                        .setUse(OperationDefinition.OperationParameterUse.IN)
                        .setMin(1)
                        .setMax("1")
                        .setDocumentation("The logical content to transform.")
                        .setType("Resource")
                )
                .addParameter( new OperationDefinition.OperationDefinitionParameterComponent()
                        .setName("return")
                        .setUse(OperationDefinition.OperationParameterUse.OUT)
                        .setMin(1)
                        .setMax("1")
                        .setDocumentation("The result of the transform.")
                        .setType("Resource")
                )
                .setId("structuremap-transform");


        return operationDefinition;
    }
}
