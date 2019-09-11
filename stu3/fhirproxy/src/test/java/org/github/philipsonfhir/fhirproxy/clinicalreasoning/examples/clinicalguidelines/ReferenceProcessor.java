package org.github.philipsonfhir.fhirproxy.clinicalreasoning.examples.clinicalguidelines;

import org.hl7.fhir.dstu3.model.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ReferenceProcessor {
    List<Reference> referenceList = new ArrayList<>();
    Base base;
    ReferenceProcessor( Base base ) {
        this.base = base;
        fillReferenceList(base);
    }
    private void fillReferenceList( Base base ){
        if ( base!=null){
            base.children().stream().forEach( child -> {
                List<Base> baseList = child.getValues();
                baseList.stream().forEach( value -> {
                    if ( value!=null ){
//                                System.out.println( child.getName() + " - "+ child.getTypeCode());
                        if ( child.getTypeCode().startsWith("Reference")){
//                                    System.out.println("     Reference found: ");
                            child.getValues().stream().forEach( refValue -> {
                                Reference reference = (Reference)refValue;
                                referenceList.add(reference);
                            });
                        } else if( child.getTypeCode().equals("*")){
                            child.getValues().stream().
                                    filter( refValue -> refValue instanceof Reference).
                                    forEach( refValue -> {
                                        Reference reference = (Reference)refValue;
                                        referenceList.add(reference);
                                    });
                        } else {
                            fillReferenceList( value );
                        }
                    }
                });
            });
            if ( base instanceof DomainResource){
                ((DomainResource) base).getExtension().stream().forEach( extension ->
                        fillReferenceList( extension )
                );
            }
        }
    }

    static void ensureId(DomainResource result ) {
        if( !result.hasId() ){
            result.children().stream().forEach( child -> {
                List<Base> baseList = child.getValues();
                baseList.stream().forEach( value -> {
                    if ( value!=null ){
//                        System.out.println( child.getName() + " - "+ child.getTypeCode());
                        if ( child.getTypeCode().startsWith("Identifier")){
                            child.getValues().stream().forEach( refValue -> {
                                Identifier identifier = (Identifier) refValue;
                                System.out.println("     Identifier found: "+ identifier.getValue());
                                result.setId( identifier.getValue().replace("_","-").replace("/","-") );
                            });
                        }
                    }
                });
            });
        }
    }

    static void replaceReferencesInResource( Base base ) {
        if ( base!=null){
            base.children().stream().forEach( child -> {
                List<Base> baseList = child.getValues();
                baseList.stream().forEach( value -> {
                    if ( value!=null ){
//                                System.out.println( child.getName() + " - "+ child.getTypeCode());
                        if ( child.getTypeCode().startsWith("Reference")){
//                                    System.out.println("     Reference found: ");
                            child.getValues().stream().forEach( refValue -> {
                                Reference reference = (Reference)refValue;
                                reference.setReference( reference.getReference().replace("_","-"));
                            });
                        } else if( child.getTypeCode().equals("*")){
                            child.getValues().stream().
                                filter( refValue -> refValue instanceof Reference).
                                forEach( refValue -> {
                                    Reference reference = (Reference)refValue;
                                    reference.setReference( reference.getReference().replace("_","-"));
                                });
                        } else {
                            replaceReferencesInResource( value );
                        }
                    }
                });
            });
            if ( base instanceof DomainResource){
                ((DomainResource) base).getExtension().stream().forEach( extension ->
                        replaceReferencesInResource( extension )
                );
            }
        }
    }

    public List<Reference> getReferences() {
        return Collections.unmodifiableList(this.referenceList);
    }
}
