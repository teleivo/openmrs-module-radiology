/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.radiology.report;

import org.openmrs.module.radiology.order.RadiologyOrder;

/**
 * Represents an intermediate object used to create a {@code RadiologyReport}.
 */
public class RadiologyReportClaim {
    
    
    private RadiologyOrder radiologyOrder;
    
    private RadiologyReport radiologyReport;
    
    public RadiologyOrder getRadiologyOrder() {
        return radiologyOrder;
    }
    
    public void setRadiologyOrder(RadiologyOrder radiologyOrder) {
        this.radiologyOrder = radiologyOrder;
    }
    
    public RadiologyReport getRadiologyReport() {
        return radiologyReport;
    }
    
    public void setRadiologyReport(RadiologyReport radiologyReport) {
        this.radiologyReport = radiologyReport;
    }
}
