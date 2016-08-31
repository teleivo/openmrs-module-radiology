package org.openmrs.module.radiology.report.template;

import org.openmrs.module.radiology.report.RadiologyReport;

/**
 * Represents a radiology report written using a {@code MrrtReportTemplate}.
 *
 * @see MrrtReportTemplate
 */
public class MrrtRadiologyReport extends RadiologyReport {

    private MrrtReportTemplate mrrtReportTemplate;

    public MrrtReportTemplate getMrrtReportTemplate() {
        return mrrtReportTemplate;
    }

    public void setMrrtReportTemplate(MrrtReportTemplate mrrtReportTemplate) {
        this.mrrtReportTemplate = mrrtReportTemplate;
    }
}
