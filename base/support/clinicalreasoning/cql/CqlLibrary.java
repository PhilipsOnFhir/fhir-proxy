package com.philips.research.philipsonfhir.fhirproxy.dstu3.support.clinicalreasoning.cql;

import org.hl7.fhir.dstu3.model.*;

public class CqlLibrary {
    private String library="";
    private boolean hasCqlExpressions = false;

    static CqlLibrary generateCqlLibrary(DomainResource instance ){
        CqlLibrary cqlLibrary;
        if       ( instance instanceof ActivityDefinition ){
            cqlLibrary = new CqlLibrary( (ActivityDefinition)instance );
        } else if( instance instanceof PlanDefinition ){
            cqlLibrary = new CqlLibrary( (PlanDefinition)instance );
        } else if( instance instanceof Measure ){
            cqlLibrary = new CqlLibrary( (Measure)instance );
        } else {
            throw new IllegalArgumentException("Class with type :"+instance.fhirType()+" cannot be concerted to a CQL library");
        }
        return cqlLibrary;
    }

    private CqlLibrary(PlanDefinition planDefinition) {
        library = "library PlDf"+getValidString( planDefinition.getIdElement().getIdPart() )+"\n\n";
        library +="using FHIR version '3.0.0'\n\n";
        for ( Reference libRef : planDefinition.getLibrary() ){
            library += "include "+libRef.getReference().replace("Library/","")+"\n\n";
        }
        int actionNo = 0;
        for ( PlanDefinition.PlanDefinitionActionComponent actionComponent: planDefinition.getAction() ){
            addDynamicValuesFromAction(actionComponent);
        }
    }

    private CqlLibrary( ActivityDefinition activityDefinition) {
        library = "library AcDf"+getValidString( activityDefinition.getIdElement().getIdPart() )+"\n\n";
        library +="using FHIR version '3.0.0'\n\n";
        for ( Reference libRef : activityDefinition.getLibrary() ){
            library += "include "+libRef.getReference().replace("Library/","")+"\n\n";
        }
        int actionNo = 0;
        activityDefinition.getDynamicValue()
            .forEach( dynamicValue -> library+= addExpression( dynamicValue.getLanguage(), dynamicValue.getExpression() ));
    }

    private CqlLibrary( Measure measure ) {
        library = "library Mea"+getValidString( measure.getIdElement().getIdPart() )+"\n\n";
        for ( Reference libRef : measure.getLibrary() ){
            library += "include "+libRef.getReference().replace("Library/","")+"\n\n";
        }
        int actionNo = 0;
        measure.getGroup().forEach( group ->
            {
                group.getPopulation().forEach( population ->
                    addExpression( "text/processors", population.getCriteria() )
                );
                group.getStratifier().forEach( stratifier ->
                    addExpression("text/processors", stratifier.getCriteria())
                );
            }
        );
        measure.getSupplementalData().forEach( measureSupplementalData ->
            addExpression( "text/processors", measureSupplementalData.getCriteria() )
        );
    }

    private void addDynamicValuesFromAction(PlanDefinition.PlanDefinitionActionComponent actionComponent) {
        for( TriggerDefinition trigger: actionComponent.getTriggerDefinition()){
            // TODO
        }
        for( PlanDefinition.PlanDefinitionActionConditionComponent conditionComponent : actionComponent.getCondition() ){
            library+= addExpression( conditionComponent.getLanguage(), conditionComponent.getExpression() );
        }
        for( PlanDefinition.PlanDefinitionActionDynamicValueComponent dynamicValueComponent:  actionComponent.getDynamicValue()){
            library+= addExpression( dynamicValueComponent.getLanguage(), dynamicValueComponent.getExpression() );
        }
        for( PlanDefinition.PlanDefinitionActionComponent planDefinitionActionComponent: actionComponent.getAction() ){
            addDynamicValuesFromAction(planDefinitionActionComponent);
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

    private String addExpression(String language, String expression) {
        String result = "// none /n";
        if ( language!=null && language.equals("text/cql")){
            result = "define "+ getCqlDefine(expression) +": "+expression+"\n\n";
            hasCqlExpressions = true;
        }
        return result;
    }

    static String getCqlDefine(String expression) {
        return getValidString("CQL" + expression.hashCode());
    }

    public boolean hasCqlExpressions() {
        return hasCqlExpressions;
    }
}