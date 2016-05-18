package org.openmrs.module.radiology.dicom;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

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
	
	public int mppsScpRspStatus;
	
	private Connection mppsScuConnection;
	
	private ApplicationEntity mppsScuAe;
	
	private Device mppsScuDevice;
	
	private ExecutorService executorService;
	
	ScheduledExecutorService scheduledExecutorService;
	
	RSPHandlerFactory rspHandlerFactory = new RSPHandlerFactory() {
		
		@Override
		public DimseRSPHandler createDimseRSPHandlerForNCreate(final MppsWithIUID mppsWithUID) {
			
			return new DimseRSPHandler(
										12) {
				
				@Override
				public void onDimseRSP(Association as, Attributes cmd, Attributes data) {
					mppsScpRspStatus = cmd.getInt(Tag.Status, -1);
					super.onDimseRSP(as, cmd, data);
				}
			};
		}
		
		@Override
		public DimseRSPHandler createDimseRSPHandlerForNSet() {
			
			return new DimseRSPHandler(12);
		}
	};
	
	@Before
	public void setUp() throws Exception {
		
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
		
		List<String> mppsFiles = new ArrayList<String>();
		File mppsDirectory = new File("src/test/resources/dicom/mpps/mpps-ncreate-missing-patientid.xml");
		mppsFiles.add(mppsDirectory.getAbsolutePath());
		mppsScu.scanFiles(mppsFiles, false);
		
		mppsScu.createMpps();
		
		File mppsFileCreated = new File(mppsStorageDirectory, MPPS_NCREATE_INSTANCE_UID);
		assertEquals("Status MISSING_ATTRIBUTE", Status.MissingAttribute, mppsScpRspStatus);
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
		
		List<String> mppsFiles = new ArrayList<String>();
		File mppsDirectory = new File("src/test/resources/dicom/mpps/mpps-ncreate.xml");
		mppsFiles.add(mppsDirectory.getAbsolutePath());
		mppsScu.scanFiles(mppsFiles, false);
		
		// Create MPPS N-CREATE
		mppsScu.createMpps();
		
		mppsScu.scanFiles(mppsFiles, false);
		
		// Create same MPPS N-CREATE again
		mppsScu.createMpps();
		
		File mppsFileCreated = new File(mppsStorageDirectory, MPPS_NCREATE_INSTANCE_UID);
		assertEquals("Status DUPLICATE_SOP_INSTANCE", Status.DuplicateSOPinstance, mppsScpRspStatus);
		assertTrue(mppsFileCreated.exists());
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
		
		List<String> mppsFiles = new ArrayList<String>();
		File mppsDirectory = new File("src/test/resources/dicom/mpps/mpps-ncreate.xml");
		mppsFiles.add(mppsDirectory.getAbsolutePath());
		mppsScu.scanFiles(mppsFiles, false);
		
		// Create MPPS N-CREATE
		mppsScu.createMpps();
		
		File mppsFileCreated = new File(mppsStorageDirectory, MPPS_NCREATE_INSTANCE_UID);
		assertEquals("Status SUCCESS", Status.Success, mppsScpRspStatus);
		assertTrue(mppsFileCreated.exists());
	}
}
