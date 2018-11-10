package com.philips.research.philipsonfhir.fhirproxy.dstu3.support.clinicalreasoning.cql;

import org.hl7.fhir.dstu3.model.Extension;
import org.hl7.fhir.dstu3.model.Questionnaire;
import org.hl7.fhir.dstu3.model.Reference;
import org.hl7.fhir.dstu3.model.StringType;

import java.util.ArrayList;
import java.util.List;

public class CqifQuestionnaire extends Questionnaire {

    public static List<Reference> getLibraryReferences( Questionnaire questionnaire ) {
        ArrayList<Reference> libraries = new ArrayList<>();
        for ( Extension extension: questionnaire.getExtension()){
            if ( extension.getUrl().equals("http://hl7.org/fhir/StructureDefinition/cqif-library")){
                libraries.add( (Reference)extension.getValue());
            }
        }
        return libraries;
    }

    public static List<String> getInitialValueExpressions(QuestionnaireItemComponent questionnaireItemComponent) {
        List<String> expressions = new ArrayList<>();
        if ( questionnaireItemComponent.hasExtension() ){
            expressions = getExpressions(questionnaireItemComponent.getExtension(),"http://hl7.org/fhir/StructureDefinition/cqif-initialValue");
        }
        return expressions;
    }

    public static List<String> getConditionValueExpressions(QuestionnaireItemComponent questionnaireItemComponent) {
        List<String> expressions = new ArrayList<>();
        if ( questionnaireItemComponent.hasExtension() ){
            expressions = getExpressions(questionnaireItemComponent.getExtension(),"http://hl7.org/fhir/StructureDefinition/cqif-condition");
        }
        return expressions;
    }

    public static List<String> getCalculatedValueExpressions(QuestionnaireItemComponent questionnaireItemComponent) {
        List<String> expressions = new ArrayList<>();
        if ( questionnaireItemComponent.hasExtension() ){
            expressions = getExpressions(questionnaireItemComponent.getExtension(),"http://hl7.org/fhir/StructureDefinition/cqif-calculatedValue");
        }
        return expressions;
    }

    private static List<String> getExpressions(List<Extension> extensions, String profile ) {
        ArrayList<String> expressions = new ArrayList<>();
        for ( Extension extension: extensions ){
            if ( extension.getUrl().equals(profile)){
                expressions.add( ((StringType)extension.getValue()).getValue() );
            }
        }
        return expressions;
    }
}
