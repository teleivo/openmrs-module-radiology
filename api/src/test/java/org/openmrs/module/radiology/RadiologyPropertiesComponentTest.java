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

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;
import org.openmrs.ConceptClass;
import org.openmrs.EncounterRole;
import org.openmrs.EncounterType;
import org.openmrs.GlobalProperty;
import org.openmrs.OrderType;
import org.openmrs.VisitType;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.ConceptService;
import org.openmrs.api.EncounterService;
import org.openmrs.api.OrderService;
import org.openmrs.api.VisitService;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.openmrs.util.OpenmrsUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

/**
 * Tests {@link RadiologyProperties}
 */
public class RadiologyPropertiesComponentTest extends BaseModuleContextSensitiveTest {
    
    
    @Autowired
    @Qualifier("adminService")
    private AdministrationService administrationService;
    
    @Autowired
    private OrderService orderService;
    
    @Autowired
    private RadiologyProperties radiologyProperties;
    
    @Autowired
    private EncounterService encounterService;
    
    @Autowired
    private ConceptService conceptService;
    
    @Autowired
    private VisitService visitService;
    
    @Rule
    public ExpectedException expectedException = ExpectedException.none();
    
    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();
    
    private Method getGlobalPropertyMethod = null;
    
    @Before
    public void setUp() throws Exception {
        
        getGlobalPropertyMethod = RadiologyProperties.class.getDeclaredMethod("getGlobalProperty",
            new Class[] { String.class, boolean.class });
        getGlobalPropertyMethod.setAccessible(true);
    }
    
    /**
     * @see RadiologyProperties#getDicomUIDOrgRoot()
     * @verifies return dicom uid org root
     */
    @Test
    public void getDicomUIDOrgRoot_shouldReturnDicomUidOrgRoot() throws Exception {
        
        administrationService.saveGlobalProperty(
            new GlobalProperty(RadiologyConstants.GP_DICOM_UID_ORG_ROOT, "1.2.826.0.1.3680043.8.2186"));
        
        assertThat(radiologyProperties.getDicomUIDOrgRoot(), is("1.2.826.0.1.3680043.8.2186"));
    }
    
    /**
     * @see RadiologyProperties#getDicomUIDOrgRoot()
     * @verifies throw illegal state exception if global property for dicom uid org root cannot be found
     */
    @Test
    public void getDicomUIDOrgRoot_shouldThrowIllegalStateExceptionIfGlobalPropertyForDicomUidOrgRootCannotBeFound()
            throws Exception {
        
        expectedException.expect(IllegalStateException.class);
        expectedException.expectMessage("Configuration required: " + RadiologyConstants.GP_DICOM_UID_ORG_ROOT);
        
        radiologyProperties.getDicomUIDOrgRoot();
    }
    
    /**
     * @see RadiologyProperties#getDicomWebViewerAddress()
     * @verifies return dicom web viewer address
     */
    @Test
    public void getDicomWebViewerAddress_shouldReturnDicomWebViewerAddress() throws Exception {
        
        administrationService
                .saveGlobalProperty(new GlobalProperty(RadiologyConstants.GP_DICOM_WEB_VIEWER_ADDRESS, "localhost"));
        
        assertThat(radiologyProperties.getDicomWebViewerAddress(), is("localhost"));
    }
    
    /**
     * @see RadiologyProperties#getDicomWebViewerAddress()
     * @verifies throw illegal state exception if global property for dicom web viewer address
     *           cannot be found
     */
    @Test
    public void
            getDicomWebViewerAddress_shouldThrowIllegalStateExceptionIfGlobalPropertyForDicomWebViewerAddressCannotBeFound()
                    throws Exception {
        
        expectedException.expect(IllegalStateException.class);
        expectedException.expectMessage("Configuration required: " + RadiologyConstants.GP_DICOM_WEB_VIEWER_ADDRESS);
        
        radiologyProperties.getDicomWebViewerAddress();
    }
    
    /**
     * @see RadiologyProperties#getDicomWebViewerPort()
     * @verifies return dicom web viewer port
     */
    @Test
    public void getDicomWebViewerPort_shouldReturnDicomWebViewerPort() throws Exception {
        
        administrationService.saveGlobalProperty(new GlobalProperty(RadiologyConstants.GP_DICOM_WEB_VIEWER_PORT, "8081"));
        
        assertThat(radiologyProperties.getDicomWebViewerPort(), is("8081"));
    }
    
    /**
     * @see RadiologyProperties#getDicomWebViewerPort()
     * @verifies throw illegal state exception if global property for dicom web viewer port cannot
     *           be found
     */
    @Test
    public void getDicomWebViewerPort_shouldThrowIllegalStateExceptionIfGlobalPropertyForDicomWebViewerPortCannotBeFound()
            throws Exception {
        
        expectedException.expect(IllegalStateException.class);
        expectedException.expectMessage("Configuration required: " + RadiologyConstants.GP_DICOM_WEB_VIEWER_PORT);
        
        radiologyProperties.getDicomWebViewerPort();
    }
    
    /**
     * @see RadiologyProperties#getDicomWebViewerBaseUrl()
     * @verifies return dicom web viewer base url
     */
    @Test
    public void getDicomWebViewerBaseUrl_shouldReturnDicomWebViewerBaseUrl() throws Exception {
        
        administrationService.saveGlobalProperty(
            new GlobalProperty(RadiologyConstants.GP_DICOM_WEB_VIEWER_BASE_URL, "/weasis-pacs-connector/viewer"));
        
        assertThat(radiologyProperties.getDicomWebViewerBaseUrl(), is("/weasis-pacs-connector/viewer"));
    }
    
    /**
     * @see RadiologyProperties#getDicomWebViewerBaseUrl()
     * @verifies throw illegal state exception if global property for dicom web viewer base url
     *           cannot be found
     */
    @Test
    public void
            getDicomWebViewerBaseUrl_shouldThrowIllegalStateExceptionIfGlobalPropertyForDicomWebViewerBaseUrlCannotBeFound()
                    throws Exception {
        
        expectedException.expect(IllegalStateException.class);
        expectedException.expectMessage("Configuration required: " + RadiologyConstants.GP_DICOM_WEB_VIEWER_BASE_URL);
        
        radiologyProperties.getDicomWebViewerBaseUrl();
    }
    
    /**
     * @see RadiologyProperties#getDicomWebViewerLocalServerName()
     * @verifies return dicom web viewer local server name
     */
    @Test
    public void getDicomWebViewerLocalServerName_shouldReturnDicomWebViewerLocalServerName() throws Exception {
        
        administrationService.saveGlobalProperty(
            new GlobalProperty(RadiologyConstants.GP_DICOM_WEB_VIEWER_LOCAL_SERVER_NAME, "oviyamlocal"));
        
        assertThat(radiologyProperties.getDicomWebViewerLocalServerName(), is("oviyamlocal"));
    }
    
    /**
     * @see RadiologyProperties#getRadiologyCareSetting()
     * @verifies return radiology care setting
     */
    @Test
    public void getRadiologyCareSetting_shouldReturnRadiologyCareSetting() {
        
        String outpatientCareSettingUuidInOpenMrsCore = "6f0c9a92-6f24-11e3-af88-005056821db0";
        administrationService.saveGlobalProperty(
            new GlobalProperty(RadiologyConstants.GP_RADIOLOGY_CARE_SETTING, outpatientCareSettingUuidInOpenMrsCore));
        
        assertThat(radiologyProperties.getRadiologyCareSetting()
                .getUuid(),
            is(outpatientCareSettingUuidInOpenMrsCore));
    }
    
    /**
     * @see RadiologyProperties#getRadiologyCareSetting()
     * @verifies throw illegal state exception if global property for radiology care setting cannot
     *           be found
     */
    @Test
    public void
            getRadiologyCareSetting_shouldThrowIllegalStateExceptionIfGlobalPropertyForRadiologyCareSettingCannotBeFound()
                    throws Exception {
        
        expectedException.expect(IllegalStateException.class);
        expectedException.expectMessage("Configuration required: " + RadiologyConstants.GP_RADIOLOGY_CARE_SETTING);
        
        radiologyProperties.getRadiologyCareSetting();
    }
    
    /**
     * @see RadiologyProperties#getRadiologyCareSetting()
     * @verifies throw illegal state exception if radiology care setting cannot be found
     */
    @Test
    public void getRadiologyCareSetting_shouldThrowIllegalStateExceptionIfRadiologyCareSettingCannotBeFound() {
        
        String nonExistingCareSettingUuid = "5a1b8b43-6f24-11e3-af99-005056821db0";
        administrationService.saveGlobalProperty(
            new GlobalProperty(RadiologyConstants.GP_RADIOLOGY_CARE_SETTING, nonExistingCareSettingUuid));
        
        expectedException.expect(IllegalStateException.class);
        expectedException
                .expectMessage("No existing care setting for uuid: " + RadiologyConstants.GP_RADIOLOGY_CARE_SETTING);
        
        radiologyProperties.getRadiologyCareSetting();
    }
    
    /**
     * @see RadiologyProperties#getRadiologyTestOrderType()
     * @verifies return order type for radiology test orders
     */
    @Test
    public void getRadiologyTestOrderType_shouldReturnOrderTypeForRadiologyTestOrders() {
        
        String radiologyTestOrderTypeUuid = "dbdb9a9b-56ea-11e5-a47f-08002719a237";
        administrationService.saveGlobalProperty(
            new GlobalProperty(RadiologyConstants.GP_RADIOLOGY_TEST_ORDER_TYPE, radiologyTestOrderTypeUuid));
        
        OrderType radiologyOrderType = new OrderType("Radiology Order", "Order type for radiology exams",
                "org.openmrs.module.radiology.order.RadiologyOrder");
        radiologyOrderType.setUuid(radiologyTestOrderTypeUuid);
        orderService.saveOrderType(radiologyOrderType);
        
        assertThat(radiologyProperties.getRadiologyTestOrderType()
                .getName(),
            is("Radiology Order"));
        assertThat(radiologyProperties.getRadiologyTestOrderType()
                .getUuid(),
            is(radiologyTestOrderTypeUuid));
    }
    
    /**
     * @see RadiologyProperties#getRadiologyTestOrderType()
     * @verifies throw illegal state exception for non existing radiology test order type
     */
    @Test
    public void getRadiologyTestOrderType_shouldThrowIllegalStateExceptionForNonExistingRadiologyTestOrderType() {
        
        expectedException.expect(IllegalStateException.class);
        expectedException.expectMessage("Configuration required: " + RadiologyConstants.GP_RADIOLOGY_TEST_ORDER_TYPE);
        
        radiologyProperties.getRadiologyTestOrderType();
    }
    
    /**
     * @see RadiologyProperties#getRadiologyOrderEncounterType()
     * @verifies return encounter type for radiology orders
     */
    @Test
    public void getRadiologyOrderEncounterType_shouldReturnEncounterTypeForRadiologyOrders() {
        
        String radiologyEncounterTypeUuid = "19db8c0d-3520-48f2-babd-77f2d450e5c7";
        administrationService.saveGlobalProperty(
            new GlobalProperty(RadiologyConstants.GP_RADIOLOGY_ORDER_ENCOUNTER_TYPE, radiologyEncounterTypeUuid));
        
        EncounterType encounterType = new EncounterType("Radiology Order Encounter", "Ordering radiology exams");
        encounterType.setUuid(radiologyEncounterTypeUuid);
        encounterService.saveEncounterType(encounterType);
        
        assertThat(radiologyProperties.getRadiologyOrderEncounterType()
                .getName(),
            is("Radiology Order Encounter"));
        assertThat(radiologyProperties.getRadiologyOrderEncounterType()
                .getUuid(),
            is(radiologyEncounterTypeUuid));
    }
    
    /**
     * @see RadiologyProperties#getRadiologyOrderEncounterType()
     * @verifies throw illegal state exception for non existing radiology encounter type
     */
    @Test
    public void getRadiologyOrderEncounterType_shouldThrowIllegalStateExceptionForNonExistingRadiologyEncounterType() {
        
        expectedException.expect(IllegalStateException.class);
        expectedException.expectMessage("Configuration required: " + RadiologyConstants.GP_RADIOLOGY_ORDER_ENCOUNTER_TYPE);
        
        radiologyProperties.getRadiologyOrderEncounterType();
    }
    
    /**
     * @see RadiologyProperties#getRadiologyOrderingProviderEncounterRole()
     * @verifies return encounter role for ordering provider
     */
    @Test
    public void getRadiologyOrderingProviderEncounterRole_shouldReturnEncounterRoleForOrderingProvider() throws Exception {
        
        String radiologyOrderingProviderEncounterRoleUuid = "13fc9b4a-49ed-429c-9dde-ca005b387a3d";
        administrationService
                .saveGlobalProperty(new GlobalProperty(RadiologyConstants.GP_RADIOLOGY_ORDERING_PROVIDER_ENCOUNTER_ROLE,
                        radiologyOrderingProviderEncounterRoleUuid));
        
        EncounterRole encounterRole = new EncounterRole();
        encounterRole.setName("Radiology Ordering Provider Encounter Role");
        encounterRole.setUuid(radiologyOrderingProviderEncounterRoleUuid);
        encounterService.saveEncounterRole(encounterRole);
        
        assertThat(radiologyProperties.getRadiologyOrderingProviderEncounterRole()
                .getName(),
            is("Radiology Ordering Provider Encounter Role"));
        assertThat(radiologyProperties.getRadiologyOrderingProviderEncounterRole()
                .getUuid(),
            is(radiologyOrderingProviderEncounterRoleUuid));
    }
    
    /**
     * @see RadiologyProperties#getRadiologyOrderingProviderEncounterRole()
     * @verifies throw illegal state exception for non existing ordering provider encounter role
     */
    @Test
    public void
            getRadiologyOrderingProviderEncounterRole_shouldThrowIllegalStateExceptionForNonExistingOrderingProviderEncounterRole()
                    throws Exception {
        expectedException.expect(IllegalStateException.class);
        expectedException.expectMessage(
            "Configuration required: " + RadiologyConstants.GP_RADIOLOGY_ORDERING_PROVIDER_ENCOUNTER_ROLE);
        
        radiologyProperties.getRadiologyOrderingProviderEncounterRole();
    }
    
    /**
     * @see RadiologyProperties#getRadiologyVisitType()
     * @verifies return visit type for radiology orders
     */
    @Test
    public void getRadiologyVisitType_shouldReturnVisitTypeForRadiologyOrders() throws Exception {
        
        String radiologyVisitTypeUuid = "fe898a34-1ade-11e1-9c71-00248140a5eb";
        administrationService
                .saveGlobalProperty(new GlobalProperty(RadiologyConstants.GP_RADIOLOGY_VISIT_TYPE, radiologyVisitTypeUuid));
        
        VisitType visitType = new VisitType();
        visitType.setName("Radiology Visit");
        visitType.setUuid(radiologyVisitTypeUuid);
        visitService.saveVisitType(visitType);
        
        assertThat(radiologyProperties.getRadiologyVisitType()
                .getName(),
            is("Radiology Visit"));
        assertThat(radiologyProperties.getRadiologyVisitType()
                .getUuid(),
            is(radiologyVisitTypeUuid));
    }
    
    /**
     * @see RadiologyProperties#getRadiologyVisitType()
     * @verifies throw illegal state exception for non existing radiology visit type
     */
    @Test
    public void getRadiologyVisitType_shouldThrowIllegalStateExceptionForNonExistingRadiologyVisitType() throws Exception {
        
        expectedException.expect(IllegalStateException.class);
        expectedException.expectMessage("Configuration required: " + RadiologyConstants.GP_RADIOLOGY_VISIT_TYPE);
        
        radiologyProperties.getRadiologyVisitType();
    }
    
    /**
     * @see RadiologyProperties#getRadiologyConceptClassNames()
     * @verifies return comma separated list of concept class names configured via concept class
     *           UUIDs in global property radiology concept classes
     */
    @Test
    public void
            getRadiologyConceptClassNames_shouldReturnCommaSeparatedListOfConceptClassNamesConfiguredViaConceptClassUUIDsInGlobalPropertyRadiologyConceptClasses()
                    throws Exception {
        List<ConceptClass> conceptClasses = new LinkedList<ConceptClass>();
        conceptClasses.add(conceptService.getConceptClassByName("Drug"));
        conceptClasses.add(conceptService.getConceptClassByName("Test"));
        conceptClasses.add(conceptService.getConceptClassByName("Anatomy"));
        conceptClasses.add(conceptService.getConceptClassByName("Question"));
        String uuidFromConceptClasses = "";
        String expectedNames = "";
        for (ConceptClass conceptClass : conceptClasses) {
            if (expectedNames.equals("")) {
                uuidFromConceptClasses = conceptClass.getUuid();
                expectedNames = conceptClass.getName();
            }
            uuidFromConceptClasses = uuidFromConceptClasses + "," + conceptClass.getUuid();
            expectedNames = expectedNames + "," + conceptClass.getName();
        }
        administrationService.setGlobalProperty(RadiologyConstants.GP_RADIOLOGY_CONCEPT_CLASSES, uuidFromConceptClasses);
        assertThat(radiologyProperties.getRadiologyConceptClassNames(), is(expectedNames));
    }
    
    /**
     * @see RadiologyProperties#getRadiologyConceptClassNames()
     * @verifies throw illegal state exception if global property radiology concept classes is null
     */
    @Test
    public void getRadiologyConceptClassNames_shouldThrowIllegalStateExceptionIfGlobalPropertyRadiologyConceptClassesIsNull()
            throws Exception {
        administrationService.setGlobalProperty("radiology.radiologyConceptClasses", null);
        
        expectedException.expect(IllegalStateException.class);
        expectedException.expectMessage("Configuration required: radiology.radiologyConceptClasses");
        
        radiologyProperties.getRadiologyConceptClassNames();
    }
    
    /**
     * @see RadiologyProperties#getRadiologyConceptClassNames()
     * @verifies throw illegal state exception if global property radiology concept classes is an
     *           empty String
     */
    @Test
    public void
            getRadiologyConceptClassNames_shouldThrowIllegalStateExceptionIfGlobalPropertyRadiologyConceptClassesIsAnEmptyString()
                    throws Exception {
        administrationService.setGlobalProperty("radiology.radiologyConceptClasses", "");
        
        expectedException.expect(IllegalStateException.class);
        expectedException.expectMessage("Configuration required: radiology.radiologyConceptClasses");
        
        radiologyProperties.getRadiologyConceptClassNames();
    }
    
    /**
     * @see RadiologyProperties#getRadiologyConceptClassNames()
     * @verifies throw illegal state exception if global property radiology concept classes is badly
     *           formatted
     */
    @Test
    public void
            getRadiologyConceptClassNames_shouldThrowIllegalStateExceptionIfGlobalPropertyRadiologyConceptClassesIsBadlyFormatted()
                    throws Exception {
        administrationService.setGlobalProperty(RadiologyConstants.GP_RADIOLOGY_CONCEPT_CLASSES,
            "AAAA-bbbbb-1111-2222/AAAA-BBBB-2222-3333");
        
        expectedException.expect(IllegalStateException.class);
        expectedException.expectMessage("Property " + RadiologyConstants.GP_RADIOLOGY_CONCEPT_CLASSES
                + " needs to be a comma separated list of concept class UUIDs (allowed characters [a-z][A-Z][0-9][,][-][ ])");
        
        radiologyProperties.getRadiologyConceptClassNames();
    }
    
    /**
     * @see RadiologyProperties#getRadiologyConceptClassNames()
     * @verifies throw illegal state exception if global property radiology concept classes contains a
     *           UUID not found among concept classes
     */
    @Test
    public void
            getRadiologyConceptClassNames_shouldThrowIllegalStateExceptionIfGlobalPropertyRadiologyConceptClassesContainsAUUIDNotFoundAmongConceptClasses()
                    throws Exception {
        
        administrationService.setGlobalProperty(RadiologyConstants.GP_RADIOLOGY_CONCEPT_CLASSES,
            conceptService.getConceptClassByName("Drug")
                    .getUuid() + "5");
        
        expectedException.expect(IllegalStateException.class);
        expectedException.expectMessage("Property " + RadiologyConstants.GP_RADIOLOGY_CONCEPT_CLASSES + " contains UUID");
        
        radiologyProperties.getRadiologyConceptClassNames();
    }
    
    /**
     * @see RadiologyProperties#getRadiologyOrderReasonConceptClassNames()
     * @verifies throw illegal state exception if global property radiology order reason concept classes is badly formatted
     */
    @Test
    public void
            getRadiologyOrderReasonConceptClassNames_shouldThrowIllegalStateExceptionIfGlobalPropertyRadiologyOrderReasonConceptClassesIsBadlyFormatted()
                    throws Exception {
        
        administrationService.setGlobalProperty(RadiologyConstants.GP_RADIOLOGY_ORDER_REASON_CONCEPT_CLASSES,
            "AAAA-bbbbb-1111-2222/AAAA-BBBB-2222-3333");
        
        expectedException.expect(IllegalStateException.class);
        expectedException.expectMessage("Property " + RadiologyConstants.GP_RADIOLOGY_ORDER_REASON_CONCEPT_CLASSES
                + " needs to be a comma separated list of concept class UUIDs (allowed characters [a-z][A-Z][0-9][,][-][ ])");
        
        radiologyProperties.getRadiologyOrderReasonConceptClassNames();
    }
    
    /**
     * @see RadiologyProperties#getRadiologyOrderReasonConceptClassNames()
     * @verifies throw illegal state exception if global property radiology order reason concept classes contains a UUID not
     *           found among concept classes
     */
    @Test
    public void
            getRadiologyOrderReasonConceptClassNames_shouldThrowIllegalStateExceptionIfGlobalPropertyRadiologyOrderReasonConceptClassesContainsAUUIDNotFoundAmongConceptClasses()
                    throws Exception {
        
        administrationService.setGlobalProperty(RadiologyConstants.GP_RADIOLOGY_ORDER_REASON_CONCEPT_CLASSES,
            conceptService.getConceptClassByName("Drug")
                    .getUuid() + "5");
        
        expectedException.expect(IllegalStateException.class);
        expectedException.expectMessage(
            "Property " + RadiologyConstants.GP_RADIOLOGY_ORDER_REASON_CONCEPT_CLASSES + " contains UUID");
        
        radiologyProperties.getRadiologyOrderReasonConceptClassNames();
    }
    
    /**
     * @see RadiologyProperties#getRadiologyOrderReasonConceptClassNames()
     * @verifies return the name of the diagnosis concept class if global property radiology order reason concept classes is
     *           null
     */
    @Test
    public void
            getRadiologyOrderReasonConceptClassNames_shouldReturnTheNameOfTheDiagnosisConceptClassIfGlobalPropertyRadiologyOrderReasonConceptClassesIsNull()
                    throws Exception {
        
        administrationService.setGlobalProperty(RadiologyConstants.GP_RADIOLOGY_ORDER_REASON_CONCEPT_CLASSES, null);
        
        ConceptClass diagnosisConceptClass = conceptService.getConceptClassByName("Diagnosis");
        assertThat(radiologyProperties.getRadiologyOrderReasonConceptClassNames(), is(diagnosisConceptClass.getName()));
    }
    
    /**
     * @see RadiologyProperties#getRadiologyOrderReasonConceptClassNames()
     * @verifies return the name of the diagnosis concept class if global property radiology order reason concept classes is
     *           an empty string
     */
    @Test
    public void
            getRadiologyOrderReasonConceptClassNames_shouldReturnTheNameOfTheDiagnosisConceptClassIfGlobalPropertyRadiologyOrderReasonConceptClassesIsAnEmptyString()
                    throws Exception {
        
        administrationService.setGlobalProperty(RadiologyConstants.GP_RADIOLOGY_ORDER_REASON_CONCEPT_CLASSES, "");
        
        ConceptClass diagnosisConceptClass = conceptService.getConceptClassByName("Diagnosis");
        assertThat(radiologyProperties.getRadiologyOrderReasonConceptClassNames(), is(diagnosisConceptClass.getName()));
    }
    
    /**
     * @see RadiologyProperties#getRadiologyOrderReasonConceptClassNames()
     * @verifies return comma separated list of concept class names configured via concept class UUIDs in global property
     *           radiology order reason concept classes
     */
    @Test
    public void
            getRadiologyOrderReasonConceptClassNames_shouldReturnCommaSeparatedListOfConceptClassNamesConfiguredViaConceptClassUUIDsInGlobalPropertyRadiologyOrderReasonConceptClasses()
                    throws Exception {
        
        List<ConceptClass> conceptClasses = new LinkedList<ConceptClass>();
        conceptClasses.add(conceptService.getConceptClassByName("Drug"));
        conceptClasses.add(conceptService.getConceptClassByName("Test"));
        conceptClasses.add(conceptService.getConceptClassByName("Anatomy"));
        conceptClasses.add(conceptService.getConceptClassByName("Diagnosis"));
        String uuidFromConceptClasses = "";
        String expectedNames = "";
        for (ConceptClass conceptClass : conceptClasses) {
            if (expectedNames.equals("")) {
                uuidFromConceptClasses = conceptClass.getUuid();
                expectedNames = conceptClass.getName();
            }
            uuidFromConceptClasses = uuidFromConceptClasses + "," + conceptClass.getUuid();
            expectedNames = expectedNames + "," + conceptClass.getName();
        }
        administrationService.setGlobalProperty(RadiologyConstants.GP_RADIOLOGY_ORDER_REASON_CONCEPT_CLASSES,
            uuidFromConceptClasses);
        assertThat(radiologyProperties.getRadiologyOrderReasonConceptClassNames(), is(expectedNames));
    }
    
    /**
     * @see RadiologyProperties#getGlobalProperty(String)
     * @verifies return global property given valid global property name
     */
    @Test
    public void getGlobalProperty_shouldReturnGlobalPropertyGivenValidGlobalPropertyName() throws Exception {
        administrationService.saveGlobalProperty(
            new GlobalProperty(RadiologyConstants.GP_DICOM_UID_ORG_ROOT, "1.2.826.0.1.3680043.8.2186"));
        
        String globalProperty = (String) getGlobalPropertyMethod.invoke(radiologyProperties,
            new Object[] { RadiologyConstants.GP_DICOM_UID_ORG_ROOT, true });
        assertThat(globalProperty, is("1.2.826.0.1.3680043.8.2186"));
    }
    
    /**
     * @see RadiologyProperties#getGlobalProperty(String, boolean)
     * @verifies return null given non required and non configured global property
     */
    @Test
    public void getGlobalProperty_shouldReturnNullGivenNonRequiredAndNonConfiguredGlobalProperty() throws Exception {
        
        String globalProperty = (String) getGlobalPropertyMethod.invoke(radiologyProperties,
            new Object[] { RadiologyConstants.GP_DICOM_UID_ORG_ROOT, false });
        assertThat(globalProperty, is(nullValue()));
    }
    
    /**
     * @see RadiologyProperties#getGlobalProperty(String)
     * @verifies throw illegal state exception given required non configured global property
     */
    @Test
    public void getGlobalProperty_shouldThrowIllegalStateExceptionGivenRequiredNonConfiguredGlobalProperty()
            throws Exception {
        expectedException.expect(InvocationTargetException.class);
        
        getGlobalPropertyMethod.invoke(radiologyProperties, new Object[] { RadiologyConstants.GP_DICOM_UID_ORG_ROOT, true });
    }
    
    /**
     * @see RadiologyProperties#getReportTemplateHome()
     * @verifies create a directory under the openmrs application data directory if GP value is relative
     */
    @Test
    public void getReportTemplateHome_shouldCreateADirectoryUnderTheOpenmrsApplicationDataDirectoryIfGPValueIsRelative()
            throws Exception {
        File openmrsApplicationDataDirectory = temporaryFolder.newFolder("openmrs_home");
        OpenmrsUtil.setApplicationDataDirectory(openmrsApplicationDataDirectory.getAbsolutePath());
        administrationService.setGlobalProperty(RadiologyConstants.GP_MRRT_REPORT_TEMPLATE_DIR, "mrrt_templates");
        File templateHome = radiologyProperties.getReportTemplateHome();
        File templateHomeFromGP =
                new File(administrationService.getGlobalProperty(RadiologyConstants.GP_MRRT_REPORT_TEMPLATE_DIR));
        
        assertNotNull(templateHome);
        assertThat(templateHome.exists(), is(true));
        assertThat(templateHome.getName(), is(templateHomeFromGP.getName()));
        assertThat(templateHome.getParentFile()
                .getName(),
            is(openmrsApplicationDataDirectory.getName()));
    }
    
    /**
     * @see RadiologyProperties#getReportTemplateHome()
     * @verifies create a directory at GP value if it is an absolute path
     */
    @Test
    public void getReportTemplateHome_shouldCreateADirectoryAtGPValueIfItIsAnAbsolutePath() throws Exception {
        File tempFolder = temporaryFolder.newFolder("/mrrt_templates");
        administrationService.setGlobalProperty(RadiologyConstants.GP_MRRT_REPORT_TEMPLATE_DIR,
            tempFolder.getAbsolutePath());
        File templateHome = radiologyProperties.getReportTemplateHome();
        File templateHomeFromGP =
                new File(administrationService.getGlobalProperty(RadiologyConstants.GP_MRRT_REPORT_TEMPLATE_DIR));
        
        assertNotNull(templateHome);
        assertThat(templateHome.exists(), is(true));
        assertThat(templateHome.getName(), is(templateHomeFromGP.getName()));
        assertThat(templateHome.getName(), is(tempFolder.getName()));
        assertThat(templateHome.isAbsolute(), is(true));
    }
    
    /**
     * @see RadiologyProperties#getReportTemplateHome()
     * @verifies throw illegal state exception if global property cannot be found
     */
    @Test
    public void getReportTemplateHome_shouldThrowIllegalStateExceptionIfGlobalPropertyCannotBeFound() throws Exception {
        expectedException.expect(IllegalStateException.class);
        radiologyProperties.getReportTemplateHome();
    }

    /**
     * @see RadiologyProperties#getReportHome()
     * @verifies create a directory under the openmrs application data directory if GP value is relative
     */
    @Test
    public void getReportHome_shouldCreateADirectoryUnderTheOpenmrsApplicationDataDirectoryIfGPValueIsRelative()
            throws Exception {
        File openmrsApplicationDataDirectory = temporaryFolder.newFolder("openmrs_home");
        OpenmrsUtil.setApplicationDataDirectory(openmrsApplicationDataDirectory.getAbsolutePath());
        administrationService.setGlobalProperty(RadiologyConstants.GP_RADIOLOGY_REPORTS_DIR, "radiology_reports");
        File reportHome = radiologyProperties.getReportHome();
        File reportHomeFromGP =
                new File(administrationService.getGlobalProperty(RadiologyConstants.GP_RADIOLOGY_REPORTS_DIR));

        assertNotNull(reportHome);
        assertThat(reportHome.exists(), is(true));
        assertThat(reportHome.getName(), is(reportHomeFromGP.getName()));
        assertThat(reportHome.getParentFile()
                        .getName(),
                is(openmrsApplicationDataDirectory.getName()));
    }

    /**
     * @see RadiologyProperties#getReportHome()
     * @verifies creates a directory at GP value if it is an absolute path
     */
    @Test
    public void getReportHome_shouldCreatesADirectoryAtGPValueIfItIsAnAbsolutePath() throws Exception {
        File tempFolder = temporaryFolder.newFolder("/radiology_reports");
        administrationService.setGlobalProperty(RadiologyConstants.GP_RADIOLOGY_REPORTS_DIR, tempFolder.getAbsolutePath());
        File reportHome = radiologyProperties.getReportHome();
        File reportHomeFromGP =
                new File(administrationService.getGlobalProperty(RadiologyConstants.GP_RADIOLOGY_REPORTS_DIR));

        assertNotNull(reportHome);
        assertThat(reportHome.exists(), is(true));
        assertThat(reportHome.getName(), is(reportHomeFromGP.getName()));
        assertThat(reportHome.getName(), is(tempFolder.getName()));
        assertThat(reportHome.isAbsolute(), is(true));
    }

    /**
     * @see RadiologyProperties#getReportHome()
     * @verifies throw illegal state exception if global property cannot be found
     */
    @Test
    public void getReportHome_shouldThrowIllegalStateExceptionIfGlobalPropertyCannotBeFound() throws Exception {
        expectedException.expect(IllegalStateException.class);
        radiologyProperties.getReportHome();
    }
}
