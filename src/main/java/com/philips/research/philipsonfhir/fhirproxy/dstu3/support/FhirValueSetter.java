package com.philips.research.philipsonfhir.fhirproxy.dstu3.support;

import org.hl7.fhir.dstu3.model.Base;
import org.hl7.fhir.dstu3.model.StringType;
import org.hl7.fhir.exceptions.FHIRException;

public class FhirValueSetter {
    public static Base setProperty(Base dest, String element, Object obj ) throws FHIRException {
        Base v = FhirValueSetter.getBaseValue(obj);
//        String item = "related";
//        String type = "type";
//        hObs.getProperty(item.hashCode(), item, false );
        // check direct or hasChildern.
        Base result = dest;
        if ( element.contains(".")){
            // has childern
            String first = element.substring(0, element.indexOf("."));
            String remaining = element.substring(element.indexOf(".")+1);
            Base[] firstElement = dest.getProperty( first.hashCode(), first, true );
            if ( firstElement.length==0){
                // create object
                dest.makeProperty( first.hashCode(), first );
                firstElement = dest.getProperty( first.hashCode(), first, true );
            }
            if ( firstElement.length==0 ){
                throw new FHIRException("Cannot create "+first);
            }
            result = setProperty( firstElement[0], remaining, v );
        } else{
            result = dest.setProperty( element.hashCode(), element, v );
        }

//// hObs.setProperty( item.hashCode(),item, hObs.makeProperty(item.hashCode(),item ))
////hObs.getProperty(item.hashCode(),item,true)[1].getProperty(type.hashCode(),type,false);
////hObs.getProperty(item.hashCode(),item,true)[1].makeProperty(type.hashCode(),type );
//        hObs.getProperty(item.hashCode(),item,true)[1].getProperty(type.hashCode(),type,false);
//        hObs.getProperty(item.hashCode(),item,true)[1].setProperty(type.hashCode(),type,new StringType("derived-from"));
//        hObs.getProperty(item.hashCode(),item,true)[3].getProperty(type.hashCode(),type,true).length
/*  Approach, split in to "." separated segments.
        if ends with ], retrieve value between [] -- index.
        retrieve <name>
        is length =0 or [i] does not exist
        makeProperty, getProperty, see before\
        Last -- use set.
*/
        return result;
    }

    private static Base getBaseValue(Object value) throws FHIRException {
        if ( value instanceof Base ) {
            return (Base) value;
        } else if ( value instanceof String ){
            return new StringType((String)value);
        }
        throw new FHIRException("Could not cast "+value.getClass()+" to Base.");
    }
}
