package com.philips.research.philipsonfhir.fhirproxy.dstu3.support.clinicalreasoning.cql;

import org.hl7.fhir.dstu3.model.*;

import java.util.List;

public class CqlLibrary {
    private String primaryLibrary = null;
    private String library="";
    private boolean hasCqlExpressions = false;


    public static CqlLibrary generateCqlLibrary(DomainResource instance, List<String> defineList) {
        CqlLibrary cqlLibrary;
        if       ( instance instanceof ActivityDefinition ){
            cqlLibrary = new CqlLibrary( (ActivityDefinition)instance, defineList );
        } else if( instance instanceof PlanDefinition ){
            cqlLibrary = new CqlLibrary( (PlanDefinition)instance, defineList );
        } else if( instance instanceof Measure ){
            cqlLibrary = new CqlLibrary( (Measure)instance, defineList );
        } else {
            throw new IllegalArgumentException("Class with type :"+instance.fhirType()+" cannot be concerted to a CQL library");
        }
        return cqlLibrary;
    }

    static CqlLibrary generateCqlLibrary(DomainResource instance ){
        return generateCqlLibrary( instance, null );
    }

    private CqlLibrary(PlanDefinition planDefinition, List<String> defineList ) {
        library = "library PlDf"+getValidString( planDefinition.getIdElement().getIdPart() )+"\n\n";
        library +="using FHIR version '3.0.0'\n\n";
        for ( Reference libRef : planDefinition.getLibrary() ){
            String libraryRef = libRef.getReference().replace("Library/","")+"\n\n";
            library += "include "+libraryRef;
            if ( this.primaryLibrary==null && defineList!=null ){
                this.primaryLibrary = libraryRef;
            }
        }
        int actionNo = 0;
        for ( PlanDefinition.PlanDefinitionActionComponent actionComponent: planDefinition.getAction() ){
            addDynamicValuesFromAction(actionComponent, defineList );
        }
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
            .forEach( dynamicValue -> library+= addExpression( dynamicValue.getLanguage(), dynamicValue.getExpression(), defineList ));
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
            library+= addExpression( conditionComponent.getLanguage(), conditionComponent.getExpression(), defineList );
        }
        for( PlanDefinition.PlanDefinitionActionDynamicValueComponent dynamicValueComponent:  actionComponent.getDynamicValue()){
            library+= addExpression( dynamicValueComponent.getLanguage(), dynamicValueComponent.getExpression(), defineList );
        }
        for( PlanDefinition.PlanDefinitionActionComponent planDefinitionActionComponent: actionComponent.getAction() ){
            addDynamicValuesFromAction(planDefinitionActionComponent, defineList );
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

    private String addExpression(String language, String expression, List<String> defineList) {
        String result = "// none /n";
        if ( language!=null && language.equals("text/cql")){

//            defineList.stream().forEach( define -> {
//                if ( expression.contains( define ) ){
//                    int location = expression.indexOf( define );
//                    if ( location==0 || !expression.substring( location-1 ).startsWith( "." )){
//                        // no library reference
//
//                    }
//                }
//            } );

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