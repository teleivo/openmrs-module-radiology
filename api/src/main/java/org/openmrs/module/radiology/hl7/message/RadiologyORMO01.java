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

import java.util.Date;

import org.openmrs.Order;
import org.openmrs.module.radiology.RadiologyOrder;
import org.openmrs.module.radiology.hl7.CommonOrderOrderControl;
import org.openmrs.module.radiology.hl7.CommonOrderPriority;
import org.openmrs.module.radiology.hl7.custommodel.v231.message.ORM_O01;
import org.openmrs.module.radiology.hl7.segment.RadiologyMSH;
import org.openmrs.module.radiology.hl7.segment.RadiologyOBR;
import org.openmrs.module.radiology.hl7.segment.RadiologyORC;
import org.openmrs.module.radiology.hl7.segment.RadiologyPID;
import org.openmrs.module.radiology.hl7.segment.RadiologyZDS;

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.parser.EncodingCharacters;
import ca.uhn.hl7v2.parser.PipeParser;

/**
 * Translates a <code>RadiologyOrder</code> to an HL7 ORM^O01 message
 */
public class RadiologyORMO01 {
	
	private static final EncodingCharacters encodingCharacters = new EncodingCharacters('|', '^', '~', '\\', '&');
	
	private static final String sendingApplication = "OpenMRSRadiologyModule";
	
	private static final String sendingFacility = "OpenMRS";
	
	private static final String orderMessageType = "ORM";
	
	private static final String orderMessageTriggerEvent = "O01";
	
	private final RadiologyOrder radiologyOrder;
	
	private final CommonOrderOrderControl commonOrderControl;
	
	private final CommonOrderPriority commonOrderPriority;
	
	/**
	 * Constructor for <code>RadiologyORMO01</code>
	 * 
	 * @param radiologyOrder radiology order
	 * @should create new radiology ormo01 object given all params
	 * @should throw illegal argument exception given null
	 * @should throw illegal argument exception if given radiology orders study is null
	 */
	public RadiologyORMO01(Order order) {
		
		if (order == null) {
			throw new IllegalArgumentException("order cannot be null.");
		}
		
		if (order instanceof RadiologyOrder) {
			this.radiologyOrder = (RadiologyOrder) order;
		} else {
			this.radiologyOrder = (RadiologyOrder) order.getPreviousOrder();
		}
		
		if (this.radiologyOrder.getStudy() == null) {
			throw new IllegalArgumentException("radiologyOrder.study cannot be null.");
		}
		
		this.commonOrderControl = RadiologyORMO01.convertOrderActionToCommonOrderControl(order.getAction());
		this.commonOrderPriority = RadiologyORMO01.convertOrderUrgencyToCommonOrderPriority(this.radiologyOrder.getUrgency());
	}
	
	/**
	 * Get the HL7 Order Control Code component used in an HL7 common order segment (ORC-1 field)
	 * given the Order.Action.
	 * 
	 * @param orderAction Order.Action to be converted to CommonOrderOrderControl
	 * @return CommonOrderOrderControl for given Order.Action
	 * @throws IllegalArgumentException given null
	 * @throws UnsupportedOperationException for Order.Action RENEW or REVISE
	 * @should return new order given order action new
	 * @should return cancel order given order action discontinue
	 * @should throw illegal argument exception given null
	 * @should throw unsupported operation exception given order action renew
	 * @should throw unsupported operation exception given order action revise
	 */
	public static CommonOrderOrderControl convertOrderActionToCommonOrderControl(Order.Action orderAction) {
		final CommonOrderOrderControl result;
		
		if (orderAction == null) {
			throw new IllegalArgumentException("orderAction cannot be null.");
		}
		
		switch (orderAction) {
			case NEW:
				result = CommonOrderOrderControl.NEW_ORDER;
				break;
			case DISCONTINUE:
				result = CommonOrderOrderControl.CANCEL_ORDER;
				break;
			default:
				throw new UnsupportedOperationException("Order.Action '" + orderAction
						+ "' not supported, can only be NEW or DISCONTINUE.");
		}
		return result;
	}
	
	/**
	 * Get the HL7 Priority component of Quantity/Timing (ORC-7) field included in an HL7 version
	 * 2.3.1 Common Order segment given the Order.Urgency.
	 * 
	 * @param orderUrgency Order.Urgency to be converted to CommonOrderPriority
	 * @return CommonOrderPriority for given Order.Urgency
	 * @should return routine given null
	 * @should return stat given order urgency stat
	 * @should return routine given order urgency routine
	 * @should return timing critical given order urgency on scheduled date
	 */
	public static CommonOrderPriority convertOrderUrgencyToCommonOrderPriority(Order.Urgency orderUrgency) {
		final CommonOrderPriority result;
		
		if (orderUrgency == null) {
			result = CommonOrderPriority.ROUTINE;
		} else {
			switch (orderUrgency) {
				case STAT:
					result = CommonOrderPriority.STAT;
					break;
				case ROUTINE:
					result = CommonOrderPriority.ROUTINE;
					break;
				case ON_SCHEDULED_DATE:
					result = CommonOrderPriority.TIMING_CRITICAL;
					break;
				default:
					result = CommonOrderPriority.ROUTINE;
					break;
			}
		}
		return result;
	}
	
	/**
	 * Create an encoded HL7 ORM^O01 message (version 2.3.1) from this <code>RadiologyORMO01</code>
	 * 
	 * @return encoded HL7 ORM^O01 message
	 * @throws HL7Exception
	 * @should create encoded hl7 ormo01 message
	 */
	public String encode() throws HL7Exception {
		
		return PipeParser.encode(createRadiologyORMO01Message(), encodingCharacters);
	}
	
	/**
	 * Create <code>ORM_O01</code> message for this <code>RadiologyORMO01</code>
	 * 
	 * @return ORM_O01 message
	 * @throws HL7Exception
	 * @should create ormo01 message
	 */
	private ORM_O01 createRadiologyORMO01Message() throws HL7Exception {
		
		final ORM_O01 result = new ORM_O01();
		
		RadiologyMSH.populateMessageHeader(result.getMSH(), sendingApplication, sendingFacility, new Date(),
			orderMessageType, orderMessageTriggerEvent);
		
		RadiologyPID.populatePatientIdentifier(result.getPIDPD1NTEPV1PV2IN1IN2IN3GT1AL1()
				.getPID(), radiologyOrder.getPatient());
		
		RadiologyORC.populateCommonOrder(result.getORCOBRRQDRQ1ODSODTRXONTEDG1RXRRXCNTEOBXNTECTIBLG()
				.getORC(), radiologyOrder, commonOrderControl, commonOrderPriority);
		
		RadiologyOBR.populateObservationRequest(result.getORCOBRRQDRQ1ODSODTRXONTEDG1RXRRXCNTEOBXNTECTIBLG()
				.getOBRRQDRQ1ODSODTRXONTEDG1RXRRXCNTEOBXNTE()
				.getOBR(), radiologyOrder);
		
		RadiologyZDS.populateZDSSegment(result.getZDS(), radiologyOrder.getStudy());
		
		return result;
	}
}
