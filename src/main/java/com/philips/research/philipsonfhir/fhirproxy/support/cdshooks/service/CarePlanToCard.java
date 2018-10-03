package com.philips.research.philipsonfhir.fhirproxy.support.cdshooks.service;

import com.philips.research.philipsonfhir.fhirproxy.support.cdshooks.model.*;
import org.hl7.fhir.dstu3.model.*;

import java.util.ArrayList;
import java.util.List;

public class CarePlanToCard {

    public static List<Card> convert(CarePlan carePlan) {
        List<Card> cards = new ArrayList<>();

        for (CarePlan.CarePlanActivityComponent activity : carePlan.getActivity()) {
            if (activity.getReferenceTarget() != null && activity.getReferenceTarget() instanceof RequestGroup) {
                RequestGroup requestGroup = (RequestGroup) activity.getReferenceTarget();
                cards = convert(requestGroup);
            }
        }

        return cards;
    }

    private static List<Card> convert(RequestGroup requestGroup) {
        List<Card> cards = new ArrayList<>();

        // links
        List<Link> links = new ArrayList<>();
        if (requestGroup.hasExtension()) {
            for (Extension extension : requestGroup.getExtension()) {
                Link link = new Link();

                if (extension.getValue() instanceof Attachment) {
                    Attachment attachment = (Attachment) extension.getValue();
                    if (attachment.hasUrl()) {
                        link.setUrl(attachment.getUrl());
                    }
                    if (attachment.hasTitle()) {
                        link.setLabel(attachment.getTitle());
                    }
                    if (attachment.hasExtension()) {
                        link.setType(attachment.getExtensionFirstRep().getValue().primitiveValue());
                    }
                }

                else {
                    throw new RuntimeException("Invalid link extension type: " + extension.getValue().fhirType());
                }

                links.add(link);
            }
        }

        if (requestGroup.hasAction()) {
            for (RequestGroup.RequestGroupActionComponent action : requestGroup.getAction()) {
                Card card = new Card();
                // basic
                if (action.hasTitle()) {
                    card.setSummary(action.getTitle());
                }
                if (action.hasDescription()) {
                    card.setDetail(action.getDescription());
                }
                if (action.hasExtension()) {
                    card.setIndicator(action.getExtensionFirstRep().getValue().toString());
                }

                // source
                if (action.hasDocumentation()) {
                    // Assuming first related artifact has everything
                    RelatedArtifact documentation = action.getDocumentationFirstRep();
                    Source source = new Source();
                    if (documentation.hasDisplay()) {
                        source.setLabel(documentation.getDisplay());
                    }
                    if (documentation.hasUrl()) {
                        source.setUrl(documentation.getUrl());
                    }
                    if (documentation.hasDocument() && documentation.getDocument().hasUrl()) {
                        source.setIcon(documentation.getDocument().getUrl());
                    }

                    card.setSource(source);
                }

                // suggestions
                // TODO - uuid
                boolean hasSuggestions = false;
                Suggestion suggestions = new Suggestion();
                Action actions = new Action();
                if (action.hasLabel()) {
                    suggestions.setLabel(action.getLabel());
                    hasSuggestions = true;
                    if (action.hasDescription()) {
                        actions.setDescription(action.getDescription());
                    }
                    if (action.hasType() && !action.getType().getCode().equals("fire-event")) {
                        String code = action.getType().getCode();
                        actions.setType( Action.ActionType.valueOf(code.equals("remove") ? "delete" : code));
                    }
                    if (action.hasResource()) {
                        actions.setResource(action.getResourceTarget());
                    }
                }
                if (hasSuggestions) {
                    suggestions.getActions().add(actions);
                    card.getSuggestions().add(suggestions);
                }
                if (!links.isEmpty()) {
                    card.setLinks(links);
                }
                cards.add(card);
            }
        }

        return cards;
    }
}
