<%@ include file="/WEB-INF/template/include.jsp"%>
<c:set var="DO_NOT_INCLUDE_JQUERY" value="true" />
<%@ include file="/WEB-INF/template/header.jsp"%>
<c:set var="INCLUDE_TIME_ADJUSTMENT" value="true" />
<%@ include file="/WEB-INF/view/module/radiology/template/includeScripts.jsp"%>

<%@ include file="/WEB-INF/view/module/radiology/localHeader.jsp"%>

<openmrs:require
  allPrivileges="Get Care Settings,Get Concepts,Get Encounter Roles,Get Encounters,Get Orders,Get Patients,Get Providers,Get Radiology Orders,Get Users,Get Visit Attribute Types,Get Visit Types,Get Visits,View Orders"
  otherwise="/login.htm" redirect="/module/radiology/radiologyOrder.form" />

<c:choose>
  <c:when test="${not empty radiologyOrder && empty radiologyOrder.orderId}">
    <!--  Create a new RadiologyOrder -->
    <openmrs:hasPrivilege privilege="Add Encounters">
      <openmrs:hasPrivilege privilege="Add Orders">
        <openmrs:hasPrivilege privilege="Add Radiology Orders">
          <openmrs:hasPrivilege privilege="Add Visits">
            <openmrs:hasPrivilege privilege="Edit Encounters">
              <openmrs:hasPrivilege privilege="Edit Visits">
                <%@ include file="radiologyOrderCreationSegment.jsp"%>
              </openmrs:hasPrivilege>
            </openmrs:hasPrivilege>
          </openmrs:hasPrivilege>
        </openmrs:hasPrivilege>
      </openmrs:hasPrivilege>
    </openmrs:hasPrivilege>

  </c:when>

  <c:otherwise>
    <!--  Show existing RadiologyOrder/discontinued Order -->
    <openmrs:hasPrivilege privilege="View Patients">
      <openmrs:portlet url="patientHeader" id="patientDashboardHeader" patientId="${order.patient.patientId}" />
      <br>
    </openmrs:hasPrivilege>
    <c:choose>
      <c:when test="${not empty radiologyOrder}">
        <!--  Show existing RadiologyOrder -->
        <%@ include file="radiologyOrderDisplaySegment.jsp"%>
        <c:if test="${radiologyOrder.completed}">
          <!--  Show form for radiology report -->
          <openmrs:hasPrivilege privilege="Add Radiology Reports">
            <openmrs:hasPrivilege privilege="Delete Radiology Reports">
              <openmrs:hasPrivilege privilege="Edit Radiology Reports">
                <openmrs:hasPrivilege privilege="Get Radiology Reports">
                    </br>
                    <openmrs:portlet url="createRadiologyReport" moduleId="radiology" parameters="orderUuid=${radiologyOrder.uuid}" />
                </openmrs:hasPrivilege>
              </openmrs:hasPrivilege>
            </openmrs:hasPrivilege>
          </openmrs:hasPrivilege>

        </c:if>
        <c:if test="${radiologyOrder.discontinuationAllowed}">
          <!--  Show form to discontinue an active non in progress/completed RadiologyOrder -->
          <openmrs:hasPrivilege privilege="Delete Radiology Orders">
            <openmrs:hasPrivilege privilege="Edit Orders">
              <%@ include file="radiologyOrderDiscontinuationSegment.jsp"%>
            </openmrs:hasPrivilege>
          </openmrs:hasPrivilege>
        </c:if>
      </c:when>
      <c:otherwise>
        <!--  Show read-only view of discontinuation Order -->
        <%@ include file="discontinuationOrderDisplaySegment.jsp"%>
      </c:otherwise>
    </c:choose>
  </c:otherwise>
</c:choose>
<%@ include file="/WEB-INF/template/footer.jsp"%>
