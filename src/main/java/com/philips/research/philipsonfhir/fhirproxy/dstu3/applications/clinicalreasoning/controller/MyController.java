package com.philips.research.philipsonfhir.fhirproxy.dstu3.applications.clinicalreasoning.controller;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import com.philips.research.philipsonfhir.fhirproxy.dstu3.support.NotImplementedException;
import com.philips.research.philipsonfhir.fhirproxy.dstu3.support.clinicalreasoning.*;
import com.philips.research.philipsonfhir.fhirproxy.dstu3.support.proxy.controller.SampleFhirGateway;
import com.philips.research.philipsonfhir.fhirproxy.dstu3.support.proxy.service.FhirServer;
import org.hl7.fhir.exceptions.FHIRException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.RestController;

import javax.net.ssl.*;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

@RestController
@Configuration
public class MyController extends SampleFhirGateway {
    private static final String url = "http://localhost:9500/baseDstu3";
    private String prop = null;

    public MyController() throws FHIRException, NotImplementedException {
        super();
//        setFhirServer(url);
    }

    @Value("${fhirserver.url}")
    public void setUrl( String urlll ) throws FHIRException {
        System.out.println("\n==fhirserver.url=============== " + urlll + "++++++++++++++++++");
        setFhirServer(urlll);
    }

    private void setFhirServer(String urlll) throws FHIRException {
        System.out.println("\n==Set fhirserver=============== " + urlll + "++++++++++++++++++");

        trustEveryone();

        this.fhirServer = new FhirServer(urlll);
        FhirContext ourCtx = this.fhirServer.getCtx();
        IGenericClient client = this.fhirServer.getCtx().newRestfulGenericClient(url);

        this.fhirServer.getFhirOperationRepository().registerOperation(new MeasureEvaluationOperation(client));
        this.fhirServer.getFhirOperationRepository().registerOperation(new StructureMapTransformOperation(url, client));
        this.fhirServer.getFhirOperationRepository().registerOperation(new ActivityDefinitionApplyOperation(url));
        this.fhirServer.getFhirOperationRepository().registerOperation(new PlanDefinitionApplyOperation(url));
        this.fhirServer.getFhirOperationRepository().registerOperation(new QuestionnairePopulateOperation(url));
        System.out.println("\n==Set fhirserver=============== " + urlll + "+++++DONE+++++++++++++");
    }

    private void trustEveryone() {
        try {
            HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier(){
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }});
            SSLContext context = SSLContext.getInstance("TLS");
            context.init(null, new X509TrustManager[]{new X509TrustManager(){
                public void checkClientTrusted(X509Certificate[] chain,
                                               String authType) throws CertificateException {}
                public void checkServerTrusted(X509Certificate[] chain,
                                               String authType) throws CertificateException {}
                public X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[0];
                }}}, new SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(
                    context.getSocketFactory());
        } catch (Exception e) { // should never happen
            e.printStackTrace();
        }

    }
}

