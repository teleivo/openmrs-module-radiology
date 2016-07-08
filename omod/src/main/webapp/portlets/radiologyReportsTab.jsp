<%@ include file="/WEB-INF/view/module/radiology/template/include.jsp"%>
<%@ include file="/WEB-INF/view/module/radiology/template/includeDatatablesWithDefaults.jsp"%>
<openmrs:htmlInclude file="/moduleResources/radiology/scripts/moment/moment-with-locales.min.js" />

<script type="text/javascript">
  // configure current locale as momentjs default, fall back to "en" if locale not found
  moment.locale([jsLocale, 'en']);

  var $j = jQuery.noConflict();
  $j(document)
          .ready(
                  function() {
                    var providerUuid = $j('#reportsTabProviderFilter');
                    var find = $j('#reportsTabFind');
                    var clearResults = $j('a#reportsTabClearFilters');

                    var radiologyReportsTable = $j('#reportsTabTable')
                            .DataTable(
                                    {
                                      "processing": true,
                                      "serverSide": true,
                                      "ajax": {
                                        headers: {
                                          Accept: "application/json; charset=utf-8",
                                          "Content-Type": "text/plain; charset=utf-8",
                                        },
                                        cache: true,
                                        dataType: "json",
                                        url: "${pageContext.request.contextPath}/ws/rest/v1/radiologyreport/",
                                        data: function(data) {
                                          return {
                                            startIndex: data.start,
                                            limit: data.length,
                                            v: "full",
                                            principalResultsInterpreter: providerUuid.val(),
                                            totalCount: true,
                                          };
                                        },
                                        "dataFilter": function(data) {
                                          var json = $j.parseJSON(data);
                                          json.recordsTotal = json.totalCount || 0;
                                          json.recordsFiltered = json.totalCount || 0;
                                          json.data = json.results;
                                          return JSON.stringify(json);
                                        }
                                      },
                                      "columns": [
                                          {
                                            "name": "view",
                                            "render": function(data, type,
                                                    full, meta) {
                                              return '<a href="${pageContext.request.contextPath}/module/radiology/radiologyReport.form?radiologyReportId='
                                                      + full.uuid
                                                      + '">View</a>';
                                            }
                                          },
                                          {
                                            "name": "radiologyOrder",
                                            "render": function(data, type,
                                                    full, meta) {
                                              return full.radiologyOrder.display;
                                            }
                                          },
                                          {
                                            "name": "principalResultsInterpreter",
                                            "render": function(data, type,
                                                    full, meta) {
                                              return full.principalResultsInterpreter.display;
                                            }
                                          },
                                          {
                                            "name": "date",
                                            "render": function(data, type,
                                                    full, meta) {
                                              var result = "";
                                              if (full.reportDate) {

                                                result = moment(full.reportDate)
                                                        .format("LLL");
                                              }
                                              return result;
                                            }
                                          },
                                          {
                                            "name": "reportStatus",
                                            "visible": false,
                                            "render": function(data, type,
                                                    full, meta) {
                                              return full.reportStatus;
                                            }
                                          }, ],
                                    });

                    // prevent form submit when user hits enter
                    $j(window).keydown(function(event) {
                      if (event.keyCode == 13) {
                        event.preventDefault();
                        return false;
                      }
                    });

                    find.on('mouseup keyup', function(event) {
                      if (event.type == 'keyup' && event.keyCode != 13) return;
                      radiologyReportsTable.ajax.reload();
                    });

                    clearResults.on('mouseup keyup', function() {
                      $j('table#reportsTabTableFilters input:text').val('');
                      providerUuid.val('');
                      radiologyReportsTable.ajax.reload();
                    });

                  });
</script>

<br>
<span class="boxHeader"> <b><spring:message code="radiology.report.boxheader" /></b> <a id="reportsTabClearFilters"
  href="#" style="float: right"> <spring:message code="radiology.clearResults" />
</a>
</span>
<div class="box">
  <table id="reportsTabTableFilters" cellspacing="10">
    <tr>
      <form>
        <td><label><spring:message code="radiology.report.principalResultsInterpreter" /></label> <radiology:providerField
            formFieldName="provider" formFieldId="reportsTabProviderFilter" /></td>
        <td><input id="reportsTabFind" type="button" value="<spring:message code="radiology.find"/>" /></td>
      </form>
    </tr>
  </table>
  <br>
  <div>
    <table id="reportsTabTable" cellspacing="0" width="100%" class="display nowrap">
      <thead>
        <tr>
          <th><spring:message code="radiology.datatables.column.report.view" /></th>
          <th><spring:message code="radiology.datatables.column.report.order" /></th>
          <th><spring:message code="radiology.datatables.column.report.principalResultsInterpreter" /></th>
          <th><spring:message code="radiology.datatables.column.report.date" /></th>
          <th><spring:message code="radiology.datatables.column.report.status" /></th>
        </tr>
      </thead>
    </table>
  </div>
</div>
<br />