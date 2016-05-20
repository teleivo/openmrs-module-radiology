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

import static org.hamcrest.core.IsNull.notNullValue;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.lang.reflect.Field;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;
import org.openmrs.api.AdministrationService;
import org.openmrs.module.radiology.dicom.MppsSCP;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

/**
 * Tests {@link RadiologyActivator}
 */
public class RadiologyActivatorComponentTest extends BaseModuleContextSensitiveTest {
	
	private static final String ACTIVATOR_TEST_DATASET = "org/openmrs/module/radiology/include/RadiologyActivatorComponentTestDataset.xml";
	
	private RadiologyActivator radiologyActivator;
	
	@Autowired
	private RadiologyProperties radiologyProperties;
	
	@Autowired
	@Qualifier("adminService")
	private AdministrationService administrationService;
	
	@Rule
	public ExpectedException expectedException = ExpectedException.none();
	
	@Rule
	public TemporaryFolder temporaryBaseFolder = new TemporaryFolder();
	
	File temporaryDicomMppsSCPStorageDirectory;
	
	@Before
	public void setUp() throws Exception {
		
		radiologyActivator = new RadiologyActivator();
		executeDataSet(ACTIVATOR_TEST_DATASET);
		
		temporaryDicomMppsSCPStorageDirectory = temporaryBaseFolder.newFolder(radiologyProperties.getDicomMppsSCPStorageDirectory());
		administrationService.setGlobalProperty(RadiologyConstants.GP_DICOM_MPPS_SCP_STORAGE_DIRECTORY,
			temporaryDicomMppsSCPStorageDirectory.getAbsolutePath());
	}
	
	/**
	 * @see RadiologyActivator#startDicomOrderFiller()
	 * @verifies should successfully start the dicom order filler
	 */
	@Test
	public void startDicomOrderFiller_shouldSuccessfullyStartTheDicomOrderFiller() throws Exception {
		
		Field mppsSCPField = RadiologyActivator.class.getDeclaredField("mppsSCP");
		mppsSCPField.setAccessible(true);
		
		MppsSCP mppsSCPFieldValue = (MppsSCP) mppsSCPField.get(radiologyActivator);
		assertThat(mppsSCPFieldValue, nullValue());
		
		radiologyActivator.startDicomOrderFiller();
		
		mppsSCPFieldValue = (MppsSCP) mppsSCPField.get(radiologyActivator);
		assertThat(mppsSCPFieldValue, notNullValue());
	}
	
	/**
	 * @see RadiologyActivator#stopDicomOrderFiller()
	 * @verifies should successfully stop the dicom order filler
	 */
	@Test
	public void stopDicomOrderFiller_shouldSuccessfullyStopTheDicomOrderFiller() throws Exception {
		
		// Field dicomOrderFillerField = RadiologyActivator.class.getDeclaredField("dicomOrderFiller");
		// dicomOrderFillerField.setAccessible(true);
		// String[] dicomOrderFillerArguments = new String[] { "-mwl", radiologyProperties.getMwlDir(), "-mpps",
		// radiologyProperties.getMppsDir(),
		// radiologyProperties.getDicomAeTitle() + ":" + radiologyProperties.getDicomMppsPort() };
		// DcmOF dicomOrderFiller = DcmOF.main(dicomOrderFillerArguments);
		// dicomOrderFillerField.set(radiologyActivator, dicomOrderFiller);
		//
		// radiologyActivator.stopDicomOrderFiller();
	}
	
	/**
	 * @see RadiologyActivator#stopDicomOrderFiller()
	 * @verifies should throw exception when unable to stop the dicom order filler
	 */
	@Test
	public void stopDicomOrderFiller_shouldThrowExceptionWhenUnableToStopTheDicomOrderFiller() throws Exception {
		
		// Field dicomOrderFillerField = RadiologyActivator.class.getDeclaredField("dicomOrderFiller");
		// dicomOrderFillerField.setAccessible(true);
		// dicomOrderFillerField.set(radiologyActivator, null);
		//
		// expectedException.expect(NullPointerException.class);
		// radiologyActivator.stopDicomOrderFiller();
	}
}
