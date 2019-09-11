package org.github.philipsonfhir.fhirproxy.clinicalreasoning.examples.bmi.definition;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import org.hl7.fhir.dstu3.model.*;
import org.hl7.fhir.dstu3.model.codesystems.LibraryType;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class BmiCqlLibrary {
    private final Library library;
    private static final String ID= "bmi";

    public BmiCqlLibrary() throws IOException {
        URL url = Resources.getResource("examples/bmi/bmi_library.cql");

        String cqlString = Resources.toString(url, Charsets.UTF_8);
        Library library = (Library) new Library()
                .setVersion("0.2.0")
                .setStatus(Enumerations.PublicationStatus.ACTIVE)
                .setExperimental(true)
                .setType( new CodeableConcept()
                        .addCoding( new Coding()
                                .setSystem(LibraryType.LOGICLIBRARY.getSystem())
                                .setCode(LibraryType.LOGICLIBRARY.toCode())
                                .setDisplay(LibraryType.LOGICLIBRARY.getDisplay())
                        )
                )
                .setId(ID);
        Attachment attachment = new Attachment();
        attachment.setContentType("text/cql");
        byte[] cqlData =cqlString.getBytes("utf-8");
        attachment.setData(cqlData);
        library.addContent(attachment);

        this.library = library;
    }

    public static String getId() {
        return ID;
    }

    public Library build(){ return library; }

}
