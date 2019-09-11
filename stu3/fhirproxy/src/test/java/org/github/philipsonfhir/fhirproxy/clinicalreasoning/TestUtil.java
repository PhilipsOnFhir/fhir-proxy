package org.github.philipsonfhir.fhirproxy.clinicalreasoning;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import org.hl7.fhir.dstu3.model.Resource;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class TestUtil {
    private static FhirContext ourCtx = FhirContext.forDstu3();

    public static void storeResource(Resource resource) throws IOException {
        IParser jsonParser = ourCtx.newJsonParser();
        jsonParser.setPrettyPrint(true);
        File exampleDir = new File("example");
        if ( !exampleDir.exists() ){
            exampleDir.mkdir();
        }

        File rscFile = new File("example/"+resource.getResourceType()+"-"+resource.getId()+".json");
        FileWriter writer = new FileWriter( rscFile );
        writer.write( jsonParser.encodeResourceToString(resource ));
        writer.close();
    }
}
