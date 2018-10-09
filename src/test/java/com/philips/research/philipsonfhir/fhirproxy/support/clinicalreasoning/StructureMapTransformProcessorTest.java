package com.philips.research.philipsonfhir.fhirproxy.support.clinicalreasoning;

import ca.uhn.fhir.context.FhirContext;
import com.philips.research.philipsonfhir.fhirproxy.support.clinicalreasoning.cql.MyWorkerContext;
import org.hl7.fhir.dstu3.hapi.validation.DefaultProfileValidationSupport;
import org.hl7.fhir.dstu3.model.StructureMap;
import org.hl7.fhir.dstu3.utils.StructureMapUtilities;
import org.hl7.fhir.exceptions.FHIRException;
import org.junit.Test;

import java.util.Map;
import java.util.TreeMap;

import static junit.framework.TestCase.assertNotNull;

public class StructureMapTransformProcessorTest {

    String obsR42R3 ="map \"http://hl7.org/fhir/StructureMap/Observation4to3\" = \"R4 to R3 Conversion for Observation\"\n" +
        "\n" +
        "\n" +
        "uses \"http://hl7.org/fhir/StructureDefinition/Observation\" alias Observation as source\n" +
        "uses \"http://hl7.org/fhir/3.0/StructureDefinition/Observation\" alias ObservationR3 as target\n" +
        "\n" +
        "imports \"http://hl7.org/fhir/StructureMap/*4to3\"\n" +
        "\n" +
        "\n" +
        "\n" +
        "group Observation extends DomainResource\n" +
        "  input src : Observation as source\n" +
        "  input tgt : ObservationR3 as target\n" +
        "\n" +
        "  \"Observation.identifier\" : for src.identifier as vs make tgt.identifier as vt\n" +
        "  \"Observation.basedOn\" : for src.basedOn as vs make tgt.basedOn as vt\n" +
        "  \"Observation.status\" : for src.status as vs make tgt.status as vt\n" +
        "  \"Observation.category\" : for src.category as vs make tgt.category as vt\n" +
        "  \"Observation.code\" : for src.code as vs make tgt.code as vt\n" +
        "  \"Observation.subject\" : for src.subject as vs make tgt.subject as vt\n" +
        "  \"Observation.context\" : for src.context as vs make tgt.context as vt\n" +
        "  \"Observation.effective-dateTime\" : for src.effective  : dateTime as vs make tgt.effective = create(\"dateTime\") as vt then dateTime(vs,vt)\n" +
        "  \"Observation.effective-Period\" : for src.effective  : Period as vs make tgt.effective = create(\"Period\") as vt then Period(vs,vt)\n" +
        "  \"Observation.issued\" : for src.issued as vs make tgt.issued as vt\n" +
        "  \"Observation.performer\" : for src.performer as vs make tgt.performer as vt\n" +
        "  \"Observation.value-Quantity\" : for src.value  : Quantity as vs make tgt.value = create(\"Quantity\") as vt then Quantity(vs,vt)\n" +
        "  \"Observation.value-CodeableConcept\" : for src.value  : CodeableConcept as vs make tgt.value = create(\"CodeableConcept\") as vt then CodeableConcept(vs,vt)\n" +
        "  \"Observation.value-string\" : for src.value  : string as vs make tgt.value = create(\"string\") as vt then string(vs,vt)\n" +
        "  \"Observation.value-boolean\" : for src.value  : boolean as vs make tgt.value = create(\"boolean\") as vt then boolean(vs,vt)\n" +
        "  \"Observation.value-Range\" : for src.value  : Range as vs make tgt.value = create(\"Range\") as vt then Range(vs,vt)\n" +
        "  \"Observation.value-Ratio\" : for src.value  : Ratio as vs make tgt.value = create(\"Ratio\") as vt then Ratio(vs,vt)\n" +
        "  \"Observation.value-SampledData\" : for src.value  : SampledData as vs make tgt.value = create(\"SampledData\") as vt then SampledData(vs,vt)\n" +
        "  \"Observation.value-time\" : for src.value  : time as vs make tgt.value = create(\"time\") as vt then time(vs,vt)\n" +
        "  \"Observation.value-dateTime\" : for src.value  : dateTime as vs make tgt.value = create(\"dateTime\") as vt then dateTime(vs,vt)\n" +
        "  \"Observation.value-Period\" : for src.value  : Period as vs make tgt.value = create(\"Period\") as vt then Period(vs,vt)\n" +
        "  \"Observation.dataAbsentReason\" : for src.dataAbsentReason as vs make tgt.dataAbsentReason as vt\n" +
        "  \"Observation.interpretation\" : for src.interpretation as vs make tgt.interpretation as vt\n" +
        "  \"Observation.comment\" : for src.comment as vs make tgt.comment as vt\n" +
        "  \"Observation.bodySite\" : for src.bodySite as vs make tgt.bodySite as vt\n" +
        "  \"Observation.method\" : for src.method as vs make tgt.method as vt\n" +
        "  \"Observation.specimen\" : for src.specimen as vs make tgt.specimen as vt\n" +
        "  \"Observation.device\" : for src.device as vs make tgt.device as vt\n" +
        "  \"Observation.referenceRange\" : for src.referenceRange as vs0 make tgt.referenceRange as vt0 then {\n" +
        "    \"Observation.referenceRange.low\" : for vs0.low as vs make vt0.low as vt\n" +
        "    \"Observation.referenceRange.high\" : for vs0.high as vs make vt0.high as vt\n" +
        "    \"Observation.referenceRange.type\" : for vs0.type as vs make vt0.type as vt\n" +
        "    \"Observation.referenceRange.appliesTo\" : for vs0.appliesTo as vs make vt0.appliesTo as vt\n" +
        "    \"Observation.referenceRange.age\" : for vs0.age as vs make vt0.age as vt\n" +
        "    \"Observation.referenceRange.text\" : for vs0.text as vs make vt0.text as vt\n" +
        "  }\n" +
        "  \"Observation.related\" : for src.hasMember as vs0 make tgt.related as vt0 then {\n" +
        "  }\n" +
        "  \"Observation.related\" : for src.derivedFrom as vs0 make tgt.related as vt0 then {\n" +
        "  }\n" +
        "  \"Observation.component\" : for src.component as vs0 make tgt.component as vt0 then {\n" +
        "    \"Observation.component.code\" : for vs0.code as vs make vt0.code as vt\n" +
        "    \"Observation.component.value-Quantity\" : for vs0.value  : Quantity as vs make vt0.value = create(\"Quantity\") as vt then Quantity(vs,vt)\n" +
        "    \"Observation.component.value-CodeableConcept\" : for vs0.value  : CodeableConcept as vs make vt0.value = create(\"CodeableConcept\") as vt then CodeableConcept(vs,vt)\n" +
        "    \"Observation.component.value-string\" : for vs0.value  : string as vs make vt0.value = create(\"string\") as vt then string(vs,vt)\n" +
        "    \"Observation.component.value-Range\" : for vs0.value  : Range as vs make vt0.value = create(\"Range\") as vt then Range(vs,vt)\n" +
        "    \"Observation.component.value-Ratio\" : for vs0.value  : Ratio as vs make vt0.value = create(\"Ratio\") as vt then Ratio(vs,vt)\n" +
        "    \"Observation.component.value-SampledData\" : for vs0.value  : SampledData as vs make vt0.value = create(\"SampledData\") as vt then SampledData(vs,vt)\n" +
        "    \"Observation.component.value-time\" : for vs0.value  : time as vs make vt0.value = create(\"time\") as vt then time(vs,vt)\n" +
        "    \"Observation.component.value-dateTime\" : for vs0.value  : dateTime as vs make vt0.value = create(\"dateTime\") as vt then dateTime(vs,vt)\n" +
        "    \"Observation.component.value-Period\" : for vs0.value  : Period as vs make vt0.value = create(\"Period\") as vt then Period(vs,vt)\n" +
        "    \"Observation.component.dataAbsentReason\" : for vs0.dataAbsentReason as vs make vt0.dataAbsentReason as vt\n" +
        "    \"Observation.component.interpretation\" : for vs0.interpretation as vs make vt0.interpretation as vt\n" +
        "  }\n" +
        "endgroup\n";

    @Test
    public void structureMapFromText() throws FHIRException {
        FhirContext ourCtx = FhirContext.forDstu3();
        MyWorkerContext hapiWorkerContext = new MyWorkerContext( ourCtx, new DefaultProfileValidationSupport() );

        // TODO Map should contain all structure maps.
        Map<String, StructureMap> mapTreeMap = new TreeMap<>();

        StructureMapUtilities structureMapUtilities = new StructureMapUtilities(hapiWorkerContext, mapTreeMap);

        StructureMap structureMap = structureMapUtilities.parse( this.obsR42R3 );
        assertNotNull( structureMap );
    }

}
