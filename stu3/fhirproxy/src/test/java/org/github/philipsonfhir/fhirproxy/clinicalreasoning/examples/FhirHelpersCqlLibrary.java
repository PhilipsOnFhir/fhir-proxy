package org.github.philipsonfhir.fhirproxy.clinicalreasoning.examples;

import ca.uhn.fhir.context.FhirContext;
import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import org.hl7.fhir.dstu3.model.*;
import org.hl7.fhir.dstu3.model.codesystems.LibraryType;

import java.io.IOException;
import java.net.URL;

public class FhirHelpersCqlLibrary {
    private final Library library;
    private static final String ID= "bmi";

    public FhirHelpersCqlLibrary() throws IOException {
        URL url = Resources.getResource("examples/general-fhirhelpers-3.json");

        String cqlLibrary = Resources.toString(url, Charsets.UTF_8);

        FhirContext ctx = FhirContext.forDstu3();
        this.library = (Library) ctx.newJsonParser().parseResource(cqlLibrary);

    }

    public static String getId() {
        return ID;
    }

    public Library build(){ return library; }

}
