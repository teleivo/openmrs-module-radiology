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
                    var patientUuid = $j('#ordersTabPatientFilter');
                    var find = $j('#ordersTabFind');
                    var clearResults = $j('a#ordersTabClearFilters');

                    var radiologyOrdersTable = $j('#ordersTabTable')
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
                                        url: "${pageContext.request.contextPath}/ws/rest/v1/radiologyorder/",
                                        data: function(data) {
                                          return {
                                            startIndex: data.start,
                                            limit: data.length,
                                            v: "full",
                                            patient: patientUuid.val(),
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
                                            "className": "details-control",
                                            "orderable": false,
                                            "data": null,
                                            "defaultContent": ""
                                          },
                                          {
                                            "name": "orderNumber",
                                            "render": function(data, type,
                                                    full, meta) {
                                              return '<a href="${pageContext.request.contextPath}/module/radiology/radiologyOrder.form?orderId='
                                                      + full.uuid
                                                      + '">'
                                                      + full.orderNumber
                                                      + '</a>';
                                            }
                                          },
                                          {
                                            "name": "patient",
                                            "render": function(data, type,
                                                    full, meta) {
                                              return full.patient.display;
                                            }
                                          },
                                          {
                                            "name": "urgency",
                                            "render": function(data, type,
                                                    full, meta) {
                                              return full.urgency;
                                            }
                                          },
                                          {
                                            "name": "concept",
                                            "render": function(data, type,
                                                    full, meta) {
                                              return full.concept.display;
                                            }
                                          },
                                          {
                                            "name": "orderer",
                                            "render": function(data, type,
                                                    full, meta) {
                                              return full.orderer.display;
                                            }
                                          },
                                          {
                                            "name": "scheduledDate",
                                            "render": function(data, type,
                                                    full, meta) {
                                              var result = "";
                                              if (full.scheduledDate) {

                                                result = moment(
                                                        full.scheduledDate)
                                                        .format("LLL");
                                              }
                                              return result;
                                            }
                                          },
                                          {
                                            "name": "dateActivated",
                                            "render": function(data, type,
                                                    full, meta) {
                                              var result = "";
                                              if (full.dateActivated) {

                                                result = moment(
                                                        full.dateActivated)
                                                        .format("LLL");
                                              }
                                              return result;
                                            }
                                          },
                                          {
                                            "name": "orderReason",
                                            "visible": false,
                                            "render": function(data, type,
                                                    full, meta) {
                                              if ((typeof (full.orderReason) !== 'undefined')
                                                      && (full.orderReason !== null)) {
                                                return full.orderReason.display;
                                              } else {
                                                return "";
                                              }
                                            }
                                          },
                                          {
                                            "name": "orderReasonNonCoded",
                                            "visible": false,
                                            "render": function(data, type,
                                                    full, meta) {
                                              return full.orderReasonNonCoded;
                                            }
                                          },
                                          {
                                            "name": "instructions",
                                            "visible": false,
                                            "render": function(data, type,
                                                    full, meta) {
                                              return full.instructions;
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
                      radiologyOrdersTable.ajax.reload();
                    });

                    clearResults.on('mouseup keyup', function() {
                      $j('table#ordersTabTableFilters input:text').val('');
                      patientUuid.val('');
                      radiologyOrdersTable.ajax.reload();
                    });

                    function formatChildRow(data) {
                      var orderReason = "";
                      var orderReasonNonCoded = "";
                      var instructions = "";

                      if ((typeof (data.orderReason) !== 'undefined')
                              && (data.orderReason !== null)) {
                        orderReason = data.orderReason.display;
                      }
                      if ((typeof (data.orderReasonNonCoded) !== 'undefined')
                              && (data.orderReasonNonCoded !== null)) {
                        orderReasonNonCoded = data.orderReasonNonCoded;
                      }
                      if ((typeof (data.instructions) !== 'undefined')
                              && (data.instructions !== null)) {
                        instructions = data.instructions;
                      }
                      return '<table cellpadding="5" cellspacing="0" border="0" style="padding-left:50px;">'
                              + '<tr>'
                              + '<td><spring:message code="radiology.datatables.column.orderReason"/>:</td>'
                              + '<td>'
                              + orderReason
                              + '</td>'
                              + '</tr>'
                              + '<tr>'
                              + '<td><spring:message code="radiology.datatables.column.orderReasonNonCoded"/>:</td>'
                              + '<td>'
                              + orderReasonNonCoded
                              + '</td>'
                              + '</tr>'
                              + '<tr>'
                              + '<td><spring:message code="radiology.datatables.column.instructions"/>:</td>'
                              + '<td>'
                              + instructions
                              + '</td>'
                              + '</tr>'
                              + '</table>';
                    }

                    $j('#ordersTabTable tbody').on('click', 'td',
                            function(e) {
                              if ($j(e.target).is(':not(td)')) { return; }

                              var tr = $j(this).closest('tr');
                              var row = radiologyOrdersTable.row(tr);

                              if (row.child.isShown()) {
                                row.child.hide();
                                tr.removeClass('shown');
                              } else {
                                row.child(formatChildRow(row.data())).show();
                                tr.addClass('shown');
                              }
                            });

                  });
</script>

<openmrs:hasPrivilege privilege="Add Radiology Orders">
  <br>
  <a href="radiologyOrder.form"><spring:message code="radiology.addOrder" /></a>
  <br>
</openmrs:hasPrivilege>
<br>
<span class="boxHeader"> <b><spring:message code="radiology.radiologyOrders" /></b> <a id="ordersTabClearFilters" href="#"
  style="float: right"> <spring:message code="radiology.clearResults" />
</a>
</span>
<div class="box">
  <table id="ordersTabTableFilters" cellspacing="10">
    <tr>
      <form>
        <td><label><spring:message code="radiology.patient" /></label> <radiology:patientField formFieldName="patient"
            formFieldId="ordersTabPatientFilter" /></td>
        <td><input id="ordersTabFind" type="button" value="<spring:message code="radiology.find"/>" /></td>
      </form>
    </tr>
  </table>
  <br>
  <div>
    <table id="ordersTabTable" cellspacing="0" width="100%" class="display nowrap">
      <thead>
        <tr>
          <th></th>
          <th><spring:message code="radiology.datatables.column.orderNumber" /></th>
          <th><spring:message code="radiology.datatables.column.patient" /></th>
          <th><spring:message code="radiology.datatables.column.priority" /></th>
          <th><spring:message code="radiology.datatables.column.imagingProcedure" /></th>
          <th><spring:message code="radiology.datatables.column.referringPhysician" /></th>
          <th><spring:message code="radiology.datatables.column.scheduledDate" /></th>
          <th><spring:message code="radiology.datatables.column.dateActivated" /></th>
          <th><spring:message code="radiology.datatables.column.orderReason" /></th>
          <th><spring:message code="radiology.datatables.column.orderReasonNonCoded" /></th>
          <th><spring:message code="radiology.datatables.column.instructions" /></th>
        </tr>
      </thead>
    </table>
  </div>
</div>
<br />