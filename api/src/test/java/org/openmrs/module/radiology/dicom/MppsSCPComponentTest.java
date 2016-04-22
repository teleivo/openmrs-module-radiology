/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.radiology.dicom;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.collection.IsArrayContaining.hasItemInArray;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.ConnectException;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.apache.commons.lang3.ArrayUtils;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.UID;
import org.dcm4che3.data.VR;
import org.dcm4che3.net.ApplicationEntity;
import org.dcm4che3.net.Association;
import org.dcm4che3.net.Connection;
import org.dcm4che3.net.Device;
import org.dcm4che3.net.DimseRSPHandler;
import org.dcm4che3.net.Status;
import org.dcm4che3.net.TransferCapability.Role;
import org.dcm4che3.net.pdu.AAssociateRQ;
import org.dcm4che3.net.pdu.PresentationContext;
import org.dcm4che3.tool.common.DicomFiles;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;

/**
 * Tests {@link MppsSCP}.
 */
public class MppsSCPComponentTest {
	
	private static String MPPS_SCP_AE_TITLE = "RADIOLOGY_MODULE";
	
	private static Integer MPPS_SCP_PORT = 11114;
	
	private static String MPPS_SCP_STORAGE_DIR = "mpps";
	
	private static Integer MPPS_SCU_PORT = 11115;
	
	private static String MPPS_NCREATE_INSTANCE_UID = "1.2.826.0.1.3680043.2.1545.1.2.1.7.20160427.175209.661.30";
	
	@Rule
	public TemporaryFolder temporaryBaseFolder = new TemporaryFolder();
	
	@Rule
	public ExpectedException expectedException = ExpectedException.none();
	
	private File mppsStorageDirectory;
	
	private MppsSCP mppsSCP;
	
	private Connection mppsScuConnection;
	
	private ApplicationEntity mppsScuApplicationEntity;
	
	private Device mppsScuDevice;
	
	private AAssociateRQ associationRQ;
	
	private Association association;
	
	private Connection remoteConnection;
	
	private ExecutorService executorService;
	
	private ScheduledExecutorService scheduledExecutorService;
	
	Attributes mppsNcreateAttributes;
	
	Attributes mppsNsetAttributes;
	
	private int mppsScpRspStatus;
	
	private Attributes mppsScpRspCmd;
	
	private DimseRSPHandler testDimseRSPHandler = new DimseRSPHandler(
																		12) {
		
		@Override
		public void onDimseRSP(Association as, Attributes cmd, Attributes data) {
			
			mppsScpRspStatus = cmd.getInt(Tag.Status, -1);
			mppsScpRspCmd = cmd;
			super.onDimseRSP(as, cmd, data);
		}
	};
	
	/**
	 * Helper class so tests get access to content of DICOM MPPS objects created by MPPS SCP.
	 */
	private static final class DicomFile {
		
		Attributes metaInformation;
		
		Attributes content;
	}
	
	/**
	 * Helper method so tests get access to content of DICOM MPPS objects created by MPPS SCP.
	 */
	static DicomFile scanFile(String fileName) {
		
		final DicomFile result = new DicomFile();
		
		DicomFiles.scan(Arrays.asList(fileName), new DicomFiles.Callback() {
			
			@Override
			public boolean dicomFile(File f, Attributes fmi, long dsPos, Attributes ds) throws Exception {
				
				result.metaInformation = fmi;
				result.content = ds;
				return false;
			}
		});
		
		return result;
	}
	
	@Before
	public void setUp() throws Exception {
		
		// setup MPPS SCP
		mppsStorageDirectory = temporaryBaseFolder.newFolder(MPPS_SCP_STORAGE_DIR);
		mppsSCP = new MppsSCP(MPPS_SCP_AE_TITLE, MPPS_SCP_PORT.toString(), mppsStorageDirectory);
		
		// setup MPPS SCU
		mppsScuConnection = new Connection();
		mppsScuConnection.setPort(MPPS_SCU_PORT);
		mppsScuConnection.setReceivePDULength(Connection.DEF_MAX_PDU_LENGTH);
		mppsScuConnection.setSendPDULength(Connection.DEF_MAX_PDU_LENGTH);
		mppsScuConnection.setMaxOpsInvoked(0);
		mppsScuConnection.setMaxOpsPerformed(0);
		mppsScuConnection.setConnectTimeout(0);
		mppsScuConnection.setRequestTimeout(0);
		mppsScuConnection.setAcceptTimeout(0);
		mppsScuConnection.setReleaseTimeout(0);
		mppsScuConnection.setResponseTimeout(0);
		mppsScuConnection.setRetrieveTimeout(0);
		mppsScuConnection.setIdleTimeout(0);
		mppsScuConnection.setSocketCloseDelay(Connection.DEF_SOCKETDELAY);
		mppsScuConnection.setSendBufferSize(0);
		mppsScuConnection.setReceiveBufferSize(0);
		
		mppsScuDevice = new Device("mppsscu");
		mppsScuDevice.addConnection(mppsScuConnection);
		
		mppsScuApplicationEntity = new ApplicationEntity("MPPSSCU");
		mppsScuDevice.addApplicationEntity(mppsScuApplicationEntity);
		mppsScuApplicationEntity.setAssociationAcceptor(true);
		mppsScuApplicationEntity.setAssociationInitiator(true);
		mppsScuApplicationEntity.addConnection(mppsScuConnection);
		
		remoteConnection = new Connection();
		remoteConnection.setHostname("localhost");
		remoteConnection.setPort(MPPS_SCP_PORT);
		
		associationRQ = new AAssociateRQ();
		associationRQ.addPresentationContext(new PresentationContext(1, UID.VerificationSOPClass, UID.ImplicitVRLittleEndian));
		associationRQ.addPresentationContext(new PresentationContext(3, UID.ModalityPerformedProcedureStepSOPClass,
				new String[] { UID.ImplicitVRLittleEndian, UID.ExplicitVRLittleEndian, UID.ExplicitVRBigEndianRetired }));
		associationRQ.setCalledAET(MPPS_SCP_AE_TITLE);
		
		// create executor
		executorService = Executors.newSingleThreadExecutor();
		scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
		mppsScuDevice.setExecutor(executorService);
		mppsScuDevice.setScheduledExecutor(scheduledExecutorService);
		
		// MPPS source files for N-CREATE/N-SET
		File mppsNcreateFile = new File("src/test/resources/dicom/mpps/" + MPPS_NCREATE_INSTANCE_UID + "-ncreate.xml");
		File mppsNsetFile = new File("src/test/resources/dicom/mpps/" + MPPS_NCREATE_INSTANCE_UID + "-nset.xml");
		DicomFile mppsNcreate = scanFile(mppsNcreateFile.getAbsolutePath());
		DicomFile mppsNset = scanFile(mppsNsetFile.getAbsolutePath());
		mppsNcreateAttributes = mppsNcreate.content;
		mppsNsetAttributes = mppsNset.content;
	}
	
	@After
	public void tearDown() throws IOException, InterruptedException {
		
		if (association != null) {
			association.waitForOutstandingRSP();
			association.release();
			association.waitForSocketClose();
		}
		if (executorService != null) {
			executorService.shutdown();
		}
		if (scheduledExecutorService != null) {
			scheduledExecutorService.shutdown();
		}
		mppsSCP.stop();
	}
	
	/**
	 * @see MppsSCP#MppsSCP(String,String,String)
	 * @verifies create an MppsSCP configured with given parameters
	 */
	@Test
	public void MppsSCP_shouldCreateAnMppsSCPConfiguredWithGivenParameters() throws Exception {
		
		assertThat(mppsSCP.getStorageDirectory(), is(mppsStorageDirectory));
	}
	
	/**
	 * @see MppsSCP#configureTransferCapability(ApplicationEntity,String)
	 * @verifies add transfer capabilities from sop class and transfer syntax defined in sopClassPropertiesUrl to
	 *           applicationEntity
	 */
	@Test
	public void configureTransferCapability_shouldAddTransferCapabilitiesFromSopClassAndTransferSyntaxDefinedInSopClassPropertiesUrlToApplicationEntity()
			throws Exception {
		
		ApplicationEntity applicationEntity = new ApplicationEntity("TEST");
		Method configureTransferCapabilityMethod = MppsSCP.class.getDeclaredMethod("configureTransferCapability",
			ApplicationEntity.class, String.class);
		configureTransferCapabilityMethod.setAccessible(true);
		configureTransferCapabilityMethod.invoke(mppsSCP, new Object[] { applicationEntity,
				"resource:dicom/sop-classes.properties" });
		assertThat(applicationEntity.getTransferCapabilities()
				.size(), is(2));
		assertTrue(applicationEntity.getTransferCapabilityFor(UID.VerificationSOPClass, Role.SCP)
				.containsTransferSyntax(UID.ImplicitVRLittleEndian));
		assertTrue(applicationEntity.getTransferCapabilityFor(UID.ModalityPerformedProcedureStepSOPClass, Role.SCP)
				.containsTransferSyntax(UID.ImplicitVRLittleEndian));
		assertTrue(applicationEntity.getTransferCapabilityFor(UID.ModalityPerformedProcedureStepSOPClass, Role.SCP)
				.containsTransferSyntax(UID.ExplicitVRLittleEndian));
		assertTrue(applicationEntity.getTransferCapabilityFor(UID.ModalityPerformedProcedureStepSOPClass, Role.SCP)
				.containsTransferSyntax(UID.ExplicitVRBigEndianRetired));
	}
	
	/**
	 * @see MppsSCP#isStarted()
	 * @verifies return true if started is true
	 */
	@Test
	public void isStarted_shouldReturnTrueIfStartedIsTrue() throws Exception {
		
		try {
			mppsSCP.start();
			
			assertTrue(mppsSCP.isStarted());
		}
		finally {
			mppsSCP.stop();
		}
	}
	
	/**
	 * @see MppsSCP#isStarted()
	 * @verifies return false if started is false
	 */
	@Test
	public void isStarted_shouldReturnFalseIfStartedIsFalse() throws Exception {
		
		assertFalse(mppsSCP.isStarted());
	}
	
	/**
	 * @see MppsSCP#isStopped()
	 * @verifies return true if started is false
	 */
	@Test
	public void isStopped_shouldReturnTrueIfStartedIsFalse() throws Exception {
		
		assertTrue(mppsSCP.isStopped());
	}
	
	/**
	 * @see MppsSCP#isStopped()
	 * @verifies return false if started is true
	 */
	@Test
	public void isStopped_shouldReturnFalseIfStartedIsTrue() throws Exception {
		
		try {
			mppsSCP.start();
			
			assertFalse(mppsSCP.isStopped());
		}
		finally {
			mppsSCP.stop();
		}
	}
	
	/**
	 * @see MppsSCP#start()
	 * @verifies start listening on device connections and set started to true
	 */
	@Test
	public void start_shouldStartListeningOnDeviceConnectionsAndSetStartedToTrue() throws Exception {
		
		try {
			mppsSCP.start();
			
			assertTrue(mppsSCP.isStarted());
		}
		finally {
			mppsSCP.stop();
		}
	}
	
	/**
	 * @see MppsSCP#stop()
	 * @verifies stop listening on device connections and set started to false
	 */
	@Test
	public void stop_shouldStopListeningOnDeviceConnectionsAndSetStartedToFalse() throws Exception {
		
		mppsSCP.start();
		mppsSCP.stop();
		
		assertTrue(mppsSCP.isStopped());
		expectedException.expect(ConnectException.class);
		expectedException.expectMessage("Connection refused: connect");
		mppsScuApplicationEntity.connect(remoteConnection, associationRQ);
	}
	
	/**
	 * @see MppsSCP#setStorageDirectory(File)
	 * @verifies set this storageDirectory to given storageDirectory and create it
	 */
	@Test
	public void setStorageDirectory_shouldSetThisStorageDirectoryToGivenStorageDirectoryAndCreateIt() throws Exception {
		
		File mppsDir = temporaryBaseFolder.newFolder("new-mpps-dir");
		mppsSCP.setStorageDirectory(mppsDir);
		
		assertNotNull(mppsSCP.getStorageDirectory());
		assertThat(mppsSCP.getStorageDirectory(), is(mppsDir));
		assertTrue(mppsSCP.getStorageDirectory()
				.exists());
	}
	
	/**
	 * @see MppsSCP#setStorageDirectory(File)
	 * @verifies set this storageDirectory to null given null
	 */
	@Test
	public void setStorageDirectory_shouldSetThisStorageDirectoryToNullGivenNull() throws Exception {
		
		mppsSCP.setStorageDirectory(null);
		assertThat(mppsSCP.getStorageDirectory(), is(nullValue()));
	}
	
	/**
	 * @see MppsSCP#create(Association,Attributes,Attributes)
	 * @verifies throw DicomServiceException if requestAttributes are not conform with DICOM IOD mppsNCreateIOD
	 */
	@Test
	public void create_shouldThrowDicomServiceExceptionIfRequestAttributesAreNotConformWithDICOMIODMppsNCreateIOD()
			throws Exception {
		
		mppsSCP.start();
		
		// Open connection from MPPS SCU to MPPS SCP
		association = mppsScuApplicationEntity.connect(remoteConnection, associationRQ);
		
		// Create MPPS N-CREATE
		// remove type 1 data element (required), see mpps-ncreate-iod.xml
		mppsNcreateAttributes.remove(Tag.ScheduledStepAttributesSequence);
		
		association.ncreate(UID.ModalityPerformedProcedureStepSOPClass, MPPS_NCREATE_INSTANCE_UID, mppsNcreateAttributes,
			null, testDimseRSPHandler);
		association.waitForOutstandingRSP();
		
		assertEquals("Status MISSING_ATTRIBUTE", Status.MissingAttribute, mppsScpRspStatus);
		assertThat(ArrayUtils.toObject(mppsScpRspCmd.getInts(Tag.AttributeIdentifierList)),
			hasItemInArray(Tag.ScheduledStepAttributesSequence));
		File mppsFileCreated = new File(mppsStorageDirectory, MPPS_NCREATE_INSTANCE_UID);
		assertFalse(mppsFileCreated.exists());
	}
	
	/**
	 * @see MppsSCP#create(Association,Attributes,Attributes)
	 * @verifies throw DicomServiceException if an MPPS file exists for DICOM MPPS SOP Instance UID given in request
	 */
	@Test
	public void create_shouldthrowDicomServiceExceptionIfAnMPPSFileExistsForDICOMMPPSSOPInstanceUIDGivenInRequest()
			throws Exception {
		
		mppsSCP.start();
		
		// Open connection from MPPS SCU to MPPS SCP
		association = mppsScuApplicationEntity.connect(remoteConnection, associationRQ);
		
		// Create MPPS N-CREATE
		association.ncreate(UID.ModalityPerformedProcedureStepSOPClass, MPPS_NCREATE_INSTANCE_UID, mppsNcreateAttributes,
			null, testDimseRSPHandler);
		association.waitForOutstandingRSP();
		
		assertEquals("Status SUCCESS", Status.Success, mppsScpRspStatus);
		File mppsFileCreated = new File(mppsStorageDirectory, MPPS_NCREATE_INSTANCE_UID);
		assertTrue(mppsFileCreated.exists());
		
		// Create same MPPS N-CREATE again
		association.ncreate(UID.ModalityPerformedProcedureStepSOPClass, MPPS_NCREATE_INSTANCE_UID, mppsNcreateAttributes,
			null, testDimseRSPHandler);
		association.waitForOutstandingRSP();
		
		assertEquals("Status DUPLICATE_SOP_INSTANCE", Status.DuplicateSOPinstance, mppsScpRspStatus);
		assertThat(mppsScpRspCmd.getString(Tag.AffectedSOPInstanceUID), is(MPPS_NCREATE_INSTANCE_UID));
	}
	
	/**
	 * @see MppsSCP#create(Association,Attributes,Attributes)
	 * @verifies throw DicomServiceException if MPPS file cannot be written
	 */
	@Test
	public void create_shouldThrowDicomServiceExceptionIfMPPSFileCannotBeWritten() throws Exception {
		
		mppsSCP.start();
		
		// Open connection from MPPS SCU to MPPS SCP
		association = mppsScuApplicationEntity.connect(remoteConnection, associationRQ);
		
		// Remove mpps storage directory to provoke failure
		mppsStorageDirectory.delete();
		
		// Create MPPS N-CREATE
		association.ncreate(UID.ModalityPerformedProcedureStepSOPClass, MPPS_NCREATE_INSTANCE_UID, mppsNcreateAttributes,
			null, testDimseRSPHandler);
		association.waitForOutstandingRSP();
		
		assertEquals("Status PROCESSING_FAILURE", Status.ProcessingFailure, mppsScpRspStatus);
	}
	
	/**
	 * @see MppsSCP#create(Association,Attributes,Attributes)
	 * @verifies create mpps file in storage directory containing request attributes
	 */
	@Test
	public void create_shouldCreateMppsFileInStorageDirectoryContainingRequestAttributes() throws Exception {
		
		mppsSCP.start();
		
		// Open connection from MPPS SCU to MPPS SCP
		association = mppsScuApplicationEntity.connect(remoteConnection, associationRQ);
		
		// Create MPPS N-CREATE
		association.ncreate(UID.ModalityPerformedProcedureStepSOPClass, MPPS_NCREATE_INSTANCE_UID, mppsNcreateAttributes,
			null, testDimseRSPHandler);
		association.waitForOutstandingRSP();
		
		assertEquals("Status SUCCESS", Status.Success, mppsScpRspStatus);
		File mppsFileCreated = new File(mppsStorageDirectory, MPPS_NCREATE_INSTANCE_UID);
		assertTrue(mppsFileCreated.exists());
		
		DicomFile mppsFile = scanFile(mppsFileCreated.getAbsolutePath());
		assertThat(mppsFile.metaInformation.getString(Tag.MediaStorageSOPClassUID),
			is(UID.ModalityPerformedProcedureStepSOPClass));
		assertThat(mppsFile.metaInformation.getString(Tag.MediaStorageSOPInstanceUID), is(MPPS_NCREATE_INSTANCE_UID));
		assertThat(mppsFile.metaInformation.getString(Tag.TransferSyntaxUID), is(UID.ExplicitVRLittleEndian));
		assertThat(mppsFile.content.getString(Tag.PatientID), is("1237"));
		assertThat(mppsFile.content.getSequence(Tag.ScheduledStepAttributesSequence)
				.get(0)
				.getString(Tag.StudyInstanceUID), is("999.999.999.1.1.2"));
		assertThat(mppsFile.content.getSequence(Tag.ScheduledStepAttributesSequence)
				.get(0)
				.getString(Tag.RequestedProcedureID), is("ORD-2001"));
		assertThat(mppsFile.content.getSequence(Tag.ScheduledStepAttributesSequence)
				.get(0)
				.getString(Tag.ScheduledProcedureStepID), is("2"));
		assertThat(mppsFile.content.getString(Tag.PerformedProcedureStepStatus), is("IN PROGRESS"));
		assertThat(mppsFile.content.getString(Tag.PerformedProcedureStepEndDate), is(nullValue()));
		assertThat(mppsFile.content.getString(Tag.PerformedProcedureStepEndTime), is(nullValue()));
	}
	
	/**
	 * @see MppsSCP#set(Association,Attributes,Attributes)
	 * @verifies throw DicomServiceException if requestAttributes are not conform with DICOM IOD mppsNSetIOD
	 */
	@Test
	public void set_shouldThrowDicomServiceExceptionIfRequestAttributesAreNotConformWithDICOMIODMppsNSetIOD()
			throws Exception {
		
		mppsSCP.start();
		
		// Open connection from MPPS SCU to MPPS SCP
		association = mppsScuApplicationEntity.connect(remoteConnection, associationRQ);
		
		// Create MPPS N-CREATE
		association.ncreate(UID.ModalityPerformedProcedureStepSOPClass, MPPS_NCREATE_INSTANCE_UID, mppsNcreateAttributes,
			null, testDimseRSPHandler);
		association.waitForOutstandingRSP();
		
		assertEquals("Status SUCCESS", Status.Success, mppsScpRspStatus);
		
		// Create MPPS N-SET
		// add type 0 data element (not allowed), see mpps-nset-iod.xml
		mppsNsetAttributes.setString(Tag.PatientSex, VR.CS, "M");
		
		association.nset(UID.ModalityPerformedProcedureStepSOPClass, MPPS_NCREATE_INSTANCE_UID, mppsNsetAttributes, null,
			testDimseRSPHandler);
		association.waitForOutstandingRSP();
		
		assertEquals("Status SUCCESS", Status.NoSuchAttribute, mppsScpRspStatus);
	}
	
	/**
	 * @see MppsSCP#set(Association,Attributes,Attributes)
	 * @verifies throw DicomServiceException if an MPPS file for DICOM MPPS SOP Instance UID given in request does not exists
	 */
	@Test
	public void set_shouldThrowDicomServiceExceptionIfAnMPPSFileForDICOMMPPSSOPInstanceUIDGivenInRequestDoesNotExists()
			throws Exception {
		
		mppsSCP.start();
		
		// Open connection from MPPS SCU to MPPS SCP
		association = mppsScuApplicationEntity.connect(remoteConnection, associationRQ);
		
		// Create MPPS N-SET
		association.nset(UID.ModalityPerformedProcedureStepSOPClass, MPPS_NCREATE_INSTANCE_UID, mppsNsetAttributes, null,
			testDimseRSPHandler);
		association.waitForOutstandingRSP();
		
		assertEquals("Status NO_SUCH_OBJECT_INSTANCE", Status.NoSuchObjectInstance, mppsScpRspStatus);
		File mppsFileCreated = new File(mppsStorageDirectory, MPPS_NCREATE_INSTANCE_UID);
		assertFalse(mppsFileCreated.exists());
		
		assertThat(mppsScpRspCmd.getString(Tag.AffectedSOPInstanceUID), is(MPPS_NCREATE_INSTANCE_UID));
	}
	
	/**
	 * @see MppsSCP#set(Association,Attributes,Attributes)
	 * @verifies throw DicomServiceException if an MPPS is received for an MPPS instance which is completed or discontinued
	 */
	@Test
	public void set_shouldThrowDicomServiceExceptionIfAnMPPSIsReceivedForAnMPPSInstanceWhichIsCompletedOrDiscontinued()
			throws Exception {
		
		mppsSCP.start();
		
		// Open connection from MPPS SCU to MPPS SCP
		association = mppsScuApplicationEntity.connect(remoteConnection, associationRQ);
		
		// Create MPPS N-CREATE
		association.ncreate(UID.ModalityPerformedProcedureStepSOPClass, MPPS_NCREATE_INSTANCE_UID, mppsNcreateAttributes,
			null, testDimseRSPHandler);
		association.waitForOutstandingRSP();
		
		assertEquals("Status SUCCESS", Status.Success, mppsScpRspStatus);
		
		// Create MPPS N-SET
		association.nset(UID.ModalityPerformedProcedureStepSOPClass, MPPS_NCREATE_INSTANCE_UID, mppsNsetAttributes, null,
			testDimseRSPHandler);
		association.waitForOutstandingRSP();
		
		assertEquals("Status SUCCESS", Status.Success, mppsScpRspStatus);
		
		// Create MPPS N-SET AGAIN for already completed MPPS
		association.nset(UID.ModalityPerformedProcedureStepSOPClass, MPPS_NCREATE_INSTANCE_UID, mppsNsetAttributes, null,
			testDimseRSPHandler);
		association.waitForOutstandingRSP();
		
		assertEquals("Status PROCESSING_FAILURE", Status.ProcessingFailure, mppsScpRspStatus);
	}
	
	/**
	 * @see MppsSCP#set(Association,Attributes,Attributes)
	 * @verifies throw DicomServiceException if MPPS file cannot be read
	 */
	@Test
	public void set_shouldThrowDicomServiceExceptionIfMPPSFileCannotBeRead() throws Exception {
		
		mppsSCP.start();
		
		// Open connection from MPPS SCU to MPPS SCP
		association = mppsScuApplicationEntity.connect(remoteConnection, associationRQ);
		
		// Create MPPS N-CREATE
		association.ncreate(UID.ModalityPerformedProcedureStepSOPClass, MPPS_NCREATE_INSTANCE_UID, mppsNcreateAttributes,
			null, testDimseRSPHandler);
		association.waitForOutstandingRSP();
		
		assertEquals("Status SUCCESS", Status.Success, mppsScpRspStatus);
		
		// Remove and recreate empty file
		File mppsFileCreated = new File(mppsStorageDirectory, MPPS_NCREATE_INSTANCE_UID);
		assertTrue(mppsFileCreated.exists());
		
		mppsFileCreated.delete();
		mppsFileCreated.createNewFile();
		assertTrue(mppsFileCreated.exists());
		
		// Create MPPS N-SET
		association.nset(UID.ModalityPerformedProcedureStepSOPClass, MPPS_NCREATE_INSTANCE_UID, mppsNsetAttributes, null,
			testDimseRSPHandler);
		association.waitForOutstandingRSP();
		
		assertEquals("Status PROCESSING_FAILURE", Status.ProcessingFailure, mppsScpRspStatus);
	}
	
	/**
	 * @see MppsSCP#set(Association,Attributes,Attributes)
	 * @verifies throw DicomServiceException if MPPS file cannot be udpated
	 */
	@Test
	public void set_shouldThrowDicomServiceExceptionIfMPPSFileCannotBeUdpated() throws Exception {
		
		mppsSCP.start();
		
		// Open connection from MPPS SCU to MPPS SCP
		association = mppsScuApplicationEntity.connect(remoteConnection, associationRQ);
		
		// Create MPPS N-CREATE
		association.ncreate(UID.ModalityPerformedProcedureStepSOPClass, MPPS_NCREATE_INSTANCE_UID, mppsNcreateAttributes,
			null, testDimseRSPHandler);
		association.waitForOutstandingRSP();
		
		assertEquals("Status SUCCESS", Status.Success, mppsScpRspStatus);
		
		// Make file read-only
		File mppsFileCreated = new File(mppsStorageDirectory, MPPS_NCREATE_INSTANCE_UID);
		assertTrue(mppsFileCreated.exists());
		
		mppsFileCreated.setWritable(false);
		assertFalse(mppsFileCreated.canWrite());
		
		// Create MPPS N-SET
		association.nset(UID.ModalityPerformedProcedureStepSOPClass, MPPS_NCREATE_INSTANCE_UID, mppsNsetAttributes, null,
			testDimseRSPHandler);
		association.waitForOutstandingRSP();
		
		assertEquals("Status PROCESSING_FAILURE", Status.ProcessingFailure, mppsScpRspStatus);
	}
	
	/**
	 * @see MppsSCP#set(Association,Attributes,Attributes)
	 * @verifies update existing mpps file in storage directory with given request attributes
	 */
	@Test
	public void set_shouldUpdateExistingMppsFileInStorageDirectoryWithGivenRequestAttributes() throws Exception {
		
		mppsSCP.start();
		
		// Open connection from MPPS SCU to MPPS SCP
		association = mppsScuApplicationEntity.connect(remoteConnection, associationRQ);
		
		// Create MPPS N-CREATE
		association.ncreate(UID.ModalityPerformedProcedureStepSOPClass, MPPS_NCREATE_INSTANCE_UID, mppsNcreateAttributes,
			null, testDimseRSPHandler);
		association.waitForOutstandingRSP();
		
		assertEquals("Status SUCCESS", Status.Success, mppsScpRspStatus);
		File mppsFileCreated = new File(mppsStorageDirectory, MPPS_NCREATE_INSTANCE_UID);
		assertTrue(mppsFileCreated.exists());
		DicomFile mppsFile = scanFile(mppsFileCreated.getAbsolutePath());
		assertThat(mppsFile.content.getString(Tag.PerformedProcedureStepStatus), is("IN PROGRESS"));
		
		// Create MPPS N-SET
		association.nset(UID.ModalityPerformedProcedureStepSOPClass, MPPS_NCREATE_INSTANCE_UID, mppsNsetAttributes, null,
			testDimseRSPHandler);
		association.waitForOutstandingRSP();
		
		assertEquals("Status SUCCESS", Status.Success, mppsScpRspStatus);
		
		mppsFile = scanFile(mppsFileCreated.getAbsolutePath());
		assertThat(mppsFile.metaInformation.getString(Tag.MediaStorageSOPClassUID),
			is(UID.ModalityPerformedProcedureStepSOPClass));
		assertThat(mppsFile.metaInformation.getString(Tag.MediaStorageSOPInstanceUID), is(MPPS_NCREATE_INSTANCE_UID));
		assertThat(mppsFile.metaInformation.getString(Tag.TransferSyntaxUID), is(UID.ExplicitVRLittleEndian));
		assertThat(mppsFile.content.getString(Tag.PatientID), is("1237"));
		assertThat(mppsFile.content.getString(Tag.PerformedProcedureStepStatus), is("COMPLETED"));
		assertThat(mppsFile.content.getString(Tag.PerformedProcedureStepEndDate), is("20160427"));
		assertThat(mppsFile.content.getString(Tag.PerformedProcedureStepEndTime), is("181312"));
	}
}
