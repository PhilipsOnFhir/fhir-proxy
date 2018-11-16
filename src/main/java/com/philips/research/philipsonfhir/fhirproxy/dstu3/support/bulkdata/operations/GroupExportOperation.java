package com.philips.research.philipsonfhir.fhirproxy.dstu3.support.bulkdata.operations;

import com.philips.research.philipsonfhir.fhirproxy.dstu3.support.NotImplementedException;
import com.philips.research.philipsonfhir.fhirproxy.dstu3.support.bulkdata.fhir.PatientExportServer;
import com.philips.research.philipsonfhir.fhirproxy.dstu3.support.proxy.operation.FhirOperationCall;
import com.philips.research.philipsonfhir.fhirproxy.dstu3.support.proxy.operation.FhirResourceInstanceOperation;
import com.philips.research.philipsonfhir.fhirproxy.dstu3.support.proxy.service.FhirServer;
import org.hl7.fhir.dstu3.model.OperationOutcome;
import org.hl7.fhir.exceptions.FHIRException;
import org.hl7.fhir.instance.model.api.IBaseResource;

import java.util.Map;

public class GroupExportOperation extends FhirResourceInstanceOperation {
    private final String url;
    private final PatientExportServer patientExportServer;

    public GroupExportOperation(String url) {
        super( "Group", "$export" );
        this.url = url;
        this.patientExportServer = new PatientExportServer( new FhirServer(url));
    }

    @Override
    public FhirOperationCall createGetOperationCall(FhirServer fhirServer, Map<String, String> queryParams) throws NotImplementedException {
        throw new NotImplementedException();
    }

    @Override
    public FhirOperationCall createGetOperationCall(FhirServer fhirServer, String resourceId, Map<String, String> queryParams) {
        return new FhirOperationCall() {
            @Override
            public IBaseResource getResult() throws FHIRException, NotImplementedException {
                String outputFormat = queryParams.get( "outputFormat" );
                String since = queryParams.get( "since" );
                String type = queryParams.get( "type" );
                return patientExportServer.exportAllGroupData( resourceId, outputFormat, since, type );
            }

            @Override
            public String getDescription() {
                return null;
            }

            @Override
            public Map<String, OperationOutcome> getErrors() {
                return null;
            }
        };
    }

    @Override
    public FhirOperationCall createPostOperationCall(FhirServer fhirServer, String resourceId, IBaseResource parseResource, Map<String, String> queryParams) throws NotImplementedException {
        throw new NotImplementedException();
    }

    @Override
    public FhirOperationCall createPostOperationCall(FhirServer fhirServer, IBaseResource parseResource, Map<String, String> queryParams) throws NotImplementedException {
        throw new NotImplementedException();
    }

}
