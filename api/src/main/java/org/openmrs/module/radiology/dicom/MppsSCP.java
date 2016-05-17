package org.openmrs.module.radiology.dicom;

import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.apache.commons.cli.ParseException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.IOD;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.UID;
import org.dcm4che3.data.ValidationResult;
import org.dcm4che3.io.DicomInputStream;
import org.dcm4che3.io.DicomOutputStream;
import org.dcm4che3.net.ApplicationEntity;
import org.dcm4che3.net.Association;
import org.dcm4che3.net.Connection;
import org.dcm4che3.net.Device;
import org.dcm4che3.net.Status;
import org.dcm4che3.net.TransferCapability;
import org.dcm4che3.net.service.BasicCEchoSCP;
import org.dcm4che3.net.service.BasicMPPSSCP;
import org.dcm4che3.net.service.DicomServiceException;
import org.dcm4che3.net.service.DicomServiceRegistry;
import org.dcm4che3.tool.common.CLIUtils;
import org.dcm4che3.util.SafeClose;

public class MppsSCP {
	
	private static final Log LOG = LogFactory.getLog(MppsSCP.class);
	
	private Device device = new Device("mppsscp");
	
	private final ApplicationEntity ae = new ApplicationEntity("*");
	
	private final Connection conn = new Connection();
	
	private File storageDir;
	
	private IOD mppsNCreateIOD;
	
	private IOD mppsNSetIOD;
	
	protected final BasicMPPSSCP mppsSCP = new BasicMPPSSCP() {
		
		@Override
		protected Attributes create(Association as, Attributes rq, Attributes rqAttrs, Attributes rsp)
				throws DicomServiceException {
			return MppsSCP.this.create(as, rq, rqAttrs);
		}
		
		@Override
		protected Attributes set(Association as, Attributes rq, Attributes rqAttrs, Attributes rsp)
				throws DicomServiceException {
			return MppsSCP.this.set(as, rq, rqAttrs);
		}
	};
	
	private boolean started = false;
	
	/**
	 * Creates an instance of MppsSCP with configured aeTitle, aePort and storageDirectory.
	 * 
	 * @param aeTitle Dicom AE Title to which ApplicationEntity will be set
	 * @param aePort Port to which Connection will be set
	 * @param storageDirectory Storage directory where DICOM Mpps files will be stored
	 * @throws ParseException
	 * @throws IOException
	 * @should create an MppsSCP configured with given parameters
	 */
	public MppsSCP(String aeTitle, String aePort, File storageDirectory) throws ParseException, IOException {
		this.device.addConnection(this.conn);
		this.device.addApplicationEntity(this.ae);
		this.ae.setAssociationAcceptor(true);
		this.ae.addConnection(this.conn);
		DicomServiceRegistry serviceRegistry = new DicomServiceRegistry();
		serviceRegistry.addDicomService(new BasicCEchoSCP());
		serviceRegistry.addDicomService(this.mppsSCP);
		this.ae.setDimseRQHandler(serviceRegistry);
		
		this.conn.setPort(Integer.valueOf(aePort));
		this.ae.setAETitle(aeTitle);
		
		this.conn.setReceivePDULength(Connection.DEF_MAX_PDU_LENGTH);
		this.conn.setSendPDULength(Connection.DEF_MAX_PDU_LENGTH);
		this.conn.setMaxOpsInvoked(0);
		this.conn.setMaxOpsPerformed(0);
		this.conn.setConnectTimeout(0);
		this.conn.setRequestTimeout(0);
		this.conn.setAcceptTimeout(0);
		this.conn.setReleaseTimeout(0);
		this.conn.setResponseTimeout(0);
		this.conn.setRetrieveTimeout(0);
		this.conn.setIdleTimeout(0);
		this.conn.setSocketCloseDelay(Connection.DEF_SOCKETDELAY);
		this.conn.setSendBufferSize(0);
		this.conn.setReceiveBufferSize(0);
		configureTransferCapability(this.ae, "resource:dicom/sop-classes.properties");
		
		this.setStorageDirectory(storageDirectory);
		this.setMppsNCreateIOD(IOD.load("resource:dicom/mpps-ncreate-iod.xml"));
		this.setMppsNSetIOD(IOD.load("resource:dicom/mpps-nset-iod.xml"));
		
		ExecutorService executorService = Executors.newCachedThreadPool();
		ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
		this.device.setScheduledExecutor(scheduledExecutorService);
		this.device.setExecutor(executorService);
	}
	
	/**
	 * Create and set storageDir to given parameter.
	 * 
	 * @param storageDir Storage directory to which storageDir will be set
	 * @should set this storageDir to given storageDir and create it
	 * @should set this storageDir to null given null
	 */
	public void setStorageDirectory(File storageDir) {
		if (storageDir != null)
			storageDir.mkdirs();
		this.storageDir = storageDir;
	}
	
	public File getStorageDirectory() {
		return this.storageDir;
	}
	
	private void setMppsNCreateIOD(IOD mppsNCreateIOD) {
		this.mppsNCreateIOD = mppsNCreateIOD;
	}
	
	private void setMppsNSetIOD(IOD mppsNSetIOD) {
		this.mppsNSetIOD = mppsNSetIOD;
	}
	
	private static void configureTransferCapability(ApplicationEntity ae, String sopClassPropertiesUrl) throws IOException {
		Properties p = CLIUtils.loadProperties(sopClassPropertiesUrl, null);
		for (String cuid : p.stringPropertyNames()) {
			String ts = p.getProperty(cuid);
			ae.addTransferCapability(new TransferCapability(null, CLIUtils.toUID(cuid), TransferCapability.Role.SCP,
					CLIUtils.toUIDs(ts)));
		}
	}
	
	/**
	 * Return true if started is true and false otherwise.
	 * 
	 * @return true if started is true and false otherwise
	 * @should return true if started is true
	 * @should return false if started is false
	 */
	public boolean isStarted() {
		
		return this.started == true;
	}
	
	/**
	 * Return true if started is false and false otherwise.
	 * 
	 * @return true if started is false and true otherwise
	 * @should return true if started is false
	 * @should return false if started is true
	 */
	public boolean isStopped() {
		
		return this.started == false;
	}
	
	/**
	 * Start listening on device's connections.
	 * 
	 * @throws IOException
	 * @throws GeneralSecurityException
	 * @should start listening on device connections and set started to true
	 */
	public void start() throws IOException, GeneralSecurityException {
		this.device.bindConnections();
		this.started = true;
	}
	
	/**
	 * Stop listening on device's connections.
	 * 
	 * @should stop listening on device connections and set started to false
	 */
	public void stop() {
		
		if (this.isStopped())
			return;
		
		this.started = false;
		
		this.device.unbindConnections();
		((ExecutorService) this.device.getExecutor()).shutdown();
		this.device.getScheduledExecutor()
				.shutdown();
		
		// very quick fix to block for listening connection
		while (this.device.getConnections()
				.get(0)
				.isListening()) {
			try {
				Thread.sleep(10);
			}
			catch (InterruptedException e) {
				// ignore
			}
		}
		
	}
	
	/**
	 * Implements the DIMSE Service Element N-CREATE for the Modality Performed Procedure Step SOP Class Role SCP.
	 * 
	 * @param association DICOM association on which MPPS N-CREATE RQ was sent
	 * @param request Attributes of DICOM MPPS N-CREATE RQ
	 * @param requestAttributes Request attributes of DICOM MPPS N-CREATE RQ
	 * @return response attributes which will be sent back to the MPPS SCU in the N-CREATE response
	 * @throws DicomServiceException if <code>requestAttributes</code> are not conform with DICOM IOD
	 *         <code>mppsNCreateIOD</code>
	 * @throws DicomServiceException if an MPPS file exists for DICOM MPPS SOP Instance UID given in <code>request</code>
	 * @throws DicomServiceException if MPPS file cannot be stored
	 * @should throw DicomServiceException if requestAttributes are not conform with DICOM IOD mppsNCreateIOD
	 * @should throw DicomServiceException if an MPPS file exists for DICOM MPPS SOP Instance UID given in request
	 * @should throw DicomServiceException if MPPS file cannot be stored
	 */
	private Attributes create(Association association, Attributes request, Attributes requestAttributes)
			throws DicomServiceException {
		
		if (mppsNCreateIOD != null) {
			ValidationResult result = requestAttributes.validate(mppsNCreateIOD);
			if (!result.isValid()) {
				throw DicomServiceException.valueOf(result, requestAttributes);
			}
		}
		if (storageDir == null) {
			return null;
		}
		String cuid = request.getString(Tag.AffectedSOPClassUID);
		String iuid = request.getString(Tag.AffectedSOPInstanceUID);
		File file = new File(storageDir, iuid);
		if (file.exists()) {
			throw new DicomServiceException(Status.DuplicateSOPinstance).setUID(Tag.AffectedSOPInstanceUID, iuid);
		}
		
		DicomOutputStream out = null;
		LOG.info(association + ": M-WRITE " + file);
		try {
			out = new DicomOutputStream(file);
			out.writeDataset(Attributes.createFileMetaInformation(iuid, cuid, UID.ExplicitVRLittleEndian), requestAttributes);
		}
		catch (IOException e) {
			LOG.warn(association + ": Failed to store MPPS:", e);
			throw new DicomServiceException(Status.ProcessingFailure, e);
		}
		finally {
			SafeClose.close(out);
		}
		return null;
	}
	
	/**
	 * Auto generated method comment
	 * 
	 * @param as
	 * @param rq
	 * @param rqAttrs
	 * @return response attributes which will be sent back to the MPPS SCU in the N-SET response
	 * @throws DicomServiceException
	 */
	private Attributes set(Association as, Attributes rq, Attributes rqAttrs) throws DicomServiceException {
		if (mppsNSetIOD != null) {
			ValidationResult result = rqAttrs.validate(mppsNSetIOD);
			if (!result.isValid())
				throw DicomServiceException.valueOf(result, rqAttrs);
		}
		if (storageDir == null)
			return null;
		String cuid = rq.getString(Tag.RequestedSOPClassUID);
		String iuid = rq.getString(Tag.RequestedSOPInstanceUID);
		File file = new File(storageDir, iuid);
		if (!file.exists())
			throw new DicomServiceException(Status.NoSuchObjectInstance).setUID(Tag.AffectedSOPInstanceUID, iuid);
		LOG.info(as + ": M-UPDATE " + file);
		Attributes data;
		DicomInputStream in = null;
		try {
			in = new DicomInputStream(file);
			data = in.readDataset(-1, -1);
		}
		catch (IOException e) {
			LOG.warn(as + ": Failed to read MPPS:", e);
			throw new DicomServiceException(Status.ProcessingFailure, e);
		}
		finally {
			SafeClose.close(in);
		}
		if (!"IN PROGRESS".equals(data.getString(Tag.PerformedProcedureStepStatus)))
			BasicMPPSSCP.mayNoLongerBeUpdated();
		
		data.addAll(rqAttrs);
		DicomOutputStream out = null;
		try {
			out = new DicomOutputStream(file);
			out.writeDataset(Attributes.createFileMetaInformation(iuid, cuid, UID.ExplicitVRLittleEndian), data);
		}
		catch (IOException e) {
			LOG.warn(as + ": Failed to update MPPS:", e);
			throw new DicomServiceException(Status.ProcessingFailure, e);
		}
		finally {
			SafeClose.close(out);
		}
		return null;
	}
}
