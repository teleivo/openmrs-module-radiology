package org.openmrs.module.radiology.dicom;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.List;

import org.dcm4che3.net.ApplicationEntity;
import org.dcm4che3.net.Connection;
import org.dcm4che3.net.Device;
import org.junit.Test;

public class MppsSCPTest {
	
	@Test
	public void testme() {
		// configureBindServer connection, ae
		// configure ae.addTransferCapability
		// configure IODs (setMppsNCreateIOD, setMppsNSetIOD)
		// set storageDirectory
	}
	
	/**
	 * @see MppsSCP#MppsSCP(String,String,String)
	 * @verifies create an MppsSCP configured with given parameters
	 */
	@Test
	public void MppsSCP_shouldCreateAnMppsSCPConfiguredWithGivenParameters() throws Exception {
		
		MppsSCP mppsSCP = new MppsSCP("RADIOLOGY_MODULE", "11114", "mpps");
		assertThat(mppsSCP.getStorageDirectory()
				.getPath(), is("mpps"));
	}
	
	/**
	 * Test method for {@link org.dcm4che3.net.Device#reconfigure(org.dcm4che3.net.Device)}.
	 */
	@Test
	public void testReconfigure() throws Exception {
		Device d1 = createDevice("test", "AET1");
		Device d2 = createDevice("test", "AET2");
		d1.reconfigure(d2);
		ApplicationEntity ae = d1.getApplicationEntity("AET2");
		assertNotNull(ae);
		List<Connection> conns = ae.getConnections();
		assertEquals(1, conns.size());
	}
	
	private Device createDevice(String name, String aet) {
		Device dev = new Device(name);
		Connection conn = new Connection("dicom", "localhost", 11112);
		dev.addConnection(conn);
		ApplicationEntity ae = new ApplicationEntity(aet);
		dev.addApplicationEntity(ae);
		ae.addConnection(conn);
		return dev;
	}
	
	/**
	 * @see MppsSCP#setStorageDirectory(File)
	 * @verifies set this storageDir to given storageDir and create it
	 */
	@Test
	public void setStorageDirectory_shouldSetThisStorageDirToGivenStorageDirAndCreateIt() throws Exception {
		
		MppsSCP mppsSCP = new MppsSCP("RADIOLOGY_MODULE", "11114", "mpps");
		
		String mppsDirName = "new-mpps-dir";
		mppsSCP.setStorageDirectory(new File(mppsDirName));
		assertNotNull(mppsSCP.getStorageDirectory());
		assertThat(mppsSCP.getStorageDirectory()
				.getPath(), is(mppsDirName));
		assertTrue(mppsSCP.getStorageDirectory()
				.exists());
	}
	
	/**
	 * @see MppsSCP#setStorageDirectory(File)
	 * @verifies set this storageDir to null given null
	 */
	@Test
	public void setStorageDirectory_shouldSetThisStorageDirToNullGivenNull() throws Exception {
		
		MppsSCP mppsSCP = new MppsSCP("RADIOLOGY_MODULE", "11114", "mpps");
		
		mppsSCP.setStorageDirectory(null);
		assertThat(mppsSCP.getStorageDirectory(), is(nullValue()));
	}
	
	/**
	 * @see MppsSCP#start()
	 * @verifies start listening on device connections and set started to true
	 */
	@Test
	public void start_shouldStartListeningOnDeviceConnectionsAndSetStartedToTrue() throws Exception {
		
		MppsSCP mppsSCP = new MppsSCP("RADIOLOGY_MODULE", "11114", "mpps");
		mppsSCP.start();
		
		assertTrue(mppsSCP.isStarted());
	}
}
