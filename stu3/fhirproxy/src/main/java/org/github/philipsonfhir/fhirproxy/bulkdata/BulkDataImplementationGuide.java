package org.github.philipsonfhir.fhirproxy.bulkdata;

import org.github.philipsonfhir.fhirproxy.common.ImplementationGuide;
import org.github.philipsonfhir.fhirproxy.common.operation.FhirOperation;
import org.hl7.fhir.dstu3.model.*;

import java.util.ArrayList;
import java.util.List;

/**
 * https://build.fhir.org/ig/HL7/bulk-data/
 */
public class BulkDataImplementationGuide implements ImplementationGuide {

    public BulkDataImplementationGuide() {

    }

    @Override
    public CapabilityStatement updateCapabilityStatement(CapabilityStatement capabilityStatement) {
        // (enough?)
        capabilityStatement.addInstantiates("http://www.hl7.org/fhir/bulk-data/CapabilityStatement-bulk-data.html");

        OperationDefinition sysExport = createOperationDefinition( true, false, null, "export", "sysExport");
        OperationDefinition groupExport = createOperationDefinition( false, true, "Group", "export", "groupExport");
        OperationDefinition patientExport = createOperationDefinition( false, true, "Patient", "export", "patientExport");

        capabilityStatement.addContained(sysExport);
        capabilityStatement.addContained(groupExport);
        capabilityStatement.addContained(patientExport);
        capabilityStatement.getRestFirstRep()
                .addOperation( new CapabilityStatement.CapabilityStatementRestOperationComponent()
                    .setName("export")
                    .setDefinition( new Reference().setReference("#"+sysExport.getId()))
                )
                .addOperation( new CapabilityStatement.CapabilityStatementRestOperationComponent()
                    .setName("export")
                    .setDefinition( new Reference().setReference("#"+groupExport.getId()))
                )
                .addOperation( new CapabilityStatement.CapabilityStatementRestOperationComponent()
                    .setName("export")
                    .setDefinition( new Reference().setReference("#"+patientExport.getId()))
                );

        return capabilityStatement;
    }

    static OperationDefinition createOperationDefinition(boolean system, boolean hasResourceType, String resourceType, String operationName, String id ) {
        OperationDefinition opdef = (OperationDefinition) new OperationDefinition()
                .setUrl("http://hl7.org/fhir/us/bulkdata/OperationDefinition/export")
                .setVersion("1.0.0")
                .setKind(OperationDefinition.OperationKind.OPERATION)
                .setCode("$export")
                .setName("BulkDataExport")
                .setSystem(system)
                .setType(hasResourceType)
                .setInstance(false)
                .addParameter( new OperationDefinition.OperationDefinitionParameterComponent()
                        .setName("_outputFormat")
                        .setUse(OperationDefinition.OperationParameterUse.OUT)
                        .setMin(0)
                        .setMax("1")
                        .setDocumentation("The format for the requested bulk data files to be generated. Servers MUST support Newline Delimited JSON, but MAY choose to support additional output formats. Servers MUST accept the full content type of application/fhir+ndjson as well as the abbreviated representations application/ndjson and ndjson. Defaults to application/fhir+ndjson")
                        .setType( "string" )
                )
                .addParameter( new OperationDefinition.OperationDefinitionParameterComponent()
                        .setName("_since")
                        .setUse(OperationDefinition.OperationParameterUse.IN)
                        .setMin(0)
                        .setMax("1")
                        .setDocumentation("Resources updated after this period will be included in the response")
                        .setType( "instant" )
                )
                .addParameter( new OperationDefinition.OperationDefinitionParameterComponent()
                        .setName("_type")
                        .setUse(OperationDefinition.OperationParameterUse.OUT)
                        .setMin(0)
                        .setMax("1")
                        .setDocumentation("A string of comma-delimited FHIR resource types. Only resources of the specified resource types(s) SHOULD be included in the response. If this parameter is omitted, the server SHOULD return all supported resources within the scope of the client authorization. For non-system-level requests, the Patient Compartment SHOULD be used as a point of reference for recommended resources to be returned as well as other resources outside of the patient compartment that are helpful in interpreting the patient data such as Organization and Practitioner. Resource references MAY be relative URIs with the format <resource type>/<id>, or absolute URIs with the same structure rooted in the base URI for the server from which the export was performed. References will be resolved looking for a resource with the specified type and id within the file set. Note: Implementations MAY limit the resources returned to specific subsets of FHIR, such as those defined in the Argonaut Implementation Guide")
                        .setType( "string")
                )
                .setId(id);
        if ( hasResourceType ){
            opdef.addResource( resourceType );
        }
        return  opdef;
    }


    public List<FhirOperation> getOperations() {
        List<FhirOperation> operationList = new ArrayList<>();
        operationList.add( new ExportFhirOperation());
        operationList.add( new PatientExportFhirOperation() );
        operationList.add( new PatientInstanceExportFhirOperation() );
        operationList.add( new GroupInstanceExportFhirOperation() );
        return operationList;
    }
}
