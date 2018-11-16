package com.philips.research.philipsonfhir.fhirproxy.dstu3.support.clinicalreasoning;

import ca.uhn.fhir.rest.client.api.IGenericClient;
import com.philips.research.philipsonfhir.fhirproxy.dstu3.support.NotImplementedException;
import com.philips.research.philipsonfhir.fhirproxy.dstu3.support.clinicalreasoning.helpers.DateHelper;
import com.philips.research.philipsonfhir.fhirproxy.dstu3.support.clinicalreasoning.processor.MeasureEvaluationProcessor;
import com.philips.research.philipsonfhir.fhirproxy.dstu3.support.proxy.operation.FhirOperationCall;
import com.philips.research.philipsonfhir.fhirproxy.dstu3.support.proxy.operation.FhirResourceInstanceOperation;
import com.philips.research.philipsonfhir.fhirproxy.dstu3.support.proxy.service.FhirServer;
import org.hl7.fhir.dstu3.model.Measure;
import org.hl7.fhir.dstu3.model.OperationOutcome;
import org.hl7.fhir.dstu3.model.ResourceType;
import org.hl7.fhir.exceptions.FHIRException;
import org.hl7.fhir.instance.model.api.IBaseResource;

import java.util.Map;

public class MeasureEvaluationOperation extends FhirResourceInstanceOperation {
    private final IGenericClient client;

    public MeasureEvaluationOperation(IGenericClient client ){
        super( ResourceType.Measure.name(), "$evaluate-measure" );
        this.client = client;
    }

    @Override
    public FhirOperationCall createGetOperationCall(FhirServer fhirServer, Map<String, String> queryparams) throws NotImplementedException {
        throw new NotImplementedException();
    }

    @Override
    public FhirOperationCall createGetOperationCall(FhirServer fhirServer, String resourceId, Map<String, String> queryParams) {
        return new FhirOperationCall() {
            @Override
            public IBaseResource getResult() throws FHIRException, NotImplementedException {
                String periodStart = queryParams.get("periodStart");
                String periodEnd   = queryParams.get( "periodEnd" );
                String measureRef  = queryParams.get( "measure" );
                String reportType  = queryParams.get( "reportType" );
                String patient     = queryParams.get( "patient" );
                String practitioner = queryParams.get("practitioner");
                String lastReceivedOn = queryParams.get( "lastReceievedOn" );

                String resId = ( measureRef!=null? measureRef: resourceId );
                Measure measure = client.read().resource( Measure.class ).withId( resourceId ).execute();

                // resolve the measurement period
//                        Interval measurementPeriod =
//                            new org.opencds.cqf.cql.runtime.Interval(
//                                DateHelper.resolveRequestDate(periodStart, true), true,
//                                DateHelper.resolveRequestDate(periodEnd, false), true
//                            );

//                        Period period = new Period();
//                        period.setStart( DateHelper.resolveRequestDate(periodStart, true) );
//                        period.setEnd( DateHelper.resolveRequestDate(periodEnd, false) );

                MeasureEvaluationProcessor measureEvaluationProcesso =
                    new MeasureEvaluationProcessor( client, null,
                        DateHelper.resolveRequestDate(periodStart, true),
                        DateHelper.resolveRequestDate( periodEnd,false ));

                return measureEvaluationProcesso.getResult();
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
