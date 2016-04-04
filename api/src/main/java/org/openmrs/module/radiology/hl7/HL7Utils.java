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

import org.openmrs.Order;
import org.openmrs.PersonName;

import ca.uhn.hl7v2.model.DataTypeException;
import ca.uhn.hl7v2.model.v231.datatype.XPN;

/**
 * HL7Utils is a utility class containing methods for transforming OpenMRS PersonName into an HL7 conform Extended Person
 * Name, mapping Order.Action and Order.Urgency to HL7 Order Control Code and Priority codes.
 */
public class HL7Utils {
	
	private HL7Utils() {
		// This class is a utility class which should not be instantiated
	};
	
	/**
	 * Map an OpenMRS PersonName to an HL7 conform Extended Person Name (XPN) as defined in HL7
	 * version 2.3.1
	 * 
	 * @param personName PersonName to be mapped
	 * @return an extended person name
	 * @throws DataTypeException
	 * @should return extended person name for given person name with family given and middlename
	 * @should return extended person name for given person name with familyname
	 * @should return extended person name for given person name with givenname
	 * @should return extended person name for given person name with middlename
	 * @should return extended person name for given person name with family and givenname
	 * @should return extended person name for given empty person name
	 * @should return empty extended person name given null
	 */
	public static XPN getExtendedPersonNameFrom(PersonName personName) throws DataTypeException {
		final XPN result = new XPN(null);
		
		if (personName != null) {
			result.getFamilyLastName()
					.getFamilyName()
					.setValue(personName.getFamilyName());
			result.getGivenName()
					.setValue(personName.getGivenName());
			result.getMiddleInitialOrName()
					.setValue(personName.getMiddleName());
		}
		return result;
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
}
