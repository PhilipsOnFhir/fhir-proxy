package com.philips.research.philipsonfhir.fhirproxy.dstu3.support.clinicalreasoning.cql;

import com.philips.research.philipsonfhir.fhirproxy.dstu3.support.clinicalreasoning.processor.QuestionnairePopulateProcessor;
import org.hl7.fhir.dstu3.model.*;

import java.util.*;

public class CqlLibrary {
    private String primaryLibrary = null;
    private String library="";
    private boolean hasCqlExpressions = false;
    private Map<String,String> expressionMap = new TreeMap<>();

    public static CqlLibrary generateCqlLibrary(DomainResource instance, List<String> defineList) {
        CqlLibrary cqlLibrary;
        if       ( instance instanceof ActivityDefinition ){
            cqlLibrary = new CqlLibrary( (ActivityDefinition)instance, defineList );
        } else if( instance instanceof PlanDefinition ){
            cqlLibrary = new CqlLibrary( (PlanDefinition)instance, defineList );
        } else if( instance instanceof Measure ){
            cqlLibrary = new CqlLibrary( (Measure)instance, defineList );
        } else if( instance instanceof Questionnaire ){
            cqlLibrary = new CqlLibrary( (Questionnaire)instance, defineList );
        } else {
            throw new IllegalArgumentException("Class with type :"+instance.fhirType()+" cannot be concerted to a CQL library");
        }
        return cqlLibrary;
    }

    static CqlLibrary generateCqlLibrary(DomainResource instance ){
        return generateCqlLibrary( instance, new ArrayList<>() );
    }

    private CqlLibrary(PlanDefinition planDefinition, List<String> defineList ) {
        library = "library PlDf"+getValidString( planDefinition.getIdElement().getIdPart() )+"\n\n";
        library +="using FHIR version '3.0.0'\n\n";
        for ( Reference libRef : planDefinition.getLibrary() ){
            String libraryRef = libRef.getReference().replace("Library/","");
            library += "include "+libraryRef+"\n\n";
            if ( this.primaryLibrary==null && defineList!=null ){
                this.primaryLibrary = libraryRef;
            }
        }
        library+="\ncontext Patient\n\n";
        int actionNo = 0;
        for ( PlanDefinition.PlanDefinitionActionComponent actionComponent: planDefinition.getAction() ){
            addDynamicValuesFromAction(actionComponent, defineList );
        }
        library+=getDefines();
    }

    private CqlLibrary( ActivityDefinition activityDefinition, List<String> defineList ) {
        library = "library AcDf"+getValidString( activityDefinition.getIdElement().getIdPart() )+"\n\n";
        library +="using FHIR version '3.0.0'\n\n";

        for ( Reference libRef : activityDefinition.getLibrary() ){
            String libraryRef = libRef.getReference().replace("Library/","")+"\n\n";
            library += "include "+libraryRef;
            if ( this.primaryLibrary==null && defineList!=null ){
                this.primaryLibrary = libraryRef;
            }
        }
        int actionNo = 0;
        activityDefinition.getDynamicValue()
            .forEach( dynamicValue -> addExpression( dynamicValue.getLanguage(), dynamicValue.getExpression(), defineList ));
        library+=getDefines();
    }

    public CqlLibrary(Questionnaire questionnaire, List<String> defineList) {
        library = "library Ques"+getValidString( questionnaire.getIdElement().getIdPart() )+"\n\n";
        library +="using FHIR version '3.0.0'\n\n";
        List<String> libraryNames = getLibraries(questionnaire);
        if ( libraryNames.size()==1 && defineList!=null && !defineList.isEmpty()){
            this.primaryLibrary = libraryNames.get(0);
        }
        for ( String libraryName: libraryNames ){
            library += "include "+libraryName+"\n\n";
        }

        library+="\ncontext Patient\n\n";
        int actionNo = 0;
        for ( Questionnaire.QuestionnaireItemComponent item : questionnaire.getItem() ){
            addDynamicValuesFromItem(item, defineList );
        }
        library+=getDefines();
    }

    private String getDefines() {
        String expressionDefines="";
        for ( Map.Entry<String,String> entry: this.expressionMap.entrySet() ){
            expressionDefines+= "define "+ entry.getKey() +": "+entry.getValue()+"\n\n";
        }
        return expressionDefines;
    }

    public static List<String> getLibraries(Questionnaire questionnaire) {
        List<String> libraries = new ArrayList<>();
        CqifQuestionnaire.getLibraryReferences(questionnaire).stream()
                .forEach( libraryRef -> libraries.add(libraryRef.getReference().replace("Library/","")));
        return libraries;
    }

    private CqlLibrary(Measure measure, List<String> defineList) {
        library = "library Mea"+getValidString( measure.getIdElement().getIdPart() )+"\n\n";
        for ( Reference libRef : measure.getLibrary() ){
            String libraryRef = libRef.getReference().replace("Library/","")+"\n\n";
            library += "include "+libraryRef;
            if ( this.primaryLibrary==null && defineList!=null ){
                this.primaryLibrary = libraryRef;
            }
        }
        int actionNo = 0;
        measure.getGroup().forEach( group ->
            {
                group.getPopulation().forEach( population ->
                    addExpression( "text/processors", population.getCriteria(), defineList )
                );
                group.getStratifier().forEach( stratifier ->
                    addExpression("text/processors", stratifier.getCriteria(), defineList )
                );
            }
        );
        measure.getSupplementalData().forEach( measureSupplementalData ->
            addExpression( "text/processors", measureSupplementalData.getCriteria(), defineList )
        );
    }


    private void addDynamicValuesFromAction(PlanDefinition.PlanDefinitionActionComponent actionComponent, List<String> defineList) {
        for( TriggerDefinition trigger: actionComponent.getTriggerDefinition()){
            // TODO
        }
        for( PlanDefinition.PlanDefinitionActionConditionComponent conditionComponent : actionComponent.getCondition() ){
            addExpression( conditionComponent.getLanguage(), conditionComponent.getExpression(), defineList );
        }
        for( PlanDefinition.PlanDefinitionActionDynamicValueComponent dynamicValueComponent:  actionComponent.getDynamicValue()){
            addExpression( dynamicValueComponent.getLanguage(), dynamicValueComponent.getExpression(), defineList );
        }
        for( PlanDefinition.PlanDefinitionActionComponent planDefinitionActionComponent: actionComponent.getAction() ){
            addDynamicValuesFromAction(planDefinitionActionComponent, defineList );
        }
    }

    private void addDynamicValuesFromItem(Questionnaire.QuestionnaireItemComponent item, List<String> defineList) {
        CqifQuestionnaire.getConditionValueExpressions(item).stream()
                .forEach( expression ->
                        addExpression( "text/cql", expression, defineList )
                );
        CqifQuestionnaire.getInitialValueExpressions(item).stream()
                .forEach( expression ->
                        addExpression( "text/cql", expression, defineList )
                );
        CqifQuestionnaire.getCalculatedValueExpressions(item).stream()
                .forEach( expression ->
                        addExpression( "text/cql", expression, defineList )
                );
        for ( Questionnaire.QuestionnaireItemComponent subQuestionnaireItemComponent: item.getItem()){
            addDynamicValuesFromItem( subQuestionnaireItemComponent, defineList );
        }
    }


    private static String getValidString(String id) {
        String result = "";
        result = id
            .replaceAll("-","D")
            .replaceAll("","")
            .replaceAll("/","");
        return result;
    }

    String getCqlLibaryStr(){
        return library;
    }

    private void addExpression(String language, String expression, List<String> defineList) {
        String result = "/* none  */ /n";
        result="";
        if ( language!=null && language.equals("text/cql")){
            String newExpression = addPrefixes( primaryLibrary, defineList, expression );
            result = "define "+ getCqlDefine(expression) +": "+newExpression+"\n\n";
            hasCqlExpressions = true;
            this.expressionMap.put( getCqlDefine(expression), newExpression );
        }

//        return result;
    }

    static String addPrefixes(String prefix, List<String> defineList, String expression) {
        Collections.sort( defineList, new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                return o2.length() - o1.length();
            }
        } );

        String newExpression = expression;
        for ( int i=0; i< defineList.size(); i++ ){
            String replacementString = String.format( "===%04d===",i );
            String searchString      = defineList.get( i );
            newExpression = newExpression.replace( searchString,replacementString  );
        }

        for ( int i=0; i< defineList.size(); i++ ){
            String replacementString = "."+defineList.get( i );
            String searchString      = String.format( "\\.\\s*===%04d===", i );
            newExpression = newExpression.replaceAll( searchString, replacementString );
        }

        for ( int i=0; i< defineList.size(); i++ ){
            String replacementString = prefix+"."+defineList.get( i );
            String searchString      = String.format( "===%04d===", i );
            newExpression = newExpression.replaceAll( searchString, replacementString  );
        }

        return newExpression;
    }

    static String getCqlDefine(String expression) {
        return getValidString("CQL" + expression.hashCode());
    }

    public boolean hasCqlExpressions() {
        return hasCqlExpressions;
    }


}
