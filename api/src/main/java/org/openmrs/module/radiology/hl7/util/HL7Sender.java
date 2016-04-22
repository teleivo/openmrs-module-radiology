package org.openmrs.module.radiology.hl7.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dcm4che3.tool.hl7snd.HL7Snd;
import org.openmrs.api.context.Context;
import org.openmrs.module.radiology.RadiologyProperties;

public class HL7Sender {
	
	private static final Log log = LogFactory.getLog(HL7Sender.class);
	
	// private static final int HL7_SEND_SUCCESS = 1;
	
	private static RadiologyProperties radiologyProperties = Context.getRegisteredComponent("radiologyProperties",
		RadiologyProperties.class);
	
	// Send HL7 ORU message to dcm4chee.
	public static boolean sendHL7Message(String hl7message) {
		
		boolean result = false;
		
		// use buffering
		File filename = new File("hl7", java.util.UUID.randomUUID()
				.toString());
		log.info("Create hl7 file: " + filename.getAbsolutePath());
		Writer output = null;
		try {
			output = new BufferedWriter(new FileWriter(filename.getAbsolutePath()));
		}
		catch (IOException e) {
			log.error("Failed to write hl7 file");
		}
		
		// OutputStream outputStream = new OutputStreamWriter(new
		// FileOutputStream(filename.getAbsolutePath()),Charset.forName("UTF-8"));
		// outputStream.write(hl7message.getBytes(Charset.forName("UTF-8")));
		//
		try {
			output.write(hl7message);
		}
		catch (IOException ioException) {
			log.error("Failed to write hl7 file");
		}
		finally {
			if (output != null) {
				try {
					output.close();
				}
				catch (IOException e) {
					// TODO Auto-generated catch block
					log.error("Error generated", e);
				}
			}
		}
		
		final String input[] = { "-c", radiologyProperties.getPacsAddress() + ":" + radiologyProperties.getPacsHL7Port(),
				filename.getAbsolutePath() };
		HL7Snd.main(input);
		return true;
	}
}
