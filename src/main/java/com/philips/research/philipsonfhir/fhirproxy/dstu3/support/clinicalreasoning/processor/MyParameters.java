package com.philips.research.philipsonfhir.fhirproxy.dstu3.support.clinicalreasoning.processor;

import org.hl7.fhir.dstu3.model.Base;
import org.hl7.fhir.dstu3.model.Parameters;

import java.util.Optional;

public class MyParameters extends Parameters{
    Base getParameter(String name ){
        ParametersParameterComponent result = getParameter().stream()
                .filter(parametersParameterComponent -> parametersParameterComponent.getName().equals(name))
                .findFirst().get();
        return result;
    }
}
