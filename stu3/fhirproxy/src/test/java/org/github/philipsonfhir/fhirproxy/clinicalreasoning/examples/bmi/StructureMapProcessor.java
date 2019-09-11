//package org.github.philipsonfhir.fhirproxy.clinicalreasoning.examples.bmi;
//
//import ca.uhn.fhir.context.FhirContext;
//import org.hl7.fhir.dstu3.hapi.ctx.MyWorkerContext;
//import org.hl7.fhir.dstu3.hapi.validation.DefaultProfileValidationSupport;
//import org.hl7.fhir.dstu3.model.Resource;
//import org.hl7.fhir.dstu3.model.StructureMap;
//import org.hl7.fhir.dstu3.utils.StructureMapUtilities;
//import org.hl7.fhir.exceptions.FHIRException;
//
//import java.util.List;
//import java.util.Map;
//import java.util.TreeMap;
//import java.util.stream.Collectors;
//
//public class StructureMapProcessor {
//    public static Resource transform(FhirContext ourCtxt, StructureMap structureMap, Resource source) throws FHIRException {
//
//        Resource content = source;
//
//        // 2 Create result
//        List<StructureMap.StructureMapStructureComponent> structureMapStructureComponents = structureMap.getStructure().stream()
//                .filter( structureMapStructureComponent -> structureMapStructureComponent.getMode().equals(StructureMap.StructureMapModelMode.PRODUCED)
//                        || structureMapStructureComponent.getMode().equals(StructureMap.StructureMapModelMode.TARGET))
//                .collect(Collectors.toList());
//
//        if (structureMapStructureComponents.size() != 1){
//            throw new FHIRException("StructureMap has more than one TARGET and PRODUCED");
//        }
//
//        Resource result = null;
//        Class resultType = null;
//        try {
//            StructureMap.StructureMapStructureComponent structure = structureMapStructureComponents.get(0);
//            String resourceUrl = structure.getUrl();
//            String resourceName = resourceUrl.replace("http://hl7.org/fhir/StructureDefinition/","");
//            resourceName = resourceName.substring(0,1).toUpperCase()+resourceName.substring(1);
//            resultType = Class.forName("org.hl7.fhir.dstu3.model." + resourceName);
//            result = (Resource) resultType.newInstance();
//        } catch (Exception e) {
//            throw new FHIRException("StructureMap can not instantiate result resource");
//        }
//
//        // 3 process structure map
////        MockWorker myWorker = new MockWorker( this.provider );
//        MyWorkerContext hapiWorkerContext = new MyWorkerContext(ourCtxt, new DefaultProfileValidationSupport());
////        HapiWorkerContext hapiWorkerContext = new HapiWorkerContext(ourCtxt, ourCtxt.newValidator());
//
//        // TODO Map should contain all structure maps.
//        Map<String, StructureMap> mapTreeMap = new TreeMap<>();
////        mapTreeMap.put(structureMap.getUrl(), structureMap);
//        mapTreeMap.put(structureMap.getId(), structureMap);
//
//        StructureMapUtilities structureMapUtilities = new StructureMapUtilities(hapiWorkerContext, mapTreeMap);
//
//        structureMapUtilities.transform(null, content, structureMap, result);
//        return result;
//    }
//}
