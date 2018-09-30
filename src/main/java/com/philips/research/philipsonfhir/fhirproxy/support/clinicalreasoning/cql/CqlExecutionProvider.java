package com.philips.research.philipsonfhir.fhirproxy.support.clinicalreasoning.cql;

import com.philips.research.philipsonfhir.fhirproxy.support.NotImplementedException;
import org.cqframework.cql.cql2elm.LibraryManager;
import org.cqframework.cql.cql2elm.LibrarySourceProvider;
import org.cqframework.cql.cql2elm.ModelManager;
import org.cqframework.cql.elm.execution.Library;
import org.cqframework.cql.elm.execution.ListTypeSpecifier;
import org.cqframework.cql.elm.execution.ParameterDef;
import org.cqframework.cql.elm.execution.UsingDef;
import org.hl7.fhir.dstu3.model.DomainResource;
import org.hl7.fhir.dstu3.model.PlanDefinition;
import org.hl7.fhir.exceptions.FHIRException;
import org.opencds.cqf.cql.data.fhir.BaseFhirDataProvider;
import org.opencds.cqf.cql.execution.Context;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.TreeMap;
import java.util.logging.Logger;

/**
 * Wraps the CQL logic in a shell so it can be used and managed both in the CDS Hooks as PlanDefinition and
 * ActivityDefinition.
 */
public class CqlExecutionProvider {
    private Library library = null;
    private CqlLibrary cqlLibrary = null;
    private Context context;
    private java.util.logging.Logger logger = Logger.getLogger( this.getClass().getName());
    static TreeMap<Integer, Library> libraryCache = new TreeMap<Integer, Library>();
    private DomainResource domainResource= null;
    private BaseFhirDataProvider provider;

    public CqlExecutionProvider(BaseFhirDataProvider dataProvider, DomainResource domainResource, String patientId) throws NotImplementedException, FHIRException {
        this.cqlLibrary = CqlLibrary.generateCqlLibrary( domainResource );
        this.provider = dataProvider;
        if ( cqlLibrary.hasCqlExpressions() ){
            library = libraryCache.get( cqlLibrary.getCqlLibaryStr().hashCode());
            if ( library==null) {
                logger.info("Compile library for " + domainResource.getId());

                library = getLibraryLoader().translateLibrary(new ByteArrayInputStream(cqlLibrary.getCqlLibaryStr().getBytes( StandardCharsets.UTF_8)), getLibraryManager(), getModelManager());

//                translateLibrary(cqlLibrary.getCqlLibaryStr(), getLibraryManager(), getModelManager());
//                library = LibraryHelper.translateLibrary(cqlLibrary.getCqlLibaryStr(), getLibraryManager(), getModelManager());
                libraryCache.put( cqlLibrary.getCqlLibaryStr().hashCode(), library );
            }
        }
        this.domainResource=domainResource;
        initialize( dataProvider, library, patientId );
    }
    public CqlExecutionProvider(BaseFhirDataProvider dataProvider, Library library, String patientId) {
        this.library = library;
        this.provider = dataProvider;
        initialize( provider, library, patientId );
    }

    public CqlExecutionProvider(BaseFhirDataProvider fhirDataProvider, PlanDefinition planDefinition, String patientId, List<Object> contextParameters) throws NotImplementedException, FHIRException {
        this( fhirDataProvider, planDefinition, patientId);
    }

    public void setContextParameters(List<Object> resources) {
        if (library.getParameters() != null) {
            for ( ParameterDef params : library.getParameters().getDef()) {
                    if (params.getParameterTypeSpecifier() instanceof ListTypeSpecifier ) {
                        context.setParameter(null, params.getName(), resources);
                    }
            }
        }
    }

    enum FhirVersion { DSTU2, DSTU3 };

    private void initialize(BaseFhirDataProvider dataProvider, Library library, String patientId) {
        if ( library==null ){
            return;
        }
        this.provider = dataProvider;
        FhirVersion version = null;
        if ( this.library.getUsings() == null ) {
            throw new RuntimeException( "The library doesn't specify a model" );
        }
        for ( UsingDef model : this.library.getUsings().getDef() ) {
            if ( model.getLocalIdentifier().equals( "FHIR" ) ) {
                if ( model.getVersion() == null ) {
                    // default
                    version = FhirVersion.DSTU3;
                }
                if ( model.getVersion().equals( "1.0.2" ) ) {
                    version = FhirVersion.DSTU2;
                }
                // TODO - STU4 support
                else
                    if ( model.getVersion().equals( "3.2.0" ) ) {
                        throw new RuntimeException( "FHIR version 3.2.0 is currently not supported" );
                    } else {
                        version = FhirVersion.DSTU3;
                    }
            }
        }
        if ( version == null ) {
            throw new RuntimeException( "The library must use the FHIR model" );
        }

        // resolve context
        context = new Context( this.library );
        // default providers/loaders
        context.registerDataProvider( "http://hl7.org/fhir", dataProvider );
        context.registerTerminologyProvider( dataProvider.getTerminologyProvider() );
        context.registerLibraryLoader( getLibraryLoader() );
        context.setExpressionCaching( true );
        context.setContextValue( "Patient", patientId );
        context.setExpressionCaching( true );

        if ( this.domainResource != null ){
            context.setParameter( null, domainResource.fhirType(), domainResource );
            context.setParameter( null, "%context", domainResource );
        }
    }

    public Object evaluateInContext(String cql){
        String cqlExpression = cql;
        if ( this.cqlLibrary!=null ){
            cqlExpression = CqlLibrary.getCqlDefine( cql );
        }
        return context.resolveExpressionRef(cqlExpression).evaluate(context);
    }

    private ModelManager modelManager;
    private ModelManager getModelManager() {
        if (modelManager == null) {
            modelManager = new ModelManager();
        }
        return modelManager;
    }

    private LibraryManager libraryManager;
    private LibraryManager getLibraryManager() {
        if (libraryManager == null) {
            libraryManager = new LibraryManager(getModelManager());
            libraryManager.getLibrarySourceLoader().clearProviders();
            libraryManager.getLibrarySourceLoader().registerProvider(getLibrarySourceProvider());
        }
        return libraryManager;
    }

    private MyLibraryLoader libraryLoader;
    private MyLibraryLoader getLibraryLoader() {
        if (libraryLoader == null) {
            libraryLoader = new MyLibraryLoader( provider.getFhirClient(), getLibraryManager(), getModelManager());
        }
        return libraryLoader;
    }

    private LibrarySourceProvider librarySourceProvider;
    private LibrarySourceProvider getLibrarySourceProvider() {
        if (librarySourceProvider == null) {
            librarySourceProvider = new MyLibrarySourceProvider( provider.getFhirClient() );
        }
        return librarySourceProvider;
    }

//    javax.xml.bind.JAXBElement a;

//    private LibraryResourceProvider getLibraryResourceProvider() {
//        return (LibraryResourceProvider)clinProvider.resolveResourceProvider("Library");
//    }

//    private List<Reference> cleanReferences(List<Reference> references) {
//        List<Reference> cleanRefs = new ArrayList<>();
//        List<Reference> noDupes = new ArrayList<>();
//
//        for (Reference reference : references) {
//            boolean dup = false;
//            for (Reference ref : noDupes) {
//                if (ref.equalsDeep(reference))
//                {
//                    dup = true;
//                }
//            }
//            if (!dup) {
//                noDupes.add(reference);
//            }
//        }
//        for (Reference reference : noDupes) {
//            cleanRefs.add(
//                    new Reference(
//                            new IdType(
//                                    reference.getReferenceElement().getResourceType(),
//                                    reference.getReferenceElement().getIdPart().replace("#", ""),
//                                    reference.getReferenceElement().getVersionIdPart()
//                            )
//                    )
//            );
//        }
//        return cleanRefs;
//    }
//
////    private Iterable<Reference> getLibraryReferences(DomainResource instance) {
////        List<Reference> references = new ArrayList<>();
////
////        if (instance.hasContained()) {
////            for (Resource resource : instance.getContained()) {
////                if (resource instanceof Library) {
////                    resource.setId(resource.getIdElement().getIdPart().replace("#", ""));
////                    getLibraryResourceProvider().getDao().update((Library) resource);
//////                    getLibraryLoader().putLibrary(resource.getIdElement().getIdPart(), getLibraryLoader().toElmLibrary((Library) resource));
////                }
////            }
////        }
////
////        if (instance instanceof ActivityDefinition) {
////            references.addAll(((ActivityDefinition)instance).getLibrary());
////        }
////
////        else if (instance instanceof PlanDefinition) {
////            references.addAll(((PlanDefinition)instance).getLibrary());
////        }
////
////        else if (instance instanceof Measure) {
////            references.addAll(((Measure)instance).getLibrary());
////        }
////
////        for (Extension extension : instance.getExtensionsByUrl("http://hl7.org/fhir/StructureDefinition/cqif-library"))
////        {
////            Type value = extension.getValue();
////
////            if (value instanceof Reference) {
////                references.add((Reference)value);
////            }
////
////            else {
////                throw new RuntimeException("Library extension does not have a value of type reference");
////            }
////        }
////
////        return cleanReferences(references);
////    }
////
////    private String buildIncludes(Iterable<Reference> references) {
////        StringBuilder builder = new StringBuilder();
////        for (Reference reference : references) {
////
////            if (builder.length() > 0) {
////                builder.append(" ");
////            }
////
////            builder.append("include ");
////
////            // TODO: This assumes the libraries resource id is the same as the library name, need to work this out better
////            builder.append(reference.getReferenceElement().getIdPart());
////
////            if (reference.getReferenceElement().getVersionIdPart() != null) {
////                builder.append(" version '");
////                builder.append(reference.getReferenceElement().getVersionIdPart());
////                builder.append("'");
////            }
////
////            builder.append(" called ");
////            builder.append(reference.getReferenceElement().getIdPart());
////        }
////
////        return builder.toString();
////    }

//    /* Evaluates the given CQL expression in the context of the given resource */
//    /* If the resource has a library extension, or a library element, that library is loaded into the context for the expression */
//    public Object evaluateInContext(DomainResource instance, String processors, String patientId) {
//        Iterable<Reference> libraries = getLibraryReferences(instance);
//
//        // Provide the instance as the value of the '%context' parameter, as well as the value of a parameter named the same as the resource
//        // This enables expressions to access the resource by root, as well as through the %context attribute
//        String source = String.format("library LocalLibrary using FHIR version '3.0.0' include FHIRHelpers version '3.0.0' called FHIRHelpers %s parameter %s %s parameter \"%%context\" %s define Expression: %s",
//                buildIncludes(libraries), instance.fhirType(), instance.fhirType(), instance.fhirType(), processors);
////        String source = String.format("library LocalLibrary using FHIR version '1.8' include FHIRHelpers version '1.8' called FHIRHelpers %s parameter %s %s parameter \"%%context\" %s define Expression: %s",
////                buildIncludes(libraries), instance.fhirType(), instance.fhirType(), instance.fhirType(), processors);
//
//        org.cqframework.processors.elm.execution.Library library = LibraryHelper.translateLibrary(source, getLibraryManager(), getModelManager());
//        Context context = new Context(library);
//        context.setParameter(null, instance.fhirType(), instance);
//        context.setParameter(null, "%context", instance);
//        context.setExpressionCaching(true);
//        context.registerLibraryLoader(getLibraryLoader());
//        context.setContextValue("Patient", patientId);
//        context.registerDataProvider("http://hl7.org/fhir", provider);
//        return context.resolveExpressionRef("Expression").evaluate(context);
//    }
}
