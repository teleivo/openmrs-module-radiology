/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.radiology.report.template;

import java.util.ArrayList;
import java.util.List;

public class ValidationResult {
    
    
    private List<MrrtReportTemplateStructureViolation> violations;
    
    public List<MrrtReportTemplateStructureViolation> getViolations() {
        return violations;
    }
    
    public ValidationResult() {
        violations = new ArrayList<>();
    }
    
    public void addViolation(String description, String messageCode) {
        MrrtReportTemplateStructureViolation ruleViolation =
                new MrrtReportTemplateStructureViolation(description, messageCode);
        violations.add(ruleViolation);
    }
    
    public void addViolation(MrrtReportTemplateStructureViolation mrrtRuleViolation) {
        violations.add(mrrtRuleViolation);
    }
    
    public boolean isNotEmpty() {
        return !violations.isEmpty();
    }
}
