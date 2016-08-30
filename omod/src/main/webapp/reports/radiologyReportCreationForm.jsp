<%@ include file="/WEB-INF/template/include.jsp"%>
<%@ include file="/WEB-INF/template/header.jsp"%>

<span id="radiologyOrderDetailsBoxHeaderId" class="boxHeader">
Create a report
</span>
<form:form method="post" modelAttribute="radiologyReportClaim" cssClass="box">
    <form:hidden path="radiologyOrder" />
    <form:hidden path="radiologyReport.radiologyOrder" />
    <input type="submit" name="createRadiologyReport" value="Create report from free text" />
</form:form>
<form:form method="post" modelAttribute="mrrtRadiologyReport" cssClass="box">
    <form:hidden path="id" />
    <form:hidden path="radiologyOrder" />
    <input type="submit" name="createRadiologyReport" value="Create report from template" />
</form:form>

<%@ include file="/WEB-INF/template/footer.jsp"%>
