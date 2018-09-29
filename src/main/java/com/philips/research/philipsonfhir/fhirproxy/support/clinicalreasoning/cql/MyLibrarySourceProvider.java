package com.philips.research.philipsonfhir.fhirproxy.support.clinicalreasoning.cql;

import ca.uhn.fhir.rest.client.api.IGenericClient;
import org.cqframework.cql.cql2elm.LibrarySourceProvider;
import org.hl7.elm.r1.VersionedIdentifier;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.Library;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

public class MyLibrarySourceProvider implements LibrarySourceProvider {

    private final IGenericClient fhirClient;

    public MyLibrarySourceProvider(IGenericClient fhirClient) {
        this.fhirClient = fhirClient;
    }

    @Override
    public InputStream getLibrarySource(VersionedIdentifier versionedIdentifier) {
        IdType id = new IdType(versionedIdentifier.getId());
        org.hl7.fhir.dstu3.model.Library lib = fhirClient.read()
            .resource( Library.class ).withId( id ).execute();

        for (org.hl7.fhir.dstu3.model.Attachment content : lib.getContent()) {
            if ( content.getContentType().equals("text/cql" )) {
                return new ByteArrayInputStream(content.getData());
            }
        }

        throw new IllegalArgumentException(String.format("Library %s%s does not contain CQL source content.", versionedIdentifier.getId(),
            versionedIdentifier.getVersion() != null ? ("-" + versionedIdentifier.getVersion()) : ""));
    }
}