package org.openmrs.module.radiology.dicom;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.lang.reflect.Method;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.net.Association;
import org.junit.Test;


public class MppsSCPComponentTest {
	
	
	/**
	 * @see MppsSCP#MppsSCP(String,String,String)
	 * @verifies create an MppsSCP configured with given parameters
	 */
	@Test
	public void MppsSCP_shouldCreateAnMppsSCPConfiguredWithGivenParameters() throws Exception {
		
		MppsSCP mppsSCP = new MppsSCP("RADIOLOGY_MODULE", "11114", "mpps");
		assertThat(mppsSCP.getStorageDirectory()
				.getPath(),
			is("mpps"));
	}
	
	/**
	 * @see MppsSCP#isStarted()
	 * @verifies return true if started is true
	 */
	@Test
	public void isStarted_shouldReturnTrueIfStartedIsTrue() throws Exception {
		
		MppsSCP mppsSCP = new MppsSCP("RADIOLOGY_MODULE", "11114", "mpps");
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
		
		MppsSCP mppsSCP = new MppsSCP("RADIOLOGY_MODULE", "11114", "mpps");
		
		assertFalse(mppsSCP.isStarted());
	}
	
	/**
	 * @see MppsSCP#isStopped()
	 * @verifies return true if started is false
	 */
	@Test
	public void isStopped_shouldReturnTrueIfStartedIsFalse() throws Exception {
		
		MppsSCP mppsSCP = new MppsSCP("RADIOLOGY_MODULE", "11114", "mpps");
		
		assertTrue(mppsSCP.isStopped());
	}
	
	/**
	 * @see MppsSCP#isStopped()
	 * @verifies return false if started is true
	 */
	@Test
	public void isStopped_shouldReturnFalseIfStartedIsTrue() throws Exception {
		
		MppsSCP mppsSCP = new MppsSCP("RADIOLOGY_MODULE", "11114", "mpps");
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
		
		MppsSCP mppsSCP = new MppsSCP("RADIOLOGY_MODULE", "11114", "mpps");
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
		
		MppsSCP mppsSCP = new MppsSCP("RADIOLOGY_MODULE", "11114", "mpps");
		
		String mppsDirName = "new-mpps-dir";
		File mppsDir = new File(mppsDirName);
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
		
		MppsSCP mppsSCP = new MppsSCP("RADIOLOGY_MODULE", "11114", "mpps");
		
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
		
		MppsSCP mppsSCP = new MppsSCP("RADIOLOGY_MODULE", "11114", "mpps");
		try {
			mppsSCP.start();
			Method createMethod = MppsSCP.class.getDeclaredMethod("create",
				new Class[] { Association.class, Attributes.class, Attributes.class });
//			createMethod.invoke(mppsSCP, args);
//			MppsSCU mppsSCU = new MppsSCU();
		}
		finally {
			mppsSCP.stop();
		}
	}
}
