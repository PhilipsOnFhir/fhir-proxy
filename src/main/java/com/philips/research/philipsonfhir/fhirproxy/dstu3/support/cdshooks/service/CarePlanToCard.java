package com.philips.research.philipsonfhir.fhirproxy.dstu3.support.cdshooks.service;

import com.philips.research.philipsonfhir.fhirproxy.dstu3.support.NotImplementedException;
import com.philips.research.philipsonfhir.fhirproxy.dstu3.support.cdshooks.model.*;
import org.hl7.fhir.dstu3.model.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CarePlanToCard {

    public static List<Card> convert(CarePlan carePlan) throws NotImplementedException {

        List<Card> cards = new ArrayList<>();

        for (CarePlan.CarePlanActivityComponent activity : carePlan.getActivity()) {
            if (activity.getReferenceTarget() != null && activity.getReferenceTarget() instanceof RequestGroup) {
                RequestGroup requestGroup = (RequestGroup) activity.getReferenceTarget();
                cards = convert(carePlan, requestGroup);
            }
            if (activity.getReference() != null && activity.getReference().getReference().startsWith( "#" ) ) {
                String reference = activity.getReference().getReference();
                Optional<Resource> optRG = carePlan.getContained().stream()
                    .filter( resource -> reference.substring( 1 ).equals( resource.getId() ) )
                    .findFirst();
                if (optRG.isPresent()) {
                    RequestGroup requestGroup = (RequestGroup) optRG.get();
                    cards = convert( carePlan, requestGroup );
                }
            }
            else {
                throw new NotImplementedException( "Retrieval of repository stored request groups is not supported." );
            }
        }

        return cards;
    }

    private static List<Card> convert( CarePlan carePlan, RequestGroup requestGroup) throws NotImplementedException {
        List<Card> cards = new ArrayList<>();

        // links
        List<Link> links = new ArrayList<>();
//        if (requestGroup.hasExtension()) {
//            for (Extension extension : requestGroup.getExtension()) {
//                Link link = new Link();
//
//                if (extension.getValue() instanceof Attachment) {
//                    Attachment attachment = (Attachment) extension.getValue();
//                    if (attachment.hasUrl()) {
//                        link.setUrl(attachment.getUrl());
//                    }
//                    if (attachment.hasTitle()) {
//                        link.setLabel(attachment.getTitle());
//                    }
//                    if (attachment.hasExtension()) {
//                        link.setType(attachment.getExtensionFirstRep().getValue().primitiveValue());
//                    }
//                }
//
//                else {
//                    throw new RuntimeException("Invalid link extension type: " + extension.getValue().fhirType());
//                }
//
//                links.add(link);
//            }
//        }

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
                for ( RequestGroup.RequestGroupActionComponent subAction : action.getAction() ){
                    Suggestion suggestion = new Suggestion();
                    suggestion.setLabel(action.getLabel());

                    Action suggestionAction = new Action();
                    suggestionAction.setType( Action.ActionType.valueOf( subAction.getType().getCode() ) );
                    suggestionAction.setDescription( action.getLabel()+ " " + action.getTitle()+ " " + action.getDescription() );
                    Resource resource = getResource( carePlan, subAction.getResource() );
                    suggestionAction.setResource( resource );

                    suggestion.getActions().add( suggestionAction );
                    card.getSuggestions().add( suggestion );
                }

                if (!links.isEmpty()) {
                    card.setLinks(links);
                }
                cards.add(card);
            }
        }

        return cards;
    }

    private static Resource getResource(CarePlan carePlan, Reference reference) throws NotImplementedException {

        if ( reference != null && reference.getReference().startsWith( "#" ) ) {
            Optional<Resource> optRG = carePlan.getContained().stream()
                .filter( resource -> reference.getReference().substring( 1 ).equals( resource.getId() ) )
                .findFirst();
            if ( optRG.isPresent() ) {
                return optRG.get();
            }
        }
        throw new NotImplementedException( "Reference " + reference.getReference() + " does not point to a resource contained in Careplan" );
    }
}
