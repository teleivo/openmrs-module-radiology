<%@ include file="/WEB-INF/template/include.jsp"%>

<%--
  available properties:
    orderUuid -> (String) the uuid of the radiology order for which a RadiologyReport should be prepared for creation
--%>

<c:set var="rOrder" value="${model.radiologyOrder}" />
<c:set var="radiologyReport" value="${model.radiologyReport}" />
    <c:set var="mrrtRadiologyReport" value="${model.mrrtRadiologyReport}" />
    <c:if test="${not empty rOrder}">
      <span id="radiologyOrderDetailsBoxHeaderId" class="boxHeader">
      Create a report
      </span>
      <form:form method="post" action="/module/radiology/radiologyReport.form?orderId=${rOrder.orderId}" modelAttribute="radiologyReport" cssClass="box">
        <form:hidden path="id" />
        <form:hidden path="radiologyOrder" />
        <input type="submit" value="Create free text report" />
      </form:form>
    <form:form method="post" action="/module/radiology/radiologyReport.form?orderId=${rOrder.orderId}" modelAttribute="mrrtRadiologyReport" cssClass="box">
        <form:hidden path="id" />
        <form:hidden path="radiologyOrder" />
        <form:hidden path="mrrtReportTemplate" />
    <input type="submit" value="Create mrrt report" />
  </form:form>
</c:if>
