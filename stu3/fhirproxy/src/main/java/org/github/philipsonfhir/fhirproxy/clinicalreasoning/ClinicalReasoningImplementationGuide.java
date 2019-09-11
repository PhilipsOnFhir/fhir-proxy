package org.github.philipsonfhir.fhirproxy.clinicalreasoning;

import org.github.philipsonfhir.fhirproxy.bulkdata.ExportFhirOperation;
import org.github.philipsonfhir.fhirproxy.bulkdata.GroupInstanceExportFhirOperation;
import org.github.philipsonfhir.fhirproxy.bulkdata.PatientExportFhirOperation;
import org.github.philipsonfhir.fhirproxy.bulkdata.PatientInstanceExportFhirOperation;
import org.github.philipsonfhir.fhirproxy.clinicalreasoning.activitydefinition.ActivityDefinitionApplyOperation;
import org.github.philipsonfhir.fhirproxy.clinicalreasoning.plandefinition.PlanDefinitionApplyOperation;
import org.github.philipsonfhir.fhirproxy.clinicalreasoning.questionnaire.QuestionnairePopulateOperation;
import org.github.philipsonfhir.fhirproxy.clinicalreasoning.structuremap.StructureMapTransformOperation;
import org.github.philipsonfhir.fhirproxy.common.ImplementationGuide;
import org.github.philipsonfhir.fhirproxy.common.operation.FhirOperation;
import org.hl7.fhir.dstu3.model.CapabilityStatement;
import org.hl7.fhir.dstu3.model.OperationDefinition;
import org.hl7.fhir.dstu3.model.Reference;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * https://build.fhir.org/ig/HL7/bulk-data/
 */
public class ClinicalReasoningImplementationGuide implements ImplementationGuide {

    private final List<FhirOperation> operationList;

    public ClinicalReasoningImplementationGuide() {
        operationList = new ArrayList<>();
        operationList.add( new StructureMapTransformOperation());
        operationList.add( new ActivityDefinitionApplyOperation());
        operationList.add( new PlanDefinitionApplyOperation());
        operationList.add( new QuestionnairePopulateOperation());
    }

    @Override
    public CapabilityStatement updateCapabilityStatement(CapabilityStatement capabilityStatement) {
        // (enough?)
        operationList.forEach( fhirOperation -> {
           OperationDefinition op = fhirOperation.getOperation();
           capabilityStatement.addContained( op );
           capabilityStatement.getRestFirstRep().addOperation( new CapabilityStatement.CapabilityStatementRestOperationComponent()
                   .setName(op.getCode())
                   .setDefinition(new Reference().setReference("#"+op.getId()))
           );
        });
        return capabilityStatement;
    }

    public List<FhirOperation> getOperations() {
        return Collections.unmodifiableList( operationList );
    }
}
