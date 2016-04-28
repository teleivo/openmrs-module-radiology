package org.openmrs.module.radiology.dicom;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.UID;
import org.dcm4che3.net.ApplicationEntity;
import org.dcm4che3.net.Association;
import org.dcm4che3.net.Connection;
import org.dcm4che3.net.Device;
import org.dcm4che3.tool.mppsscu.MppsSCU;
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
	
	@Rule
	public TemporaryFolder temporaryBaseFolder = new TemporaryFolder();
	
	private File mppsStorageDirectory;
	
	private MppsSCP mppsSCP;
	
	@Before
	public void setUp() throws Exception {
		
		mppsStorageDirectory = temporaryBaseFolder.newFolder(MPPS_SCP_STORAGE_DIR);
		mppsSCP = new MppsSCP(MPPS_SCP_AE_TITLE, MPPS_SCP_PORT.toString(), mppsStorageDirectory);
	}
	
	@After
	public void tearDown() {
		
		// mppsSCP.stop();
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
	 * @verifies DicomServiceException if an MPPS file exists for DICOM MPPS SOP Instance UID given in request
	 */
	@Test
	public void create_shouldDicomServiceExceptionIfAnMPPSFileExistsForDICOMMPPSSOPInstanceUIDGivenInRequest()
			throws Exception {
		
		// setup MPPS SCU
		Connection mppsScuConnection = new Connection();
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
		
		Device mppsScuDevice = new Device("mppsscu");
		mppsScuDevice.addConnection(mppsScuConnection);
		
		ApplicationEntity mppsScuAe = new ApplicationEntity("MPPSSCU");
		mppsScuDevice.addApplicationEntity(mppsScuAe);
		mppsScuAe.setAssociationAcceptor(true);
		mppsScuAe.setAssociationInitiator(true);
		mppsScuAe.addConnection(mppsScuConnection);
		
		MppsSCU mppsScu = new MppsSCU(mppsScuAe);
		
		mppsScu.getAAssociateRQ()
				.setCalledAET(MPPS_SCP_AE_TITLE);
		mppsScu.getRemoteConnection()
				.setHostname("localhost");
		mppsScu.getRemoteConnection()
				.setPort(MPPS_SCP_PORT);
		
		mppsScu.setTransferSyntaxes(new String[] { UID.ImplicitVRLittleEndian, UID.ExplicitVRLittleEndian,
				UID.ExplicitVRBigEndianRetired });
		mppsScu.setAttributes(new Attributes());
		
		// create executor
		ExecutorService executorService = Executors.newSingleThreadExecutor();
		ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
		mppsScuDevice.setExecutor(executorService);
		mppsScuDevice.setScheduledExecutor(scheduledExecutorService);
		
		try {
			LOG.info("start MPPS SCP");
			// Start MPPS SCP
			mppsSCP.start();
			
			// Open connection from MPPS SCU to MPPS SCP
			mppsScu.open();
			// MppsSCU.main(new String[] { "-b", "MPPSSCU@:11115", "-c", "RADIOLOGY_MODULE@localhost:11114" });
			List<String> mppsFiles = new ArrayList<String>();
			File mppsDirectory = new File("src/test/resources/dicom/mpps/mpps-ncreate.xml");
			System.out.println(mppsDirectory.getAbsolutePath());
			mppsFiles.add(mppsDirectory.getAbsolutePath());
			mppsScu.scanFiles(mppsFiles, true);
			
			// Create MPPS N-CREATE
			mppsScu.createMpps();
			// mppsScu.echo();
			// mppsScu.createMpps();
			File mppsFileCreated = new File(mppsStorageDirectory,
					"1.2.826.0.1.3680043.2.1545.1.2.1.7.20160427.175209.661.30");
			System.out.println("We did it: " + mppsFileCreated.getAbsolutePath());
			assertTrue(mppsFileCreated.exists());
		}
		finally {
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
		}
	}
}
