package org.openmrs.module.radiology.report.template;

import org.jsoup.select.Elements;

/**
 * Validates <meta> tags of an Mrrt Report Template.
 */
public class MetaTagsValidationEngine implements ValidationEngine<Elements> {

    @Override public ValidationResult run(Elements subject) {

        final ValidationResult validationResult = new ValidationResult();

        final Elements metatagsWithCharsetAttribute = subject.select("charset");
        if (metatagsWithCharsetAttribute.isEmpty() || metatagsWithCharsetAttribute.size() > 1) {
            validationResult.addViolation("radiology.report.template.validation.error.meta.charset",0,0);
        }

        final Elements dublinAttributes = subject.select("name");
        if (dublinAttributes.isEmpty()) {
            validationResult.addViolation("radiology.report.template.validation.error.meta.dublinCore",0,0);
        }

        return validationResult;
    }
}
