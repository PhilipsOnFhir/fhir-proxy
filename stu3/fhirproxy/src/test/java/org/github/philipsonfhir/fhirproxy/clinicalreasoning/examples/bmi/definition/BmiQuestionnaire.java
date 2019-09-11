package org.github.philipsonfhir.fhirproxy.clinicalreasoning.examples.bmi.definition;

import ca.uhn.fhir.context.FhirContext;
import org.github.philipsonfhir.fhirproxy.clinicalreasoning.common.MyWorkerContext;
import org.hl7.fhir.dstu3.hapi.validation.DefaultProfileValidationSupport;
import org.hl7.fhir.dstu3.model.*;
import org.hl7.fhir.dstu3.utils.StructureMapUtilities;
import org.hl7.fhir.exceptions.FHIRException;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class BmiQuestionnaire {
    private String linkIdHeigth = "height";
    private String linkIdWeigth = "weight";
    private String source = "source";
    private String target = "target";

    public static String getId() {
        return "bmiQuestionnaire";
    }

    public Questionnaire buildQuestionnaire() throws FHIRException {
        return (Questionnaire) new Questionnaire()
                .setTitle("Height and Weight for BMI")
                .setDescription("Measure Weight in Height in order to determine BMI")
                .addItem( new Questionnaire.QuestionnaireItemComponent()
                        .setLinkId(linkIdHeigth)
                        .setRequired(true)
                        .setPrefix("1")
                        .setText("Measure the patients height in m.")
                        //                        .setType(Questionnaire.QuestionnaireItemType.DECIMAL)
                        .setType(Questionnaire.QuestionnaireItemType.QUANTITY)
                )
                .addItem( new Questionnaire.QuestionnaireItemComponent()
                        .setLinkId(linkIdWeigth)
                        .setRequired(true)
                        .setPrefix("2")
                        //                        .setType(Questionnaire.QuestionnaireItemType.DECIMAL)
                        .setText("Measure the patients height in kg.")
                        .setType(Questionnaire.QuestionnaireItemType.QUANTITY)
                )
//                .addItem((Questionnaire.QuestionnaireItemComponent) new Questionnaire.QuestionnaireItemComponent()
//                        .setLinkId("bmi")
//                        .setRequired(true)
//                        .setPrefix("3")
//                        //                        .setType(Questionnaire.QuestionnaireItemType.DECIMAL)
//                        .setText("Calculated BMI.")
//                        .setType(Questionnaire.QuestionnaireItemType.DECIMAL)
//                        .addExtension( new Extension()
//                                .setUrl("http://hl7.org/fhir/StructureDefinition/cqif-condition")
//                                .setValue( new StringType("$this.item.where(linkId='height').answer.value.value.exists() * $this.item.where(linkId='height').answer.value.value.exists() )" )
//                                )
//                        )
//                        .addExtension( new Extension()
//                                .setUrl("http://hl7.org/fhir/StructureDefinition/cqif-calculatedValue")
//                                .setValue( new StringType("$this.item.where(linkId='height').answer.value.value * $this.item.where(linkId='height').answer.value.value )" ))
//                        )
//                )
                .addExtension( new Extension()
                        .setUrl("http://hl7.org/fhir/StructureDefinition/questionnaire-targetStructureMap")
                        .setValue( new Reference().setReference("StructureMap/"+createBmiObsStructureMap().getId()))
                )
                .addExtension( new Extension()
                        .setUrl("http://hl7.org/fhir/StructureDefinition/questionnaire-targetStructureMap")
                        .setValue( new Reference().setReference("StructureMap/"+createHeightObsStructureMap().getId()))
                )
                .addExtension( new Extension()
                        .setUrl("http://hl7.org/fhir/StructureDefinition/questionnaire-targetStructureMap")
                        .setValue( new Reference().setReference("StructureMap/"+createWeightObsStructureMap().getId()))
                )
//                .addContained( this.createBmiObsStructureMap())
//                .addContained( this.createHeightObsStructureMap())
//                .addContained( this.createWeightObsStructureMap())
                .setId(getId())
                ;

    }

    public StructureMap createBmiObsStructureMap() throws FHIRException {
        InputStream is = this.getClass().getResourceAsStream( "/examples/bmi/BmiQrToBmiObs.fhirmap" );
        BufferedReader reader = new BufferedReader( new InputStreamReader( is ) );
        String mapStr = reader.lines().collect( Collectors.joining( System.lineSeparator() ) );
        StructureMapUtilities structureMapUtilities = new StructureMapUtilities( new MyWorkerContext( FhirContext.forDstu3(), new DefaultProfileValidationSupport() ) );
        StructureMap structureMap = (StructureMap) structureMapUtilities.parse( mapStr ).setId("BmiQrToBmiObs");
        return  structureMap;
    }

    public StructureMap createWeightObsStructureMap() throws FHIRException {
        InputStream is = this.getClass().getResourceAsStream( "/examples/bmi/BmiQrToWeightObs.fhirmap" );
        BufferedReader reader = new BufferedReader( new InputStreamReader( is ) );
        String mapStr = reader.lines().collect( Collectors.joining( System.lineSeparator() ) );
        StructureMapUtilities structureMapUtilities = new StructureMapUtilities( new MyWorkerContext( FhirContext.forDstu3(), new DefaultProfileValidationSupport() ) );
        StructureMap structureMap = (StructureMap) structureMapUtilities.parse( mapStr ).setId("BmiQrToWeightObs");
        return  structureMap;
    }

    public StructureMap createHeightObsStructureMap() throws FHIRException {
        InputStream is = this.getClass().getResourceAsStream( "/examples/bmi/BmiQrToHeightObs.fhirmap" );
        BufferedReader reader = new BufferedReader( new InputStreamReader( is ) );
        String mapStr = reader.lines().collect( Collectors.joining( System.lineSeparator() ) );
        StructureMapUtilities structureMapUtilities = new StructureMapUtilities( new MyWorkerContext( FhirContext.forDstu3(), new DefaultProfileValidationSupport() ) );
        StructureMap structureMap = (StructureMap) structureMapUtilities.parse( mapStr ).setId("BmiQrToHeightObs");
        return  structureMap;

//
//        return (StructureMap) new StructureMap()
//                .setTitle("Generate Heigth Observation based on BMI QuestionnaireResponse")
//                .setExperimental(true)
//                .addStructure( new StructureMap.StructureMapStructureComponent()
//                        .setUrl("http://hl7.org/fhir/StructureDefinition/questionnaireresponse")
//                        .setMode( StructureMap.StructureMapModelMode.SOURCE )
//                )
//                .addStructure( new StructureMap.StructureMapStructureComponent()
//                        .setUrl("http://hl7.org/fhir/StructureDefinition/observation")
//                        .setMode( StructureMap.StructureMapModelMode.TARGET )
//                )
//                .addGroup( new StructureMap.StructureMapGroupComponent()
//                        .setTypeMode(StructureMap.StructureMapGroupTypeMode.NONE) // not the default mapping
//                        .addInput( new StructureMap.StructureMapGroupInputComponent()
//                                .setName(source)
//                                .setMode(StructureMap.StructureMapInputMode.SOURCE)
//                        )
//                        .addInput( new StructureMap.StructureMapGroupInputComponent()
//                                .setName(target)
//                                .setMode(StructureMap.StructureMapInputMode.TARGET)
//                        )
//                        .addRule( new StructureMap.StructureMapGroupRuleComponent()
//                                .setName("fillqty")
//                                .setSource( getUnusedSource() )
//                                .addTarget( new StructureMap.StructureMapGroupRuleTargetComponent()
//                                        .setContext( target )
//                                        .setContextType( StructureMap.StructureMapContextType.VARIABLE )
//                                        .setElement("value")
//                                        .setTransform(StructureMap.StructureMapTransform.EVALUATE)
//                                        .addParameter(new StructureMap.StructureMapGroupRuleTargetParameterComponent()
//                                                .setValue( new IdType("source"))
//                                        )
//                                        .addParameter(new StructureMap.StructureMapGroupRuleTargetParameterComponent()
//                                                .setValue( new StringType("item.where(linkId!='height').answer.value"))
//                                        )
//                                )
//                        )
//                        .addRule(new StructureMap.StructureMapGroupRuleComponent()
//                                .setName("Subject")
//                                .addSource( new StructureMap.StructureMapGroupRuleSourceComponent()
//                                        .setContext(source)
//                                        .setElement("subject")
//                                        .setVariable("a")
//                                )
//                                .addTarget( new StructureMap.StructureMapGroupRuleTargetComponent()
//                                        .setContext(target)
//                                        .setContextType(StructureMap.StructureMapContextType.VARIABLE)
//                                        .setElement("subject")
//                                        .setTransform(StructureMap.StructureMapTransform.COPY)
//                                        .addParameter(new StructureMap.StructureMapGroupRuleTargetParameterComponent()
//                                                .setValue( new IdType("a"))
//                                        )
//                                )
//
//                        )
//                        .addRule( new StructureMap.StructureMapGroupRuleComponent()
//                                .setName("R1")
//                                .setSource( getUnusedSource() )
//                                .addTarget( new StructureMap.StructureMapGroupRuleTargetComponent()
//                                        .setContext( target )
//                                        .setContextType( StructureMap.StructureMapContextType.VARIABLE )
//                                        .setElement("id")
//                                        .setTransform(StructureMap.StructureMapTransform.UUID)
//                                )
//                        )
//
//                        .addRule(new StructureMap.StructureMapGroupRuleComponent()
//                                .setName("coding")
//                                .setSource( getUnusedSource() )
//                                .addTarget( new StructureMap.StructureMapGroupRuleTargetComponent()
//                                        .setContext( target )
//                                        .setContextType( StructureMap.StructureMapContextType.VARIABLE )
//                                        .setElement("code")
//                                        .setTransform(StructureMap.StructureMapTransform.CC)
//                                        .addParameter( new StructureMap.StructureMapGroupRuleTargetParameterComponent()
//                                                .setValue( new StringType("http://loinc.org"))
//                                        )
//                                        .addParameter( new StructureMap.StructureMapGroupRuleTargetParameterComponent()
//                                                .setValue( new StringType("8302-2"))
//                                        )
//                                        .addParameter( new StructureMap.StructureMapGroupRuleTargetParameterComponent()
//                                                .setValue( new StringType("Body height"))
//                                        )
//                                )
//                        )
//                        .addRule(new StructureMap.StructureMapGroupRuleComponent()
//                                .setName("RelatedType")
//                                .setSource( getUnusedSource() )
//                                .addTarget( new StructureMap.StructureMapGroupRuleTargetComponent()
//                                        .setContext( target )
//                                        .setContextType( StructureMap.StructureMapContextType.VARIABLE )
//                                        .setElement("related.type")
//                                        .setTransform(StructureMap.StructureMapTransform.COPY)
//
//                                        .addParameter( new StructureMap.StructureMapGroupRuleTargetParameterComponent()
//                                                .setValue( new StringType("derived-from"))
//                                        )
//                                )
//                        )
//                        .addRule(new StructureMap.StructureMapGroupRuleComponent()
//                                .setName("RelatedTarget")
//                                .setSource( getUnusedSource() )
//                                .addTarget( new StructureMap.StructureMapGroupRuleTargetComponent()
//                                        .setContext( target )
//                                        .setContextType( StructureMap.StructureMapContextType.VARIABLE )
//                                        .setElement("related.target")
//                                        .setTransform(StructureMap.StructureMapTransform.REFERENCE)
//                                        .addParameter( new StructureMap.StructureMapGroupRuleTargetParameterComponent()
//                                                .setValue( new IdType("source"))
//                                        )
//                                )
//                        )
//
//                )
//                .setId("BmiQrToHeightObs");
    }


//    private List<StructureMap.StructureMapGroupRuleSourceComponent> getUnusedSource() {
//        List<StructureMap.StructureMapGroupRuleSourceComponent> result = new ArrayList();
//        result.add( new StructureMap.StructureMapGroupRuleSourceComponent()
//                .setContext(source)
//                .setElement("id")
//                .setVariable("a")
//        );
//        return result;
//    }
}
