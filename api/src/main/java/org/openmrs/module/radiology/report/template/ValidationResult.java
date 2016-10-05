package org.openmrs.module.radiology.report.template;

import java.util.ArrayList;
import java.util.List;

public class ValidationResult {

    List<MrrtRuleViolation> violations;

    public ValidationResult() {
        violations = new ArrayList<>();
    }

    public void addViolation(String description, int columNumber, int lineNumber) {
        MrrtRuleViolation ruleViolation = new MrrtRuleViolation(description, columNumber, lineNumber);
        violations.add(ruleViolation);
    }
}
