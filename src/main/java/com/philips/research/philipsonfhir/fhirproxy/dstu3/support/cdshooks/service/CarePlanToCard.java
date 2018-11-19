package com.philips.research.philipsonfhir.fhirproxy.dstu3.support.cdshooks.service;

import com.philips.research.philipsonfhir.fhirproxy.dstu3.support.NotImplementedException;
import com.philips.research.philipsonfhir.fhirproxy.dstu3.support.cdshooks.model.*;
import org.hl7.fhir.dstu3.model.*;
import org.hl7.fhir.exceptions.FHIRException;
import org.omg.CORBA.IDLType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@EnableConfigurationProperties
public class CarePlanToCard {
    @Value("${fhirserver.url}")
    private String url;

    public static List<Card> convert(CarePlan carePlan, String fhirServerurl, String questionnaireServerUrl) throws FHIRException {
        CarePlanToCard carePlanToCard = new CarePlanToCard();

        List<Card> cards = new ArrayList<>();
        for (CarePlan.CarePlanActivityComponent activity : carePlan.getActivity()) {
            if (activity.getReferenceTarget() != null && activity.getReferenceTarget() instanceof RequestGroup) {
                RequestGroup requestGroup = (RequestGroup) activity.getReferenceTarget();
                cards = carePlanToCard.convert(carePlan, requestGroup, fhirServerurl, questionnaireServerUrl );
            }
            if (activity.getReference() != null && activity.getReference().getReference().startsWith( "#" ) ) {
                String reference = activity.getReference().getReference();
                Optional<Resource> optRG = carePlan.getContained().stream()
                    .filter( resource -> reference.substring( 1 ).equals( resource.getId() ) )
                    .findFirst();
                if (optRG.isPresent()) {
                    RequestGroup requestGroup = (RequestGroup) optRG.get();
                    cards = carePlanToCard.convert( carePlan, requestGroup, fhirServerurl, questionnaireServerUrl);
                }
            }
            else {
                throw new NotImplementedException( "Retrieval of repository stored request groups is not supported." );
            }
        }
        return cards;
    }

    private List<Card> convert( CarePlan carePlan, RequestGroup requestGroup, String fhirServerUrl, String questionnaireServerUrl ) throws FHIRException {
        List<Card> cards = new ArrayList<>();

        if (requestGroup.hasAction()) {
            for (RequestGroup.RequestGroupActionComponent action : requestGroup.getAction()) {
                Card card = new Card();
                card.setIndicator( "info" ); //TODO - r4 priority field

                Source source = new Source();
                source.setLabel( carePlan.getTitle() );
                if ( carePlan.hasDefinition() && !carePlan.getDefinition().isEmpty() ){
                    source.setUrl( "http://localhost:4200/fhir/"+carePlan.getDefinition().get(0).getReference()+"?fs="+fhirServerUrl);
                }

                card.setSource(source);


                // basic
                if (action.hasTitle()) {
                    card.setSummary(action.getTitle());
                }
                if (action.hasDescription()) {
                    card.setDetail(action.getDescription());
                }

                // suggestions
                // TODO - uuid
                for ( RequestGroup.RequestGroupActionComponent subAction : action.getAction() ){
                    if ( subAction.hasType() && subAction.getType().getCode().equals("launch")){
                        Link link = new Link();
                        link.setType("absolute");
                        link.setLabel(
                                (subAction.hasLabel()? subAction.getLabel() : (subAction.hasLabel()&&subAction.hasTitle()?" ":"")) +
                                (subAction.hasTitle()?subAction.getTitle():(subAction.hasDescription()?" ":""))+
                                (subAction.hasDescription()?subAction.getDescription():"")
                        );
                        if ( subAction.hasResource()) {
                            // TODO make these values dynamic
                            if ( subAction.getResource().getReference().startsWith(ResourceType.Questionnaire.name())){
                                String url = questionnaireServerUrl+subAction.getResource().getReference()+"?fs="+fhirServerUrl;
                                if ( carePlan.hasSubject() ){
                                    String prefix="Patient/";
                                    url+="&patient="+carePlan.getSubject().getReference();
                                }
                                link.setUrl( url );
                            } else {
                                throw new FHIRException("Invalid value in Questionnaire reference extension "+ subAction.getResource().getReference());
                            }
                        } else if (subAction.hasExtension()) {
                            for (Extension extension : subAction.getExtension()) {
                                if (extension.getValue() instanceof Attachment) {
                                    Attachment attachment = (Attachment) extension.getValue();
                                    if (attachment.hasUrl()) {
                                        link.setUrl(attachment.getUrl());
                                    }
                                    if (attachment.hasTitle() && link.getLabel() != null && !link.getLabel().isEmpty()) {
                                        link.setLabel(attachment.getTitle());
                                    }
                                    link.setType("absolute");
                                }
                            }
                        } else {
                                throw new FHIRException("Invalid action with code launch: " + subAction.getTitle());
                        }
                        card.getLinks().add(link);
                    } else {
                        Suggestion suggestion = new Suggestion();
                        suggestion.setLabel(action.getLabel());

                        if (  (!subAction.hasAction() || subAction.getAction().isEmpty()) && subAction.hasType() ){
                            Action suggestionAction = createActionFromRequestGroupAction(carePlan, subAction);
                            suggestion.getActions().add( suggestionAction );
                        } else if ( subAction.hasAction() && !subAction.getAction().isEmpty() && !subAction.hasType() ) {
                            for ( RequestGroup.RequestGroupActionComponent rqAction: subAction.getAction() ) {
                                Action suggestionAction = createActionFromRequestGroupAction(carePlan, rqAction);
                                suggestion.getActions().add(suggestionAction);
                            }
                        } else {
                            throw new FHIRException( "error processing subaction "+subAction.getTitle()+ " "+ subAction.getDescription());
                        }

                        card.getSuggestions().add(suggestion);
                    }
                }

                // links
                // TODO determine correct way to address this.
//                if (action.hasExtension()) {
//                    for (Extension extension : action.getExtension()) {
//                        Link link = new Link();
//
//                        if (extension.getValue() instanceof Attachment) {
//                            Attachment attachment = (Attachment) extension.getValue();
//                            if (attachment.hasUrl()) {
//                                link.setUrl(attachment.getUrl());
//                            }
//                            if (attachment.hasTitle()) {
//                                link.setLabel(attachment.getTitle());
//                            }
//                            link.setType("absolute");
//                        } else if (extension.getValue() instanceof Reference) {
//                            Reference reference = (Reference) extension.getValue();
//                            // TODO make these values dynamic
//                            if ( reference.getReference().startsWith(ResourceType.Questionnaire.name())){
//                                link.setType("absolute");
//                                link.setLabel(reference.getDisplay());
//                                link.setUrl( questionnaireServerUrl+reference.getReference()+"?fs="+fhirServerUrl);
//                            } else {
//                                throw new FHIRException("Invalid value in Questionnaire reference extension "+ reference.getReference());
//                            }
//                        } else {
//                            throw new FHIRException("Invalid link extension type: " + extension.getValue().fhirType());
//                        }
//
//                        links.add(link);
//                    }
//                    card.setLinks(links);
//                }

                cards.add(card);
            }
        }

        return cards;
    }

    private Action createActionFromRequestGroupAction( CarePlan carePlan, RequestGroup.RequestGroupActionComponent requestGroupActionAction) throws NotImplementedException {
        Action action = new Action();
        action.setDescription(
                (requestGroupActionAction.hasLabel()?requestGroupActionAction.getLabel() + " ":"") +
                (requestGroupActionAction.hasTitle()?requestGroupActionAction.getTitle() + " ":"") +
                (requestGroupActionAction.hasDescription()?requestGroupActionAction.getDescription():"")
        );
        action.setType(Action.ActionType.valueOf(requestGroupActionAction.getType().getCode()));
        Resource resource = getResource(carePlan, requestGroupActionAction.getResource());
        action.setResource(resource);

        return action;
    }

    private Resource getResource(CarePlan carePlan, Reference reference) throws NotImplementedException {

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
