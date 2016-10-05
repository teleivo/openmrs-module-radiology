package org.openmrs.module.radiology.report.template;

public interface ValidationEngine<T> {

    /**
     * Validates a subject collecting the results in ....
     *
     * @param subject the subject to be validated
     * @return the validation result
     */
    public ValidationResult run(T subject);
}
