/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, 
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can 
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under 
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 * 
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS 
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.radiology.hl7.message;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.nullValue;
import static org.hamcrest.core.StringEndsWith.endsWith;
import static org.hamcrest.core.StringStartsWith.startsWith;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
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
import org.openmrs.module.radiology.MwlStatus;
import org.openmrs.module.radiology.RadiologyOrder;
import org.openmrs.module.radiology.Study;
import org.openmrs.module.radiology.hl7.CommonOrderOrderControl;
import org.openmrs.module.radiology.hl7.CommonOrderPriority;
import org.openmrs.module.radiology.hl7.custommodel.v231.message.ORM_O01;
import org.springframework.util.ReflectionUtils;

import ca.uhn.hl7v2.parser.EncodingCharacters;
import ca.uhn.hl7v2.parser.PipeParser;

/**
 * Tests {@link RadiologyORMO01}
 */
public class RadiologyORMO01Test {
	
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
	 * @see RadiologyORMO01#RadiologyORMO01(RadiologyOrder)
	 * @verifies create new radiology ormo01 object given all params
	 */
	@Test
	public void RadiologyORMO01_shouldCreateNewRadiologyOrmo01ObjectGivenAllParams() throws Exception {
		
		RadiologyORMO01 radiologyOrderMessage = new RadiologyORMO01(radiologyOrder);
		assertNotNull(radiologyOrderMessage);
	}
	
	/**
	 * @see RadiologyORMO01#RadiologyORMO01(RadiologyOrder)
	 * @verifies throw illegal argument exception given null as radiologyOrder
	 */
	@Test
	public void RadiologyORMO01_shouldThrowIllegalArgumentExceptionGivenNullAsRadiologyOrder() throws Exception {
		
		expectedException.expect(IllegalArgumentException.class);
		expectedException.expectMessage(is("radiologyOrder cannot be null."));
		new RadiologyORMO01(null);
	}
	
	/**
	 * @see RadiologyORMO01#RadiologyORMO01(RadiologyOrder)
	 * @verifies throw illegal argument exception if given radiology orders study is null
	 */
	@Test
	public void RadiologyORMO01_shouldThrowIllegalArgumentExceptionIfGivenRadiologyOrdersStudyIsNull() throws Exception {
		radiologyOrder.setStudy(null);
		
		expectedException.expect(IllegalArgumentException.class);
		expectedException.expectMessage(is("radiologyOrder.study cannot be null."));
		new RadiologyORMO01(radiologyOrder);
	}
	
	/**
	 * @see RadiologyORMO01#encode()
	 * @verifies create encoded hl7 ormo01 message
	 */
	@Test
	public void encode_shouldCreateEncodedHl7Ormo01Message() throws Exception {
		
		RadiologyORMO01 radiologyOrderMessage = new RadiologyORMO01(radiologyOrder);
		String encodedOrmMessage = radiologyOrderMessage.encode();
		
		assertThat(encodedOrmMessage, startsWith("MSH|^~\\&|OpenMRSRadiologyModule|OpenMRS|||"));
		assertThat(
			encodedOrmMessage,
			endsWith("||ORM^O01||P|2.3.1\r"
					+ "PID|||100||Doe^John^Francis||19500401000000|M\r"
					+ "ORC|NW|ORD-20|||||^^^20150204143500^^S\r"
					+ "OBR||||^^^^CT ABDOMEN PANCREAS WITH IV CONTRAST|||||||||||||||ORD-20|1||||CT||||||||||||||||||||^CT ABDOMEN PANCREAS WITH IV CONTRAST\r"
					+ "ZDS|1.2.826.0.1.3680043.8.2186.1.1^^Application^DICOM\r"));
	}
	
	/**
	 * @see RadiologyORMO01#createRadiologyORMO01Message()
	 * @verifies create ormo01 message
	 */
	@Test
	public void createRadiologyORMO01Message_shouldCreateOrmo01Message() throws Exception {
		
		RadiologyORMO01 radiologyOrderMessage = new RadiologyORMO01(radiologyOrder);
		
		Method createRadiologyORMO01Message = ReflectionUtils.findMethod(RadiologyORMO01.class,
			"createRadiologyORMO01Message");
		createRadiologyORMO01Message.setAccessible(true);
		ORM_O01 ormO01 = (ORM_O01) createRadiologyORMO01Message.invoke(radiologyOrderMessage);
		String encodedOrmMessage = PipeParser.encode(ormO01, encodingCharacters);
		
		assertThat(encodedOrmMessage, startsWith("MSH|^~\\&|OpenMRSRadiologyModule|OpenMRS|||"));
		assertThat(
			encodedOrmMessage,
			endsWith("||ORM^O01||P|2.3.1\r"
					+ "PID|||100||Doe^John^Francis||19500401000000|M\r"
					+ "ORC|NW|ORD-20|||||^^^20150204143500^^S\r"
					+ "OBR||||^^^^CT ABDOMEN PANCREAS WITH IV CONTRAST|||||||||||||||ORD-20|1||||CT||||||||||||||||||||^CT ABDOMEN PANCREAS WITH IV CONTRAST\r"
					+ "ZDS|1.2.826.0.1.3680043.8.2186.1.1^^Application^DICOM\r"));
	}
	
	/**
	 * @see {@link RadiologyORMO01#convertOrderUrgencyToCommonOrderPriority(Order.Urgency)}
	 * @verifies should return hl7 common order priority given order urgency
	 */
	@Test
	public void getCommonOrderPriorityFrom_shouldReturnHL7CommonOrderPriorityGivenOrderUrgency() {
		
		assertThat(RadiologyORMO01.convertOrderUrgencyToCommonOrderPriority(Order.Urgency.STAT),
			is(CommonOrderPriority.STAT));
		assertThat(RadiologyORMO01.convertOrderUrgencyToCommonOrderPriority(Order.Urgency.ROUTINE),
			is(CommonOrderPriority.ROUTINE));
		assertThat(RadiologyORMO01.convertOrderUrgencyToCommonOrderPriority(Order.Urgency.ON_SCHEDULED_DATE),
			is(CommonOrderPriority.TIMING_CRITICAL));
	}
	
	/**
	 * @see {@link RadiologyORMO01#convertOrderUrgencyToCommonOrderPriority(Order.Urgency)}
	 * @verifies should return default hl7 common order priority given null
	 */
	@Test
	public void convertOrderUrgencyToCommonOrderPriority_shouldReturnDefaultHL7CommonOrderPriorityGivenNull() {
		
		assertThat(RadiologyORMO01.convertOrderUrgencyToCommonOrderPriority(null), is(CommonOrderPriority.ROUTINE));
	}
	
	/**
	 * @see RadiologyORMO01#convertOrderActionToCommonOrderControl(Action)
	 * @verifies return cancel order given order action discontinue
	 */
	@Test
	public void convertOrderActionToCommonOrderControl_shouldReturnCancelOrderGivenOrderActionDiscontinue() throws Exception {
		
		radiologyOrder.setAction(Order.Action.DISCONTINUE);
		assertThat(RadiologyORMO01.convertOrderActionToCommonOrderControl(radiologyOrder.getAction()),
			is(CommonOrderOrderControl.CANCEL_ORDER));
	}
	
	/**
	 * @see RadiologyORMO01#convertOrderActionToCommonOrderControl(Action)
	 * @verifies return new order given order action new
	 */
	@Test
	public void convertOrderActionToCommonOrderControl_shouldReturnNewOrderGivenOrderActionNew() throws Exception {
		
		radiologyOrder.setAction(Order.Action.NEW);
		assertThat(RadiologyORMO01.convertOrderActionToCommonOrderControl(radiologyOrder.getAction()),
			is(CommonOrderOrderControl.NEW_ORDER));
	}
	
	/**
	 * @see RadiologyORMO01#convertOrderActionToCommonOrderControl(Action)
	 * @verifies return null given any other order action
	 */
	@Test
	public void convertOrderActionToCommonOrderControl_shouldReturnNullGivenAnyOtherOrderAction() throws Exception {
		
		radiologyOrder.setAction(Order.Action.RENEW);
		assertThat(RadiologyORMO01.convertOrderActionToCommonOrderControl(radiologyOrder.getAction()), is(nullValue()));
		
		radiologyOrder.setAction(Order.Action.REVISE);
		assertThat(RadiologyORMO01.convertOrderActionToCommonOrderControl(radiologyOrder.getAction()), is(nullValue()));
	}
	
	/**
	 * @see RadiologyORMO01#convertOrderActionToCommonOrderControl(Action)
	 * @verifies return new order for order action new
	 */
	@Test
	public void convertOrderActionToCommonOrderControl_shouldReturnNewOrderForOrderActionNew() throws Exception {
		
		assertThat(RadiologyORMO01.convertOrderActionToCommonOrderControl(null), is(nullValue()));
	}
	
	/**
	 * @see RadiologyORMO01#convertOrderActionToCommonOrderControl(Action)
	 * @verifies return null given null
	 */
	@Test
	public void convertOrderActionToCommonOrderControl_shouldReturnNullGivenNull() throws Exception {
		
		assertThat(RadiologyORMO01.convertOrderActionToCommonOrderControl(null), is(nullValue()));
	}
	
	/**
	 * @see RadiologyORMO01#convertOrderUrgencyToCommonOrderPriority(Urgency)
	 * @verifies return routine given null
	 */
	@Test
	public void convertOrderUrgencyToCommonOrderPriority_shouldReturnRoutineGivenNull() throws Exception {
		
		assertThat(RadiologyORMO01.convertOrderUrgencyToCommonOrderPriority(null), is(CommonOrderPriority.ROUTINE));
	}
	
	/**
	 * @see RadiologyORMO01#convertOrderUrgencyToCommonOrderPriority(Urgency)
	 * @verifies return stat given order urgency stat
	 */
	@Test
	public void convertOrderUrgencyToCommonOrderPriority_shouldReturnStatGivenOrderUrgencyStat() throws Exception {
		
		assertThat(RadiologyORMO01.convertOrderUrgencyToCommonOrderPriority(Order.Urgency.STAT),
			is(CommonOrderPriority.STAT));
	}
	
	/**
	 * @see RadiologyORMO01#convertOrderUrgencyToCommonOrderPriority(Urgency)
	 * @verifies return routine given order urgency routine
	 */
	@Test
	public void convertOrderUrgencyToCommonOrderPriority_shouldReturnRoutineGivenOrderUrgencyRoutine() throws Exception {
		
		assertThat(RadiologyORMO01.convertOrderUrgencyToCommonOrderPriority(Order.Urgency.ROUTINE),
			is(CommonOrderPriority.ROUTINE));
	}
	
	/**
	 * @see RadiologyORMO01#convertOrderUrgencyToCommonOrderPriority(Urgency)
	 * @verifies return timing critical given order urgency on scheduled date
	 */
	@Test
	public void convertOrderUrgencyToCommonOrderPriority_shouldReturnTimingCriticalGivenOrderUrgencyOnScheduledDate()
			throws Exception {
		
		assertThat(RadiologyORMO01.convertOrderUrgencyToCommonOrderPriority(Order.Urgency.ON_SCHEDULED_DATE),
			is(CommonOrderPriority.TIMING_CRITICAL));
	}
}
