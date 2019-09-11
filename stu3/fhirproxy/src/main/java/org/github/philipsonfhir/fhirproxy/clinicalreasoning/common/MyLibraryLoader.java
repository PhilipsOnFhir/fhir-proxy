package org.github.philipsonfhir.fhirproxy.clinicalreasoning.common;

import ca.uhn.fhir.rest.client.api.IGenericClient;
import org.cqframework.cql.cql2elm.CqlTranslator;
import org.cqframework.cql.cql2elm.CqlTranslatorException;
import org.cqframework.cql.cql2elm.LibraryManager;
import org.cqframework.cql.cql2elm.ModelManager;
import org.cqframework.cql.elm.execution.Library;
import org.cqframework.cql.elm.execution.VersionedIdentifier;
import org.cqframework.cql.elm.tracking.TrackBack;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.exceptions.FHIRException;
import org.opencds.cqf.cql.execution.CqlLibraryReader;
import org.opencds.cqf.cql.execution.LibraryLoader;

import javax.xml.bind.JAXBException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class MyLibraryLoader implements LibraryLoader {
    private final LibraryManager libraryManager;
    private final ModelManager modelManager;
    IGenericClient fhirClient = null;
    private Map<String, Library> libraries = new HashMap<>();

    public MyLibraryLoader( IGenericClient fhirClient, LibraryManager libraryManager, ModelManager modelManager) {
        this.libraryManager = libraryManager;
        this.modelManager   = modelManager;
        this.fhirClient = fhirClient;
    }

    private Library resolveLibrary(VersionedIdentifier libraryIdentifier) {
        if (libraryIdentifier == null) {
            throw new IllegalArgumentException("Library identifier is null.");
        }

        if (libraryIdentifier.getId() == null) {
            throw new IllegalArgumentException("Library identifier id is null.");
        }

        String mangledId = this.mangleIdentifer(libraryIdentifier);

        Library library = libraries.get(mangledId);
        if (library == null) {
            library = loadLibrary(libraryIdentifier);
            libraries.put(mangledId, library);
        }

        return library;
    }

    private String mangleIdentifer(VersionedIdentifier libraryIdentifier) {
        String id = libraryIdentifier.getId();
        String version = libraryIdentifier.getVersion();

        return version == null ? id : id + "-" + version;
    }

    private String errorsToString(Iterable<CqlTranslatorException> exceptions) {
        ArrayList<String> errors = new ArrayList<>();
        for (CqlTranslatorException error : exceptions) {
            TrackBack tb = error.getLocator();
            String lines = tb == null ? "[n/a]" : String.format("%s[%d:%d, %d:%d]",
                    (tb.getLibrary() != null ? tb.getLibrary().getId() + (tb.getLibrary().getVersion() != null
                            ? ("-" + tb.getLibrary().getVersion()) : "") : ""),
                    tb.getStartLine(), tb.getStartChar(), tb.getEndLine(), tb.getEndChar());
            errors.add(lines + error.getMessage());
        }

        return errors.toString();
    }

    private Library loadLibrary(VersionedIdentifier libraryIdentifier) {
        org.hl7.elm.r1.VersionedIdentifier identifier = new org.hl7.elm.r1.VersionedIdentifier()
                .withId(libraryIdentifier.getId())
                .withSystem(libraryIdentifier.getSystem())
                .withVersion(libraryIdentifier.getVersion());

        ArrayList<CqlTranslatorException> errors = new ArrayList<>();
        org.hl7.elm.r1.Library translatedLibrary = libraryManager.resolveLibrary(identifier, errors).getLibrary();

        if (CqlTranslatorException.HasErrors(errors)) {
            throw new IllegalArgumentException(errorsToString(errors));
        }
        try {
            CqlTranslator translator = getTranslator("", libraryManager, modelManager);

            if (translator.getErrors().size() > 0) {
                throw new IllegalArgumentException(errorsToString(translator.getErrors()));
            }

            return readLibrary(
                    new ByteArrayInputStream(
                            translator.convertToXml(translatedLibrary).getBytes(StandardCharsets.UTF_8)
                    )
            );
        } catch (JAXBException e) {
            throw new IllegalArgumentException(String.format("Errors occurred translating library %s%s.",
                    identifier.getId(), identifier.getVersion() != null ? ("-" + identifier.getVersion()) : ""));
        }
    }

    @Override
    public Library load(VersionedIdentifier versionedIdentifier) {
        return resolveLibrary(versionedIdentifier);
    }

//    @Override
//    public Library load(VersionedIdentifier libraryIdentifier) {
//        IdType idType = new IdType( libraryIdentifier.getId() );
//        org.hl7.fhir.dstu3.model.Library library = fhirClient.read()
//            .resource( org.hl7.fhir.dstu3.model.Library.class )
//            .withId( idType )
//            .execute();
//
//
//        Library elmLibrary = null;
//        try {
//            elmLibrary = toElmLibrary(library);
//        } catch ( FHIRException e ) { }
//        if (elmLibrary != null) {
//            return elmLibrary;
//        }
//
//        org.hl7.elm.r1.VersionedIdentifier identifier = new org.hl7.elm.r1.VersionedIdentifier()
//            .withId(libraryIdentifier.getId())
//            .withSystem(libraryIdentifier.getSystem())
//            .withVersion(libraryIdentifier.getVersion());
//
//        ArrayList<CqlTranslatorException> errors = new ArrayList<>();
//        org.hl7.elm.r1.Library translatedLibrary = libraryManager.resolveLibrary(identifier, errors).getLibrary();
//
//        if (errors.size() > 0) {
//            throw new IllegalArgumentException(errorsToString(errors));
//        }
//        try {
//            return readLibrary(
//                new ByteArrayInputStream(
//                    getTranslator("", libraryManager, modelManager)
//                        .convertToXml(translatedLibrary).getBytes(StandardCharsets.UTF_8)
//                )
//            );
//        } catch ( JAXBException | FHIRException e) {
//            throw new IllegalArgumentException(String.format("Errors occurred translating library %s%s.",
//                identifier.getId(), identifier.getVersion() != null ? ("-" + identifier.getVersion()) : ""));
//        }
//    }
//
//    public Library toElmLibrary(org.hl7.fhir.dstu3.model.Library library) throws FHIRException {
//        InputStream is = null;
//        for (org.hl7.fhir.dstu3.model.Attachment content : library.getContent()) {
//            if (content.hasData()) {
//                is = new ByteArrayInputStream(content.getData());
//                if (content.getContentType().equals("application/elm+xml")) {
//                    return readLibrary(is);
//                } else if (content.getContentType().equals("text/processors") || content.getContentType().equals("text/cql" )) {
//                    //TODO why text/processors
//                    return translateLibrary(is, libraryManager, modelManager);
//                }
//            }
//        }
//        return null;
//    }

    Library readLibrary(InputStream xmlStream) {
        try {
            return CqlLibraryReader.read(xmlStream);
        } catch (IOException | JAXBException e) {
            throw new IllegalArgumentException("Error encountered while reading ELM xml: " + e.getMessage());
        }
    }

//    private Library readLibrary(InputStream xmlStream) {
//        try {
//            return CqlLibraryReader.read(xmlStream);
//        } catch ( IOException | JAXBException e) {
//            throw new IllegalArgumentException("Error encountered while reading ELM xml: " + e.getMessage());
//        }
//    }
//
    Library translateLibrary(InputStream cqlStream, LibraryManager libraryManager, ModelManager modelManager) throws FHIRException {
        CqlTranslator translator = getTranslator(cqlStream, libraryManager, modelManager);
        return readLibrary(new ByteArrayInputStream(translator.toXml().getBytes( StandardCharsets.UTF_8)));
    }

    CqlTranslator getTranslator(String cql, LibraryManager libraryManager, ModelManager modelManager) {
            return getTranslator(new ByteArrayInputStream(cql.getBytes(StandardCharsets.UTF_8)), libraryManager, modelManager);
    }

    CqlTranslator getTranslator(InputStream cqlStream, LibraryManager libraryManager, ModelManager modelManager) {
        ArrayList<CqlTranslator.Options> options = new ArrayList<>();
        options.add(CqlTranslator.Options.EnableAnnotations);
        options.add(CqlTranslator.Options.EnableLocators);
        options.add(CqlTranslator.Options.DisableListDemotion);
        options.add(CqlTranslator.Options.DisableListPromotion);
        options.add(CqlTranslator.Options.DisableMethodInvocation);
        CqlTranslator translator;
        try {
            translator = CqlTranslator.fromStream(cqlStream, modelManager, libraryManager,
                    options.toArray(new CqlTranslator.Options[options.size()]));
        } catch (IOException e) {
            throw new IllegalArgumentException(String.format("Errors occurred translating library: %s", e.getMessage()));
        }

        return translator;
    }
//    private CqlTranslator getTranslator(InputStream cqlStream, LibraryManager libraryManager, ModelManager modelManager) throws FHIRException {
//        ArrayList<CqlTranslator.Options> options = new ArrayList<>();
////        options.add(CqlTranslator.Options.EnableDateRangeOptimization);
//        options.add(CqlTranslator.Options.EnableAnnotations);
//        options.add(CqlTranslator.Options.EnableDetailedErrors);
//        CqlTranslator translator;
//        try {
//            translator = CqlTranslator.fromStream(cqlStream, modelManager, libraryManager,
//                options.toArray(new CqlTranslator.Options[options.size()]));
//        } catch (IOException e) {
//            throw new FHIRException(String.format("Errors occurred translating library: %s", e.getMessage()));
//        }
//
//        if (translator.getErrors().size() > 0) {
//            throw new FHIRException(errorsToString(translator.getErrors()));
//        }
//
//        return translator;
//    }
//
//    private String errorsToString(Iterable<CqlTranslatorException> exceptions) {
//        ArrayList<String> errors = new ArrayList<>();
//        for (CqlTranslatorException error : exceptions) {
//            TrackBack tb = error.getLocator();
//            String lines = tb == null ? "[n/a]" : String.format("%s[%d:%d, %d:%d]",
//                (tb.getLibrary() != null ? tb.getLibrary().getId() + (tb.getLibrary().getVersion() != null
//                    ? ("-" + tb.getLibrary().getVersion()) : "") : ""),
//                tb.getStartLine(), tb.getStartChar(), tb.getEndLine(), tb.getEndChar());
//            errors.add(lines + error.getMessage());
//        }
//
//        return errors.toString();
//    }
//
//    private CqlTranslator getTranslator(String cql, LibraryManager libraryManager, ModelManager modelManager) throws FHIRException {
//        return getTranslator(new ByteArrayInputStream(cql.getBytes(StandardCharsets.UTF_8)), libraryManager, modelManager);
//    }


}
