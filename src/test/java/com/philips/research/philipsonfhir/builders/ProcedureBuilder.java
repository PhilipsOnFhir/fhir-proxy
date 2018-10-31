package com.philips.research.philipsonfhir.builders;

import org.hl7.fhir.dstu3.model.*;
import org.hl7.fhir.dstu3.model.Procedure.ProcedurePerformerComponent;
import org.hl7.fhir.dstu3.model.Procedure.ProcedureStatus;
import org.hl7.fhir.exceptions.FHIRException;
import org.hl7.fhir.exceptions.FHIRFormatError;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ProcedureBuilder extends BaseBuilder<Procedure> implements IBuilderHelper {

    public ProcedureBuilder() {
        super(new Procedure());
    }

    public ProcedureBuilder(String id, Patient patient, Practitioner practitioner, String procedureDateString) throws FHIRFormatError {
        this();
        String status = "completed";

        buildId("Procedure-" + id + "-" + patient.getId());
        buildStatus(status);
        Date performanceStart = createDate(procedureDateString);
        Date performanceEnd   = new Date(); performanceEnd.setTime( createDate(procedureDateString).getTime()+60000*15);

        buildPerformedPeriod(performanceStart, performanceEnd);
        buildSubject(patient);
        buildPerformer(practitioner);
    }

    public ProcedureBuilder buildId(String id) {
        complexProperty.setId(id);
        return this;
    }

    public ProcedureBuilder buildIdentifier(List<Identifier> identifiers) {
        complexProperty.setIdentifier(identifiers);
        return this;
    }

    public ProcedureBuilder buildIdentifier(Identifier identifier) {
        if (!complexProperty.hasIdentifier()) {
            complexProperty.setIdentifier(new ArrayList<>());
        }

        complexProperty.addIdentifier(identifier);
        return this;
    }

    public ProcedureBuilder buildStatus(String procedureStatus) {
        ProcedureStatus status;

        try {
            status = ProcedureStatus.fromCode(procedureStatus);
        } catch (FHIRException e) {
            status = ProcedureStatus.valueOf(procedureStatus);
        }

        complexProperty.setStatus(status);
        return this;
    }

    public ProcedureBuilder buildCode(String code, String system, String display) {
        complexProperty.setCode(buildCodeableConcept(code, system, display));
        return this;
    }

    public ProcedureBuilder buildCategory(String code, String system) {
        complexProperty.setCategory(buildCodeableConcept(code, system));
        return this;
    }

    public ProcedureBuilder buildPerformedPeriod(Date start, Date end) throws FHIRFormatError {
        PeriodBuilder periodBuilder = new PeriodBuilder();

        periodBuilder.buildStart(start);
        periodBuilder.buildEnd(end);

        complexProperty.setPerformed(periodBuilder.build());
        return this;
    }

    public ProcedureBuilder buildSubject(Patient patient) {
        Reference reference = new Reference(patient);

        complexProperty.setSubject(reference);
        return this;
    }

    public ProcedureBuilder buildPerformer(Practitioner practitioner) {
        Reference reference = new Reference(practitioner);

        ProcedurePerformerComponent performerComponent = new ProcedurePerformerComponent();
        List<ProcedurePerformerComponent> performers = new ArrayList<>();

        performerComponent.setActor(reference);
        performers.add(performerComponent);

        complexProperty.setPerformer(performers);
        return this;
    }
}