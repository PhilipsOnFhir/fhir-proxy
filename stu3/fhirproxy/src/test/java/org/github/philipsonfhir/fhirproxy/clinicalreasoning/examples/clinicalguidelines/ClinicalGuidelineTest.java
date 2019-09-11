package org.github.philipsonfhir.fhirproxy.clinicalreasoning.examples.clinicalguidelines;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import com.google.common.io.Resources;
import org.apache.commons.io.IOUtils;
import org.github.philipsonfhir.fhirproxy.clinicalreasoning.examples.bmi.TestServer;
import org.hl7.fhir.dstu3.model.Base;
import org.hl7.fhir.dstu3.model.DomainResource;
import org.hl7.fhir.dstu3.model.Reference;
import org.hl7.fhir.dstu3.model.Resource;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.junit.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class ClinicalGuidelineTest {
    private TestServer testServer = new TestServer();

    @Test
    public void storeStu3Resources() throws IOException {
        IParser parser = FhirContext.forDstu3().newJsonParser();

        loadAndStore( parser, "FHIRv300_Model_Definition.json", "FHIRv300_Model_Definition");
        loadAndStore( parser, "FHIRHelpers_v300_Library.json", "FHIRHelpers-v300-Library");
        loadAndStore( parser, "CDC_Common_Logic_FHIRv300_Library.json", "CDC-Common-Logic-FHIRv300-Library");
        loadAndStore( parser, "CDS_Connect_Commons_for_FHIRv300_Library.json", "CDS_Connect_Commons_for_FHIRv300_Library");
        loadAndStore( parser, "Anthrax_Post_Exposure_Prophylaxis_FHIRv300_Library.json", "Anthrax-Post-Exposure-Prophylaxis-FHIRv300-Library");
        loadAndStore( parser, "PlanDefinition_AntimicrobialPregnant_FHIRv300.json", "PlanDefinition_AntimicrobialPregnant");
        loadAndStore( parser, "PlanDefinition_SecondVaccineDoseAndAntimicrobialNotPregnant_FHIRv300.json", "anthrax-post-exposure-prophylaxis");
//        IBaseResource res = parser.parseResource(Resources.getResource("examples/clinicalguidelines/Anthrax_Post_Exposure_Prophylaxis_FHIRv300_Library.json").openStream());
//        testServer.putResource((Resource) res, "Anthrax_Post_Exposure_Prophylaxis_FHIRv300_Library");


    }

    private void loadAndStore(IParser parser, String filename, String id) throws IOException {
        System.out.println(filename);
        IBaseResource res = parser.parseResource(Resources.getResource("examples/clinicalguidelines/"+filename).openStream());
        testServer.putResource((Resource) res, id.replace("_","-"));
    }

//    @Test
    public void storeResourcesAdvanced() throws IOException {
        IParser parser = FhirContext.forDstu3().newJsonParser();
        List<String> files = IOUtils.readLines(ClinicalGuidelineTest.class.getClassLoader()
                .getResourceAsStream("examples/clinicalguidelines"), StandardCharsets.UTF_8);

        List<String> readResources = new ArrayList<>();
        Map<String, IBaseResource> resourceCache = new HashMap<>();
        Set<String> storedCache = new HashSet<>();

        for ( String fileStr : files ){
            if ( fileStr.contains("v102")|| fileStr.contains("v400")){
                System.out.println("IGNORE      " + fileStr);
            } else {
               try {
                    IBaseResource res = parser.parseResource(Resources.getResource("examples/clinicalguidelines/" + fileStr).openStream());
                    System.out.println("PARSED      " + fileStr);
                    ReferenceProcessor.ensureId((DomainResource) res);
                    ReferenceProcessor.replaceReferencesInResource((DomainResource) res);
                    //                resourceCache.put( ((DomainResource) res).getResourceType()+"/"+((DomainResource) res).getIdBase(), res );
                    DomainResource dr = (DomainResource) res;
                    String key = dr.getResourceType() + "/" + dr.getIdElement().getIdPart();
                    resourceCache.put(key, res);
                } catch (ca.uhn.fhir.parser.DataFormatException dfe) {
                    System.out.println("PARSE ERROR " + fileStr);
                } catch (Exception e) {
                    System.out.println("OTHER ERROR " + fileStr);
                }
            }
//            System.out.println("STORED      "+fileStr);
        }

        System.out.println("Read-ALL----------------------------------------------------------------");
//        resourceCache.keySet().forEach(str -> System.out.println(str));
        Iterator<Map.Entry<String, IBaseResource>> it = resourceCache.entrySet().iterator();
        while (it.hasNext()){
            Map.Entry<String, IBaseResource> entry = it.next();
            System.out.println("PROCESS     "+entry.getKey());
            storeAdvanced( resourceCache, storedCache, entry.getKey(), entry.getValue() );
        }
        System.out.println("STORED ALL--------------------------------------------------------------");


    }

    private void storeAdvanced(Map<String, IBaseResource> resourceCache, Set<String> storedCache, String resourceRference, IBaseResource resource) {
        if( storedCache.contains(resourceRference) ) {
            System.out.println("ALREADY DONE " + resourceRference);
            return;
        }
        ReferenceProcessor referenceProcessor = new ReferenceProcessor((Base) resource);
        Iterator<Reference> it = referenceProcessor.getReferences().iterator();
        while (it.hasNext()) {
            Reference reference = it.next();
            IBaseResource referredResource = resourceCache.get(reference.getReference());
            if (referredResource != null) {
                storeAdvanced(resourceCache, storedCache, reference.getReference(), referredResource);
            } else {
                System.out.println("SKIPPED     " + reference.getReference());
            }
        }
        try {
            testServer.putResource((Resource) resource);
            storedCache.add(resourceRference);

            System.out.println("STORED      " + ((Base) resource).getIdBase());
        } catch (Exception e) {
            System.out.println("STORE ERROR " + ((Base) resource).getIdBase());
        }
    }

//    @Test
    public void storeResources() throws IOException {
        IParser parser = FhirContext.forDstu3().newJsonParser();
        List<String> files = IOUtils.readLines(ClinicalGuidelineTest.class.getClassLoader()
                .getResourceAsStream("examples/clinicalguidelines"), StandardCharsets.UTF_8);

        List<String> stored = new ArrayList<>();

        for ( String fileStr : files ){
            try {
                IBaseResource res = parser.parseResource(Resources.getResource("examples/clinicalguidelines/" + fileStr).openStream());
                System.out.println("PARSED      "+fileStr);
                ReferenceProcessor.ensureId((DomainResource) res);
                ReferenceProcessor.replaceReferencesInResource((DomainResource) res);
                testServer.putResource((Resource) res);
                stored.add(fileStr);
            } catch ( ca.uhn.fhir.parser.DataFormatException dfe ){
                System.out.println("PARSE ERROR "+fileStr);
            } catch ( Exception e){
                System.out.println("STORE ERROR "+fileStr);
            }
            System.out.println("STORED      "+fileStr);
        }

        System.out.println("Stored--------------------------------------------------------------------");
        stored.forEach(str -> System.out.println(str));

    }


}
