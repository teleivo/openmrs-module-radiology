/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, 
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can 
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under 
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 * 
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS 
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.radiology.hl7;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import java.lang.reflect.Field;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.openmrs.Order;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.PatientIdentifierType;
import org.openmrs.PersonName;
import org.openmrs.module.radiology.Modality;
import org.openmrs.module.radiology.RadiologyOrder;
import org.openmrs.module.radiology.Study;
import org.openmrs.test.Verifies;

import ca.uhn.hl7v2.model.DataTypeException;
import ca.uhn.hl7v2.model.v231.datatype.XPN;
import ca.uhn.hl7v2.parser.EncodingCharacters;
import ca.uhn.hl7v2.parser.PipeParser;

/**
 * Tests methods in the {@link HL7Utils}
 */
public class HL7UtilsTest {
	
	private static final EncodingCharacters encodingCharacters = new EncodingCharacters('|', '^', '~', '\\', '&');
	
	@Rule
	public ExpectedException expectedException = ExpectedException.none();
	
	private Patient patient = null;
	
	private Study study = null;
	
	private RadiologyOrder radiologyOrder = null;
	
	@Before
	public void runBeforeEachTest() throws Exception {
		
		patient = new Patient();
		patient.setPatientId(1);
		
		PatientIdentifierType patientIdentifierType = new PatientIdentifierType();
		patientIdentifierType.setPatientIdentifierTypeId(1);
		patientIdentifierType.setName("Test Identifier Type");
		patientIdentifierType.setDescription("Test description");
		PatientIdentifier patientIdentifier = new PatientIdentifier();
		patientIdentifier.setIdentifierType(patientIdentifierType);
		patientIdentifier.setIdentifier("100");
		patientIdentifier.setPreferred(true);
		Set<PatientIdentifier> patientIdentifiers = new HashSet<PatientIdentifier>();
		patientIdentifiers.add(patientIdentifier);
		patient.addIdentifiers(patientIdentifiers);
		
		patient.setGender("M");
		
		Set<PersonName> personNames = new HashSet<PersonName>();
		PersonName personName = new PersonName();
		personName.setFamilyName("Doe");
		personName.setGivenName("John");
		personName.setMiddleName("Francis");
		personNames.add(personName);
		patient.setNames(personNames);
		
		Calendar calendar = Calendar.getInstance();
		calendar.set(1950, Calendar.APRIL, 1, 0, 0, 0);
		patient.setBirthdate(calendar.getTime());
		
		radiologyOrder = new RadiologyOrder();
		radiologyOrder.setOrderId(20);
		
		Field orderNumber = Order.class.getDeclaredField("orderNumber");
		orderNumber.setAccessible(true);
		orderNumber.set(radiologyOrder, "ORD-" + radiologyOrder.getOrderId());
		
		radiologyOrder.setPatient(patient);
		calendar.set(2015, Calendar.FEBRUARY, 4, 14, 35, 0);
		radiologyOrder.setScheduledDate(calendar.getTime());
		radiologyOrder.setUrgency(Order.Urgency.ON_SCHEDULED_DATE);
		radiologyOrder.setInstructions("CT ABDOMEN PANCREAS WITH IV CONTRAST");
		
		study = new Study();
		study.setStudyId(1);
		study.setStudyInstanceUid("1.2.826.0.1.3680043.8.2186.1.1");
		study.setModality(Modality.CT);
		radiologyOrder.setStudy(study);
	}
	
	/**
	 * Test HL7Utils.getExtendedPersonNameFrom
	 * 
	 * @throws DataTypeException
	 * @see {@link HL7Utils#getExtendedPersonNameFrom(PersonName)}
	 */
	@Test
	@Verifies(value = "should return extended person name for given person name with family given and middlename", method = "getExtendedPersonNameFrom(PersonName)")
	public void getExtendedPersonNameFrom_shouldReturnExtendedPersonNameForGivenPersonNameWithFamilyGivenAndMiddleName()
			throws DataTypeException {
		
		PersonName personName = new PersonName();
		personName.setFamilyName("Doe");
		personName.setGivenName("John");
		personName.setMiddleName("Francis");
		
		XPN xpn = HL7Utils.getExtendedPersonNameFrom(personName);
		String extendedPersonName = PipeParser.encode(xpn, encodingCharacters);
		
		assertThat(extendedPersonName, is("Doe^John^Francis"));
	}
	
	/**
	 * Test HL7Utils.getExtendedPersonNameFrom
	 * 
	 * @throws DataTypeException
	 * @see {@link HL7Utils#getExtendedPersonNameFrom(PersonName)}
	 */
	@Test
	@Verifies(value = "should return extended person name for given person name with familyname", method = "getExtendedPersonNameFrom(PersonName)")
	public void getExtendedPersonNameFrom_shouldReturnExtendedPersonNameForGivenPersonNameWithFamilyName()
			throws DataTypeException {
		
		PersonName personName = new PersonName();
		personName.setFamilyName("Doe");
		
		XPN xpn = HL7Utils.getExtendedPersonNameFrom(personName);
		String extendedPersonName = PipeParser.encode(xpn, encodingCharacters);
		
		assertThat(extendedPersonName, is("Doe"));
	}
	
	/**
	 * Test HL7Utils.getExtendedPersonNameFrom
	 * 
	 * @throws DataTypeException
	 * @see {@link HL7Utils#getExtendedPersonNameFrom(PersonName)}
	 */
	@Test
	@Verifies(value = "should return extended person name for given person name with givenname", method = "getExtendedPersonNameFrom(PersonName)")
	public void getExtendedPersonNameFrom_shouldReturnExtendedPersonNameForGivenPersonNameWithGivenName()
			throws DataTypeException {
		
		PersonName personName = new PersonName();
		personName.setGivenName("John");
		
		XPN xpn = HL7Utils.getExtendedPersonNameFrom(personName);
		String extendedPersonName = PipeParser.encode(xpn, encodingCharacters);
		
		assertThat(extendedPersonName, is("^John"));
	}
	
	/**
	 * Test HL7Utils.getExtendedPersonNameFrom
	 * 
	 * @throws DataTypeException
	 * @see {@link HL7Utils#getExtendedPersonNameFrom(PersonName)}
	 */
	@Test
	@Verifies(value = "should return extended person name for given person name with middlename", method = "getExtendedPersonNameFrom(PersonName)")
	public void getExtendedPersonNameFrom_shouldReturnExtendedPersonNameForGivenPersonNameWithMiddleName()
			throws DataTypeException {
		
		PersonName personName = new PersonName();
		personName.setMiddleName("Francis");
		
		XPN xpn = HL7Utils.getExtendedPersonNameFrom(personName);
		String extendedPersonName = PipeParser.encode(xpn, encodingCharacters);
		
		assertThat(extendedPersonName, is("^^Francis"));
	}
	
	/**
	 * Test HL7Utils.getExtendedPersonNameFrom
	 * 
	 * @throws DataTypeException
	 * @see {@link HL7Utils#getExtendedPersonNameFrom(PersonName)}
	 */
	@Test
	@Verifies(value = "should return extended person name for given person name with family and givenname", method = "getExtendedPersonNameFrom(PersonName)")
	public void getExtendedPersonNameFrom_shouldReturnExtendedPersonNameForGivenPersonNameWithFamilyAndGivenName()
			throws DataTypeException {
		
		PersonName personName = new PersonName();
		personName.setFamilyName("Doe");
		personName.setGivenName("John");
		
		XPN xpn = HL7Utils.getExtendedPersonNameFrom(personName);
		String extendedPersonName = PipeParser.encode(xpn, encodingCharacters);
		
		assertThat(extendedPersonName, is("Doe^John"));
	}
	
	/**
	 * Test HL7Utils.getExtendedPersonNameFrom
	 * 
	 * @throws DataTypeException
	 * @see {@link HL7Utils#getExtendedPersonNameFrom(PersonName)}
	 */
	@Test
	@Verifies(value = "should return extended person name for given empty person name", method = "getExtendedPersonNameFrom(PersonName)")
	public void getExtendedPersonNameFrom_shouldReturnExtendedPersonNameForGivenEmptyPersonName() throws DataTypeException {
		
		PersonName personName = new PersonName();
		
		XPN xpn = HL7Utils.getExtendedPersonNameFrom(personName);
		String extendedPersonName = PipeParser.encode(xpn, encodingCharacters);
		
		assertThat(extendedPersonName, is(""));
	}
	
	/**
	 * Test HL7Utils.getExtendedPersonNameFrom
	 * 
	 * @throws DataTypeException
	 * @see {@link HL7Utils#getExtendedPersonNameFrom(PersonName)}
	 */
	@Test
	@Verifies(value = "should return empty extended person name given null", method = "getExtendedPersonNameFrom(PersonName)")
	public void getExtendedPersonNameFrom_shouldReturnEmptyExtendedPersonNameGivenNull() throws DataTypeException {
		
		XPN xpn = HL7Utils.getExtendedPersonNameFrom(null);
		String extendedPersonName = PipeParser.encode(xpn, encodingCharacters);
		
		assertThat(extendedPersonName, is(""));
	}
	
	/**
	 * @see HL7Utils#convertOrderActionToCommonOrderControl(Order.Action)
	 * @verifies return cancel order given order action discontinue
	 */
	@Test
	public void convertOrderActionToCommonOrderControl_shouldReturnCancelOrderGivenOrderActionDiscontinue() throws Exception {
		
		radiologyOrder.setAction(Order.Action.DISCONTINUE);
		assertThat(HL7Utils.convertOrderActionToCommonOrderControl(radiologyOrder.getAction()),
			is(CommonOrderOrderControl.CANCEL_ORDER));
	}
	
	/**
	 * @see HL7Utils#convertOrderActionToCommonOrderControl(Order.Action)
	 * @verifies return new order given order action new
	 */
	@Test
	public void convertOrderActionToCommonOrderControl_shouldReturnNewOrderGivenOrderActionNew() throws Exception {
		
		radiologyOrder.setAction(Order.Action.NEW);
		assertThat(HL7Utils.convertOrderActionToCommonOrderControl(radiologyOrder.getAction()),
			is(CommonOrderOrderControl.NEW_ORDER));
	}
	
	/**
	 * @see HL7Utils#convertOrderActionToCommonOrderControl(Order.Action)
	 * @verifies throw illegal argument exception given null
	 */
	@Test
	public void convertOrderActionToCommonOrderControl_shouldThrowIllegalArgumentExceptionGivenNull() throws Exception {
		
		expectedException.expect(IllegalArgumentException.class);
		expectedException.expectMessage(is("orderAction cannot be null."));
		HL7Utils.convertOrderActionToCommonOrderControl(null);
	}
	
	/**
	 * @see HL7Utils#convertOrderActionToCommonOrderControl(Order.Action)
	 * @verifies throw unsupported operation exception given order action renew
	 */
	@Test
	public void convertOrderActionToCommonOrderControl_shouldThrowUnsupportedOperationExceptionGivenOrderActionRenew()
			throws Exception {
		
		expectedException.expect(UnsupportedOperationException.class);
		expectedException.expectMessage(is("Order.Action 'RENEW' not supported, can only be NEW or DISCONTINUE."));
		HL7Utils.convertOrderActionToCommonOrderControl(Order.Action.RENEW);
	}
	
	/**
	 * @see HL7Utils#convertOrderActionToCommonOrderControl(Order.Action)
	 * @verifies throw unsupported operation exception given order action revise
	 */
	@Test
	public void convertOrderActionToCommonOrderControl_shouldThrowUnsupportedOperationExceptionGivenOrderActionRevise()
			throws Exception {
		
		expectedException.expect(UnsupportedOperationException.class);
		expectedException.expectMessage(is("Order.Action 'REVISE' not supported, can only be NEW or DISCONTINUE."));
		HL7Utils.convertOrderActionToCommonOrderControl(Order.Action.REVISE);
	}
	
	/**
	 * @see HL7Utils#convertOrderUrgencyToCommonOrderPriority(Order.Urgency)
	 * @verifies return routine given null
	 */
	@Test
	public void convertOrderUrgencyToCommonOrderPriority_shouldReturnRoutineGivenNull() throws Exception {
		
		assertThat(HL7Utils.convertOrderUrgencyToCommonOrderPriority(null), is(CommonOrderPriority.ROUTINE));
	}
	
	/**
	 * @see HL7Utils#convertOrderUrgencyToCommonOrderPriority(Order.Urgency)
	 * @verifies return stat given order urgency stat
	 */
	@Test
	public void convertOrderUrgencyToCommonOrderPriority_shouldReturnStatGivenOrderUrgencyStat() throws Exception {
		
		assertThat(HL7Utils.convertOrderUrgencyToCommonOrderPriority(Order.Urgency.STAT), is(CommonOrderPriority.STAT));
	}
	
	/**
	 * @see HL7Utils#convertOrderUrgencyToCommonOrderPriority(Order.Urgency)
	 * @verifies return routine given order urgency routine
	 */
	@Test
	public void convertOrderUrgencyToCommonOrderPriority_shouldReturnRoutineGivenOrderUrgencyRoutine() throws Exception {
		
		assertThat(HL7Utils.convertOrderUrgencyToCommonOrderPriority(Order.Urgency.ROUTINE), is(CommonOrderPriority.ROUTINE));
	}
	
	/**
	 * @see HL7Utils#convertOrderUrgencyToCommonOrderPriority(Order.Urgency)
	 * @verifies return timing critical given order urgency on scheduled date
	 */
	@Test
	public void convertOrderUrgencyToCommonOrderPriority_shouldReturnTimingCriticalGivenOrderUrgencyOnScheduledDate()
			throws Exception {
		
		assertThat(HL7Utils.convertOrderUrgencyToCommonOrderPriority(Order.Urgency.ON_SCHEDULED_DATE),
			is(CommonOrderPriority.TIMING_CRITICAL));
	}
}
