/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.module.radiology.study;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.hibernate.cfg.Environment;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.openmrs.Patient;
import org.openmrs.api.PatientService;
import org.openmrs.module.radiology.Modality;
import org.openmrs.module.radiology.PerformedProcedureStepStatus;
import org.openmrs.module.radiology.RadiologyOrder;
import org.openmrs.module.radiology.RadiologyProperties;
import org.openmrs.module.radiology.RadiologyService;
import org.openmrs.module.radiology.ScheduledProcedureStepStatus;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Tests {@link RadiologyStudyService}
 */
public class RadiologyStudyServiceComponentTest extends BaseModuleContextSensitiveTest {
	
	private static final String STUDIES_TEST_DATASET = "org/openmrs/module/radiology/include/RadiologyServiceComponentTestDataset.xml";
	
	private static final int PATIENT_ID_WITH_TWO_STUDIES_AND_NO_NON_RADIOLOGY_ORDER = 70021;
	
	private static final int RADIOLOGY_ORDER_ID_WITHOUT_STUDY = 2004;
	
	private static final int EXISTING_RADIOLOGY_ORDER_ID = 2001;
	
	private static final int NON_EXISTING_RADIOLOGY_ORDER_ID = 99999;
	
	private static final String EXISTING_STUDY_INSTANCE_UID = "1.2.826.0.1.3680043.8.2186.1.1";
	
	private static final String NON_EXISTING_STUDY_INSTANCE_UID = "1.2.826.0.1.3680043.8.2186.1.9999";
	
	private static final int EXISTING_STUDY_ID = 1;
	
	private static final int NON_EXISTING_STUDY_ID = 99999;
	
	@Autowired
	private PatientService patientService;
	
	@Autowired
	private RadiologyService radiologyService;
	
	@Autowired
	private RadiologyStudyService radiologyStudyService;
	
	@Autowired
	private RadiologyProperties radiologyProperties;
	
	@Rule
	public ExpectedException expectedException = ExpectedException.none();
	
	/**
	 * Overriding following method is necessary to enable MVCC which is disabled by default in DB h2
	 * used for the component tests. This prevents following exception:
	 * org.hibernate.exception.GenericJDBCException: could not load an entity:
	 * [org.openmrs.GlobalProperty#order.nextOrderNumberSeed] due to "Timeout trying to lock table "
	 * GLOBAL_PROPERTY"; SQL statement:" which occurs in all tests touching methods that call
	 * orderService.saveOrder()
	 */
	@Override
	public Properties getRuntimeProperties() {
		Properties result = super.getRuntimeProperties();
		String url = result.getProperty(Environment.URL);
		if (url.contains("jdbc:h2:") && !url.contains(";MVCC=TRUE")) {
			result.setProperty(Environment.URL, url + ";MVCC=TRUE");
		}
		return result;
	}
	
	@Before
	public void runBeforeAllTests() throws Exception {
		executeDataSet(STUDIES_TEST_DATASET);
	}
	
	/**
	 * @see RadiologyStudyService#saveStudy(Study)
	 * @verifies create new study from given study object
	 */
	@Test
	public void saveStudy_shouldCreateNewStudyFromGivenStudyObject() throws Exception {
		
		Study radiologyStudy = getUnsavedStudy();
		RadiologyOrder radiologyOrder = radiologyService.getRadiologyOrderByOrderId(RADIOLOGY_ORDER_ID_WITHOUT_STUDY);
		radiologyOrder.setStudy(radiologyStudy);
		
		Study createdStudy = radiologyStudyService.saveStudy(radiologyStudy);
		
		assertNotNull(createdStudy);
		assertThat(createdStudy, is(radiologyStudy));
		assertThat(createdStudy.getStudyId(), is(radiologyStudy.getStudyId()));
		assertNotNull(createdStudy.getStudyInstanceUid());
		assertThat(createdStudy.getStudyInstanceUid(), is(radiologyProperties.getStudyPrefix() + createdStudy.getStudyId()));
		assertThat(createdStudy.getModality(), is(radiologyStudy.getModality()));
		assertThat(createdStudy.getRadiologyOrder(), is(radiologyStudy.getRadiologyOrder()));
	}
	
	/**
	 * Convenience method to get a Study object with all required values filled (except
	 * radiologyOrder) in but which is not yet saved in the database
	 * 
	 * @return Study object that can be saved to the database
	 */
	public Study getUnsavedStudy() {
		
		Study study = new Study();
		study.setModality(Modality.CT);
		study.setScheduledStatus(ScheduledProcedureStepStatus.SCHEDULED);
		return study;
	}
	
	/**
	 * @see RadiologyStudyService#saveStudy(Study)
	 * @verifies update existing study
	 */
	@Test
	public void saveStudy_shouldUpdateExistingStudy() throws Exception {
		
		Study existingStudy = radiologyStudyService.getStudyByStudyId(EXISTING_STUDY_ID);
		Modality modalityPreUpdate = existingStudy.getModality();
		Modality modalityPostUpdate = Modality.XA;
		existingStudy.setModality(modalityPostUpdate);
		
		Study updatedStudy = radiologyStudyService.saveStudy(existingStudy);
		
		assertNotNull(updatedStudy);
		assertThat(updatedStudy, is(existingStudy));
		assertThat(modalityPreUpdate, is(not(modalityPostUpdate)));
		assertThat(updatedStudy.getModality(), is(modalityPostUpdate));
	}
	
	/**
	 * @see RadiologyStudyService#getStudyByStudyId(Integer)
	 * @verifies should return study for given study id
	 */
	@Test
	public void getStudyByStudyId_shouldReturnStudyForGivenStudyId() throws Exception {
		
		Study study = radiologyStudyService.getStudyByStudyId(EXISTING_STUDY_ID);
		
		assertNotNull(study);
		assertThat(study.getRadiologyOrder()
				.getOrderId(), is(EXISTING_RADIOLOGY_ORDER_ID));
	}
	
	/**
	 * @see RadiologyStudyService#getStudyByStudyId(Integer)
	 * @verifies should return null if no match was found
	 */
	@Test
	public void getStudyByStudyId_shouldReturnNullIfNoMatchIsFound() throws Exception {
		
		Study study = radiologyStudyService.getStudyByStudyId(NON_EXISTING_STUDY_ID);
		
		assertNull(study);
	}
	
	/**
	 * @see RadiologyStudyService#getStudyByOrderId(Integer)
	 * @verifies should return study associated with radiology order for which order id is given
	 */
	@Test
	public void getStudyByOrderId_shouldReturnStudyMatching() throws Exception {
		
		Study study = radiologyStudyService.getStudyByOrderId(EXISTING_RADIOLOGY_ORDER_ID);
		
		assertNotNull(study);
		assertThat(study.getRadiologyOrder()
				.getOrderId(), is(EXISTING_RADIOLOGY_ORDER_ID));
	}
	
	/**
	 * @see RadiologyStudyService#getStudyByOrderId(Integer)
	 * @verifies should return null if no match was found
	 */
	@Test
	public void getStudyByOrderId_shouldReturnNullIfNoMatchIsFound() {
		
		Study study = radiologyStudyService.getStudyByOrderId(NON_EXISTING_RADIOLOGY_ORDER_ID);
		
		assertNull(study);
	}
	
	/**
	 * @see RadiologyStudyService#getStudyByOrderId(Integer)
	 * @verifies should throw illegal argument exception given null
	 */
	@Test
	public void getStudyByOrderId_shouldThrowIllegalArgumentExceptionGivenNull() {
		
		expectedException.expect(IllegalArgumentException.class);
		expectedException.expectMessage("orderId is required");
		radiologyStudyService.getStudyByOrderId(null);
	}
	
	/**
	 * @see RadiologyStudyService#getStudyByStudyInstanceUid(String)
	 * @verifies should return study matching study instance uid
	 */
	@Test
	public void getStudyByStudyInstanceUid_shouldReturnStudyMatchingUid() throws Exception {
		Study study = radiologyStudyService.getStudyByStudyInstanceUid(EXISTING_STUDY_INSTANCE_UID);
		
		assertNotNull(study);
		assertThat(study.getStudyInstanceUid(), is(EXISTING_STUDY_INSTANCE_UID));
	}
	
	/**
	 * @see RadiologyStudyService#getStudyByStudyInstanceUid(String)
	 * @verifies should return null if no match was found
	 */
	@Test
	public void getStudyByStudyInstanceUid_shouldReturnNullIfNoMatchIsFound() throws Exception {
		Study study = radiologyStudyService.getStudyByStudyInstanceUid(NON_EXISTING_STUDY_INSTANCE_UID);
		
		assertNull(study);
	}
	
	/**
	 * @see RadiologyStudyService#getStudyByStudyInstanceUid(String)
	 * @verifies should throw IllegalArgumentException if study instance uid is null
	 */
	@Test
	public void getStudyByStudyInstanceUid_shouldThrowIllegalArgumentExceptionIfUidIsNull() throws Exception {
		expectedException.expect(IllegalArgumentException.class);
		expectedException.expectMessage("studyInstanceUid is required");
		radiologyStudyService.getStudyByStudyInstanceUid(null);
	}
	
	/**
	 * @see RadiologyStudyService#getStudiesByRadiologyOrders(List<RadiologyOrder>)
	 * @verifies should fetch all studies for given radiology orders
	 */
	@Test
	public void getStudiesByRadiologyOrders_shouldFetchAllStudiesForGivenRadiologyOrders() throws Exception {
		
		Patient patient = patientService.getPatient(PATIENT_ID_WITH_TWO_STUDIES_AND_NO_NON_RADIOLOGY_ORDER);
		List<RadiologyOrder> radiologyOrders = radiologyService.getRadiologyOrdersByPatient(patient);
		
		List<Study> studies = radiologyStudyService.getStudiesByRadiologyOrders(radiologyOrders);
		
		assertThat(studies.size(), is(radiologyOrders.size()));
		assertThat(studies.get(0)
				.getRadiologyOrder(), is(radiologyOrders.get(0)));
		assertThat(studies.get(1)
				.getRadiologyOrder(), is(radiologyOrders.get(1)));
	}
	
	/**
	 * @see RadiologyStudyService#getStudiesByRadiologyOrders(List<RadiologyOrder>)
	 * @verifies should return empty list given radiology orders without associated studies
	 */
	@Test
	public void getStudiesByRadiologyOrders_shouldReturnEmptyListGivenRadiologyOrdersWithoutAssociatedStudies()
			throws Exception {
		
		RadiologyOrder radiologyOrderWithoutStudy = radiologyService.getRadiologyOrderByOrderId(RADIOLOGY_ORDER_ID_WITHOUT_STUDY);
		List<RadiologyOrder> radiologyOrders = Arrays.asList(radiologyOrderWithoutStudy);
		
		List<Study> studies = radiologyStudyService.getStudiesByRadiologyOrders(radiologyOrders);
		
		assertThat(radiologyOrders.size(), is(1));
		assertThat(studies.size(), is(0));
	}
	
	/**
	 * @see RadiologyStudyService#getStudiesByRadiologyOrders(List<RadiologyOrder>)
	 * @verifies should return empty list given empty radiology order list
	 */
	@Test
	public void getStudiesByRadiologyOrders_shouldReturnEmptyListGivenEmptyRadiologyOrderList() throws Exception {
		
		List<RadiologyOrder> orders = new ArrayList<RadiologyOrder>();
		
		List<Study> studies = radiologyStudyService.getStudiesByRadiologyOrders(orders);
		
		assertThat(orders.size(), is(0));
		assertThat(studies.size(), is(0));
	}
	
	/**
	 * @see RadiologyStudyService#getStudiesByRadiologyOrders(List<RadiologyOrder>)
	 * @verifies should throw IllegalArgumentException given null
	 */
	@Test
	public void getStudiesByRadiologyOrders_shouldThrowIllegalArgumentExceptionGivenNull() throws Exception {
		
		expectedException.expect(IllegalArgumentException.class);
		expectedException.expectMessage("radiologyOrders are required");
		radiologyStudyService.getStudiesByRadiologyOrders(null);
	}
	
	/**
	 * @see RadiologyStudyService#updateStudyPerformedStatus(String,PerformedProcedureStepStatus)
	 * @verifies update performed status of study associated with given study instance uid
	 */
	@Test
	public void updateStudyPerformedStatus_shouldUpdatePerformedStatusOfStudyAssociatedWithGivenStudyInstanceUid()
			throws Exception {
		
		Study existingStudy = radiologyStudyService.getStudyByStudyId(EXISTING_STUDY_ID);
		PerformedProcedureStepStatus performedStatusPreUpdate = existingStudy.getPerformedStatus();
		PerformedProcedureStepStatus performedStatusPostUpdate = PerformedProcedureStepStatus.COMPLETED;
		
		Study updatedStudy = radiologyStudyService.updateStudyPerformedStatus(existingStudy.getStudyInstanceUid(),
			performedStatusPostUpdate);
		
		assertNotNull(updatedStudy);
		assertThat(updatedStudy, is(existingStudy));
		assertThat(performedStatusPreUpdate, is(not(performedStatusPostUpdate)));
		assertThat(updatedStudy.getPerformedStatus(), is(performedStatusPostUpdate));
	}
	
	/**
	 * @see RadiologyStudyService#updateStudyPerformedStatus(String,PerformedProcedureStepStatus)
	 * @verifies throw illegal argument exception if study instance uid is null
	 */
	@Test
	public void updateStudyPerformedStatus_shouldThrowIllegalArgumentExceptionIfStudyInstanceUidIsNull() throws Exception {
		
		expectedException.expect(IllegalArgumentException.class);
		expectedException.expectMessage("studyInstanceUid is required");
		radiologyStudyService.updateStudyPerformedStatus(null, PerformedProcedureStepStatus.COMPLETED);
	}
	
	/**
	 * @see RadiologyStudyService#updateStudyPerformedStatus(String,PerformedProcedureStepStatus)
	 * @verifies throw illegal argument exception if performed status is null
	 */
	@Test
	public void updateStudyPerformedStatus_shouldThrowIllegalArgumentExceptionIfPerformedStatusIsNull() throws Exception {
		
		expectedException.expect(IllegalArgumentException.class);
		expectedException.expectMessage("performedStatus is required");
		radiologyStudyService.updateStudyPerformedStatus(EXISTING_STUDY_INSTANCE_UID, null);
	}
}
