package com.philips.research.philipsonfhir.fhirproxy.dstu3.support.cdshooks.service;

import com.philips.research.philipsonfhir.fhirproxy.dstu3.support.NotImplementedException;
import com.philips.research.philipsonfhir.fhirproxy.dstu3.support.cdshooks.model.Card;
import org.hl7.fhir.dstu3.model.*;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

public class CarePlanToCardTest {
    @Test
    public void emptyCarePlan() throws NotImplementedException {
        CarePlan carePlan = new CarePlan();
        List<Card> cardList = CarePlanToCard.convert( carePlan );

        assertNotNull( cardList );
        assertTrue( cardList.isEmpty() );
    }

    @Test
    public void emptyRequestGroup() throws NotImplementedException {
        CarePlan carePlan = new CarePlan();
        RequestGroup requestGroup = (RequestGroup) new RequestGroup().setId( "someId" );
        carePlan.addActivity( new CarePlan.CarePlanActivityComponent()
            .setReference( new Reference(  ).setReference( "#"+requestGroup.getId() ) )
        );
        List<Card> cardList = CarePlanToCard.convert( carePlan );

        assertNotNull( cardList );
        assertTrue( cardList.isEmpty() );
    }

    @Test
    public void emptyActionRequestGroup() throws NotImplementedException {
        CarePlan carePlan = new CarePlan();
        RequestGroup requestGroup = (RequestGroup) new RequestGroup()
            .addAction( new RequestGroup.RequestGroupActionComponent() )
            .setId( "someId" );

        carePlan
            .addActivity( new CarePlan.CarePlanActivityComponent()
                .setReference( new Reference(  ).setReference( "#"+requestGroup.getId() ) )
            )
            .addContained( requestGroup );
        List<Card> cardList = CarePlanToCard.convert( carePlan );

        assertNotNull( cardList );
        assertTrue( cardList.isEmpty() );
    }

    @Test
    public void depth1ActionRequestGroup() throws NotImplementedException {
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
        List<Card> cardList = CarePlanToCard.convert( carePlan );

        assertNotNull( cardList );
        assertFalse( cardList.isEmpty() );
        Card card = cardList.get( 0 );
        assertEquals( action.getTitle(), card.getSummary() );
        assertEquals( action.getDescription(), card.getDetail() );
    }

    @Test
    public void suggestionActionRequestGroup() throws NotImplementedException {
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
        List<Card> cardList = CarePlanToCard.convert( carePlan );

        assertNotNull( cardList );
        assertFalse( cardList.isEmpty() );
        Card card = cardList.get( 0 );
        assertEquals( rootAction.getTitle(), card.getSummary() );
        assertEquals( rootAction.getDescription(), card.getDetail() );
        assertFalse( card.getSuggestions().isEmpty() );
    }
}