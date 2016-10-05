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

import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

/**
 * Validates <meta> tags of an Mrrt Report Template.
 */
public class MetaTagsValidationEngine implements ValidationEngine<Elements> {
    
    
    public static String META_CHARSET = "meta[charset]";
    
    public static String META_NAME = "meta[name]";
    
    List<ValidationRule<Elements>> rules;
    
    public MetaTagsValidationEngine() {
        rules = new ArrayList<>();
        rules.add(new ElementsExpressionValidationRule("radiology.report.template.validation.error.meta.charset",
                META_CHARSET, subject -> subject.isEmpty() || subject.size() > 1));
        rules.add(new ElementsExpressionValidationRule("radiology.report.template.validation.error.meta.dublinCore",
                META_NAME, subject -> subject.isEmpty()));
    }
    
    /**
     * @see org.openmrs.module.radiology.report.template.MetaTagsValidationEngine#run(Elements)
     */
    @Override
    public ValidationResult run(Elements subject) {
        
        final ValidationResult validationResult = new ValidationResult();
        for (ValidationRule rule : rules) {
            rule.check(validationResult, subject);
        }
        return validationResult;
    }
}
