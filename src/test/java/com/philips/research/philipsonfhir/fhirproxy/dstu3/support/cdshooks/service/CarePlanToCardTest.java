package com.philips.research.philipsonfhir.fhirproxy.dstu3.support.cdshooks.service;

import com.philips.research.philipsonfhir.fhirproxy.dstu3.support.NotImplementedException;
import com.philips.research.philipsonfhir.fhirproxy.dstu3.support.cdshooks.model.Card;
import org.hl7.fhir.dstu3.model.*;
import org.hl7.fhir.exceptions.FHIRException;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

public class CarePlanToCardTest {
    @Test
    public void emptyCarePlan() throws NotImplementedException, FHIRException {
        CarePlan carePlan = new CarePlan();
        List<Card> cardList = CarePlanToCard.convert( carePlan, null, null );

        assertNotNull( cardList );
        assertTrue( cardList.isEmpty() );
    }

    @Test
    public void emptyRequestGroup() throws NotImplementedException, FHIRException {
        CarePlan carePlan = new CarePlan();
        RequestGroup requestGroup = (RequestGroup) new RequestGroup().setId( "someId" );
        carePlan.addActivity( new CarePlan.CarePlanActivityComponent()
            .setReference( new Reference(  ).setReference( "#"+requestGroup.getId() ) )
        );
        List<Card> cardList = CarePlanToCard.convert( carePlan, null, null );

        assertNotNull( cardList );
        assertTrue( cardList.isEmpty() );
    }

    @Test
    public void emptyActionRequestGroup() throws FHIRException {
        CarePlan carePlan = new CarePlan();
        RequestGroup requestGroup = (RequestGroup) new RequestGroup()
            .addAction( new RequestGroup.RequestGroupActionComponent() )
            .setId( "someId" );

        carePlan
            .addActivity( new CarePlan.CarePlanActivityComponent()
                .setReference( new Reference(  ).setReference( "#"+requestGroup.getId() ) )
            )
            .addContained( requestGroup );
        List<Card> cardList = CarePlanToCard.convert( carePlan, null, null );

        assertNotNull( cardList );
        assertTrue( cardList.isEmpty() );
    }

    @Test
    public void depth1ActionRequestGroup() throws FHIRException {
        CarePlan carePlan = new CarePlan();
        RequestGroup.RequestGroupActionComponent action = new RequestGroup.RequestGroupActionComponent()
            .setTitle( "some title" )
            .setDescription( "description" );
        RequestGroup requestGroup = (RequestGroup) new RequestGroup()
            .addAction( action )
            .setId( "someId" );
        carePlan.addActivity( new CarePlan.CarePlanActivityComponent()
            .setReference( new Reference(  ).setReference( "#"+requestGroup.getId() ) )
        )
            .addContained( requestGroup )
        ;
        List<Card> cardList = CarePlanToCard.convert( carePlan, null, null );

        assertNotNull( cardList );
        assertFalse( cardList.isEmpty() );
        Card card = cardList.get( 0 );
        assertEquals( action.getTitle(), card.getSummary() );
        assertEquals( action.getDescription(), card.getDetail() );
    }

    @Test
    public void suggestionActionRequestGroup() throws FHIRException {
        CarePlan carePlan = new CarePlan();
        RequestGroup.RequestGroupActionComponent rootAction = new RequestGroup.RequestGroupActionComponent()
                .setTitle( "some title" )
                .setDescription( "description" )
                .setSelectionBehavior( RequestGroup.ActionSelectionBehavior.ATMOSTONE );

        Resource someResource = new Observation().setId( "resourceId" );
        RequestGroup.RequestGroupActionComponent suggestion1Action = new RequestGroup.RequestGroupActionComponent()
                .setTitle( "suggestion1" )
                .setDescription( "why suggestion is needed" )
                .setType( new Coding( ).setSystem( "http://hl7.org/fhir/ValueSet/action-type" ).setCode( "create" ) )
                .setResource( new Reference(  ).setReference( "#"+someResource.getId() ) );
        carePlan.addContained( someResource );
        rootAction.addAction( suggestion1Action );

        RequestGroup requestGroup = (RequestGroup) new RequestGroup()
                .addAction( rootAction )
                .setId( "someId" );
        carePlan.addActivity( new CarePlan.CarePlanActivityComponent()
                .setReference( new Reference(  ).setReference( "#"+requestGroup.getId() ) )
        )
                .addContained( requestGroup )
        ;
        List<Card> cardList = CarePlanToCard.convert( carePlan, null, null );

        assertNotNull( cardList );
        assertFalse( cardList.isEmpty() );
        Card card = cardList.get( 0 );
        assertEquals( rootAction.getTitle(), card.getSummary() );
        assertEquals( rootAction.getDescription(), card.getDetail() );
        assertFalse( card.getSuggestions().isEmpty() );
    }

    @Test
    public void linkActionRequestGroup() throws FHIRException {
        CarePlan carePlan = new CarePlan();

        Attachment attachement = new Attachment()
                .setUrl("http://localhost:4200/questionnaire/382429?fs=http://localhost:8080/context/435224/fhir&patient=myId")
                .setTitle("fill in questionnaire")
                .setContentType("text/plain");

        RequestGroup.RequestGroupActionComponent rootAction = (RequestGroup.RequestGroupActionComponent) new RequestGroup.RequestGroupActionComponent()
                .setTitle( "some title" )
                .setDescription( "description" )
                .setSelectionBehavior( RequestGroup.ActionSelectionBehavior.ATMOSTONE )
                .addAction((RequestGroup.RequestGroupActionComponent) new RequestGroup.RequestGroupActionComponent()
                        .setType(new Coding().setCode("launch"))
                        .addExtension( new Extension()
                                .setUrl("http://research.philips.com/connect/cdshookslink")
                                .setValue( attachement )
                        )
                );

        RequestGroup requestGroup = (RequestGroup) new RequestGroup()
                .addAction( rootAction )
                .setId( "someId" );

        carePlan.addActivity( new CarePlan.CarePlanActivityComponent()
                .setReference( new Reference(  ).setReference( "#"+requestGroup.getId() ) )
                )
                .addContained( requestGroup )
        ;
        String fhirServerurl = "http://fhirserver";
        String questionnaireServerUrl = "http://questionnaireserver";
        List<Card> cardList = CarePlanToCard.convert( carePlan, fhirServerurl, questionnaireServerUrl );

        assertNotNull( cardList );
        assertFalse( "must have cards", cardList.isEmpty() );
        Card card = cardList.get( 0 );
        assertEquals( "title differs", rootAction.getTitle(), card.getSummary() );
        assertEquals( "description differs", rootAction.getDescription(), card.getDetail() );
        assertTrue( "suggestions present", card.getSuggestions().isEmpty() );
        assertFalse("link not present", card.getLinks().isEmpty());
        assertEquals( attachement.getUrl(), card.getLinks().get(0).getUrl());
    }

    @Test
    public void questionnaireActionRequestGroup() throws FHIRException {
        CarePlan carePlan = new CarePlan();

        Reference reference = new Reference()
            .setReference("Questionnaire/3249329042")
            .setDisplay("Fill in Questionnaire");

        RequestGroup.RequestGroupActionComponent rootAction = (RequestGroup.RequestGroupActionComponent) new RequestGroup.RequestGroupActionComponent()
                .setTitle( "some title" )
                .setDescription( "description" )
                .setSelectionBehavior( RequestGroup.ActionSelectionBehavior.ATMOSTONE )
                .addAction( new RequestGroup.RequestGroupActionComponent()
                        .setTitle("test")
                        .setResource(reference)
                        .setType(new Coding().setCode("launch"))
                );
//                .addExtension( new Extension()
//                        .setUrl("http://research.philips.com/connect/questionnaireReference")
//                        .setValue( reference )
//                );

        RequestGroup requestGroup = (RequestGroup) new RequestGroup()
                .addAction( rootAction )
                .setId( "someId" );

        carePlan.addActivity( new CarePlan.CarePlanActivityComponent()
                .setReference( new Reference(  ).setReference( "#"+requestGroup.getId() ) )
        )
                .addContained( requestGroup )
        ;

        String fhirServerurl = "http://fhirserver/";
        String questionairreServerUrl = "http://fhirserver/";
        List<Card> cardList = CarePlanToCard.convert( carePlan, fhirServerurl, questionairreServerUrl );

        assertNotNull( cardList );
        assertFalse( "must have cards", cardList.isEmpty() );
        Card card = cardList.get( 0 );
        assertEquals( "title differs", rootAction.getTitle(), card.getSummary() );
        assertEquals( "description differs", rootAction.getDescription(), card.getDetail() );
        assertTrue( "suggestions present", card.getSuggestions().isEmpty() );
        assertFalse("link not present", card.getLinks().isEmpty());
        assertTrue( card.getLinks().get(0).getUrl().contains( reference.getReference()));
        assertTrue( card.getLinks().get(0).getUrl().contains( fhirServerurl));
        assertEquals( rootAction.getAction().get(0).getTitle(), card.getLinks().get(0).getLabel() );
        assertEquals( "absolute", card.getLinks().get(0).getType());
    }
}