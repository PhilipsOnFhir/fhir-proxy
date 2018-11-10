package com.philips.research.philipsonfhir.fhirproxy.dstu3.support.clinicalreasoning.cql;

import org.hl7.fhir.dstu3.model.*;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static junit.framework.TestCase.assertNotNull;
import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class CqlLibraryTest {

    @Test
    public void testDefinePrefixing(){

        List<String> defineList = new ArrayList();
        defineList.add("Expression1");
        defineList.add("Expression2");
        defineList.add("\"Quoted Expression\"");
        defineList.add("\"Quoted Expression1\"");

        assertEquals( "prefix.Expression1", CqlLibrary.addPrefixes("prefix", defineList, "Expression1") );
        assertEquals( "prefix.Expression1", CqlLibrary.addPrefixes("prefix", defineList, "prefix.Expression1") );
        assertEquals( "prefix.Expression1", CqlLibrary.addPrefixes("prefix", defineList, "prefix.  Expression1") );
        assertEquals( "prefix .Expression1", CqlLibrary.addPrefixes("prefix", defineList, "prefix .Expression1") );
        assertEquals( "prefixOther.Expression1", CqlLibrary.addPrefixes("prefix", defineList, "prefixOther.Expression1") );
        assertEquals( "prefix.Expression1 and prefix.Expression2", CqlLibrary.addPrefixes("prefix", defineList, "Expression1 and Expression2") );
        assertEquals( "prefix.Expression1 and prefix.Expression2 or prefix.Expression1",
            CqlLibrary.addPrefixes("prefix", defineList, "Expression1 and Expression2 or Expression1")
        );
        assertEquals( "prefix.Expression1 and prefix.\"Quoted Expression\" or prefix.Expression1",
            CqlLibrary.addPrefixes("prefix", defineList, "Expression1 and \"Quoted Expression\" or Expression1")
        );
        assertEquals( "prefix.\"Quoted Expression1\"",
            CqlLibrary.addPrefixes("prefix", defineList, "\"Quoted Expression1\"")
        );
    }

    @Test
    public void testPlainPlanDefinition(){
        PlanDefinition planDefinition = (PlanDefinition) new PlanDefinition()
            .addAction( new PlanDefinition.PlanDefinitionActionComponent()
            )
            .addAction( new PlanDefinition.PlanDefinitionActionComponent()
            ).setId( "cqlTest1" );

        CqlLibrary cqlLibrary = CqlLibrary.generateCqlLibrary( planDefinition );
        assertNotNull( cqlLibrary );
        assertFalse( cqlLibrary.hasCqlExpressions() );
    }

    @Test
    public void testNoLibraryPlanDefinition(){
        PlanDefinition planDefinition = (PlanDefinition) new PlanDefinition()
            .addAction( new PlanDefinition.PlanDefinitionActionComponent()
                .addDynamicValue( new PlanDefinition.PlanDefinitionActionDynamicValueComponent()
                    .setLanguage( "text/cql" )
                    .setPath( "somePath" )
                    .setExpression( "now()" )
                )
            )
            .addAction( new PlanDefinition.PlanDefinitionActionComponent()
                .addDynamicValue( new PlanDefinition.PlanDefinitionActionDynamicValueComponent()
                    .setLanguage( "text/cql" )
                    .setPath( "somePath" )
                    .setExpression( "Patient.id.value" )
                )
            ).setId( "cqlTest2" );

        CqlLibrary cqlLibrary = CqlLibrary.generateCqlLibrary( planDefinition );
        assertNotNull( cqlLibrary );
        assertTrue( cqlLibrary.hasCqlExpressions() );
        assertNotNull( cqlLibrary.getCqlLibaryStr() );
        System.out.println(cqlLibrary.getCqlLibaryStr());
        assertTrue( cqlLibrary.getCqlLibaryStr().contains( "context Patient" ) );

        planDefinition.getAction().stream()
            .filter( action -> action.hasDynamicValue() )
            .forEach( action -> action.getDynamicValue().stream().forEach(
                dynamicValue -> assertTrue( cqlLibrary.getCqlLibaryStr().contains( dynamicValue.getExpression() )
                )
                )
            );

    }

    @Test
    public void testLibraryPlanDefinition(){
        PlanDefinition planDefinition = (PlanDefinition) new PlanDefinition()
            .addLibrary( new Reference(  ).setReference( "mylib" ) )
            .addAction( new PlanDefinition.PlanDefinitionActionComponent()
                .addDynamicValue( new PlanDefinition.PlanDefinitionActionDynamicValueComponent()
                    .setLanguage( "text/cql" )
                    .setPath( "somePath" )
                    .setExpression( "Expression1" )
                )
            )
            .addAction( new PlanDefinition.PlanDefinitionActionComponent()
                .addDynamicValue( new PlanDefinition.PlanDefinitionActionDynamicValueComponent()
                    .setLanguage( "text/cql" )
                    .setPath( "somePath" )
                    .setExpression( "Expression2" )
                )
            ).setId( "cqlTest3" );

        List<String> defineList = new ArrayList<>(  );
        defineList.add("Expression1");
        defineList.add("Expression2");
        defineList.add("Expression");

        CqlLibrary cqlLibrary = CqlLibrary.generateCqlLibrary( planDefinition, defineList );

        assertNotNull( cqlLibrary.getCqlLibaryStr(),cqlLibrary );
        assertTrue( cqlLibrary.getCqlLibaryStr(),cqlLibrary.hasCqlExpressions() );
        assertNotNull( cqlLibrary.getCqlLibaryStr(),cqlLibrary.getCqlLibaryStr() );
//        System.out.println(cqlLibrary.getCqlLibaryStr());
        assertTrue( cqlLibrary.getCqlLibaryStr().contains( "context Patient" ) );

        planDefinition.getAction().stream()
            .filter( action -> action.hasDynamicValue() )
            .forEach( action -> action.getDynamicValue().stream().forEach(
                dynamicValue -> assertTrue(  cqlLibrary.getCqlLibaryStr(), cqlLibrary.getCqlLibaryStr().contains( dynamicValue.getExpression() ))
                )
            );

        assertTrue( cqlLibrary.getCqlLibaryStr(),cqlLibrary.getCqlLibaryStr().contains( "mylib.Expression1" ) );
        assertTrue( cqlLibrary.getCqlLibaryStr(),cqlLibrary.getCqlLibaryStr().contains( "mylib.Expression2" ) );
    }

    @Test
    public void testDuplicateExpressionLibraryPlanDefinition(){
        PlanDefinition planDefinition = (PlanDefinition) new PlanDefinition()
                .addLibrary( new Reference(  ).setReference( "mylib" ) )
                .addAction( new PlanDefinition.PlanDefinitionActionComponent()
                        .addDynamicValue( new PlanDefinition.PlanDefinitionActionDynamicValueComponent()
                                .setLanguage( "text/cql" )
                                .setPath( "somePath" )
                                .setExpression( "Expression1" )
                        )
                )
                .addAction( new PlanDefinition.PlanDefinitionActionComponent()
                        .addDynamicValue( new PlanDefinition.PlanDefinitionActionDynamicValueComponent()
                                .setLanguage( "text/cql" )
                                .setPath( "somePath" )
                                .setExpression( "Expression1" )
                        )
                ).setId( "cqlTest3" );

        List<String> defineList = new ArrayList<>(  );
        defineList.add("Expression1");
        defineList.add("Expression2");
        defineList.add("Expression");

        CqlLibrary cqlLibrary = CqlLibrary.generateCqlLibrary( planDefinition, defineList );

        assertNotNull( cqlLibrary.getCqlLibaryStr(),cqlLibrary );
        assertTrue( cqlLibrary.getCqlLibaryStr(),cqlLibrary.hasCqlExpressions() );
        assertNotNull( cqlLibrary.getCqlLibaryStr(),cqlLibrary.getCqlLibaryStr() );
//        System.out.println(cqlLibrary.getCqlLibaryStr());
        assertTrue( cqlLibrary.getCqlLibaryStr().contains( "context Patient" ) );

        planDefinition.getAction().stream()
                .filter( action -> action.hasDynamicValue() )
                .forEach( action -> action.getDynamicValue().stream().forEach(
                        dynamicValue -> assertTrue(  cqlLibrary.getCqlLibaryStr(), cqlLibrary.getCqlLibaryStr().contains( dynamicValue.getExpression() ))
                        )
                );

        assertTrue( cqlLibrary.getCqlLibaryStr(),cqlLibrary.getCqlLibaryStr().contains( "mylib.Expression1" ) );
        assertEquals( cqlLibrary.getCqlLibaryStr().lastIndexOf("mylib.Expression1"), cqlLibrary.getCqlLibaryStr().indexOf("mylib.Expression1"));
    }

    @Test
    public void testQuestionnaire(){
        Questionnaire questionnaire = (Questionnaire) new Questionnaire()
                .setTitle("Title")
                .setDescription("Description")
                .addItem(new Questionnaire.QuestionnaireItemComponent()
                        .setLinkId("1")
                        .setText("text1")
                        .setInitial( new StringType("initial"))
                )
                .addItem(new Questionnaire.QuestionnaireItemComponent()
                        .setLinkId("2")
                        .setText("text1")
                        .addItem( new Questionnaire.QuestionnaireItemComponent()
                                .setLinkId("2.1")
                        )
                )
                .setId("cqifQuestionnaire");
        questionnaire
                .setMeta(
                        new Meta().addProfile("http://hl7.org/fhir/StructureDefinition/cqif-questionnaire")
                );
//        questionnaire.addExtension(
//                new Extension()
//                        .setUrl("http://hl7.org/fhir/StructureDefinition/cqif-library")
//                        .setValue(new Reference().setReference(ResourceType.Library+"/myLibrary"))
//        );
        Questionnaire.QuestionnaireItemComponent cqifItem = (Questionnaire.QuestionnaireItemComponent) new Questionnaire.QuestionnaireItemComponent()
                .setLinkId("3")
                .setText("text3")
                .addExtension(
                        new Extension()
                                .setUrl("http://hl7.org/fhir/StructureDefinition/cqif-initialValue")
                                .setValue(new StringType("( AgeInYears() >= 18 and AgeInYears() <= 65 )"))
                );
        questionnaire.addItem(cqifItem);

        CqlLibrary cqlLibrary =  CqlLibrary.generateCqlLibrary( questionnaire, new ArrayList<>() );
        assertTrue( cqlLibrary.hasCqlExpressions());
        assertTrue( cqlLibrary.getCqlLibaryStr().contains(cqifItem.getExtension().get(0).getValue().primitiveValue()));
    }

    @Test
    public void testLibaryQuestionnaire(){
        Questionnaire questionnaire = (Questionnaire) new Questionnaire()
                .setTitle("Title")
                .setDescription("Description")
                .addItem(new Questionnaire.QuestionnaireItemComponent()
                        .setLinkId("1")
                        .setText("text1")
                        .setInitial( new StringType("initial"))
                )
                .addItem(new Questionnaire.QuestionnaireItemComponent()
                        .setLinkId("2")
                        .setText("text1")
                        .addItem( new Questionnaire.QuestionnaireItemComponent()
                                .setLinkId("2.1")
                        )
                )
                .setId("cqifQuestionnaire");
        questionnaire
                .setMeta(
                        new Meta().addProfile("http://hl7.org/fhir/StructureDefinition/cqif-questionnaire")
                );
        questionnaire.addExtension(
                new Extension()
                        .setUrl("http://hl7.org/fhir/StructureDefinition/cqif-library")
                        .setValue(new Reference().setReference(ResourceType.Library+"/myLibrary"))
        );
        Questionnaire.QuestionnaireItemComponent cqifItem1 = (Questionnaire.QuestionnaireItemComponent) new Questionnaire.QuestionnaireItemComponent()
                .setLinkId("3")
                .setText("text3")
                .addExtension(
                        new Extension()
                                .setUrl("http://hl7.org/fhir/StructureDefinition/cqif-initialValue")
                                .setValue(new StringType("( AgeInYears() >= 18 and AgeInYears() <= 65 )"))
                );
        questionnaire.addItem(cqifItem1);
        Questionnaire.QuestionnaireItemComponent cqifItem2 = (Questionnaire.QuestionnaireItemComponent) new Questionnaire.QuestionnaireItemComponent()
                .setLinkId("3")
                .setText("text3")
                .addExtension(
                        new Extension()
                                .setUrl("http://hl7.org/fhir/StructureDefinition/cqif-initialValue")
                                .setValue( new StringType("initialValue1"))
                );
        questionnaire.addItem(cqifItem2);

        List<String> defineList = new ArrayList<>();
        defineList.add("initialValue1");
        CqlLibrary cqlLibrary =  CqlLibrary.generateCqlLibrary( questionnaire, defineList );
        System.out.println(cqlLibrary.getCqlLibaryStr());
        assertTrue( cqlLibrary.hasCqlExpressions());
        assertTrue( cqlLibrary.getCqlLibaryStr().contains(cqifItem2.getExtension().get(0).getValue().primitiveValue()));
    }

    @Test
    public void getPrimaryLibraryTest(){
        String libraryName = "somename";
        Questionnaire questionnaire = (Questionnaire) new Questionnaire()
                .setTitle("Title")
                .setDescription("Description")
                .addItem(new Questionnaire.QuestionnaireItemComponent()
                        .setLinkId("1")
                        .setText("text1")
                        .setInitial( new StringType("initial"))
                )
                .addItem(new Questionnaire.QuestionnaireItemComponent()
                        .setLinkId("2")
                        .setText("text1")
                        .addItem( new Questionnaire.QuestionnaireItemComponent()
                                .setLinkId("2.1")
                        )
                )
                .addExtension(
                        new Extension()
                                .setUrl("http://hl7.org/fhir/StructureDefinition/cqif-library")
                                .setValue(new Reference().setReference(ResourceType.Library+"/"+libraryName))
                )
                .setMeta(
                        new Meta().addProfile("http://hl7.org/fhir/StructureDefinition/cqif-questionnaire")
                )
                .setId("cqifQuestionnaire");

        List<String> libraries = CqlLibrary.getLibraries( questionnaire );
        assertEquals( 1, libraries.size() );
        assertEquals( libraryName, libraries.get(0));

        questionnaire.addExtension(
                new Extension()
                        .setUrl("http://hl7.org/fhir/StructureDefinition/cqif-library")
                        .setValue(new Reference().setReference(ResourceType.Library+"/other"))
        );
        libraries = CqlLibrary.getLibraries( questionnaire );
        assertEquals( 2, libraries.size() );
    }
}