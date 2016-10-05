package org.openmrs.module.radiology.report.template;

public class MrrtRuleViolation {

    private final String description;

    private final int columnNumber;

    private final int lineNumber;

    public MrrtRuleViolation(String description, int columnNumber, int lineNumber){
        this.description = description;
        this.columnNumber = columnNumber;
        this.lineNumber = lineNumber;
    }
}
