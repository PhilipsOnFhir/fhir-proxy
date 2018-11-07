package com.philips.research.philipsonfhir.fhirproxy.dstu3.support.clinicalreasoning.processor;

import ca.uhn.fhir.rest.client.api.IGenericClient;
import com.philips.research.philipsonfhir.fhirproxy.dstu3.support.clinicalreasoning.helpers.FhirMeasureBundler;
import org.hl7.fhir.dstu3.model.*;
import org.hl7.fhir.exceptions.FHIRException;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.opencds.cqf.cql.data.DataProvider;
import org.opencds.cqf.cql.execution.Context;
import org.opencds.cqf.cql.runtime.Interval;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class MeasureEvaluationProcessor {

    private static final Logger logger = LoggerFactory.getLogger( MeasureEvaluationProcessor.class);
    private final IGenericClient client;
    private final Date startPeriod;
    private final Date endPeriod;

    private DataProvider provider;
    private Interval measurementPeriod;
    private MeasureReport measurementReport;

    public MeasureEvaluationProcessor( IGenericClient client, DataProvider provider, Date startPerdiod, Date endPeriod) {
        this.provider = provider;
        this.client   = client;
        this.measurementPeriod =
            new org.opencds.cqf.cql.runtime.Interval(
                startPerdiod, true ,
                endPeriod, true
            );
        this.startPeriod = startPerdiod;
        this.endPeriod   = endPeriod;


    }

    public MeasureReport evaluatePatientMeasure(Measure measure, Context context, String patientId ) throws FHIRException {
        logger.info("Generating individual report");

        if (patientId == null) {
            evaluatePopulationMeasure(measure, context);
        }

        Iterable<Object> patientRetrieve = provider.retrieve("Patient", patientId, "Patient", null, null, null, null, null, null, null, null);
        Patient patient = null;
        if (patientRetrieve.iterator().hasNext()) {
            patient = (Patient) patientRetrieve.iterator().next();
        }

        return evaluate(measure, context, patient == null ? Collections.emptyList() : Collections.singletonList(patient), MeasureReport.MeasureReportType.INDIVIDUAL);
    }

    public MeasureReport evaluatePatientListMeasure(Measure measure, Context context, String practitionerRef) throws FHIRException {
        logger.info("Generating patient-list report");

        List<Patient> patients = new ArrayList<>();
        if (practitionerRef != null) {
            Practitioner practitioner = client.read().resource( Practitioner.class ).withId( practitionerRef ).execute();

            Bundle bundle = client.search().forResource( Patient.class ).where( Patient.GENERAL_PRACTITIONER.hasId( practitionerRef ) )
                .returnBundle( Bundle.class ).execute();
            List<Resource> resources = retrieveAllResources( client, bundle );
            resources.stream().forEach( resource -> patients.add( (Patient)resource) );
//            Search ParameterMap map = new SearchParameterMap();
//            map.add(
//                    "general-practitioner",
//                    new ReferenceParam(
//                            practitionerRef.startsWith("Practitioner/")
//                                    ? practitionerRef
//                                    : "Practitioner/" + practitionerRef
//                    )
//            );

//            provider.resolvePath(  )
//            if (provider instanceof JpaDataProvider) {
//                IBundleProvider patientProvider = ((JpaDataProvider) provider).resolveResourceProvider("Patient").getDao().doSearch(map);
//                List<IBaseResource> patientList = patientProvider.getResources(0, patientProvider.size());
//                patientList.forEach(x -> patients.add((Patient) x));
//            }
////            List<IBaseResource> patientList = new ArrayList<>(  );
//            results.getEntry().stream().forEach( entry -> {
//                patients.add( (Patient) entry.doGet() );
//            } );
        }
        return evaluate(measure, context, patients, MeasureReport.MeasureReportType.PATIENTLIST);
    }

    public MeasureReport evaluatePopulationMeasure(Measure measure, Context context) throws FHIRException {
        logger.info("Generating summary report");

        List<Patient> patients = new ArrayList<>();
        List<Resource> resources = new ArrayList<>();

        Bundle bundle = client.search().forResource( Patient.class ).returnBundle( Bundle.class ).execute();

        resources = retrieveAllResources( client, bundle );
        resources.stream().forEach( resource -> patients.add( (Patient)resource) );
//
//
//        if (provider instanceof JpaDataProvider) {
//            IBundleProvider patientProvider = ((JpaDataProvider) provider).resolveResourceProvider("Patient").getDao().doSearch(new SearchParameterMap());
//            List<IBaseResource> patientList = patientProvider.getResources(0, patientProvider.size());
//            patientList.forEach(x -> patients.add((Patient) x));
//        }
        return evaluate(measure, context, patients, MeasureReport.MeasureReportType.SUMMARY);
    }

    public static List<Resource> retrieveAllResources( IGenericClient client,  Bundle bundle) throws FHIRException {
        List<Resource> iBaseResourceList = new ArrayList<>();
        bundle.getEntry().stream()
            .forEach( bundleEntryComponent -> iBaseResourceList.add( bundleEntryComponent.getResource() ) );
        Bundle currentBundle = bundle;
        while ( currentBundle.getLink(Bundle.LINK_NEXT)!=null ){
            System.out.println("read Bundle");
            Bundle nextBundle = client.loadPage().next( currentBundle ).execute();

            nextBundle.getEntry().stream()
                .forEach( bundleEntryComponent -> iBaseResourceList.add( bundleEntryComponent.getResource() ) );

            currentBundle = nextBundle;
        }
        return Collections.unmodifiableList( iBaseResourceList );
    }

    public IBaseResource getResult() {
        return this.measurementReport;
    }


    public static enum MeasureScoring {
        PROPORTION,
        RATIO,
        CONTINUOUSVARIABLE,
        COHORT;

        private MeasureScoring() {
        }

        public static MeasureEvaluationProcessor.MeasureScoring fromCode(String codeString) {
            if(codeString != null && !"".equals(codeString)) {
                if("proportion".equals(codeString)) {
                    return PROPORTION;
                } else if("ratio".equals(codeString)) {
                    return RATIO;
                } else if("continuous-variable".equals(codeString)) {
                    return CONTINUOUSVARIABLE;
                } else if ("cohort".equals(codeString)) {
                    return COHORT;
                } else if( Configuration.isAcceptInvalidEnums()) {
                    return null;
                } else {
                    return null;
                    //throw new FHIRException("Unknown MeasureScoring code \'" + codeString + "\'");
                }
            } else {
                return null;
            }
        }

        public String toCode() {
            switch(ordinal()) {
                case 1:
                    return "proportion";
                case 2:
                    return "ratio";
                case 3:
                    return "continuous-variable";
                case 4:
                    return "cohort";
                default:
                    return "?";
            }
        }

        public String getSystem() {
            return "http://hl7.org/fhir/measure-scoring";
        }

        public String getDefinition() {
            switch(ordinal()) {
                case 1:
                    return "The measure score is defined using a proportion";
                case 2:
                    return "The measure score is defined using a ratio";
                case 3:
                    return "The score is defined by a calculation of some quantity";
                case 4:
                    return "The measure is a cohort definition";
                default:
                    return "?";
            }
        }

        public String getDisplay() {
            switch(ordinal()) {
                case 1:
                    return "Proportion";
                case 2:
                    return "Ratio";
                case 3:
                    return "Continuous Variable";
                case 4:
                    return "Cohort";
                default:
                    return "?";
            }
        }
    }

    public static enum MeasurePopulationType {
        INITIALPOPULATION,
        NUMERATOR,
        NUMERATOREXCLUSION,
        DENOMINATOR,
        DENOMINATOREXCLUSION,
        DENOMINATOREXCEPTION,
        MEASUREPOPULATION,
        MEASUREPOPULATIONEXCLUSION,
        MEASUREOBSERVATION;

        private MeasurePopulationType() {
        }

        public static MeasureEvaluationProcessor.MeasurePopulationType fromCode(String codeString) {
            if(codeString != null && !"".equals(codeString)) {
                if("initial-population".equals(codeString)) {
                    return INITIALPOPULATION;
                } else if("numerator".equals(codeString)) {
                    return NUMERATOR;
                } else if("numerator-exclusion".equals(codeString)) {
                    return NUMERATOREXCLUSION;
                } else if ("denominator".equals(codeString)) {
                    return DENOMINATOR;
                } else if ("denominator-exclusion".equals(codeString)) {
                    return DENOMINATOREXCLUSION;
                } else if ("denominator-exception".equals(codeString)) {
                    return DENOMINATOREXCEPTION;
                } else if("measure-population".equals(codeString)) {
                    return MEASUREPOPULATION;
                } else if ("measure-population-exclusion".equals(codeString)) {
                    return MEASUREPOPULATIONEXCLUSION;
                } else if("measure-observation".equals(codeString)) {
                    return MEASUREOBSERVATION;
                } else if( Configuration.isAcceptInvalidEnums()) {
                    return null;
                } else {
                    return null;
                    //throw new FHIRException("Unknown MeasureScoring code \'" + codeString + "\'");
                }
            } else {
                return null;
            }
        }

        public String toCode() {
            switch(ordinal()) {
                case 1:
                    return "initial-population";
                case 2:
                    return "numerator";
                case 3:
                    return "numerator-exclusion";
                case 4:
                    return "denominator";
                case 5:
                    return "denominator-exclusion";
                case 6:
                    return "denominator-exception";
                case 7:
                    return "measure-population";
                case 8:
                    return "measure-population-exclusion";
                case 9:
                    return "measure-observation";
                default:
                    return "?";
            }
        }

        public String getSystem() {
            return "http://hl7.org/fhir/measure-population";
        }

        public String getDefinition() {
            switch(ordinal()) {
                case 1:
                    return "The initial population refers to all patients or events to be evaluated by a quality measure involving patients who share a common set of specified characterstics. All patients or events counted (for example, as numerator, as denominator) are drawn from the initial population";
                case 2:
                    return "\tThe upper portion of a fraction used to calculate a rate, proportion, or ratio. Also called the measure focus, it is the target process, condition, event, or outcome. Numerator criteria are the processes or outcomes expected for each patient, or event defined in the denominator. A numerator statement describes the clinical action that satisfies the conditions of the measure";
                case 3:
                    return "Numerator exclusion criteria define patients or events to be removed from the numerator. Numerator exclusions are used in proportion and ratio measures to help narrow the numerator (for inverted measures)";
                case 4:
                    return "The lower portion of a fraction used to calculate a rate, proportion, or ratio. The denominator can be the same as the initial population, or a subset of the initial population to further constrain the population for the purpose of the measure";
                case 5:
                    return "Denominator exclusion criteria define patients or events that should be removed from the denominator before determining if numerator criteria are met. Denominator exclusions are used in proportion and ratio measures to help narrow the denominator. For example, patients with bilateral lower extremity amputations would be listed as a denominator exclusion for a measure requiring foot exams";
                case 6:
                    return "Denominator exceptions are conditions that should remove a patient or event from the denominator of a measure only if the numerator criteria are not met. Denominator exception allows for adjustment of the calculated score for those providers with higher risk populations. Denominator exception criteria are only used in proportion measures";
                case 7:
                    return "Measure population criteria define the patients or events for which the individual observation for the measure should be taken. Measure populations are used for continuous variable measures rather than numerator and denominator criteria";
                case 8:
                    return "Measure population criteria define the patients or events that should be removed from the measure population before determining the outcome of one or more continuous variables defined for the measure observation. Measure population exclusion criteria are used within continuous variable measures to help narrow the measure population";
                case 9:
                    return "Defines the individual observation to be performed for each patient or event in the measure population. Measure observations for each case in the population are aggregated to determine the overall measure score for the population";
                default:
                    return "?";
            }
        }

        public String getDisplay() {
            switch(ordinal()) {
                case 1:
                    return "Initial Population";
                case 2:
                    return "Numerator";
                case 3:
                    return "Numerator Exclusion";
                case 4:
                    return "Denominator";
                case 5:
                    return "Denominator Exclusion";
                case 6:
                    return "Denominator Exception";
                case 7:
                    return "Measure Population";
                case 8:
                    return "Measure Population Exclusion";
                case 9:
                    return "Measure Observation";
                default:
                    return "?";
            }
        }
    }

    private Iterable<Resource> evaluateCriteria(Context context, Patient patient, Measure.MeasureGroupPopulationComponent pop) {
        context.setContextValue("Patient", patient.getIdElement().getIdPart());
        Object result = context.resolveExpressionRef(pop.getCriteria()).evaluate(context);
        if (result instanceof Boolean ) {
            if (((Boolean)result)) {
                return Collections.singletonList(patient);
            }
            else {
                return Collections.emptyList();
            }
        }

        return (Iterable)result;
    }

    private boolean evaluatePopulationCriteria(Context context, Patient patient,
                                               Measure.MeasureGroupPopulationComponent criteria, HashMap<String, Resource> population,
                                               Measure.MeasureGroupPopulationComponent exclusionCriteria, HashMap<String, Resource> exclusionPopulation
    ) {
        boolean inPopulation = false;
        if (criteria != null) {
            for ( Resource resource : evaluateCriteria(context, patient, criteria)) {
                inPopulation = true;
                population.put(resource.getId(), resource);
            }
        }

        if (inPopulation) {
            // Are they in the denominator exclusion?
            if (exclusionCriteria != null) {
                for ( Resource resource : evaluateCriteria(context, patient, exclusionCriteria)) {
                    inPopulation = false;
                    exclusionPopulation.put(resource.getId(), resource);
                    population.remove(resource.getId());
                }
            }
        }

        return inPopulation;
    }

    private void addPopulationCriteriaReport(MeasureReport.MeasureReportGroupComponent reportGroup, Measure.MeasureGroupPopulationComponent populationCriteria, int populationCount) {
        if (populationCriteria != null) {
            MeasureReport.MeasureReportGroupPopulationComponent populationReport = new MeasureReport.MeasureReportGroupPopulationComponent();
            populationReport.setCode(populationCriteria.getCode());
            populationReport.setIdentifier(populationCriteria.getIdentifier());
            populationReport.setCount(populationCount);
            reportGroup.addPopulation(populationReport);
        }
    }

    private MeasureReport evaluate(Measure measure, Context context, List<Patient> patients, MeasureReport.MeasureReportType type)
    {
        MeasureReport measureReport = new MeasureReport(  )
            .setStatus( MeasureReport.MeasureReportStatus.COMPLETE )
            .setType( type )
            .setMeasure( new Reference(  ).setReference( "Measure/"+measure.getIdElement().getValue() ) )
            ;

//        MeasureReportBuilder reportBuilder = new MeasureReportBuilder();
//        reportBuilder.buildStatus("complete");
//        reportBuilder.buildType(type);
//        reportBuilder.buildMeasureReference(measure.getIdElement().getValue());

        if (type == MeasureReport.MeasureReportType.INDIVIDUAL && !patients.isEmpty()) {
            measureReport.setPatient( new Reference(  ).setReference( "Patient/"+patients.get(0).getIdElement().getValue() ) );
//            reportBuilder.buildPatientReference(patients.get(0).getIdElement().getValue());
        }

        Period period = new Period();
        period.setStart( this.startPeriod  );
        period.setEnd( this.endPeriod );

        measureReport.setPeriod( period );
//        reportBuilder.buildPeriod(measurementPeriod);

        MeasureReport report = this.measurementReport;

        List<Patient> initialPopulation = getInitalPopulation(measure, patients, context);
        HashMap<String,Resource> resources = new HashMap<>();

        MeasureScoring measureScoring = MeasureScoring.fromCode(measure.getScoring().getCodingFirstRep().getCode());
        if (measureScoring == null) {
            throw new RuntimeException("Measure scoring is required in order to calculate.");
        }

        for ( Measure.MeasureGroupComponent group : measure.getGroup()) {
            MeasureReport.MeasureReportGroupComponent reportGroup = new MeasureReport.MeasureReportGroupComponent();
            reportGroup.setIdentifier(group.getIdentifier());
            report.getGroup().add(reportGroup);

            // Declare variables to avoid a hash lookup on every patient
            // TODO: Isn't quite right, there may be multiple initial populations for a ratio measure...
            Measure.MeasureGroupPopulationComponent initialPopulationCriteria = null;
            Measure.MeasureGroupPopulationComponent numeratorCriteria = null;
            Measure.MeasureGroupPopulationComponent numeratorExclusionCriteria = null;
            Measure.MeasureGroupPopulationComponent denominatorCriteria = null;
            Measure.MeasureGroupPopulationComponent denominatorExclusionCriteria = null;
            Measure.MeasureGroupPopulationComponent denominatorExceptionCriteria = null;
            Measure.MeasureGroupPopulationComponent measurePopulationCriteria = null;
            Measure.MeasureGroupPopulationComponent measurePopulationExclusionCriteria = null;
            // TODO: Isn't quite right, there may be multiple measure observations...
            Measure.MeasureGroupPopulationComponent measureObservationCriteria = null;

            HashMap<String, Resource> numerator = null;
            HashMap<String, Resource> numeratorExclusion = null;
            HashMap<String, Resource> denominator = null;
            HashMap<String, Resource> denominatorExclusion = null;
            HashMap<String, Resource> denominatorException = null;
            HashMap<String, Resource> measurePopulation = null;
            HashMap<String, Resource> measurePopulationExclusion = null;
            HashMap<String, Resource> measureObservation = null;

            for ( Measure.MeasureGroupPopulationComponent pop : group.getPopulation()) {
                MeasurePopulationType populationType = MeasurePopulationType.fromCode(pop.getCode().getCodingFirstRep().getCode());
                if (populationType != null) {
                    switch (populationType) {
                        case INITIALPOPULATION:
                            initialPopulationCriteria = pop;
                            // Initial population is already computed
                            break;
                        case NUMERATOR:
                            numeratorCriteria = pop;
                            numerator = new HashMap<String, Resource>();
                            break;
                        case NUMERATOREXCLUSION:
                            numeratorExclusionCriteria = pop;
                            numeratorExclusion = new HashMap<String, Resource>();
                            break;
                        case DENOMINATOR:
                            denominatorCriteria = pop;
                            denominator = new HashMap<String, Resource>();
                            break;
                        case DENOMINATOREXCLUSION:
                            denominatorExclusionCriteria = pop;
                            denominatorExclusion = new HashMap<String, Resource>();
                            break;
                        case DENOMINATOREXCEPTION:
                            denominatorExceptionCriteria = pop;
                            denominatorException = new HashMap<String, Resource>();
                            break;
                        case MEASUREPOPULATION:
                            measurePopulationCriteria = pop;
                            measurePopulation = new HashMap<String, Resource>();
                            break;
                        case MEASUREPOPULATIONEXCLUSION:
                            measurePopulationExclusionCriteria = pop;
                            measurePopulationExclusion = new HashMap<String, Resource>();
                            break;
                        case MEASUREOBSERVATION:
                            break;
                    }
                }
            }

            switch (measureScoring) {
                case PROPORTION:
                case RATIO: {

                    // For each patient in the initial population
                    for ( Patient patient : initialPopulation) {

                        // Are they in the denominator?
                        boolean inDenominator = evaluatePopulationCriteria(context, patient,
                                denominatorCriteria, denominator,
                                denominatorExclusionCriteria, denominatorExclusion);

                        if (inDenominator) {
                            // Are they in the numerator?
                            boolean inNumerator = evaluatePopulationCriteria(context, patient,
                                    numeratorCriteria, numerator,
                                    numeratorExclusionCriteria, numeratorExclusion);

                            if (!inNumerator && inDenominator && (denominatorExceptionCriteria != null)) {
                                // Are they in the denominator exception?
                                for ( Resource resource : evaluateCriteria(context, patient, denominatorExceptionCriteria)) {
                                    denominatorException.put(resource.getId(), resource);
                                    denominator.remove(resource.getId());
                                }
                            }
                        }

                    }

                    // Calculate actual measure score, Count(numerator) / Count(denominator)
                    if (denominator != null && numerator != null && denominator.size() > 0) {
                        reportGroup.setMeasureScore(numerator.size() / denominator.size());
                    }

                    break;
                }
                case CONTINUOUSVARIABLE: {

                    // For each patient in the initial population
                    for ( Patient patient : initialPopulation) {

                        // Are they in the measure population?
                        boolean inMeasurePopulation = evaluatePopulationCriteria(context, patient,
                                measurePopulationCriteria, measurePopulation,
                                measurePopulationExclusionCriteria, measurePopulationExclusion);

                        if (inMeasurePopulation) {
                            // TODO: Evaluate measure observations
                            for ( Resource resource : evaluateCriteria(context, patient, measureObservationCriteria)) {
                                measureObservation.put(resource.getId(), resource);
                            }
                        }
                    }

                    break;
                }
                case COHORT: {
                    // Only initial population matters for a cohort measure
                    break;
                }
            }

            // Add population reports for each group
            addPopulationCriteriaReport(reportGroup, initialPopulationCriteria, initialPopulation != null ? initialPopulation.size() : 0);
            addPopulationCriteriaReport(reportGroup, numeratorCriteria, numerator != null ? numerator.size() : 0);
            addPopulationCriteriaReport(reportGroup, numeratorExclusionCriteria, numeratorExclusion != null ? numeratorExclusion.size() : 0);
            addPopulationCriteriaReport(reportGroup, denominatorCriteria, denominator != null ? denominator.size() : 0);
            addPopulationCriteriaReport(reportGroup, denominatorExclusionCriteria, denominatorExclusion != null ? denominatorExclusion.size() : 0);
            addPopulationCriteriaReport(reportGroup, denominatorExceptionCriteria, denominatorException != null ? denominatorException.size() : 0);
            addPopulationCriteriaReport(reportGroup, measurePopulationCriteria,  measurePopulation != null ? measurePopulation.size() : 0);
            addPopulationCriteriaReport(reportGroup, measurePopulationExclusionCriteria,  measurePopulationExclusion != null ? measurePopulationExclusion.size() : 0);
            // TODO: Measure Observations...
        }

        FhirMeasureBundler bundler = new FhirMeasureBundler();
//        if (resources.isEmpty()) {
//            for ( Object element : context.getEvaluatedResourcesList()) {
//                if (element instanceof Resource ) {
//                    resources.put(((Resource) element).getId(), (Resource) element);
//                }
//            }
//        }
        org.hl7.fhir.dstu3.model.Bundle evaluatedResources = bundler.bundle(resources.values());
        evaluatedResources.setId( UUID.randomUUID().toString());
        report.setEvaluatedResources(new Reference('#' + evaluatedResources.getId()));
        report.addContained(evaluatedResources);
        return report;
    }

    private List<Patient> getInitalPopulation(Measure measure, List<Patient> population, Context context) {
        // TODO: Needs to account for multiple population groups
        List<Patient> initalPop = new ArrayList<>();
        for ( Measure.MeasureGroupComponent group : measure.getGroup()) {
            for ( Measure.MeasureGroupPopulationComponent pop : group.getPopulation()) {
                if (pop.getCode().getCodingFirstRep().getCode().equals("initial-population")) {
                    for ( Patient patient : population) {
                        context.setContextValue("Patient", patient.getIdElement().getIdPart());
                        Object result = context.resolveExpressionRef(pop.getCriteria()).evaluate(context);
                        if (result == null) {
                            continue;
                        }
                        if ((Boolean) result) {
                            initalPop.add(patient);
                        }
                    }
                }
            }
        }
        return initalPop;
    }
}
