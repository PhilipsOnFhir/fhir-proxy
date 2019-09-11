package org.github.philipsonfhir.fhirproxy.clinicalreasoning.examples.bmi.definition;

import org.hl7.fhir.dstu3.model.*;

import java.util.ArrayList;
import java.util.List;

public class QuestionnaireRequestActivityDefinition {
    static final String ID ="enterQuestionnaireAd";
    private String source = "source";
    private String target = "target";

    private List<StructureMap.StructureMapGroupRuleSourceComponent> getUnusedSource() {
        List<StructureMap.StructureMapGroupRuleSourceComponent> result = new ArrayList();
        result.add( new StructureMap.StructureMapGroupRuleSourceComponent()
            .setContext(source)
            .setElement("id")
            .setVariable("a")
        );
        return result;
    }

    public StructureMap createHeightObsStructureMap() {
        return (StructureMap) new StructureMap()
            .setTitle( "Generate Questionnaire Extension" )
            .setExperimental( true )
            .addStructure( new StructureMap.StructureMapStructureComponent()
                .setUrl( "http://hl7.org/fhir/StructureDefinition/activitydefinition" )
                .setMode( StructureMap.StructureMapModelMode.SOURCE )
            )
            .addStructure( new StructureMap.StructureMapStructureComponent()
                .setUrl( "http://hl7.org/fhir/StructureDefinition/extension" )
                .setMode( StructureMap.StructureMapModelMode.TARGET )
            )
            .addGroup( new StructureMap.StructureMapGroupComponent()
                .setTypeMode( StructureMap.StructureMapGroupTypeMode.NONE ) // not the default mapping
                .addInput( new StructureMap.StructureMapGroupInputComponent()
                    .setName( source )
                    .setMode( StructureMap.StructureMapInputMode.SOURCE )
                )
                .addInput( new StructureMap.StructureMapGroupInputComponent()
                    .setName( target )
                    .setMode( StructureMap.StructureMapInputMode.TARGET )
                )
                .addRule( new StructureMap.StructureMapGroupRuleComponent()
                    .setName( "seturl" )
                    .setSource( getUnusedSource() )
                    .addTarget( new StructureMap.StructureMapGroupRuleTargetComponent()
                        .setContext( target )
                        .setContextType( StructureMap.StructureMapContextType.VARIABLE )
                        .setElement( "url" )
                        .setTransform( StructureMap.StructureMapTransform.COPY )
                        .addParameter( new StructureMap.StructureMapGroupRuleTargetParameterComponent()
                            .setValue( new StringType( "http://hl7.org/fhir/StructureDefinition/procedurerequest-questionnaireRequest" ) )
                        )
                    )
                )
                .addRule( new StructureMap.StructureMapGroupRuleComponent()
                    .setName( "setextension" )
                    .setSource( getUnusedSource() )
                    .addTarget( new StructureMap.StructureMapGroupRuleTargetComponent()
                        .setContext( target )
                        .setContextType( StructureMap.StructureMapContextType.VARIABLE )
                        .setElement( "extension" )
                        .setTransform( StructureMap.StructureMapTransform.COPY )
                        .addParameter( new StructureMap.StructureMapGroupRuleTargetParameterComponent()
                            .setValue( new StringType( "Questionnaire/bmiQuestionnaire" ) )
                        )
                    )
                )
            )
            .setId( "ProcReqExtensionSM" );
    }
        ActivityDefinition ad =
        (ActivityDefinition) new ActivityDefinition()
            .setName("Enter BMI values")
            .setTitle("Enter BMI values")
            .setStatus(Enumerations.PublicationStatus.DRAFT)
            .setKind( ActivityDefinition.ActivityDefinitionKind.PROCEDUREREQUEST)
            .addDynamicValue( new ActivityDefinition.ActivityDefinitionDynamicValueComponent()
                .setDescription("Set required time to now")
                .setLanguage("text/fhirpath")
                .setExpression("now()")
                .setPath("occurrence")
            )
            .setTransform( new Reference(  ).setReference( "#ProcReqExtensionSM" ) )
            .addContained( this.createHeightObsStructureMap() )
            .setId( ID );

    public ActivityDefinition build(){
        return ad;
    }

}
