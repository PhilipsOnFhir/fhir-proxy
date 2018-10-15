package com.philips.research.philipsonfhir.fhirproxy.dstu3.support.clinicalreasoning.cql;

import org.hl7.fhir.dstu3.model.PlanDefinition;
import org.hl7.fhir.dstu3.model.Reference;
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
}