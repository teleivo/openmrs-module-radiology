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
package org.openmrs.module.radiology.hl7.v231.util;

import org.openmrs.module.radiology.hl7.util.MessageControlIDGenerator;
import org.springframework.stereotype.Component;

/**
 *
 */
@Component
public class DatetimeMessageControlIDGenerator implements MessageControlIDGenerator {
	
	/**
	 * @see org.openmrs.module.radiology.hl7.util.MessageControlIDGenerator#getNewID()
	 */
	@Override
	public String getNewMessageControlID() {
		return String.valueOf(System.currentTimeMillis());
	}
}
