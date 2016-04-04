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
	 * @see RadiologyORMO01#RadiologyORMO01(RadiologyOrder,CommonOrderOrderControl,CommonOrderPriority)
	 * @verifies create new radiology ormo01 object given all params
	 */
	@Test
	public void RadiologyORMO01_shouldCreateNewRadiologyOrmo01ObjectGivenAllParams() throws Exception {
		
		RadiologyORMO01 radiologyOrderMessage = new RadiologyORMO01(radiologyOrder, CommonOrderOrderControl.NEW_ORDER,
				CommonOrderPriority.STAT);
		assertNotNull(radiologyOrderMessage);
	}
	
	/**
	 * @see RadiologyORMO01#RadiologyORMO01(RadiologyOrder,CommonOrderOrderControl,CommonOrderPriority)
	 * @verifies throw illegal argument exception given null as radiologyOrder
	 */
	@Test
	public void RadiologyORMO01_shouldThrowIllegalArgumentExceptionGivenNullAsRadiologyOrder() throws Exception {
		
		expectedException.expect(IllegalArgumentException.class);
		expectedException.expectMessage(is("radiologyOrder cannot be null."));
		new RadiologyORMO01(null, CommonOrderOrderControl.NEW_ORDER, CommonOrderPriority.STAT);
	}
	
	/**
	 * @see RadiologyORMO01#RadiologyORMO01(RadiologyOrder,CommonOrderOrderControl,CommonOrderPriority)
	 * @verifies throw illegal argument exception if given radiology orders study is null
	 */
	@Test
	public void RadiologyORMO01_shouldThrowIllegalArgumentExceptionIfGivenRadiologyOrdersStudyIsNull() throws Exception {
		radiologyOrder.setStudy(null);
		
		expectedException.expect(IllegalArgumentException.class);
		expectedException.expectMessage(is("radiologyOrder.study cannot be null."));
		new RadiologyORMO01(radiologyOrder, CommonOrderOrderControl.NEW_ORDER, CommonOrderPriority.STAT);
	}
	
	/**
	 * @see RadiologyORMO01#RadiologyORMO01(RadiologyOrder,CommonOrderOrderControl,CommonOrderPriority)
	 * @verifies throw illegal argument exception given null as orderControlCode
	 */
	@Test
	public void RadiologyORMO01_shouldThrowIllegalArgumentExceptionGivenNullAsOrderControlCode() throws Exception {
		
		expectedException.expect(IllegalArgumentException.class);
		expectedException.expectMessage(is("orderControlCode cannot be null."));
		new RadiologyORMO01(radiologyOrder, null, CommonOrderPriority.STAT);
	}
	
	/**
	 * @see RadiologyORMO01#RadiologyORMO01(RadiologyOrder,CommonOrderOrderControl,CommonOrderPriority)
	 * @verifies throw illegal argument exception given null as orderControlPriority
	 */
	@Test
	public void RadiologyORMO01_shouldThrowIllegalArgumentExceptionGivenNullAsOrderControlPriority() throws Exception {
		
		expectedException.expect(IllegalArgumentException.class);
		expectedException.expectMessage(is("orderControlPriority cannot be null."));
		new RadiologyORMO01(radiologyOrder, CommonOrderOrderControl.NEW_ORDER, null);
	}
	
	/**
	 * @see RadiologyORMO01#encode()
	 * @verifies create encoded hl7 ormo01 message
	 */
	@Test
	public void encode_shouldCreateEncodedHl7Ormo01Message() throws Exception {
		
		RadiologyORMO01 radiologyOrderMessage = new RadiologyORMO01(radiologyOrder, CommonOrderOrderControl.NEW_ORDER,
				CommonOrderPriority.STAT);
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
		
		RadiologyORMO01 radiologyOrderMessage = new RadiologyORMO01(radiologyOrder, CommonOrderOrderControl.NEW_ORDER,
				CommonOrderPriority.STAT);
		
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
}
