<%@ include file="/WEB-INF/view/module/radiology/template/includeTags.jsp"%>
<c:set var="DO_NOT_INCLUDE_JQUERY" value="true" />
<%@ include file="/WEB-INF/template/header.jsp"%>
<%@ include file="/WEB-INF/template/include.jsp"%>
<%@ include file="/WEB-INF/view/module/radiology/template/includeScripts.jsp"%>

// TODO do not include moment js, since we do not include the INCLUDE_TIME_ADJUSTMENT we do not have $j defined
<script>
var $j = jQuery.noConflict();
</script>

<span id="radiologyOrderDetailsBoxHeaderId" class="boxHeader">
Create a report
</span>
<form:form method="post" modelAttribute="radiologyReportClaim" cssClass="box">
    <form:hidden path="radiologyOrder" />
    <form:hidden path="radiologyReport.radiologyOrder" />
    <input type="submit" name="createRadiologyReport" value="Create report from free text" />
</form:form>
<form:form method="post" modelAttribute="mrrtRadiologyReportClaim" cssClass="box">
    <form:hidden path="radiologyOrder" />
    <radiology:mrrtReportTemplateField formFieldName="mrrtReportTemplate" formFieldId="mrrtReportTemplateSearch" />
    <input type="submit" name="createRadiologyReportTemplate" value="Create report from template" />
</form:form>

<%@ include file="/WEB-INF/template/footer.jsp"%>
