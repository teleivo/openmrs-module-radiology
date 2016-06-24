package org.openmrs.module.radiology.order.web.resource;

import org.openmrs.TestOrder;
import org.openmrs.api.context.Context;
import org.openmrs.module.radiology.order.RadiologyOrder;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.annotation.PropertyGetter;
import org.openmrs.module.webservices.rest.web.annotation.SubClassHandler;
import org.openmrs.module.webservices.rest.web.api.RestService;
import org.openmrs.module.webservices.rest.web.representation.DefaultRepresentation;
import org.openmrs.module.webservices.rest.web.representation.FullRepresentation;
import org.openmrs.module.webservices.rest.web.representation.Representation;
import org.openmrs.module.webservices.rest.web.resource.api.PageableResult;
import org.openmrs.module.webservices.rest.web.resource.impl.BaseDelegatingSubclassHandler;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingResourceDescription;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingSubclassHandler;
import org.openmrs.module.webservices.rest.web.response.ResourceDoesNotSupportOperationException;
import org.openmrs.module.webservices.rest.web.v1_0.resource.openmrs1_10.TestOrderSubclassHandler1_10;
import org.openmrs.module.webservices.rest.web.v1_0.resource.openmrs2_0.RestConstants2_0;

/**
 * Exposes the {@link RadiologyOrder} subclass as a type in
 * {@link org.openmrs.module.webservices.rest.web.v1_0.resource.openmrs1_10.TestOrderSubclassHandler1_10}
 */
@SubClassHandler(supportedClass = RadiologyOrder.class, supportedOpenmrsVersions = { "2.0.*" })
public class RadiologyOrderResource extends BaseDelegatingSubclassHandler<TestOrder, RadiologyOrder>
        implements DelegatingSubclassHandler<TestOrder, RadiologyOrder> {
    
    
    /**
     * @see org.openmrs.module.webservices.rest.web.resource.impl.DelegatingSubclassHandler#getTypeName()
     */
    @Override
    public String getTypeName() {
        return "radiologyorder";
    }
    
    /**
     * @see org.openmrs.module.webservices.rest.web.resource.impl.DelegatingResourceHandler#getRepresentationDescription(org.openmrs.module.webservices.rest.web.representation.Representation)
     * @should return default representation given instance of defaultrepresentation
     * @should return full representation given instance of fullrepresentation
     * @should return null for representation other then default or full
     */
    @Override
    public DelegatingResourceDescription getRepresentationDescription(Representation rep) {
        if (rep instanceof DefaultRepresentation) {
            TestOrderSubclassHandler1_10 testOrderResource =
                    (TestOrderSubclassHandler1_10) Context.getService(RestService.class)
                            .getResourceBySupportedClass(TestOrder.class);
            DelegatingResourceDescription description = testOrderResource.getRepresentationDescription(rep);
            return description;
        } else if (rep instanceof FullRepresentation) {
            TestOrderSubclassHandler1_10 testOrderResource =
                    (TestOrderSubclassHandler1_10) Context.getService(RestService.class)
                            .getResourceBySupportedClass(TestOrder.class);
            DelegatingResourceDescription description = testOrderResource.getRepresentationDescription(rep);
            return description;
        } else {
            return null;
        }
    }
    
    /**
     * @see org.openmrs.module.webservices.rest.web.resource.impl.DelegatingResourceHandler#getCreatableProperties()
     */
    @Override
    public DelegatingResourceDescription getCreatableProperties() {
        
        TestOrderSubclassHandler1_10 testOrderResource = (TestOrderSubclassHandler1_10) Context.getService(RestService.class)
                .getResourceBySupportedClass(TestOrder.class);
        DelegatingResourceDescription description = testOrderResource.getCreatableProperties();
        return description;
    }
    
    /**
     * @see org.openmrs.module.webservices.rest.web.resource.impl.BaseDelegatingResource#getResourceVersion()
     * @should return supported resource version
     */
    @Override
    public String getResourceVersion() {
        
        return RestConstants2_0.RESOURCE_VERSION;
    }
    
    /**
     * Display string for {@link RadiologyOrder}
     * 
     * @param radiologyOrder RadiologyOrder of which display string shall be returned
     * @return ConceptName of given radiologyOrder
     * @should return concept name of given radiologyOrder
     * @should return no concept string if given radiologyOrders concept is null
     */
    @PropertyGetter("display")
    public String getDisplayString(RadiologyOrder radiologyOrder) {
        
        if (radiologyOrder.getConcept() == null)
            return "[No Concept]";
        return radiologyOrder.getConcept()
                .getName()
                .getName();
    }
    
    /**
     * @see org.openmrs.module.webservices.rest.web.resource.impl.BaseDelegatingResource#newDelegate()
     * @should throw ResourceDoesNotSupportOperationException
     */
    @Override
    public RadiologyOrder newDelegate() throws ResourceDoesNotSupportOperationException {
        
        throw new ResourceDoesNotSupportOperationException();
    }
    
    /**
     * @see org.openmrs.module.webservices.rest.web.resource.impl.DelegatingResourceHandler#save(java.lang.Object)
     * @should throw ResourceDoesNotSupportOperationException
     */
    @Override
    public RadiologyOrder save(RadiologyOrder delegate) throws ResourceDoesNotSupportOperationException {
        
        throw new ResourceDoesNotSupportOperationException();
    }
    
    /**
     * @see org.openmrs.module.webservices.rest.web.resource.impl.BaseDelegatingResource#purge(java.lang.Object,
     *      org.openmrs.module.webservices.rest.web.RequestContext)
     * @should throw ResourceDoesNotSupportOperationException
     */
    @Override
    public void purge(RadiologyOrder delegate, RequestContext context) throws ResourceDoesNotSupportOperationException {
        
        throw new ResourceDoesNotSupportOperationException();
    }
    
    /**
     * @see DelegatingSubclassHandler#getAllByType(org.openmrs.module.webservices.rest.web.RequestContext)
     */
    @SuppressWarnings("deprecation")
    @Override
    public PageableResult getAllByType(RequestContext arg0) throws ResourceDoesNotSupportOperationException {
        throw new ResourceDoesNotSupportOperationException();
    }
}
