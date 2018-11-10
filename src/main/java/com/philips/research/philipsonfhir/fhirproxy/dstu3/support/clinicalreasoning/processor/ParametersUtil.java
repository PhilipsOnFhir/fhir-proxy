package com.philips.research.philipsonfhir.fhirproxy.dstu3.support.clinicalreasoning.processor;

import org.hl7.fhir.dstu3.model.Base;
import org.hl7.fhir.dstu3.model.Parameters;
import org.hl7.fhir.dstu3.model.Type;

import java.util.Optional;

public class ParametersUtil {
//    Base getParameter(String name ){
//        Type result = getParameter().stream()
//                .filter(parametersParameterComponent -> parametersParameterComponent.getName().equals(name))
//                .findFirst().get().getValue();
//        return result;
//    }

    public static Parameters.ParametersParameterComponent getParameter(Parameters myParameters, String name) {
        Parameters.ParametersParameterComponent result = myParameters.getParameter().stream()
                .filter(parametersParameterComponent -> parametersParameterComponent.getName().equals(name))
                .findFirst().get();
        return result;
    }
}
