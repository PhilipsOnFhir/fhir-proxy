package org.github.philipsonfhir.fhirproxy.clinicalreasoning.examples.bmi;

import org.github.philipsonfhir.fhirproxy.clinicalreasoning.examples.FhirHelpersCqlLibrary;
import org.github.philipsonfhir.fhirproxy.clinicalreasoning.examples.bmi.definition.*;
import org.hl7.fhir.dstu3.model.*;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

public class BmiTest {

    @Test
    public void storeResources() throws IOException {
        BmiQuestionnaire  bmiq = new BmiQuestionnaire();
        TestServer testServer = new TestServer();

        testServer.putStoreResource( new OurCodeSystem().build() );
        testServer.putStoreResource( new WeightObservationValueSet().build() );
        testServer.putStoreResource( new HeightObservationValueSet().build() );
        testServer.putStoreResource( new BmiObservationValueSet().build() );

        testServer.putStoreResource(new BmiPlanDefinition().build());
        testServer.putStoreResource(new FhirHelpersCqlLibrary().build());
        testServer.putStoreResource(new BmiCqlLibrary().build());
        testServer.putStoreResource(new BmiObsActivityDefinition().build());
        testServer.putStoreResource(new HeightActivityDefinition().build());
        testServer.putStoreResource(new WeightActivityDefinition().build());

        testServer.putStoreResource(bmiq.buildQuestionnaire());
        testServer.putStoreResource(bmiq.createBmiObsStructureMap());
        testServer.putStoreResource(bmiq.createHeightObsStructureMap());
        testServer.putStoreResource(bmiq.createWeightObsStructureMap());
    }
}
