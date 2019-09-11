package org.github.philipsonfhir.fhirproxy.clinicalreasoning.common;

import org.cqframework.cql.cql2elm.CqlTranslatorException;
import org.cqframework.cql.cql2elm.LibraryManager;
import org.cqframework.cql.cql2elm.LibrarySourceProvider;
import org.cqframework.cql.cql2elm.ModelManager;
import org.cqframework.cql.elm.execution.*;
import org.github.philipsonfhir.fhirproxy.common.FhirProxyNotImplementedException;
import org.hl7.fhir.dstu3.model.DomainResource;
import org.hl7.fhir.dstu3.model.PlanDefinition;
import org.hl7.fhir.exceptions.FHIRException;
import org.opencds.cqf.cql.data.fhir.BaseFhirDataProvider;
import org.opencds.cqf.cql.execution.Context;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Wraps the CQL logic in a shell so it can be used and managed both in the CDS Hooks as PlanDefinition and
 * ActivityDefinition.
 */
public class CqlExecutionProvider {
    private Library defaultLibrary = null;
    private Library library = null;
    private CqlLibrary cqlLibrary = null;
    private Context context;
    private Logger logger = Logger.getLogger( this.getClass().getName());
    static TreeMap<Integer, Library> libraryCache = new TreeMap<Integer, Library>();
    private DomainResource domainResource= null;
    private BaseFhirDataProvider provider;

    public CqlExecutionProvider(BaseFhirDataProvider dataProvider, DomainResource domainResource, String patientId) throws FHIRException {
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

//    public CqlExecutionProvider(BaseFhirDataProvider dataProvider, Library library, String patientId) {
//        this.library = library;
//        this.provider = dataProvider;
//        initialize( provider, library, patientId );
//    }
    public CqlExecutionProvider(BaseFhirDataProvider fhirDataProvider, DomainResource domainResource, String patientId, List<Object> contextParameters) {
        this( fhirDataProvider, domainResource, patientId);
    }

    public CqlExecutionProvider(BaseFhirDataProvider fhirDataProvider, PlanDefinition planDefinition, String patientId, List<Object> contextParameters) throws FHIRException {
        this( fhirDataProvider, planDefinition, patientId);
    }

    public CqlExecutionProvider(BaseFhirDataProvider dataProvider, DomainResource domainResource, String patientId, VersionedIdentifier vid, List<Object> contextParameters) throws FHIRException {
        if ( vid!=null ) {
            List<CqlTranslatorException> errors = new ArrayList<>();
            this.provider = dataProvider;
            Library primaryLibary = this.getLibraryLoader().load( vid );
            List<String> defineList = primaryLibary.getStatements().getDef().stream().map( def -> def.getName() ).collect( Collectors.toList() );
            this.cqlLibrary = CqlLibrary.generateCqlLibrary( domainResource, defineList );
        } else {
            this.cqlLibrary = CqlLibrary.generateCqlLibrary( domainResource );
        }

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
//        org.opencds.cqf.helpers.LibraryHelper libraryHelper;
//        .createLibraryLoader(this.getLibraryResourceProvider());
        return libraryLoader;
    }

    private LibrarySourceProvider librarySourceProvider;
    private LibrarySourceProvider getLibrarySourceProvider() {
        if (librarySourceProvider == null) {
            librarySourceProvider = new MyLibrarySourceProvider( provider.getFhirClient() );
        }
        return librarySourceProvider;
    }
}
