/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.radiology.order.web.resource;

import org.openmrs.Order;
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
import org.openmrs.module.webservices.rest.web.v1_0.resource.openmrs1_10.OrderResource1_10;
import org.openmrs.module.webservices.rest.web.v1_0.resource.openmrs2_0.RestConstants2_0;

/**
 * Exposes the {@link RadiologyOrder} subclass as a type in
 * {@link org.openmrs.module.webservices.rest.web.v1_0.resource.openmrs1_8.OrderResource1_8}
 */
@SubClassHandler(supportedClass = RadiologyOrder.class, supportedOpenmrsVersions = { "2.0.*" })
public class RadiologyOrderSubclassHandler extends BaseDelegatingSubclassHandler<Order, RadiologyOrder>
        implements DelegatingSubclassHandler<Order, RadiologyOrder> {
    
    
    /**
     * @see org.openmrs.module.webservices.rest.web.resource.impl.
     *      DelegatingResourceHandler#getRepresentationDescription(org.openmrs.
     *      module.webservices.rest.web.representation.Representation)
     * @should return default representation given instance of
     *         defaultrepresentation
     * @should return full representation given instance of fullrepresentation
     * @should return null for representation other then default or full
     */
    @Override
    public DelegatingResourceDescription getRepresentationDescription(Representation rep) {
        
        if (rep instanceof DefaultRepresentation) {
            OrderResource1_10 orderResource = (OrderResource1_10) Context.getService(RestService.class)
                    .getResourceBySupportedClass(Order.class);
            DelegatingResourceDescription description = orderResource.getRepresentationDescription(rep);
            description.addProperty("accessionNumber");
            description.addProperty("scheduledDate");
            return description;
        } else if (rep instanceof FullRepresentation) {
            OrderResource1_10 orderResource = (OrderResource1_10) Context.getService(RestService.class)
                    .getResourceBySupportedClass(Order.class);
            DelegatingResourceDescription description = orderResource.getRepresentationDescription(rep);
            description.addProperty("accessionNumber");
            description.addProperty("scheduledDate");
            return description;
        } else {
            return null;
        }
    }
    
    /**
     * @see org.openmrs.module.webservices.rest.web.resource.impl.DelegatingResourceHandler#getResourceVersion()
     * @should return supported resource version
     */
    @Override
    public String getResourceVersion() {
        
        return RestConstants2_0.RESOURCE_VERSION;
    }
    
    /**
     * @see org.openmrs.module.webservices.rest.web.resource.impl.DelegatingSubclassHandler#getTypeName()
     * @should return type name of resource
     */
    @Override
    public String getTypeName() {
        
        return "radiologyorder";
    }
    
    /**
     * Get the display string for a {@link RadiologyOrder}.
     * 
     * @param radiologyOrder the radiology order of which the display string shall be returned
     * @return the accession number and the concept name of given radiology order
     * @should return accession number and concept name of given radiology order
     * @should return no concept string if given radiologyOrders concept is null
     */
    @PropertyGetter("display")
    public String getDisplayString(RadiologyOrder radiologyOrder) {
        
        if (radiologyOrder.getConcept() == null) {
            return radiologyOrder.getAccessionNumber() + " - " + "[No Concept]";
        } else {
            return radiologyOrder.getAccessionNumber() + " - " + radiologyOrder.getConcept()
                    .getName()
                    .getName();
        }
    }
    
    /**
     * @see org.openmrs.module.webservices.rest.web.resource.impl.DelegatingResourceHandler#getCreatableProperties()
     * @should throw ResourceDoesNotSupportOperationException
     */
    @Override
    public DelegatingResourceDescription getCreatableProperties() throws ResourceDoesNotSupportOperationException {
        throw new ResourceDoesNotSupportOperationException();
    }
    
    /**
     * @see org.openmrs.module.webservices.rest.web.resource.impl.DelegatingResourceHandler#getUpdatableProperties()
     * @should throw ResourceDoesNotSupportOperationException
     */
    @Override
    public DelegatingResourceDescription getUpdatableProperties() throws ResourceDoesNotSupportOperationException {
        throw new ResourceDoesNotSupportOperationException();
    }
    
    /**
     * @see org.openmrs.module.webservices.rest.web.resource.impl.DelegatingResourceHandler#getAllByType(org.openmrs.module.webservices.rest.web.RequestContext)
     * @should throw ResourceDoesNotSupportOperationException
     */
    @Override
    public PageableResult getAllByType(RequestContext context) throws ResourceDoesNotSupportOperationException {
        throw new ResourceDoesNotSupportOperationException();
    }
    
    /**
     * @see org.openmrs.module.webservices.rest.web.resource.impl.DelegatingResourceHandler#newDelegate()
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
     * @see org.openmrs.module.webservices.rest.web.resource.impl.DelegatingResourceHandler#purge(java.lang.Object,
     *      org.openmrs.module.webservices.rest.web.RequestContext)
     * @should throw ResourceDoesNotSupportOperationException
     */
    @Override
    public void purge(RadiologyOrder delegate, RequestContext context) throws ResourceDoesNotSupportOperationException {
        
        throw new ResourceDoesNotSupportOperationException();
    }
    
}
