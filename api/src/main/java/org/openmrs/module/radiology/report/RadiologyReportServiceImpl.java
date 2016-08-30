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

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.APIException;
import org.openmrs.api.impl.BaseOpenmrsService;
import org.openmrs.module.radiology.RadiologyProperties;
import org.openmrs.module.radiology.order.RadiologyOrder;
import org.openmrs.util.OpenmrsUtil;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
class RadiologyReportServiceImpl extends BaseOpenmrsService implements RadiologyReportService {
    
    
    private static final Log log = LogFactory.getLog(RadiologyReportServiceImpl.class);
    
    private RadiologyReportDAO radiologyReportDAO;
    
    private RadiologyProperties radiologyProperties;
    
    public void setRadiologyReportDAO(RadiologyReportDAO radiologyReportDAO) {
        this.radiologyReportDAO = radiologyReportDAO;
    }
    
    public void setRadiologyProperties(RadiologyProperties radiologyProperties) {
        this.radiologyProperties = radiologyProperties;
    }
    
    /**
     * @see RadiologyReportService#createRadiologyReport(RadiologyReportClaim)
     * @param radiologyReportClaim
     */
    @Override
    @Transactional
    public synchronized RadiologyReport createRadiologyReport(RadiologyReportClaim radiologyReportClaim) {
        
        if (radiologyReportClaim == null) {
            throw new IllegalArgumentException("radiologyReportClaim cannot be null");
        }
        if (radiologyReportClaim.getRadiologyReport() == null) {
            throw new IllegalArgumentException("radiologyReportClaim.radiologyReport cannot be null");
        }
        if (radiologyReportClaim.getRadiologyOrder() == null) {
            throw new IllegalArgumentException("radiologyReport.radiologyOrder cannot be null");
        }
        if (radiologyReportClaim.getRadiologyOrder()
                .isNotCompleted()) {
            throw new APIException("radiology.RadiologyReport.cannot.create.for.not.completed.order");
        }
        if (radiologyReportDAO.hasRadiologyOrderClaimedRadiologyReport(radiologyReportClaim.getRadiologyOrder())) {
            throw new APIException("radiology.RadiologyReport.cannot.create.already.claimed");
        }
        if (radiologyReportDAO.hasRadiologyOrderCompletedRadiologyReport(radiologyReportClaim.getRadiologyOrder())) {
            throw new APIException("radiology.RadiologyReport.cannot.create.already.completed");
        }
        return radiologyReportDAO.saveRadiologyReport(radiologyReportClaim.getRadiologyReport());
    }
    
    /**
     * @see RadiologyReportService#saveRadiologyReportDraft(RadiologyReport)
     */
    @Override
    @Transactional
    public synchronized RadiologyReport saveRadiologyReportDraft(RadiologyReport radiologyReport) {
        
        if (radiologyReport == null) {
            throw new IllegalArgumentException("radiologyReport cannot be null");
        }
        if (radiologyReport.getReportId() == null) {
            throw new IllegalArgumentException("radiologyReport.reportId cannot be null");
        }
        if (radiologyReport.getStatus() == RadiologyReportStatus.COMPLETED) {
            throw new APIException("radiology.RadiologyReport.cannot.saveDraft.already.completed");
        }
        if (radiologyReport.getVoided()) {
            throw new APIException("radiology.RadiologyReport.cannot.saveDraft.already.voided");
        }
        if (radiologyReportDAO.hasRadiologyOrderCompletedRadiologyReport(radiologyReport.getRadiologyOrder())) {
            throw new APIException("radiology.RadiologyReport.cannot.saveDraft.already.reported");
        }
        
        File file = saveRadiologyReportFile(radiologyReport);
        radiologyReport.setFilename(file.getAbsolutePath());
        return radiologyReportDAO.saveRadiologyReport(radiologyReport);
    }
    
    private File saveRadiologyReportFile(RadiologyReport radiologyReport) {
        
        // save report body to filesystem
        File file;
        if (StringUtils.isBlank(radiologyReport.getFilename())) {
            file = new File(radiologyProperties.getReportHome(), UUID.randomUUID()
                    .toString());
            try {
                file.createNewFile();
            }
            catch (Exception exception) {
                throw new APIException(exception);
            }
        } else {
            file = new File(radiologyReport.getFilename());
        }
        
        InputStream inputStream = null;
        FileOutputStream fileOutputStream = null;
        try {
            inputStream = IOUtils.toInputStream(radiologyReport.getBody(), "UTF-8");
            fileOutputStream = new FileOutputStream(file);
            OpenmrsUtil.copyFile(inputStream, fileOutputStream);
            inputStream.close();
            fileOutputStream.close();
            radiologyReport.setBody(null);
        }
        catch (Exception exception) {
            throw new APIException(exception);
        }
        finally {
            IOUtils.closeQuietly(inputStream);
            IOUtils.closeQuietly(fileOutputStream);
        }
        
        return file;
    }
    
    /**
     * @see RadiologyReportService#getRadiologyReportWithBody(RadiologyReport)
     */
    public RadiologyReport getRadiologyReportWithBody(RadiologyReport radiologyReport) {
        
        if (radiologyReport == null) {
            throw new IllegalArgumentException("radiologyReport cannot be null.");
        }
        // right after createRadiologyReport the RadiologyReport.filename is null since in this stage only a shell is created to block anyone else but the creator from reporting the order
        if (StringUtils.isBlank(radiologyReport.getFilename())) {
            return radiologyReport;
        }
        
        File file = new File(radiologyReport.getFilename());
        try {
            radiologyReport.setBody(OpenmrsUtil.getFileAsString(file));
        }
        catch (Exception exception) {
            throw new APIException(exception);
        }
        
        return radiologyReport;
    }
    
    /**
     * @see RadiologyReportService#voidRadiologyReport(RadiologyReport, String)
     */
    @Override
    @Transactional
    public RadiologyReport voidRadiologyReport(RadiologyReport radiologyReport, String voidReason) {
        
        if (radiologyReport == null) {
            throw new IllegalArgumentException("radiologyReport cannot be null");
        }
        if (radiologyReport.getReportId() == null) {
            throw new IllegalArgumentException("radiologyReport.reportId cannot be null");
        }
        if (StringUtils.isBlank(voidReason)) {
            throw new IllegalArgumentException("voidReason cannot be null or empty");
        }
        if (radiologyReport.getStatus() == RadiologyReportStatus.COMPLETED) {
            throw new APIException("radiology.RadiologyReport.cannot.void.completed");
        }
        return radiologyReportDAO.saveRadiologyReport(radiologyReport);
    }
    
    /**
     * @see RadiologyReportService#saveRadiologyReport(RadiologyReport)
     */
    @Override
    @Transactional
    public synchronized RadiologyReport saveRadiologyReport(RadiologyReport radiologyReport) {
        
        if (radiologyReport == null) {
            throw new IllegalArgumentException("radiologyReport cannot be null");
        }
        if (radiologyReport.getReportId() == null) {
            throw new IllegalArgumentException("radiologyReport.reportId cannot be null");
        }
        if (radiologyReport.getStatus() == null) {
            throw new IllegalArgumentException("radiologyReport.status cannot be null");
        }
        if (radiologyReport.getStatus() == RadiologyReportStatus.COMPLETED) {
            throw new APIException("radiology.RadiologyReport.cannot.complete.completed");
        }
        if (radiologyReport.getVoided()) {
            throw new APIException("radiology.RadiologyReport.cannot.complete.voided");
        }
        radiologyReport.setDate(new Date());
        radiologyReport.setStatus(RadiologyReportStatus.COMPLETED);
        
        File file = saveRadiologyReportFile(radiologyReport);
        radiologyReport.setFilename(file.getAbsolutePath());
        return radiologyReportDAO.saveRadiologyReport(radiologyReport);
    }
    
    /**
     * @see RadiologyReportService#getRadiologyReport(Integer)
     */
    @Override
    public RadiologyReport getRadiologyReport(Integer reportId) {
        
        if (reportId == null) {
            throw new IllegalArgumentException("reportId cannot be null");
        }
        return radiologyReportDAO.getRadiologyReport(reportId);
    }
    
    /**
     * @see RadiologyReportService#getRadiologyReportByUuid(String)
     */
    @Override
    public RadiologyReport getRadiologyReportByUuid(String radiologyReportUuid) {
        
        if (radiologyReportUuid == null) {
            throw new IllegalArgumentException("radiologyReportUuid cannot be null");
        }
        return radiologyReportDAO.getRadiologyReportByUuid(radiologyReportUuid);
    }
    
    /**
     * @see RadiologyReportService#hasRadiologyOrderClaimedRadiologyReport(RadiologyOrder)
     */
    @Override
    public boolean hasRadiologyOrderClaimedRadiologyReport(RadiologyOrder radiologyOrder) {
        
        if (radiologyOrder == null) {
            throw new IllegalArgumentException("radiologyOrder cannot be null");
        }
        return radiologyReportDAO.hasRadiologyOrderClaimedRadiologyReport(radiologyOrder);
    }
    
    /**
     * @see RadiologyReportService#hasRadiologyOrderCompletedRadiologyReport(RadiologyOrder)
     */
    @Override
    public boolean hasRadiologyOrderCompletedRadiologyReport(RadiologyOrder radiologyOrder) {
        
        if (radiologyOrder == null) {
            throw new IllegalArgumentException("radiologyOrder cannot be null");
        }
        return radiologyReportDAO.hasRadiologyOrderCompletedRadiologyReport(radiologyOrder);
    }
    
    /**
     * @see RadiologyReportService#getActiveRadiologyReportByRadiologyOrder(RadiologyOrder)
     */
    @Override
    public RadiologyReport getActiveRadiologyReportByRadiologyOrder(RadiologyOrder radiologyOrder) {
        
        if (radiologyOrder == null) {
            throw new IllegalArgumentException("radiologyOrder cannot be null");
        }
        if (hasRadiologyOrderCompletedRadiologyReport(radiologyOrder)) {
            return radiologyReportDAO.getActiveRadiologyReportByRadiologyOrder(radiologyOrder);
        }
        if (hasRadiologyOrderClaimedRadiologyReport(radiologyOrder)) {
            return radiologyReportDAO.getActiveRadiologyReportByRadiologyOrder(radiologyOrder);
        }
        return null;
    }
    
    /**
     * @see RadiologyReportService#getRadiologyReports(RadiologyReportSearchCriteria)
     */
    @Override
    public List<RadiologyReport> getRadiologyReports(RadiologyReportSearchCriteria radiologyReportSearchCriteria) {
        
        if (radiologyReportSearchCriteria == null) {
            throw new IllegalArgumentException("radiologyReportSearchCriteria cannot be null");
        }
        return radiologyReportDAO.getRadiologyReports(radiologyReportSearchCriteria);
    }
}
