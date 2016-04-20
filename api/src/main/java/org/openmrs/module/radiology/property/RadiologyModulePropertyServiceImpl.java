/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.radiology.property;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.CareSetting;
import org.openmrs.EncounterRole;
import org.openmrs.EncounterType;
import org.openmrs.OrderType;
import org.openmrs.VisitType;
import org.openmrs.api.impl.BaseOpenmrsService;
import org.springframework.beans.factory.annotation.Autowired;

class RadiologyModulePropertyServiceImpl extends BaseOpenmrsService implements RadiologyModulePropertyService {
	
	private static final Log log = LogFactory.getLog(RadiologyModulePropertyServiceImpl.class);
	
	@Autowired
	private RadiologyModuleProperties radiologyModuleProperties;
	
	/**
	 * @see RadiologyModulePropertyService#getMppsDir()
	 */
	@Override
	public String getMppsDir() {
		return radiologyModuleProperties.getMppsDir();
	}
	
	/**
	 * @see RadiologyModulePropertyService#getMwlDir()
	 */
	@Override
	public String getMwlDir() {
		return radiologyModuleProperties.getMwlDir();
	}
	
	/**
	 * @see RadiologyModulePropertyService#getPacsAddress()
	 */
	@Override
	public String getPacsAddress() {
		return radiologyModuleProperties.getPacsAddress();
	}
	
	/**
	 * @see RadiologyModulePropertyService#getPacsHL7Port()
	 */
	@Override
	public String getPacsHL7Port() {
		return radiologyModuleProperties.getPacsHL7Port();
	}
	
	/**
	 * @see RadiologyModulePropertyService#getPacsDicomAeTitle()
	 */
	@Override
	public String getPacsDicomAeTitle() {
		return radiologyModuleProperties.getPacsDicomAeTitle();
	}
	
	/**
	 * @see RadiologyModulePropertyService#getDicomMppsPort()
	 */
	@Override
	public String getDicomMppsPort() {
		return radiologyModuleProperties.getDicomMppsPort();
	}
	
	/**
	 * @see RadiologyModulePropertyService#getDicomAeTitle()
	 */
	@Override
	public String getDicomAeTitle() {
		return radiologyModuleProperties.getDicomAeTitle();
	}
	
	/**
	 * @see RadiologyModulePropertyService#getDicomMppsPort()
	 */
	@Override
	public String getDicomUIDOrgRoot() {
		return radiologyModuleProperties.getDicomUIDOrgRoot();
	}
	
	/**
	 * @see RadiologyModulePropertyService#getDicomUIDApplication()
	 */
	@Override
	public String getDicomUIDApplication() {
		return radiologyModuleProperties.getDicomUIDApplication();
	}
	
	/**
	 * @see RadiologyModulePropertyService#getDicomUIDTypeStudy()
	 */
	@Override
	public String getDicomUIDTypeStudy() {
		return radiologyModuleProperties.getDicomUIDTypeStudy();
	}
	
	/**
	 * @see RadiologyModulePropertyService#getDicomSpecificCharacterSet()
	 */
	@Override
	public String getDicomSpecificCharacterSet() {
		return radiologyModuleProperties.getDicomSpecificCharacterSet();
	}
	
	/**
	 * @see RadiologyModulePropertyService#getStudyPrefix()
	 */
	@Override
	public String getStudyPrefix() {
		return radiologyModuleProperties.getStudyPrefix();
	}
	
	/**
	 * @see RadiologyModulePropertyService#getDicomWebViewerAddress()
	 */
	@Override
	public String getDicomWebViewerAddress() {
		return radiologyModuleProperties.getDicomWebViewerAddress();
	}
	
	/**
	 * @see RadiologyModulePropertyService#getDicomWebViewerPort()
	 */
	@Override
	public String getDicomWebViewerPort() {
		return radiologyModuleProperties.getDicomWebViewerPort();
	}
	
	/**
	 * @see RadiologyModulePropertyService#getDicomWebViewerBaseUrl()
	 */
	@Override
	public String getDicomWebViewerBaseUrl() {
		return radiologyModuleProperties.getDicomWebViewerBaseUrl();
	}
	
	/**
	 * @see RadiologyModulePropertyService#getDicomWebViewerLocalServerName()
	 */
	@Override
	public String getDicomWebViewerLocalServerName() {
		return radiologyModuleProperties.getDicomWebViewerLocalServerName();
	}
	
	/**
	 * @see RadiologyModulePropertyService#getRadiologyCareSetting()
	 */
	@Override
	public CareSetting getRadiologyCareSetting() {
		return radiologyModuleProperties.getRadiologyCareSetting();
	}
	
	/**
	 * @see RadiologyModulePropertyService#getRadiologyTestOrderType()
	 */
	@Override
	public OrderType getRadiologyTestOrderType() {
		return radiologyModuleProperties.getRadiologyTestOrderType();
	}
	
	/**
	 * @see RadiologyModulePropertyService#getRadiologyOrderEncounterType()
	 */
	@Override
	public EncounterType getRadiologyOrderEncounterType() {
		return radiologyModuleProperties.getRadiologyOrderEncounterType();
	}
	
	/**
	 * @see RadiologyModulePropertyService#getRadiologyOrderingProviderEncounterRole()
	 */
	@Override
	public EncounterRole getRadiologyOrderingProviderEncounterRole() {
		return radiologyModuleProperties.getRadiologyOrderingProviderEncounterRole();
	}
	
	/**
	 * @see RadiologyModulePropertyService#getRadiologyVisitType()
	 */
	@Override
	public VisitType getRadiologyVisitType() {
		return radiologyModuleProperties.getRadiologyVisitType();
	}
	
	/**
	 * @see RadiologyModulePropertyService#getRadiologyConceptClassNames()
	 */
	@Override
	public String getRadiologyConceptClassNames() {
		return radiologyModuleProperties.getRadiologyConceptClassNames();
	}
}
