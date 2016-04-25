package org.openmrs.module.radiology.dicom;

import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
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
	
	private static ResourceBundle rb = ResourceBundle.getBundle("org.dcm4che3.tool.mppsscp.messages");
	
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
	
	public MppsSCP() throws IOException {
		device.addConnection(conn);
		device.addApplicationEntity(ae);
		ae.setAssociationAcceptor(true);
		ae.addConnection(conn);
		DicomServiceRegistry serviceRegistry = new DicomServiceRegistry();
		serviceRegistry.addDicomService(new BasicCEchoSCP());
		serviceRegistry.addDicomService(mppsSCP);
		ae.setDimseRQHandler(serviceRegistry);
	}
	
	/**
	 * Bind the MPPS SCP to the provided preconfigured ae
	 *
	 * @param applicationEntity
	 */
	public MppsSCP(ApplicationEntity applicationEntity) {
		device = applicationEntity.getDevice();
		applicationEntity.setAssociationAcceptor(true);
		DicomServiceRegistry serviceRegistry = new DicomServiceRegistry();
		serviceRegistry.addDicomService(new BasicCEchoSCP());
		serviceRegistry.addDicomService(mppsSCP);
		applicationEntity.setDimseRQHandler(serviceRegistry);
	}
	
	public MppsSCP(String aeTitle, String aePort, String storageDirectory) throws ParseException, IOException {
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
		
		this.setStorageDirectory(new File(storageDirectory));
		this.setMppsNCreateIOD(IOD.load("resource:dicom/mpps-ncreate-iod.xml"));
		this.setMppsNSetIOD(IOD.load("resource:dicom/mpps-nset-iod.xml"));
		
		ExecutorService executorService = Executors.newCachedThreadPool();
		ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
		device.setScheduledExecutor(scheduledExecutorService);
		device.setExecutor(executorService);
	}
	
	public void start() throws IOException, GeneralSecurityException {
		device.bindConnections();
		started = true;
	}
	
	public void stop() {
		
		if (!started)
			return;
		
		started = false;
		
		device.unbindConnections();
		((ExecutorService) device.getExecutor()).shutdown();
		device.getScheduledExecutor()
				.shutdown();
		
		// very quick fix to block for listening connection
		while (device.getConnections()
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
	
	public void setStorageDirectory(File storageDir) {
		if (storageDir != null)
			storageDir.mkdirs();
		this.storageDir = storageDir;
	}
	
	public File getStorageDirectory() {
		return storageDir;
	}
	
	private void setMppsNCreateIOD(IOD mppsNCreateIOD) {
		this.mppsNCreateIOD = mppsNCreateIOD;
	}
	
	private void setMppsNSetIOD(IOD mppsNSetIOD) {
		this.mppsNSetIOD = mppsNSetIOD;
	}
	
	public static MppsSCP createFromCommandLineArgs(String[] args) throws IOException, ParseException,
			GeneralSecurityException {
		CommandLine cl = parseComandLine(args);
		MppsSCP result = new MppsSCP();
		
		CLIUtils.configureBindServer(result.conn, result.ae, cl);
		CLIUtils.configure(result.conn, cl);
		configureTransferCapability(result.ae, cl);
		
		result.configure(cl.getOptionValue("mpps-ncreate-iod", "resource:dicom/mpps-ncreate-iod.xml"),
			cl.getOptionValue("mpps-nset-iod", "resource:dicom/mpps-nset-iod.xml"), cl.getOptionValue("directory", "."));
		
		return result;
	}
	
	private static CommandLine parseComandLine(String[] args) throws ParseException {
		Options opts = new Options();
		CLIUtils.addBindServerOption(opts);
		CLIUtils.addAEOptions(opts);
		CLIUtils.addCommonOptions(opts);
		addStorageDirectoryOptions(opts);
		addTransferCapabilityOptions(opts);
		addIODOptions(opts);
		return CLIUtils.parseComandLine(args, opts, rb, MppsSCP.class);
	}
	
	@SuppressWarnings("static-access")
	private static void addStorageDirectoryOptions(Options opts) {
		opts.addOption(null, "ignore", false, rb.getString("ignore"));
		opts.addOption(OptionBuilder.hasArg()
				.withArgName("path")
				.withDescription(rb.getString("directory"))
				.withLongOpt("directory")
				.create(null));
	}
	
	@SuppressWarnings("static-access")
	private static void addTransferCapabilityOptions(Options opts) {
		opts.addOption(OptionBuilder.hasArg()
				.withArgName("file|url")
				.withDescription(rb.getString("sop-classes"))
				.withLongOpt("sop-classes")
				.create(null));
	}
	
	@SuppressWarnings("static-access")
	private static void addIODOptions(Options opts) {
		opts.addOption(null, "no-validate", false, rb.getString("no-validate"));
		opts.addOption(OptionBuilder.hasArg()
				.withArgName("file|url")
				.withDescription(rb.getString("ncreate-iod"))
				.withLongOpt("ncreate-iod")
				.create(null));
		opts.addOption(OptionBuilder.hasArg()
				.withArgName("file|url")
				.withDescription(rb.getString("nset-iod"))
				.withLongOpt("nset-iod")
				.create(null));
	}
	
	private static void configureTransferCapability(ApplicationEntity ae, String sopClassPropertiesUrl) throws IOException {
		Properties p = CLIUtils.loadProperties(sopClassPropertiesUrl, null);
		for (String cuid : p.stringPropertyNames()) {
			String ts = p.getProperty(cuid);
			ae.addTransferCapability(new TransferCapability(null, CLIUtils.toUID(cuid), TransferCapability.Role.SCP,
					CLIUtils.toUIDs(ts)));
		}
	}
	
	private static void configureTransferCapability(ApplicationEntity ae, CommandLine cl) throws IOException {
		Properties p = CLIUtils.loadProperties(cl.getOptionValue("sop-classes", "resource:dicom/sop-classes.properties"),
			null);
		for (String cuid : p.stringPropertyNames()) {
			String ts = p.getProperty(cuid);
			ae.addTransferCapability(new TransferCapability(null, CLIUtils.toUID(cuid), TransferCapability.Role.SCP,
					CLIUtils.toUIDs(ts)));
		}
	}
	
	private void configure(String mppsNCreateIOD, String mppsNSetIOD, String directory) throws IOException,
			GeneralSecurityException {
		this.setStorageDirectory(new File(directory));
		this.setMppsNCreateIOD(IOD.load(mppsNCreateIOD));
		this.setMppsNSetIOD(IOD.load(mppsNSetIOD));
		
		ExecutorService executorService = Executors.newCachedThreadPool();
		ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
		device.setScheduledExecutor(scheduledExecutorService);
		device.setExecutor(executorService);
	}
	
	private Attributes create(Association as, Attributes rq, Attributes rqAttrs) throws DicomServiceException {
		if (mppsNCreateIOD != null) {
			ValidationResult result = rqAttrs.validate(mppsNCreateIOD);
			if (!result.isValid())
				throw DicomServiceException.valueOf(result, rqAttrs);
		}
		if (storageDir == null)
			return null;
		String cuid = rq.getString(Tag.AffectedSOPClassUID);
		String iuid = rq.getString(Tag.AffectedSOPInstanceUID);
		File file = new File(storageDir, iuid);
		if (file.exists())
			throw new DicomServiceException(Status.DuplicateSOPinstance).setUID(Tag.AffectedSOPInstanceUID, iuid);
		DicomOutputStream out = null;
		LOG.info(as + ": M-WRITE " + file);
		try {
			out = new DicomOutputStream(file);
			out.writeDataset(Attributes.createFileMetaInformation(iuid, cuid, UID.ExplicitVRLittleEndian), rqAttrs);
		}
		catch (IOException e) {
			LOG.warn(as + ": Failed to store MPPS:", e);
			throw new DicomServiceException(Status.ProcessingFailure, e);
		}
		finally {
			SafeClose.close(out);
		}
		return null;
	}
	
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
