package com.philips.research.philipsonfhir.fhirproxy.dstu3.support.clinicalreasoning.cql;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class CqlLibraryTest {

    @Test
    public void testDefinePrefixxing(){

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

//    private String addPrefixes(String prefix, List<String> defineList, String expression) {
//        Collections.sort( defineList, new Comparator<String>() {
//            @Override
//            public int compare(String o1, String o2) {
//                return o2.length() - o1.length();
//            }
//        } );
//
//        String newExpression = expression;
//        for ( int i=0; i< defineList.size(); i++ ){
//            String replacementString = String.format( "===%04d===",i );
//            String searchString      = defineList.get( i );
//            newExpression = newExpression.replace( searchString,replacementString  );
//        }
//
//        for ( int i=0; i< defineList.size(); i++ ){
//            String replacementString = "."+defineList.get( i );
//            String searchString      = String.format( "\\.\\s*===%04d===", i );
//            newExpression = newExpression.replaceAll( searchString, replacementString );
//        }
//
//        for ( int i=0; i< defineList.size(); i++ ){
//            String replacementString = prefix+"."+defineList.get( i );
//            String searchString      = String.format( "===%04d===", i );
//            newExpression = newExpression.replaceAll( searchString, replacementString  );
//        }
//
//        return newExpression;
//    }

}