package org.github.philipsonfhir.fhirproxy.clinicalreasoning.examples.bmi.definition;

import org.hl7.fhir.dstu3.model.CodeSystem;
import org.hl7.fhir.dstu3.model.Coding;

public class OurCodeSystem {
    private CodeSystem result;
    static final String id = "cdsCodeSystem";
    static final String codeSystemUrl = "http://research.philips.com/codesystem/"+id;

    public OurCodeSystem(){
        this.result = (CodeSystem) new CodeSystem()
                .setName("PhilipsClinicalReasoning")
                .setUrl( codeSystemUrl )
                .addConcept( getConceptDefinitionComponent( weigth ) )
                .addConcept( getConceptDefinitionComponent( heigth ) )
                .addConcept( getConceptDefinitionComponent( bmi) )
                .setId(id);
    }

    private CodeSystem.ConceptDefinitionComponent getConceptDefinitionComponent(String[] data) {
        return new CodeSystem.ConceptDefinitionComponent().setCode(data[0]).setDisplay(data[1]);
    }

    static String[] heigth = { "8302-2", "Heigth observation" };
    static String[] weigth = { "27113001", "Weigth observation" };
    static String[] bmi = { "39156-5", "BMI observation" };
    static String[] hasbled = { "704180000",  "HAS-BLED bleeding risk score" };

    public static Coding getHasbledRiskCoding() {
        return getCoding( hasbled );
    }

    public static Coding getBmiCoding() {
        return getCoding( bmi );
    }

    public static Coding getHeigthCoding() {
        return getCoding( heigth );
    }

    public static Coding getWeigthCoding() {
        return getCoding( weigth );
    }

    private static Coding getCoding(String[] heigth) {
        return new Coding().setCode( heigth[0] ).setDisplay( heigth[1]).setSystem(codeSystemUrl);
    }

    public CodeSystem build(){
        return result;
    }
}
