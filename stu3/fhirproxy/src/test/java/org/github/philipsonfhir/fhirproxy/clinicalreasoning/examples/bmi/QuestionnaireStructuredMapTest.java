package org.github.philipsonfhir.fhirproxy.clinicalreasoning.examples.bmi;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import org.github.philipsonfhir.fhirproxy.clinicalreasoning.common.FhirValueSetter;
import org.github.philipsonfhir.fhirproxy.clinicalreasoning.common.MyWorkerContext;
import org.github.philipsonfhir.fhirproxy.clinicalreasoning.examples.bmi.definition.BmiQuestionnaire;
import org.github.philipsonfhir.fhirproxy.clinicalreasoning.structuremap.StructureMapTransformWorker;
import org.github.philipsonfhir.fhirproxy.testutil.NameGenerator;
import org.hl7.fhir.dstu3.hapi.validation.DefaultProfileValidationSupport;
import org.hl7.fhir.dstu3.model.*;
import org.hl7.fhir.dstu3.utils.FHIRPathEngine;
import org.hl7.fhir.exceptions.FHIRException;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class QuestionnaireStructuredMapTest {
    static String source = "source";
    static String target = "target";
    private TestServer testServer = new TestServer();
    private QuestionnaireResponse questionnaireResponse;
    private Patient subject;
    private Questionnaire bmiQuestionnaire;
    private StructureMap heightStructureMap;
    private StructureMap weightStructureMap;
    private StructureMap bmiStructureMap;

//    @Before
//    public void init(){
//        this.testServer = new Dstu3TestServer("http://localhost:9001/cqf-ruler/baseDstu3");
//
//        createQuestionnaireResponse();
//        this.testServer.putResource( bmiQuestionnaire );
//        this.testServer.putResource( questionnaireResponse );
//
//        this.testServer.putResource( createHeightObsStructureMap() );
//
//    }

    @Before
    public void createAll(){
        createQuestionnaireResponse();
        BmiQuestionnaire bmiQuestionnaire = new BmiQuestionnaire();
        this.bmiQuestionnaire   = bmiQuestionnaire.buildQuestionnaire();
        this.heightStructureMap = bmiQuestionnaire.createHeightObsStructureMap();
        this.weightStructureMap = bmiQuestionnaire.createWeightObsStructureMap();
        this.bmiStructureMap    = bmiQuestionnaire.createBmiObsStructureMap();
//        this.testServer = new Dstu3TestServer("http://localhost:9001/cqf-ruler/baseDstu3");
//        this.testServer = new Dstu3TestServer("http://130.145.227.123:9002/cqf-ruler/baseDstu3");
//        this.testServer = new TestServer("http://localhost:8080/fhir");

    }

    @Test
    public void storeAll() throws IOException {
        testServer.putStoreResource(heightStructureMap);
        testServer.putStoreResource(weightStructureMap);
        testServer.putStoreResource(bmiStructureMap);
        testServer.putStoreResource(bmiQuestionnaire);
    }


    @Test
    public void createQuestionnaireResponse(){

        this.subject = (Patient) new Patient()
                .setGender(Enumerations.AdministrativeGender.MALE)
                .addName(NameGenerator.createPatientHumanName())
                .setId("SMsubject");

        String linkIdHeigth = "height";
        String linkIdWeigth = "weight";

        this.bmiQuestionnaire = new BmiQuestionnaire().buildQuestionnaire();

        this.questionnaireResponse = (QuestionnaireResponse) new QuestionnaireResponse()
                .setQuestionnaire( new Reference().setReference("Questionnaire/"+bmiQuestionnaire.getId()))
                .setStatus(QuestionnaireResponse.QuestionnaireResponseStatus.COMPLETED)
                .setSubject(new Reference().setReference("Patient/"+subject.getId()))
                .addItem( new QuestionnaireResponse.QuestionnaireResponseItemComponent()
                        .setLinkId(linkIdHeigth)
                        .addAnswer( new QuestionnaireResponse.QuestionnaireResponseItemAnswerComponent()
                                .setValue( new Quantity(1.80).setUnit("m"))
                        )
                )
                .addItem( new QuestionnaireResponse.QuestionnaireResponseItemComponent()
                                .setLinkId(linkIdWeigth)
                                .addAnswer( new QuestionnaireResponse.QuestionnaireResponseItemAnswerComponent()
//                                .setValue( new DecimalType(81))
                                                .setValue( new Quantity(81).setUnit("kg"))
                                )
                )
                .setId("bmiResponse1");

    }


    @Test
    public void testHeightObsFromQuestionnaireResponseSM() throws FHIRException, IOException {


//        testServer.putResource(structureMap);



        FhirContext ourCtx = FhirContext.forDstu3();
        MyWorkerContext hapiWorkerContext = new MyWorkerContext(ourCtx, new DefaultProfileValidationSupport());
        FHIRPathEngine fpe = new FHIRPathEngine(hapiWorkerContext);

        Observation obs = new Observation().setValue( new Quantity().setValue(1.8).setSystem("http:exaxmple.com"));
        System.out.println( ourCtx.newJsonParser().encodeResourceToString(obs));

        fpe.evaluate( questionnaireResponse, "item.where(name='hgt').answer.value");
        fpe.evaluate( questionnaireResponse, "item.answer.value");
        fpe.evaluate( questionnaireResponse, "item.where(linkId='height').answer.value");
        fpe.evaluate( questionnaireResponse, "item.where(linkId='weight').answer.value.value * item.where(linkId='weight').answer.value.value/ item.where(linkId='height').answer.value.value");

        StructureMapTransformWorker structureMapTransformWorker = new StructureMapTransformWorker();
        IParser jsonParser = ourCtx.newJsonParser().setPrettyPrint(true);
        System.out.println("-----");
        IBaseResource hObs = structureMapTransformWorker.doTransform( heightStructureMap, questionnaireResponse );
        System.out.println("-----");
        IBaseResource wObs = structureMapTransformWorker.doTransform( weightStructureMap, questionnaireResponse );
        System.out.println("-----");
        IBaseResource bmiObs = structureMapTransformWorker.doTransform( bmiStructureMap, questionnaireResponse );
        System.out.println("-----");
        System.out.println("-----");
        System.out.println( jsonParser.encodeResourceToString(weightStructureMap));
        System.out.println("-----");
        System.out.println( jsonParser.encodeResourceToString(heightStructureMap));
        System.out.println("-----");
        System.out.println( jsonParser.encodeResourceToString(wObs));
        System.out.println("-----");
        System.out.println( jsonParser.encodeResourceToString(hObs));
        System.out.println("-----");
        System.out.println( jsonParser.encodeResourceToString(bmiObs));
        System.out.println("-----");
        testServer.storeResource((Resource) wObs);
        testServer.storeResource((Resource) hObs);
        testServer.storeResource((Resource) bmiObs);
    }

    @Test
    public  void testQuantityAssignement() throws FHIRException {
        StructureMap map = (StructureMap) new StructureMap()
                .setExperimental(true)
                .addStructure( new StructureMap.StructureMapStructureComponent()
                        .setUrl("http://hl7.org/fhir/StructureDefinition/questionnaireresponse")
                        .setMode( StructureMap.StructureMapModelMode.SOURCE )
                )
                .addStructure( new StructureMap.StructureMapStructureComponent()
                        .setUrl("http://hl7.org/fhir/StructureDefinition/observation")
                        .setMode( StructureMap.StructureMapModelMode.TARGET )
                )
                .addGroup( new StructureMap.StructureMapGroupComponent()
                        .setTypeMode(StructureMap.StructureMapGroupTypeMode.NONE) // not the default mapping
                        .addInput( new StructureMap.StructureMapGroupInputComponent()
                                .setName(source)
                                .setMode(StructureMap.StructureMapInputMode.SOURCE)
                        )
                        .addInput( new StructureMap.StructureMapGroupInputComponent()
                                .setName(target)
                                .setMode(StructureMap.StructureMapInputMode.TARGET)
                        )
                        .addRule( new StructureMap.StructureMapGroupRuleComponent()
                                .setName("createQty")
                                .setSource( getUnusedSource() )
                                .addTarget( new StructureMap.StructureMapGroupRuleTargetComponent()
                                        .setContext( target )
                                        .setContextType( StructureMap.StructureMapContextType.VARIABLE )
                                        .setElement("value")
                                        .setTransform(StructureMap.StructureMapTransform.QTY)
                                        .addParameter(new StructureMap.StructureMapGroupRuleTargetParameterComponent()
                                                .setValue( new DecimalType("684"))
                                        )
                                        .addParameter(new StructureMap.StructureMapGroupRuleTargetParameterComponent()
                                                .setValue( new StringType("kg/m^2"))
                                        )
                                        .addParameter(new StructureMap.StructureMapGroupRuleTargetParameterComponent()
                                                .setValue( new StringType("ucum"))
                                        )
                                )
                        )
                )                .setId("dummy");
        createQuestionnaireResponse();
        FhirContext ourCtx = FhirContext.forDstu3();
        MyWorkerContext hapiWorkerContext = new MyWorkerContext(ourCtx, new DefaultProfileValidationSupport());
        FHIRPathEngine fpe = new FHIRPathEngine(hapiWorkerContext);

        IParser jsonParser = ourCtx.newJsonParser().setPrettyPrint(true);
        Resource resource = (Resource) new StructureMapTransformWorker().doTransform( map, questionnaireResponse );
        System.out.println("-----");
        System.out.println(jsonParser.encodeResourceToString(resource));
        assertTrue( resource instanceof  Observation);
        Observation obs = (Observation)resource;
        assertEquals(new BigDecimal(684), obs.getValueQuantity().getValue());

        // Create a quantity. Parameters = (text) or (value, unit, [system, code]) where text is the natural representation e.g. [comparator]value[space]unit
    }

    private List<StructureMap.StructureMapGroupRuleSourceComponent> getUnusedSource() {
        List<StructureMap.StructureMapGroupRuleSourceComponent> result = new ArrayList();
        result.add( new StructureMap.StructureMapGroupRuleSourceComponent()
                .setContext(source)
                .setElement("id")
                .setVariable("a")
        );
        return result;
    }

    @Test
    public void testAssignemt() throws FHIRException {

        // test childern based assignment
        Observation dest = new Observation();
        StructureMap.StructureMapGroupRuleTargetComponent tgt = new StructureMap.StructureMapGroupRuleTargetComponent()
                .setElement("related.type");
        Base v = new StringType("derived-from");
        v = FhirValueSetter.setProperty(dest, tgt.getElement(), v);

        assertEquals(dest.getRelated().size(), 1);
        assertEquals(dest.getRelated().get(0).getType().toCode(), "derived-from");

        // test direct assignment
        FhirValueSetter.setProperty(dest, "id", new IdType("myid"));
        assertEquals("myid", dest.getId());

        // test Quality assignment
        try {
            v = FhirValueSetter.setProperty(dest, "value.value", new DecimalType("684"));
            fail("should throw exception");
        } catch (FHIRException e) {
        }

        try {
            v = FhirValueSetter.setProperty(dest, "valueQuantity.value", new DecimalType("684"));
            fail("should throw exception");
        } catch (FHIRException e) {
        }

        dest.setValue(new Quantity());
        v = FhirValueSetter.setProperty(dest, "value.value", new DecimalType("684"));
        assertTrue(dest.getValue() instanceof Quantity);
        assertEquals(new BigDecimal(684), dest.getValueQuantity().getValue());

        v = FhirValueSetter.setProperty(dest, "status", new StringType("final"));
    }


}
