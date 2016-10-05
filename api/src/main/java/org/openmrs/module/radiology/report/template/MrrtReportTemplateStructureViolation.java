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

/**
 * Represents a violation of the report template structure defined by the IHE Management of Radiology Report Templates (MRRT).
 */
public class MrrtReportTemplateStructureViolation {
    
    
    private final String description;
    
    private final String messageCode;
    
    public String getDescription() {
        return description;
    }
    
    public String getMessageCode() {
        return messageCode;
    }
    
    public MrrtReportTemplateStructureViolation(String description, String messageCode) {
        this.description = description;
        this.messageCode = messageCode;
    }
    
    @Override
    public String toString() {
        return description;
    }
}
