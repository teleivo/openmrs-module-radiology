/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.radiology.report.web;

import org.apache.commons.lang3.StringUtils;
import org.openmrs.module.radiology.dicom.DicomWebViewer;
import org.openmrs.module.radiology.order.RadiologyOrder;
import org.openmrs.module.radiology.order.RadiologyOrderService;
import org.openmrs.module.radiology.report.RadiologyReport;
import org.openmrs.module.radiology.report.template.MrrtRadiologyReport;
import org.openmrs.web.controller.PortletController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * Controller for the radiology report creation portlet.
 */
@Controller
@RequestMapping("**/createRadiologyReport.portlet")
public class CreateRadiologyReportPortletController extends PortletController {
    
    
    @Autowired
    private RadiologyOrderService radiologyOrderService;
    
    /**
     * @see PortletController#populateModel(HttpServletRequest,
     *      Map)
     * @should populate model with radiology order if given order uuid model entry matches a radiology order and dicom viewer
     *         url if radiology order is completed
     * @should populate model with radiology order if given order uuid model entry matches a radiology order and no dicom
     *         viewer url if radiology order is not completed
     */
    @Override
    protected void populateModel(HttpServletRequest request, Map<String, Object> model) {
        
        String orderUuid = (String) model.get("orderUuid");
        if (StringUtils.isBlank(orderUuid)) {
            return;
        }
        final RadiologyOrder radiologyOrder = radiologyOrderService.getRadiologyOrderByUuid(orderUuid);
        if (radiologyOrder == null) {
            return;
        }
        model.put("radiologyOrder", radiologyOrder);
        
        final RadiologyReport radiologyReport = new RadiologyReport();
        radiologyReport.setRadiologyOrder(radiologyOrder);
        model.put("radiologyReport", radiologyReport);
        
        final MrrtRadiologyReport mrrtRadiologyReport = new MrrtRadiologyReport();
        mrrtRadiologyReport.setRadiologyOrder(radiologyOrder);
        model.put("mrrtRadiologyReport", mrrtRadiologyReport);
    }
}
