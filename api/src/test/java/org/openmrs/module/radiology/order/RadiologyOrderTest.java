/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.radiology.order;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.openmrs.Order;
import org.openmrs.module.radiology.PerformedProcedureStepStatus;
import org.openmrs.module.radiology.order.RadiologyOrder;
import org.openmrs.module.radiology.study.Study;

/**
 * Tests {@link RadiologyOrder}
 */
public class RadiologyOrderTest {
	
	/**
	 * @see RadiologyOrder#setStudy(Study)
	 * @verifies set the study attribute to given study
	 */
	@Test
	public void setStudy_shouldSetTheStudyAttributeToGivenStudy() throws Exception {
		
		RadiologyOrder radiologyOrder = new RadiologyOrder();
		Study study = new Study();
		radiologyOrder.setStudy(study);
		
		assertThat(radiologyOrder.getStudy(), is(study));
	}
	
	/**
	 * @see RadiologyOrder#setStudy(Study)
	 * @verifies set the radiology order of given study to this radiology order
	 */
	@Test
	public void setStudy_shouldSetTheRadiologyOrderOfGivenStudyToThisRadiologyOrder() throws Exception {
		
		RadiologyOrder radiologyOrder = new RadiologyOrder();
		Study study = new Study();
		radiologyOrder.setStudy(study);
		
		assertThat(study.getRadiologyOrder(), is(radiologyOrder));
	}
	
	/**
	 * @see RadiologyOrder#setStudy(Study)
	 * @verifies not fail given null
	 */
	@Test
	public void setStudy_shouldNotFailGivenNull() throws Exception {
		
		RadiologyOrder radiologyOrder = new RadiologyOrder();
		radiologyOrder.setStudy(null);
		
		assertNotNull(radiologyOrder);
	}
	
	/**
	 * @see RadiologyOrder#isInProgress()
	 * @verifies return false if associated study is null
	 */
	@Test
	public void isInProgress_shouldReturnFalseIfAssociatedStudyIsNull() throws Exception {
		
		RadiologyOrder radiologyOrder = new RadiologyOrder();
		radiologyOrder.setStudy(null);
		
		assertFalse(radiologyOrder.isInProgress());
	}
	
	/**
	 * @see RadiologyOrder#isInProgress()
	 * @verifies return false if associated study is not in progress
	 */
	@Test
	public void isInProgress_shouldReturnFalseIfAssociatedStudyIsNotInProgress() throws Exception {
		
		RadiologyOrder radiologyOrder = new RadiologyOrder();
		Study study = new Study();
		study.setPerformedStatus(PerformedProcedureStepStatus.COMPLETED);
		radiologyOrder.setStudy(study);
		
		assertFalse(radiologyOrder.isInProgress());
	}
	
	/**
	 * @see RadiologyOrder#isInProgress()
	 * @verifies return true if associated study is in progress
	 */
	@Test
	public void isInProgress_shouldReturnTrueIfAssociatedStudyIsInProgress() throws Exception {
		
		RadiologyOrder radiologyOrder = new RadiologyOrder();
		Study study = new Study();
		study.setPerformedStatus(PerformedProcedureStepStatus.IN_PROGRESS);
		radiologyOrder.setStudy(study);
		
		assertTrue(radiologyOrder.isInProgress());
	}
	
	/**
	 * @see RadiologyOrder#isNotInProgress()
	 * @verifies return true if associated study is null
	 */
	@Test
	public void isNotInProgress_shouldReturnTrueIfAssociatedStudyIsNull() throws Exception {
		
		RadiologyOrder radiologyOrder = new RadiologyOrder();
		radiologyOrder.setStudy(null);
		
		assertTrue(radiologyOrder.isNotInProgress());
	}
	
	/**
	 * @see RadiologyOrder#isNotInProgress()
	 * @verifies return true if associated study is not in progress
	 */
	@Test
	public void isNotInProgress_shouldReturnTrueIfAssociatedStudyIsNotInProgress() throws Exception {
		
		RadiologyOrder radiologyOrder = new RadiologyOrder();
		Study study = new Study();
		study.setPerformedStatus(PerformedProcedureStepStatus.COMPLETED);
		radiologyOrder.setStudy(study);
		
		assertTrue(radiologyOrder.isNotInProgress());
	}
	
	/**
	 * @see RadiologyOrder#isNotInProgress()
	 * @verifies return false if associated study in progress
	 */
	@Test
	public void isNotInProgress_shouldReturnFalseIfAssociatedStudyInProgress() throws Exception {
		
		RadiologyOrder radiologyOrder = new RadiologyOrder();
		Study study = new Study();
		study.setPerformedStatus(PerformedProcedureStepStatus.IN_PROGRESS);
		radiologyOrder.setStudy(study);
		
		assertFalse(radiologyOrder.isNotInProgress());
	}
	
	/**
	 * @see RadiologyOrder#isCompleted()
	 * @verifies return false if associated study is null
	 */
	@Test
	public void isCompleted_shouldReturnFalseIfAssociatedStudyIsNull() throws Exception {
		
		RadiologyOrder radiologyOrder = new RadiologyOrder();
		radiologyOrder.setStudy(null);
		
		assertFalse(radiologyOrder.isCompleted());
	}
	
	/**
	 * @see RadiologyOrder#isCompleted()
	 * @verifies return false if associated study is not completed
	 */
	@Test
	public void isCompleted_shouldReturnFalseIfAssociatedStudyIsNotCompleted() throws Exception {
		
		RadiologyOrder radiologyOrder = new RadiologyOrder();
		radiologyOrder.setStudy(new Study());
		
		assertFalse(radiologyOrder.isCompleted());
	}
	
	/**
	 * @see RadiologyOrder#isCompleted()
	 * @verifies return true if associated study is completed
	 */
	@Test
	public void isCompleted_shouldReturnTrueIfAssociatedStudyIsCompleted() throws Exception {
		
		RadiologyOrder radiologyOrder = new RadiologyOrder();
		Study study = new Study();
		study.setPerformedStatus(PerformedProcedureStepStatus.COMPLETED);
		radiologyOrder.setStudy(study);
		
		assertTrue(radiologyOrder.isCompleted());
	}
	
	/**
	 * @see RadiologyOrder#isNotCompleted()
	 * @verifies return true if associated study is null
	 */
	@Test
	public void isNotCompleted_shouldReturnTrueIfAssociatedStudyIsNull() throws Exception {
		
		RadiologyOrder radiologyOrder = new RadiologyOrder();
		radiologyOrder.setStudy(null);
		
		assertTrue(radiologyOrder.isNotCompleted());
	}
	
	/**
	 * @see RadiologyOrder#isNotCompleted()
	 * @verifies return true if associated study is not completed
	 */
	@Test
	public void isNotCompleted_shouldReturnTrueIfAssociatedStudyIsNotCompleted() throws Exception {
		
		RadiologyOrder radiologyOrder = new RadiologyOrder();
		radiologyOrder.setStudy(new Study());
		
		assertTrue(radiologyOrder.isNotCompleted());
	}
	
	/**
	 * @see RadiologyOrder#isNotCompleted()
	 * @verifies return false if associated study is completed
	 */
	@Test
	public void isNotCompleted_shouldReturnFalseIfAssociatedStudyIsCompleted() throws Exception {
		
		RadiologyOrder radiologyOrder = new RadiologyOrder();
		Study study = new Study();
		study.setPerformedStatus(PerformedProcedureStepStatus.COMPLETED);
		radiologyOrder.setStudy(study);
		
		assertFalse(radiologyOrder.isNotCompleted());
	}
	
	/**
	 * @see RadiologyOrder#isDiscontinuationAllowed()
	 * @verifies return false if order is not active
	 */
	@Test
	public void isDiscontinuationAllowed_shouldReturnFalseIfOrderIsNotActive() throws Exception {
		
		RadiologyOrder radiologyOrder = new RadiologyOrder();
		Study study = new Study();
		study.setPerformedStatus(PerformedProcedureStepStatus.IN_PROGRESS);
		radiologyOrder.setStudy(study);
		radiologyOrder.setAction(Order.Action.DISCONTINUE);
		
		assertFalse(radiologyOrder.isDiscontinuationAllowed());
	}
	
	/**
	 * @see RadiologyOrder#isDiscontinuationAllowed()
	 * @verifies return false if radiology order is in progress
	 */
	@Test
	public void isDiscontinuationAllowed_shouldReturnFalseIfRadiologyOrderIsInProgress() throws Exception {
		
		RadiologyOrder radiologyOrder = new RadiologyOrder();
		Study study = new Study();
		study.setPerformedStatus(PerformedProcedureStepStatus.IN_PROGRESS);
		radiologyOrder.setStudy(study);
		
		assertFalse(radiologyOrder.isDiscontinuationAllowed());
	}
	
	/**
	 * @see RadiologyOrder#isDiscontinuationAllowed()
	 * @verifies return false if radiology order is completed
	 */
	@Test
	public void isDiscontinuationAllowed_shouldReturnFalseIfRadiologyOrderIsCompleted() throws Exception {
		
		RadiologyOrder radiologyOrder = new RadiologyOrder();
		Study study = new Study();
		study.setPerformedStatus(PerformedProcedureStepStatus.COMPLETED);
		radiologyOrder.setStudy(study);
		
		assertFalse(radiologyOrder.isDiscontinuationAllowed());
	}
	
	/**
	 * @see RadiologyOrder#isDiscontinuationAllowed()
	 * @verifies return true if radiology order is active not in progress and not completed
	 */
	@Test
	public void isDiscontinuationAllowed_shouldReturnTrueIfRadiologyOrderIsActiveNotInProgressAndNotCompleted()
			throws Exception {
		
		RadiologyOrder radiologyOrder = new RadiologyOrder();
		radiologyOrder.setStudy(null);
		
		assertTrue(radiologyOrder.isDiscontinuationAllowed());
	}
}
