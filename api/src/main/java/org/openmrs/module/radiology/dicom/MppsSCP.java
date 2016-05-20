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
import org.openmrs.api.context.Context;
import org.openmrs.module.radiology.dicom.code.PerformedProcedureStepStatus;
import org.openmrs.module.radiology.study.RadiologyStudyService;

/**
 * Represents a DICOM Application Entity conforming to the Modality Performed Procedure Step SOP Class as an SCP as described
 * in DICOM PS3.4 2016b - Service Class Specifications
 * Following DIMSE Services are thus supported:
 * <ul>
 * <li>N-CREATE</li>
 * <li>N-SET</li>
 * </ul>
 */
public class MppsSCP {
	
	private static final Log LOG = LogFactory.getLog(MppsSCP.class);
	
	private static final String SOP_CLASS_PROPERTIES_RESOURCE = "resource:dicom/sop-classes.properties";
	
	private static final String IOD_NCREATE_RESOURCE = "resource:dicom/mpps-ncreate-iod.xml";
	
	private static final String IOD_NSET_RESOURCE = "resource:dicom/mpps-nset-iod.xml";
	
	private static final String IN_PROGRESS = "IN PROGRESS";
	
	private static RadiologyStudyService radiologyStudyService = Context.getRegisteredComponent("radiologyStudyService",
		RadiologyStudyService.class);
	
	/**
	 * Device of MPPS SCP.
	 */
	private Device device = new Device("mppsscp");
	
	/**
	 * Application Entity of MPPS SCP.
	 */
	private final ApplicationEntity applicationEntity = new ApplicationEntity("*");
	
	/**
	 * Connection of MPPS SCP.
	 */
	private final Connection connection = new Connection();
	
	/**
	 * DICOM Service for MPPS N-CREATE and N-SET as SCP.
	 */
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
	
	/**
	 * Filesystem directory where received MPPS will be stored and updated.
	 */
	private File storageDirectory;
	
	/**
	 * IOD against which incoming N-CREATE MPPS are validated.
	 */
	private IOD mppsNCreateIOD;
	
	/**
	 * IOD against which incoming N-SET MPPS are validated.
	 */
	private IOD mppsNSetIOD;
	
	/**
	 * Indicates if MPPS SCP is started or stopped.
	 */
	private boolean started = false;
	
	/**
	 * Creates an instance of <code>MppsSCP</code> with configured AE Title, Port and storage directory for MPPS files.
	 * 
	 * @param applicationEntityTitle Dicom AE Title to which MPPS SCP Application Entity will be listening
	 * @param applicationEntityPort Port on which MPPS SCP will be listening
	 * @param storageDirectory Storage directory where MPPS SCP will store MPPS files
	 * @throws ParseException
	 * @throws IOException
	 * @should create an MppsSCP configured with given parameters
	 */
	public MppsSCP(String applicationEntityTitle, String applicationEntityPort, File storageDirectory)
			throws ParseException, IOException {
		
		this.device.addConnection(this.connection);
		this.device.addApplicationEntity(this.applicationEntity);
		this.applicationEntity.setAssociationAcceptor(true);
		this.applicationEntity.addConnection(this.connection);
		DicomServiceRegistry serviceRegistry = new DicomServiceRegistry();
		serviceRegistry.addDicomService(new BasicCEchoSCP());
		serviceRegistry.addDicomService(this.mppsSCP);
		this.applicationEntity.setDimseRQHandler(serviceRegistry);
		
		this.connection.setPort(Integer.valueOf(applicationEntityPort));
		this.applicationEntity.setAETitle(applicationEntityTitle);
		configureTransferCapability(this.applicationEntity, SOP_CLASS_PROPERTIES_RESOURCE);
		
		this.setStorageDirectory(storageDirectory);
		this.setMppsNCreateIOD(IOD.load(IOD_NCREATE_RESOURCE));
		this.setMppsNSetIOD(IOD.load(IOD_NSET_RESOURCE));
		
		ExecutorService executorService = Executors.newCachedThreadPool();
		ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
		this.device.setScheduledExecutor(scheduledExecutorService);
		this.device.setExecutor(executorService);
	}
	
	/**
	 * Create and set <code>storageDirectory</code> to given parameter.
	 * 
	 * @param storageDirectory Storage directory to which <code>storageDirectory</code> will be set
	 * @should set this storageDirectory to given storageDirectory and create it
	 * @should set this storageDirectory to null given null
	 */
	public void setStorageDirectory(File storageDirectory) {
		
		if (storageDirectory != null) {
			storageDirectory.mkdirs();
		}
		this.storageDirectory = storageDirectory;
	}
	
	/**
	 * Get <code>storageDirectory</code>.
	 */
	public File getStorageDirectory() {
		return this.storageDirectory;
	}
	
	/**
	 * Set <code>mppsNCreateIOD</code> to given parameter.
	 * 
	 * @param mppsNCreateIOD IOD to which <code>mppsNCreateIOD</code> will be set
	 */
	private void setMppsNCreateIOD(IOD mppsNCreateIOD) {
		
		this.mppsNCreateIOD = mppsNCreateIOD;
	}
	
	/**
	 * Set <code>mppsNSetIOD</code> to given parameter.
	 * 
	 * @param mppsNSetIOD IOD to which <code>mppsNSetIOD</code> will be set
	 */
	private void setMppsNSetIOD(IOD mppsNSetIOD) {
		
		this.mppsNSetIOD = mppsNSetIOD;
	}
	
	/**
	 * Add transfer capabilities (SOP classes and transfer syntaxes; as SCP) defined in properties file at
	 * <code>sopClassPropertiesUrl</code> to given <code>applicationEntity</code>.
	 * 
	 * @param applicationEntity Application entity to which transfer capabilities are added
	 * @param sopClassPropertiesUrl Properties file url from which transfer capabilities are read
	 * @should add transfer capabilities from sop class and transfer syntax defined in sopClassPropertiesUrl to
	 *         applicationEntity
	 */
	private static void configureTransferCapability(ApplicationEntity applicationEntity, String sopClassPropertiesUrl)
			throws IOException {
		
		Properties properties = CLIUtils.loadProperties(sopClassPropertiesUrl, null);
		for (String sopClassUID : properties.stringPropertyNames()) {
			String transferSyntax = properties.getProperty(sopClassUID);
			applicationEntity.addTransferCapability(new TransferCapability(null, CLIUtils.toUID(sopClassUID),
					TransferCapability.Role.SCP, CLIUtils.toUIDs(transferSyntax)));
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
		
		if (this.isStopped()) {
			return;
		}
		
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
	 * @should throw DicomServiceException if MPPS file cannot be written
	 * @should create mpps file in storage directory containing request attributes
	 */
	private Attributes create(Association association, Attributes request, Attributes requestAttributes)
			throws DicomServiceException {
		
		if (mppsNCreateIOD != null) {
			ValidationResult result = requestAttributes.validate(mppsNCreateIOD);
			if (!result.isValid()) {
				throw DicomServiceException.valueOf(result, requestAttributes);
			}
		}
		
		if (storageDirectory == null) {
			return null;
		}
		
		String instanceUID = request.getString(Tag.AffectedSOPInstanceUID);
		File file = new File(storageDirectory, instanceUID);
		if (file.exists()) {
			throw new DicomServiceException(Status.DuplicateSOPinstance).setUID(Tag.AffectedSOPInstanceUID, instanceUID);
		}
		
		DicomOutputStream out = null;
		LOG.info(association + ": M-WRITE " + file);
		try {
			out = new DicomOutputStream(file);
			out.writeDataset(Attributes.createFileMetaInformation(instanceUID, request.getString(Tag.AffectedSOPClassUID),
				UID.ExplicitVRLittleEndian), requestAttributes);
		}
		catch (IOException e) {
			LOG.warn(association + ": Failed to store MPPS:", e);
			throw new DicomServiceException(Status.ProcessingFailure, e);
		}
		finally {
			SafeClose.close(out);
		}
		
		String studyInstanceUID = requestAttributes.getSequence(Tag.ScheduledStepAttributesSequence)
				.get(0)
				.getString(Tag.StudyInstanceUID, "");
		radiologyStudyService.updateStudyPerformedStatus(studyInstanceUID, PerformedProcedureStepStatus.IN_PROGRESS);
		
		return null;
	}
	
	/**
	 * Implements the DIMSE Service Element N-SET for the Modality Performed Procedure Step SOP Class Role SCP.
	 * 
	 * @param association DICOM association on which MPPS N-SET RQ was sent
	 * @param request Attributes of DICOM MPPS N-SET RQ
	 * @param requestAttributes Request attributes of DICOM MPPS N-SET RQ
	 * @return response attributes which will be sent back to the MPPS SCU in the N-SET response
	 * @throws DicomServiceException if <code>requestAttributes</code> are not conform with DICOM IOD
	 *         <code>mppsNSetIOD</code>
	 * @throws DicomServiceException if an MPPS file for DICOM MPPS SOP Instance UID given in <code>request</code> does not
	 *         exists
	 * @throws DicomServiceException if MPPS file cannot be updated
	 * @should throw DicomServiceException if requestAttributes are not conform with DICOM IOD mppsNSetIOD
	 * @should throw DicomServiceException if an MPPS file for DICOM MPPS SOP Instance UID given in request does not exists
	 * @should throw DicomServiceException if an MPPS is received for an MPPS instance which is completed or discontinued
	 * @should throw DicomServiceException if MPPS file cannot be read
	 * @should throw DicomServiceException if MPPS file cannot be udpated
	 * @should update existing mpps file in storage directory with given request attributes
	 */
	private Attributes set(Association association, Attributes request, Attributes requestAttributes)
			throws DicomServiceException {
		
		if (mppsNSetIOD != null) {
			ValidationResult result = requestAttributes.validate(mppsNSetIOD);
			if (!result.isValid())
				throw DicomServiceException.valueOf(result, requestAttributes);
		}
		
		if (storageDirectory == null) {
			return null;
		}
		
		String instanceUID = request.getString(Tag.RequestedSOPInstanceUID);
		File file = new File(storageDirectory, instanceUID);
		if (!file.exists()) {
			throw new DicomServiceException(Status.NoSuchObjectInstance).setUID(Tag.AffectedSOPInstanceUID, instanceUID);
		}
		
		LOG.info(association + ": M-UPDATE " + file);
		Attributes data;
		DicomInputStream in = null;
		try {
			in = new DicomInputStream(file);
			data = in.readDataset(-1, -1);
		}
		catch (IOException e) {
			LOG.warn(association + ": Failed to read MPPS:", e);
			throw new DicomServiceException(Status.ProcessingFailure, e);
		}
		finally {
			SafeClose.close(in);
		}
		
		if (!IN_PROGRESS.equals(data.getString(Tag.PerformedProcedureStepStatus))) {
			BasicMPPSSCP.mayNoLongerBeUpdated();
		}
		
		data.addAll(requestAttributes);
		DicomOutputStream out = null;
		try {
			out = new DicomOutputStream(file);
			out.writeDataset(Attributes.createFileMetaInformation(instanceUID, request.getString(Tag.RequestedSOPClassUID),
				UID.ExplicitVRLittleEndian), data);
		}
		catch (IOException e) {
			LOG.warn(association + ": Failed to update MPPS:", e);
			throw new DicomServiceException(Status.ProcessingFailure, e);
		}
		finally {
			SafeClose.close(out);
		}
		
		String studyInstanceUID = requestAttributes.getSequence(Tag.ScheduledStepAttributesSequence)
				.get(0)
				.getString(Tag.StudyInstanceUID, "");
		
		if (requestAttributes.getString(Tag.PerformedProcedureStepStatus) == "COMPLETED") {
			radiologyStudyService.updateStudyPerformedStatus(studyInstanceUID, PerformedProcedureStepStatus.COMPLETED);
		} else {
			radiologyStudyService.updateStudyPerformedStatus(studyInstanceUID, PerformedProcedureStepStatus.DISCONTINUED);
		}
		
		return null;
	}
}
