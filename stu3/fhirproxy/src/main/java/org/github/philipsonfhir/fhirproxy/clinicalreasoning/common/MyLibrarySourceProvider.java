package org.github.philipsonfhir.fhirproxy.clinicalreasoning.common;

import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import org.cqframework.cql.cql2elm.LibrarySourceProvider;
import org.github.philipsonfhir.fhirproxy.common.FhirProxyException;
import org.hl7.elm.r1.VersionedIdentifier;
import org.hl7.fhir.dstu3.model.Bundle;
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
        org.hl7.fhir.dstu3.model.Library lib = null;
        try {
            lib= fhirClient.read().resource(Library.class).withId(id).execute();
        } catch( ResourceNotFoundException e ){
            System.out.println("read library "+id.getIdPart()+" by identifier");
            Bundle bundle = fhirClient.search().byUrl("Library?identifier="+id.getIdPart()).returnBundle(Bundle.class).execute();
            if ( bundle!=null && bundle.getEntry().size()>0 ){
                lib = (Library) bundle.getEntryFirstRep().getResource();
            }
        }
        if ( lib==null ){
            System.out.println("Library not found");
            throw new ResourceNotFoundException("Library "+id.getIdPart()+" could not be loaded");
        }
        for (org.hl7.fhir.dstu3.model.Attachment content : lib.getContent()) {
            if ( content.getContentType().equals("text/cql" )) {
                return new ByteArrayInputStream(content.getData());
            }
        }

        throw new IllegalArgumentException(String.format("Library %s%s does not contain CQL source content.", versionedIdentifier.getId(),
            versionedIdentifier.getVersion() != null ? ("-" + versionedIdentifier.getVersion()) : ""));
    }
}
