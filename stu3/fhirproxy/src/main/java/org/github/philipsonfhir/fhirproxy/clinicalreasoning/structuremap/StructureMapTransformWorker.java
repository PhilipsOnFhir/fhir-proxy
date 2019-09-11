package org.github.philipsonfhir.fhirproxy.clinicalreasoning.structuremap;

import ca.uhn.fhir.context.FhirContext;
import org.github.philipsonfhir.fhirproxy.clinicalreasoning.common.MyWorkerContext;
import org.hl7.fhir.dstu3.hapi.validation.DefaultProfileValidationSupport;
import org.hl7.fhir.dstu3.model.Resource;
import org.hl7.fhir.dstu3.model.ResourceFactory;
import org.hl7.fhir.dstu3.model.ResourceType;
import org.hl7.fhir.dstu3.model.StructureMap;
import org.hl7.fhir.dstu3.utils.StructureMapUtilities;
import org.hl7.fhir.exceptions.FHIRException;
import org.hl7.fhir.instance.model.api.IBaseResource;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

public class StructureMapTransformWorker {

    private static final DefaultProfileValidationSupport defaultProfileValidationSupport = new DefaultProfileValidationSupport();
    private static final MyWorkerContext hapiWorkerContext = new MyWorkerContext(  FhirContext.forDstu3(), defaultProfileValidationSupport );;

    public StructureMapTransformWorker() {
    }

    public IBaseResource doTransform(StructureMap structuredMap, Resource content) throws FHIRException {
        return doTransform(structuredMap, content, null );
    }
    public IBaseResource doTransform(StructureMap structuredMap, Resource content, Resource result) throws FHIRException {

        List<StructureMap.StructureMapStructureComponent> structureMapStructureComponents = structuredMap.getStructure().stream()
                .filter( structureMapStructureComponent -> structureMapStructureComponent.getMode().equals( StructureMap.StructureMapModelMode.PRODUCED)
                        || structureMapStructureComponent.getMode().equals( StructureMap.StructureMapModelMode.TARGET))
                .collect(Collectors.toList());

        if (structureMapStructureComponents.size() != 1){
            throw new FHIRException("StructureMap has more than one TARGET and PRODUCED");
        }

        if ( result == null ) {
            try {
                StructureMap.StructureMapStructureComponent structure = structureMapStructureComponents.get(0);
                String resourceUrl = structure.getUrl();
                String resourceName = resourceUrl.replace("http://hl7.org/fhir/StructureDefinition/", "");
                org.hl7.fhir.dstu3.model.ResourceType rt = null;
                for (org.hl7.fhir.dstu3.model.ResourceType resourceType : ResourceType.values()) {
                    if (resourceType.name().toLowerCase().equals(resourceName.toLowerCase())) {
                        rt = resourceType;
                    }
                }
                result = ResourceFactory.createResource(rt.name());
            } catch (Exception e) {
                throw new FHIRException("StructureMap can not instantiate result resource");
            }
        }
        // 3 process structure map

        // TODO Map should contain all structure maps.
        Map<String, StructureMap> mapTreeMap = new TreeMap<>();
        mapTreeMap.put(structuredMap.getId(), structuredMap);

        StructureMapUtilities structureMapUtilities = new StructureMapUtilities(hapiWorkerContext, mapTreeMap);

        structureMapUtilities.transform(null, content, structuredMap, result);
        return result;
    }

}
