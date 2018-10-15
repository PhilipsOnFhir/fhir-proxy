package com.philips.research.philipsonfhir.fhirproxy.dstu3.support.cdshooks.model;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class Card {
    String summary;
    String detail;
    String indicator;
    Source source;
    List<Suggestion> suggestions;
    List<Link> links;
}
