package edu.escuelaing.arsw.medigo.shared.domain.model;
public record MedicationId(Long value) {
    public static MedicationId of(Long value) { return new MedicationId(value); }
}