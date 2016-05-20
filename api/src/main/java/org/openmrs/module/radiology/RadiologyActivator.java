/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.radiology;

import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;

import org.apache.commons.cli.ParseException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.context.Context;
import org.openmrs.module.BaseModuleActivator;
import org.openmrs.module.radiology.dicom.MppsSCP;

/**
 * This class contains the logic that is run every time this module is either started or stopped.
 */

public class RadiologyActivator extends BaseModuleActivator {
	
	private static final Log log = LogFactory.getLog(RadiologyActivator.class);
	
	private MppsSCP mppsSCP;
	
	@Override
	public void willStart() {
		log.info("Trying to start up Radiology Module");
	}
	
	@Override
	public void started() {
		startDicomOrderFiller();
		log.info("Radiology Module successfully started");
	}
	
	@Override
	public void willStop() {
		log.info("Trying to shut down Radiology Module");
	}
	
	@Override
	public void stopped() {
		stopDicomOrderFiller();
		log.info("Radiology Module successfully stopped");
	}
	
	/**
	 * Start dicom order filler
	 * 
	 * @should successfully start the dicom order filler
	 */
	void startDicomOrderFiller() {
		
		final RadiologyProperties radiologyProperties = Context.getRegisteredComponent("radiologyProperties",
			RadiologyProperties.class);
		
		log.info("Creating OpenMRS MPPS SCP Client with: AE Title=" + radiologyProperties.getDicomMppsSCPAeTitle()
				+ " AE Port=" + radiologyProperties.getDicomMppsSCPPort() + " storage directory= "
				+ radiologyProperties.getDicomMppsSCPStorageDirectory());
		try {
			this.mppsSCP = new MppsSCP(radiologyProperties.getDicomMppsSCPAeTitle(),
					radiologyProperties.getDicomMppsSCPPort(), new File(
							radiologyProperties.getDicomMppsSCPStorageDirectory()));
		}
		catch (IOException ioException) {
			log.error("Error creating OpenMRS MPPS SCP Client", ioException);
		}
		catch (ParseException parseException) {
			log.error("Error creating OpenMRS MPPS SCP Client", parseException);
		}
		
		log.info("Starting OpenMRS MPPS SCP Client");
		try {
			this.mppsSCP.start();
			log.info("OpenMRS MPPS SCP Client started");
		}
		catch (IOException ioException) {
			log.error("Error starting OpenMRS MPPS SCP Client", ioException);
		}
		catch (GeneralSecurityException generalSecurityException) {
			log.error("Error starting OpenMRS MPPS SCP Client", generalSecurityException);
		}
	}
	
	/**
	 * Stop dicom order filler
	 * 
	 * @should throw exception when unable to stop the dicom order filler
	 * @should successfully stop the dicom order filler
	 */
	void stopDicomOrderFiller() {
		log.info("Trying to stop MPPSScu : OpenMRS MPPS SCU Client (dcmof)");
		this.mppsSCP.stop();
	}
}
