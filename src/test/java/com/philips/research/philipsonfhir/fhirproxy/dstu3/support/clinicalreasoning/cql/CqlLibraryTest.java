package com.philips.research.philipsonfhir.fhirproxy.dstu3.support.clinicalreasoning.cql;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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

        assertEquals( " prefix.Expression1", addPrefixes("prefix", defineList, "Expression1") );
        assertEquals( "prefix.Expression1", addPrefixes("prefix", defineList, "prefix.Expression1") );
        assertEquals( "prefixOther.Expression1", addPrefixes("prefix", defineList, "prefixOther.Expression1") );
        assertEquals( " prefix.Expression1 and  prefix.Expression2", addPrefixes("prefix", defineList, "Expression1 and Expression2") );
        assertEquals( " prefix.Expression1 and  prefix.Expression2 or  prefix.Expression1",
            addPrefixes("prefix", defineList, "Expression1 and Expression2 or Expression1")
        );
        assertEquals( " prefix.Expression1 and  prefix.\"Quoted Expression\" or  prefix.Expression1",
            addPrefixes("prefix", defineList, "Expression1 and \"Quoted Expression\" or Expression1")
        );
        assertEquals( "\"Quoted Expression1\"",
            addPrefixes("prefix", defineList, "\"Quoted Expression1\"")
        );
    }

    private String addPrefixes(String prefix, List<String> defineList, String expression) {
        Collections.sort( defineList, new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                return o2.length() - o1.length();
            }
        } );
//        String[] strings = Stream.of( defineList.toArray() ).sorted( Comparator.comparingInt( String::length ) ).toArray( String[]::new );
//        sortedDefineList = Stream.of( defineList.toArray() ).sorted( Comparator.comparingInt( String::length ) ).toArray();
        String newExpression = expression;
        for ( String define : defineList ) {
            int location = 0;
            while ( location < newExpression.length() && newExpression.substring( location ).contains( define ) ) {
                location = newExpression.indexOf( define, location );
                if ( location == 0 || !newExpression.substring( location - 1 ).startsWith( "." ) ) {
                    //1 newExpression = newExpression.substring( 0, location ) + " " + prefix + "." + newExpression.substring( location );
                    newExpression = "=="+newExpression.substring( location+define.length() );
                }
                //1 location += prefix.length() + define.length();
            }
        }
//        int location = 0;
//        for ( String define: defineList ){
//            while ( newExpression.contains( define ) && !newExpression.contains( "."+define )){
//                location = newExpression.indexOf( define );
//                newExpression = newExpression.substring( 0, location )+" "+prefix+"."+newExpression.substring( location );
//            }
//        }
        return newExpression;
    }

}