package com.philips.research.philipsonfhir.fhirproxy.support.proxy.operation;

import com.philips.research.philipsonfhir.fhirproxy.support.NotImplementedException;
import com.philips.research.philipsonfhir.fhirproxy.support.proxy.service.FhirServer;
import org.hl7.fhir.dstu3.model.Patient;
import org.hl7.fhir.exceptions.FHIRException;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class FhirOperationTest {

    @Test
    public void testResourceOperation() throws FHIRException, NotImplementedException {

        IBaseResource iBaseResource = new Patient();
        String operationName = "$myOperationName";
        Map<String, String> queryparams = null;
        String resourceType = "Patient";
        FhirServer fhirServerMock = mock( FhirServer.class );

        when( fhirServerMock.getResourceOperation( resourceType, operationName, queryparams ) ).thenReturn( iBaseResource );
        assertEquals( "Operation does not return a result.", iBaseResource, fhirServerMock.getResourceOperation( resourceType, operationName, queryparams ) );

        GenericFhirResourceOperation operation = new GenericFhirResourceOperation( resourceType, operationName );
        FhirOperationCall call = operation.createOperationCall( fhirServerMock,  null );

        assertNotEquals( null, call.getDescription() );
        IBaseResource result = call.getResult();

        assertEquals( "Operation does not return a result.", iBaseResource, result );
    }

    @Test
    public void testResourceInstanceOperation() throws FHIRException, NotImplementedException {

        IBaseResource iBaseResource = new Patient();
        String operationName = "$myInstanceOperationName";
        String resourceId = "someId";
        Map<String, String> queryparams = null;
        String resourceType = "Group";
        FhirServer fhirServerMock = mock( FhirServer.class );

        when( fhirServerMock.getResource( resourceType, resourceId, operationName, queryparams ) ).thenReturn( iBaseResource );

        GenericFhirResourceInstanceOperation operation = new GenericFhirResourceInstanceOperation( resourceType, operationName );
        FhirOperationCall call = operation.createOperationCall( fhirServerMock, resourceId, queryparams );

        assertNotEquals( null, call.getDescription() );
        IBaseResource result = call.getResult();

        assertEquals( "Operation does not return a result.", iBaseResource, result );
    }

    @Test
    public void testOperationRegistration() throws FHIRException {

        String operationName = "$myInstanceOperationName1";
        String resourceId = "someId";
        Map<String, String> queryparams = null;
        String resourceType = "Group";
        FhirServer fhirServerMock = mock( FhirServer.class );
        GenericFhirResourceInstanceOperation fhirOperation  = mock( GenericFhirResourceInstanceOperation.class );
        GenericFhirResourceInstanceOperationCall call = mock( GenericFhirResourceInstanceOperationCall.class );

        when( fhirOperation.getOperationName()).thenReturn( operationName );
        when( fhirOperation.getResourceType()).thenReturn( resourceType );
        when( fhirOperation.createOperationCall( fhirServerMock, resourceId, queryparams )).thenReturn( call );

        FhirOperationRepository fhirOperationService = new FhirOperationRepository();

        fhirOperationService.registerOperation( fhirOperation );

        FhirOperationCall retOp = fhirOperationService.getGetOperation( fhirServerMock, resourceType, resourceId, operationName, queryparams );
        assertEquals( call, retOp );
    }
}