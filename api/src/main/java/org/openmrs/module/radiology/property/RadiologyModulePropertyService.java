package org.openmrs.module.radiology.property;

import org.openmrs.CareSetting;
import org.openmrs.EncounterRole;
import org.openmrs.EncounterType;
import org.openmrs.OrderType;
import org.openmrs.VisitType;
import org.openmrs.api.OpenmrsService;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
public interface RadiologyModulePropertyService extends OpenmrsService {
	
	/**
	 * Return mpps directory.
	 * 
	 * @return mpps directory
	 */
	public String getMppsDir();
	
	/**
	 * Return mwl directory.
	 * 
	 * @return mwl directory
	 */
	public String getMwlDir();
	
	/**
	 * Return PACS address.
	 * 
	 * @return pacs address
	 * @throws IllegalStateException if global property for pacs address cannot be found
	 * @should return pacs address
	 * @should throw illegal state exception if global property for pacs address cannot be found
	 */
	public String getPacsAddress();
	
	/**
	 * Return PACS HL7 port.
	 * 
	 * @return pacs hl7 port
	 * @throws IllegalStateException if global property for pacs hl7 port cannot be found
	 * @should return pacs hl7 port
	 * @should throw illegal state exception if global property for pacs hl7 port cannot be found
	 */
	public String getPacsHL7Port();
	
	/**
	 * Return PACS DICOM AE title.
	 * 
	 * @return pacs dicom ae title
	 * @throws IllegalStateException if global property for pacs dicom ae title cannot be found
	 * @should return pacs dicom ae title
	 * @should throw illegal state exception if global property for pacs dicom ae title cannot be
	 *         found
	 */
	public String getPacsDicomAeTitle();
	
	/**
	 * Return DICOM MPPS port.
	 * 
	 * @return dicom mpps port
	 * @throws IllegalStateException if global property for dicom mpps port cannot be found
	 * @should return pacs dicom mpps port
	 * @should throw illegal state exception if global property for dicom mpps port cannot be found
	 */
	public String getDicomMppsPort();
	
	/**
	 * Return DICOM AE title.
	 * 
	 * @return dicom ae title
	 * @throws IllegalStateException if global property for dicom ae title cannot be found
	 * @should return dicom ae title
	 * @should throw illegal state exception if global property for dicom ae title cannot be found
	 */
	public String getDicomAeTitle();
	
	/**
	 * Return DICOM UID component used to identify the org root.
	 * 
	 * @return dicom uid org root
	 * @throws IllegalStateException if global property for dicom uid org root cannot be found
	 * @should return dicom uid org root
	 * @should throw illegal state exception if global property for dicom uid org root cannot be found
	 */
	public String getDicomUIDOrgRoot();
	
	/**
	 * Return DICOM UID component used to identify this application.
	 * 
	 * @return dicom uid application
	 * @throws IllegalStateException if global property for dicom uid application cannot be found
	 * @should return dicom uid application
	 * @should throw illegal state exception if global property for dicom uid application cannot be found
	 */
	public String getDicomUIDApplication();
	
	/**
	 * Return DICOM UID component used to identify the UID Type Study.
	 * 
	 * @return dicom uid type study
	 * @throws IllegalStateException if global property for dicom uid type study cannot be found
	 * @should return dicom uid type study
	 * @should throw illegal state exception if global property for dicom uid type study cannot be found
	 */
	public String getDicomUIDTypeStudy();
	
	/**
	 * Return DICOM specific character set.
	 * 
	 * @return dicom specific character set
	 * @throws IllegalStateException if global property for dicom specific character set cannot be
	 *         found
	 * @should return dicom specific character set
	 * @should throw illegal state exception if global property for dicom specific character set
	 *         cannot be found
	 */
	public String getDicomSpecificCharacterSet();
	
	/**
	 * Return study prefix Example: 1.2.826.0.1.3680043.8.2186.1. (With last dot)
	 * 
	 * @return study prefix
	 * @should return study prefix consisting of org root and application uid and study uid slug
	 */
	public String getStudyPrefix();
	
	/**
	 * Return DICOM web viewer address.
	 * 
	 * @return DICOM web viewer address
	 * @throws IllegalStateException if global property for dicom web viewer address cannot be found
	 * @should return dicom web viewer address
	 * @should throw illegal state exception if global property for dicom web viewer address cannot
	 *         be found
	 */
	public String getDicomWebViewerAddress();
	
	/**
	 * Return DICOM web viewer port.
	 * 
	 * @return DICOM web viewer port
	 * @throws IllegalStateException if global property for dicom web viewer port cannot be found
	 * @should return dicom web viewer port
	 * @should throw illegal state exception if global property for dicom web viewer port cannot be
	 *         found
	 */
	public String getDicomWebViewerPort();
	
	/**
	 * Return DICOM web viewer base url.
	 * 
	 * @return DICOM web viewer base url
	 * @throws IllegalStateException if global property for dicom web viewer base url cannot be
	 *         found
	 * @should return dicom web viewer base url
	 * @should throw illegal state exception if global property for dicom web viewer base url cannot
	 *         be found
	 */
	public String getDicomWebViewerBaseUrl();
	
	/**
	 * Return DICOM web viewer local server name.
	 * 
	 * @return DICOM web viewer local server name
	 * @should return dicom web viewer local server name
	 */
	public String getDicomWebViewerLocalServerName();
	
	/**
	 * Get CareSetting for RadiologyOrder's
	 * 
	 * @return CareSetting for radiology orders
	 * @should return radiology care setting
	 * @should throw illegal state exception if global property for radiology care setting cannot be
	 *         found
	 * @should throw illegal state exception if radiology care setting cannot be found
	 */
	public CareSetting getRadiologyCareSetting();
	
	/**
	 * Test order type for radiology order
	 * 
	 * @return test order type for radiology order
	 * @should return order type for radiology test orders
	 * @should throw illegal state exception for non existing radiology test order type
	 */
	public OrderType getRadiologyTestOrderType();
	
	/**
	 * Get EncounterType for RadiologyOrder's
	 * 
	 * @return EncounterType for radiology orders
	 * @should return encounter type for radiology orders
	 * @should throw illegal state exception for non existing radiology encounter type
	 */
	public EncounterType getRadiologyOrderEncounterType();
	
	/**
	 * Get EncounterRole for the ordering provider
	 * 
	 * @return EncounterRole for ordering provider
	 * @should return encounter role for ordering provider
	 * @should throw illegal state exception for non existing ordering provider encounter role
	 */
	public EncounterRole getRadiologyOrderingProviderEncounterRole();
	
	/**
	 * Get VisitType for RadiologyOrder's
	 * 
	 * @return visitType for radiology orders
	 * @should return visit type for radiology orders
	 * @should throw illegal state exception for non existing radiology visit type
	 */
	public VisitType getRadiologyVisitType();
	
	/**
	 * Gets the Name of the ConceptClass for the UUID from the config
	 *
	 * @return a String that contains the Names of the ConceptClasses seperated by a comma
	 * @should throw illegal state exception if global property radiologyConceptClasses is null
	 * @should throw illegal state exception if global property radiologyConceptClasses is an empty
	 *         string
	 * @should throw illegal state exception if global property radiologyConceptClasses is badly
	 *         formatted
	 * @should throw illegal state exception if global property radiologyConceptClasses contains a
	 *         UUID not found among ConceptClasses
	 * @should returns comma separated list of ConceptClass names configured via ConceptClass UUIDs
	 *         in global property radiologyConceptClasses
	 */
	public String getRadiologyConceptClassNames();
}
