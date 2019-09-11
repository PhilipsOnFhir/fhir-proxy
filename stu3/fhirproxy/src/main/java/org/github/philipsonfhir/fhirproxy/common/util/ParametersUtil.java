package org.github.philipsonfhir.fhirproxy.common.util;

import org.hl7.fhir.dstu3.model.Parameters;

import java.util.Optional;

public class ParametersUtil {
//    Base getParameter(String name ){
//        Type result = getParameter().stream()
//                .filter(parametersParameterComponent -> parametersParameterComponent.getName().equals(name))
//                .findFirst().get().getValue();
//        return result;
//    }

    public static Parameters.ParametersParameterComponent getParameter(Parameters myParameters, String name) {
        Optional<Parameters.ParametersParameterComponent> opt = myParameters.getParameter().stream()
                .filter(parametersParameterComponent -> parametersParameterComponent.getName().equals(name))
                .findFirst();
        if ( opt.isPresent() ){
            return opt.get();
        }
        return null;
    }

    public static boolean holdsParameter(Parameters myParameters, String name) {
        Optional<Parameters.ParametersParameterComponent> result = myParameters.getParameter().stream()
                .filter(parametersParameterComponent -> parametersParameterComponent.getName().equals(name))
                .findFirst();
        return result.isPresent();
    }
}
