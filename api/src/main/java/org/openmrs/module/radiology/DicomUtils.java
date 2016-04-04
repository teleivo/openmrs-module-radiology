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

import org.apache.log4j.Logger;
import org.dcm4che2.data.DicomElement;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.SpecificCharacterSet;
import org.dcm4che2.data.Tag;
import org.openmrs.api.context.Context;

/**
 * DicomUtils is a utility class helping to process DicomObject's like DICOM MPPS messages and
 * create and send HL7 order messages for RadiologyOrder's
 */
public class DicomUtils {
	
	private static final Logger log = Logger.getLogger(DicomUtils.class);
	
	private DicomUtils() {
		// This class is a utility class which should not be instantiated
	};
	
	private static RadiologyProperties radiologyProperties = Context.getRegisteredComponent("radiologyProperties",
		RadiologyProperties.class);
	
	/**
	 * <p>
	 * Updates the PerformedStatus of an existing Study in the database to the Performed Procedure Step Status of a given
	 * DicomObject containing a DICOM N-CREATE/N-SET command
	 * </p>
	 * 
	 * @param mppsObject the DICOM MPPS object containing a DICOM N-CREATE/N-SET command with DICOM
	 *        tag Performed Procedure Step Status
	 * @should set performed status of an existing study in database to performed procedure step
	 *         status IN_PROGRESS of given mpps object
	 * @should set performed status of an existing study in database to performed procedure step
	 *         status DISCONTINUED of given mpps object
	 * @should set performed status of an existing study in database to performed procedure step
	 *         status COMPLETED of given mpps object
	 * @should not fail if study instance uid referenced in dicom mpps cannot be found
	 */
	public static void updateStudyPerformedStatusByMpps(DicomObject mppsObject) {
		try {
			final String studyInstanceUid = getStudyInstanceUidFromMpps(mppsObject);
			
			final String performedProcedureStepStatusString = getPerformedProcedureStepStatus(mppsObject);
			final PerformedProcedureStepStatus performedProcedureStepStatus = PerformedProcedureStepStatus.getMatchForDisplayName(performedProcedureStepStatusString);
			
			radiologyService().updateStudyPerformedStatus(studyInstanceUid, performedProcedureStepStatus);
			log.info("Received Update from dcm4chee. Updating Performed Procedure Step Status for study :"
					+ studyInstanceUid + " to Status : "
					+ PerformedProcedureStepStatus.getNameOrUnknown(performedProcedureStepStatus));
		}
		catch (NumberFormatException e) {
			log.error("Number can not be parsed");
		}
		catch (Exception e) {
			log.error("Error : " + e.getMessage());
		}
	}
	
	/**
	 * <p>
	 * Gets the Study Instance UID of a DICOM MPPS object
	 * </p>
	 * 
	 * @param mppsObject the DICOM MPPS object containing the Study Instance UID
	 * @should return study instance uid given dicom object
	 * @should return null given dicom mpps object without scheduled step attributes sequence
	 * @should return null given dicom mpps object with scheduled step attributes sequence missing
	 *         study instance uid tag
	 */
	public static String getStudyInstanceUidFromMpps(DicomObject mppsObject) {
		
		final SpecificCharacterSet specificCharacterSet = new SpecificCharacterSet(
				radiologyProperties.getDicomSpecificCharacterSet());
		
		final DicomElement scheduledStepAttributesSequenceElement = mppsObject.get(Tag.ScheduledStepAttributesSequence);
		if (scheduledStepAttributesSequenceElement == null) {
			return null;
		}
		
		final DicomObject scheduledStepAttributesSequence = scheduledStepAttributesSequenceElement.getDicomObject();
		
		final DicomElement studyInstanceUidElement = scheduledStepAttributesSequence.get(Tag.StudyInstanceUID);
		if (studyInstanceUidElement == null) {
			return null;
		}
		
		final String studyInstanceUid = studyInstanceUidElement.getValueAsString(specificCharacterSet, 0);
		
		return studyInstanceUid;
	}
	
	/**
	 * <p>
	 * Gets the Performed Procedure Step Status of a DICOM object
	 * </p>
	 * 
	 * @param dicomObject the DICOM object containing the Performed Procedure Step Status
	 * @should return performed procedure step status given dicom object
	 * @should return null given given dicom object without performed procedure step status
	 */
	public static String getPerformedProcedureStepStatus(DicomObject dicomObject) {
		
		final SpecificCharacterSet specificCharacterSet = new SpecificCharacterSet(
				radiologyProperties.getDicomSpecificCharacterSet());
		
		final DicomElement performedProcedureStepStatusElement = dicomObject.get(Tag.PerformedProcedureStepStatus);
		if (performedProcedureStepStatusElement == null) {
			return null;
		}
		
		final String performedProcedureStepStatus = performedProcedureStepStatusElement.getValueAsString(
			specificCharacterSet, 0);
		
		return performedProcedureStepStatus;
	}
	
	static RadiologyService radiologyService() {
		return Context.getService(RadiologyService.class);
	}
}
