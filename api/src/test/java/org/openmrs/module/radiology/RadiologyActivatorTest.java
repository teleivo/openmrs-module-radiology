package org.openmrs.module.radiology;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.openmrs.api.context.Context;
import org.openmrs.test.BaseContextMockTest;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

/**
 * Tests {@link RadiologyActivator}.
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(Context.class)
public class RadiologyActivatorTest extends BaseContextMockTest {
	
	@Mock
	private RadiologyProperties radiologyProperties;
	
	@InjectMocks
	private RadiologyActivator radiologyActivator = new RadiologyActivator();
	
	@Before
	public void runBeforeAllTests() {
		when(radiologyProperties.getDicomMppsSCPAeTitle()).thenReturn("RADIOLOGY_MODULE");
		when(radiologyProperties.getDicomMppsSCPPort()).thenReturn("11114");
		when(radiologyProperties.getDicomMppsSCPStorageDirectory()).thenReturn("mpps");
		PowerMockito.mockStatic(Context.class);
		Mockito.when(Context.getRegisteredComponent("radiologyProperties", RadiologyProperties.class))
				.thenReturn(radiologyProperties);
	}
	
	/**
	 * @see RadiologyActivator#getDicomOrderFillerArguments()
	 * @verifies should return dicom order filler arguments
	 */
	@Test
	public void getDicomOrderFillerArguments_shouldReturnDicomOrderFillerArguments() throws Exception {
		
		String[] dicomOrderFillerArguments = radiologyActivator.getDicomOrderFillerArguments();
		
		assertThat(dicomOrderFillerArguments[0], is("--bind"));
		assertThat(dicomOrderFillerArguments[1], is("RADIOLOGY_MODULE:11114"));
		assertThat(dicomOrderFillerArguments[2], is("--directory"));
		assertThat(dicomOrderFillerArguments[3], is("mpps"));
	}
}
