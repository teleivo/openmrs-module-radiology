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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.UID;
import org.dcm4che3.net.ApplicationEntity;
import org.dcm4che3.net.Association;
import org.dcm4che3.net.Connection;
import org.dcm4che3.net.Device;
import org.dcm4che3.net.DimseRSPHandler;
import org.dcm4che3.net.Status;
import org.dcm4che3.tool.common.DicomFiles;
import org.dcm4che3.tool.mppsscu.MppsSCU;
import org.dcm4che3.tool.mppsscu.MppsSCU.MppsWithIUID;
import org.dcm4che3.tool.mppsscu.MppsSCU.RSPHandlerFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * Tests {@link MppsSCP}.
 */
public class MppsSCPComponentTest {
	
	private static final Log LOG = LogFactory.getLog(MppsSCPComponentTest.class);
	
	private static String MPPS_SCP_AE_TITLE = "RADIOLOGY_MODULE";
	
	private static Integer MPPS_SCP_PORT = 11114;
	
	private static String MPPS_SCP_STORAGE_DIR = "mpps";
	
	private static Integer MPPS_SCU_PORT = 11115;
	
	private static String MPPS_NCREATE_INSTANCE_UID = "1.2.826.0.1.3680043.2.1545.1.2.1.7.20160427.175209.661.30";
	
	@Rule
	public TemporaryFolder temporaryBaseFolder = new TemporaryFolder();
	
	private File mppsStorageDirectory;
	
	private MppsSCP mppsSCP;
	
	private MppsSCU mppsScu;
	
	private Connection mppsScuConnection;
	
	private ApplicationEntity mppsScuAe;
	
	private Device mppsScuDevice;
	
	private ExecutorService executorService;
	
	private ScheduledExecutorService scheduledExecutorService;
	
	private int mppsScpRspStatus;
	
	private Attributes mppsScpRspCmd;
	
	private Attributes mppsScpRspData;
	
	private RSPHandlerFactory rspHandlerFactory = new RSPHandlerFactory() {
		
		@Override
		public DimseRSPHandler createDimseRSPHandlerForNCreate(final MppsWithIUID mppsWithUID) {
			
			return new DimseRSPHandler(
										12) {
				
				@Override
				public void onDimseRSP(Association as, Attributes cmd, Attributes data) {
					mppsScpRspStatus = cmd.getInt(Tag.Status, -1);
					mppsScpRspCmd = cmd;
					mppsScpRspData = data;
					switch (cmd.getInt(Tag.Status, -1)) {
						case Status.Success:
						case Status.AttributeListError:
						case Status.AttributeValueOutOfRange:
							mppsWithUID.iuid = cmd.getString(Tag.AffectedSOPInstanceUID, mppsWithUID.iuid);
							mppsScu.addCreatedMpps(mppsWithUID);
					}
					super.onDimseRSP(as, cmd, data);
				}
			};
		}
		
		@Override
		public DimseRSPHandler createDimseRSPHandlerForNSet() {
			
			return new DimseRSPHandler(
										12) {
				
				@Override
				public void onDimseRSP(Association as, Attributes cmd, Attributes data) {
					mppsScpRspStatus = cmd.getInt(Tag.Status, -1);
					mppsScpRspCmd = cmd;
					mppsScpRspData = data;
					// super.onDimseRSP(as, cmd, data);
				}
			};
		}
	};
	
	/**
	 * Helper class so tests get access to content of DICOM MPPS objects created by MPPS SCP.
	 */
	static final class DicomFile {
		
		Attributes metaInformation;
		
		Attributes content;
		
		DicomFile() {
			
		}
	}
	
	/**
	 * Helper method so tests get access to content of DICOM MPPS objects created by MPPS SCP.
	 */
	public static DicomFile scanFile(String fileName) {
		
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
		// File test = new File("mpps");
		// mppsSCP = new MppsSCP(MPPS_SCP_AE_TITLE, MPPS_SCP_PORT.toString(), test);
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
		
		mppsScuAe = new ApplicationEntity("MPPSSCU");
		mppsScuDevice.addApplicationEntity(mppsScuAe);
		mppsScuAe.setAssociationAcceptor(true);
		mppsScuAe.setAssociationInitiator(true);
		mppsScuAe.addConnection(mppsScuConnection);
		
		mppsScu = new MppsSCU(mppsScuAe);
		mppsScu.getAAssociateRQ()
				.setCalledAET(MPPS_SCP_AE_TITLE);
		mppsScu.getRemoteConnection()
				.setHostname("localhost");
		mppsScu.getRemoteConnection()
				.setPort(MPPS_SCP_PORT);
		
		mppsScu.setTransferSyntaxes(new String[] { UID.ImplicitVRLittleEndian, UID.ExplicitVRLittleEndian,
				UID.ExplicitVRBigEndianRetired });
		mppsScu.setAttributes(new Attributes());
		mppsScu.setUIDSuffix("78");
		
		mppsScu.setRspHandlerFactory(rspHandlerFactory);
		
		// create executor
		executorService = Executors.newSingleThreadExecutor();
		scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
		mppsScuDevice.setExecutor(executorService);
		mppsScuDevice.setScheduledExecutor(scheduledExecutorService);
	}
	
	@After
	public void tearDown() throws IOException, InterruptedException {
		
		// mppsSCP.stop();
		if (mppsScu != null) {
			mppsScu.close();
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
	 * @see MppsSCP#setStorageDirectory(File)
	 * @verifies set this storageDir to given storageDir and create it
	 */
	@Test
	public void setStorageDirectory_shouldSetThisStorageDirToGivenStorageDirAndCreateIt() throws Exception {
		
		File mppsDir = temporaryBaseFolder.newFolder("new-mpps-dir");
		mppsSCP.setStorageDirectory(mppsDir);
		
		assertNotNull(mppsSCP.getStorageDirectory());
		assertThat(mppsSCP.getStorageDirectory(), is(mppsDir));
		assertTrue(mppsSCP.getStorageDirectory()
				.exists());
	}
	
	/**
	 * @see MppsSCP#setStorageDirectory(File)
	 * @verifies set this storageDir to null given null
	 */
	@Test
	public void setStorageDirectory_shouldSetThisStorageDirToNullGivenNull() throws Exception {
		
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
		mppsScu.open();
		
		// Create MPPS N-CREATE
		List<String> mppsFiles = new ArrayList<String>();
		File mppsDirectory = new File("src/test/resources/dicom/mpps/mpps-ncreate-missing-patientid.xml");
		mppsFiles.add(mppsDirectory.getAbsolutePath());
		mppsScu.scanFiles(mppsFiles, false);
		mppsScu.createMpps();
		
		assertEquals("Status MISSING_ATTRIBUTE", Status.MissingAttribute, mppsScpRspStatus);
		assertThat(ArrayUtils.toObject(mppsScpRspCmd.getInts(Tag.AttributeIdentifierList)), hasItemInArray(Tag.PatientID));
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
		// mppsScuScpAssociation = mppsScuAe.connect(mppsScuConnection, mppsScu.getAAssociateRQ());
		mppsScu.open();
		
		// Create MPPS N-CREATE
		List<String> mppsFiles = new ArrayList<String>();
		File mppsDirectory = new File("src/test/resources/dicom/mpps/" + MPPS_NCREATE_INSTANCE_UID + "-ncreate.xml");
		mppsFiles.add(mppsDirectory.getAbsolutePath());
		mppsScu.scanFiles(mppsFiles, false);
		mppsScu.createMpps();
		
		File mppsFileCreated = new File(mppsStorageDirectory, MPPS_NCREATE_INSTANCE_UID);
		assertTrue(mppsFileCreated.exists());
		
		// Create same MPPS N-CREATE again
		mppsScu.scanFiles(mppsFiles, false);
		mppsScu.createMpps();
		
		assertEquals("Status DUPLICATE_SOP_INSTANCE", Status.DuplicateSOPinstance, mppsScpRspStatus);
		assertThat(mppsScpRspCmd.getString(Tag.AffectedSOPInstanceUID), is(MPPS_NCREATE_INSTANCE_UID));
	}
	
	/**
	 * @see MppsSCP#create(Association,Attributes,Attributes)
	 * @verifies create mpps file in storage directory containing request attributes
	 */
	@Test
	public void create_shouldCreateMppsFileInStorageDirectoryContainingRequestAttributes() throws Exception {
		
		mppsSCP.start();
		
		// Open connection from MPPS SCU to MPPS SCP
		mppsScu.open();
		
		// Create MPPS N-CREATE
		List<String> mppsFiles = new ArrayList<String>();
		File mppsDirectory = new File("src/test/resources/dicom/mpps/" + MPPS_NCREATE_INSTANCE_UID + "-ncreate.xml");
		mppsFiles.add(mppsDirectory.getAbsolutePath());
		mppsScu.scanFiles(mppsFiles, false);
		mppsScu.createMpps();
		
		assertEquals("Status SUCCESS", Status.Success, mppsScpRspStatus);
		File mppsFileCreated = new File(mppsStorageDirectory, MPPS_NCREATE_INSTANCE_UID);
		assertTrue(mppsFileCreated.exists());
		
		DicomFile mppsFile = scanFile(mppsFileCreated.getAbsolutePath());
		assertThat(mppsFile.metaInformation.getString(Tag.MediaStorageSOPClassUID),
			is(UID.ModalityPerformedProcedureStepSOPClass));
		assertThat(mppsFile.metaInformation.getString(Tag.MediaStorageSOPInstanceUID), is(MPPS_NCREATE_INSTANCE_UID));
		assertThat(mppsFile.metaInformation.getString(Tag.TransferSyntaxUID), is(UID.ExplicitVRLittleEndian));
		assertThat(mppsFile.content.getString(Tag.PatientID), is("1237"));
		assertThat(mppsFile.content.getString(Tag.PerformedProcedureStepStatus), is("IN PROGRESS"));
		
		mppsScu.updateMpps();
		DicomFile newfile = scanFile(mppsFileCreated.getAbsolutePath());
		assertThat(newfile.content.getString(Tag.PerformedProcedureStepStatus), is("COMPLETED"));
	}
	
	/**
	 * @see MppsSCP#set(Association,Attributes,Attributes)
	 * @verifies throw DicomServiceException if an MPPS file for DICOM MPPS SOP Instance UID given in request does not exists
	 */
	// @Test
	public void set_shouldThrowDicomServiceExceptionIfAnMPPSFileForDICOMMPPSSOPInstanceUIDGivenInRequestDoesNotExists()
			throws Exception {
		
		mppsSCP.start();
		
		// Open connection from MPPS SCU to MPPS SCP
		mppsScu.open();
		
		// Create MPPS N-CREATE
		List<String> mppsFiles = new ArrayList<String>();
		File mppsDirectory = new File("src/test/resources/dicom/mpps/" + MPPS_NCREATE_INSTANCE_UID + "-ncreate.xml");
		mppsFiles.add(mppsDirectory.getAbsolutePath());
		mppsScu.scanFiles(mppsFiles, false);
		mppsScu.createMpps();
		
		// DicomFile mpps = scanFile(mppsDirectory.getAbsolutePath());
		// mppsScu.addMPPS(MPPS_NCREATE_INSTANCE_UID, mpps.content);
		// mppsScu.addCreatedMpps(mpps);
		
		assertEquals("Status SUCCESS", Status.Success, mppsScpRspStatus);
		File mppsFileCreated = new File(mppsStorageDirectory, MPPS_NCREATE_INSTANCE_UID);
		assertTrue(mppsFileCreated.exists());
		
		// delete MPPS file, so it will not be found when N-SET RQ comes in
		assertTrue(mppsFileCreated.delete());
		assertFalse(mppsFileCreated.exists());
		
		// Create MPPS N-SET
		mppsScu.updateMpps();
		assertTrue(mppsFileCreated.exists());
		
		assertEquals("Status NO_SUCH_OBJECT_INSTANCE", Status.NoSuchObjectInstance, mppsScpRspStatus);
		assertThat(mppsScpRspCmd.getString(Tag.AffectedSOPInstanceUID), is(MPPS_NCREATE_INSTANCE_UID));
	}
	
	/**
	 * UPDATE method signature and javadocs with generate test case plugin
	 */
	@Test
	public void set_shouldUpdateExistingMppsFileInStorageDirectoryContainingRequestAttributes() throws Exception {
		
		mppsSCP.start();
		
		// Open connection from MPPS SCU to MPPS SCP
		mppsScu.open();
		
		// Create MPPS N-CREATE
		List<String> mppsFiles = new ArrayList<String>();
		File mppsDirectory = new File("src/test/resources/dicom/mpps/" + MPPS_NCREATE_INSTANCE_UID + "-ncreate.xml");
		mppsFiles.add(mppsDirectory.getAbsolutePath());
		mppsScu.scanFiles(mppsFiles, false);
		mppsScu.createMpps();
		
		assertEquals("Status SUCCESS", Status.Success, mppsScpRspStatus);
		File mppsFileCreated = new File(mppsStorageDirectory, MPPS_NCREATE_INSTANCE_UID);
		// assertTrue(mppsFileCreated.exists());
		
		// Create MPPS N-SET
		// DicomFile instance =
		// scanFile("src/test/resources/dicom/mpps/1.3.6.1.4.1.25403.2199141309252.6396.20160427181538.71.xml");
		// assertTrue(mppsScu.addInstance(instance.content));
		Attributes instance = new Attributes();
		// instance.setString(Tag.PerformedStationAETitle, VR.AE, )
		mppsScu.addInstance(instance);
		mppsScu.updateMpps();
		
		assertEquals("Status SUCCESS", Status.Success, mppsScpRspStatus);
		// assertTrue(mppsFileCreated.exists());
		
		DicomFile mppsFile = scanFile(mppsFileCreated.getAbsolutePath());
		assertThat(mppsFile.metaInformation.getString(Tag.MediaStorageSOPClassUID),
			is(UID.ModalityPerformedProcedureStepSOPClass));
		assertThat(mppsFile.metaInformation.getString(Tag.MediaStorageSOPInstanceUID), is(MPPS_NCREATE_INSTANCE_UID));
		assertThat(mppsFile.metaInformation.getString(Tag.TransferSyntaxUID), is(UID.ExplicitVRLittleEndian));
		assertThat(mppsFile.content.getString(Tag.PatientID), is("1237"));
		// assertThat(mppsFile.content.getString(Tag.PerformedProcedureStepStatus), is("COMPLETED"));
		assertThat(mppsFile.content.getString(Tag.PerformedProcedureStepEndDate), is("20160427"));
		assertThat(mppsFile.content.getString(Tag.PerformedProcedureStepEndTime), is("181549.69"));
	}
}
