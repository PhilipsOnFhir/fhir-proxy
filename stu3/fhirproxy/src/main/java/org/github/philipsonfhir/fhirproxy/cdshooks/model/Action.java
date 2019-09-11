package org.github.philipsonfhir.fhirproxy.cdshooks.model;

import ca.uhn.fhir.context.FhirContext;
import lombok.Getter;
import lombok.Setter;
import org.hl7.fhir.dstu3.model.Resource;

@Getter
@Setter
public class Action {
    public enum ActionType {create, update, delete}

    ActionType type;
    String description;
    String resource;
    public void setResource( Resource fhirResource ){
        resource = (FhirContext.forDstu3().newJsonParser()).encodeResourceToString(fhirResource);
    }
}
