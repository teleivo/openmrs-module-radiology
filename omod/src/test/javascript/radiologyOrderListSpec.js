describe(
        "The radiology order list",
        function() {
          var request;
          var patientQuery;
          var startDate;
          var endDate;
          var clearResults;

          jasmine.getFixtures().fixturesPath = "spec/fixtures";

          beforeEach(function() {
            loadFixtures('radiologyOrder.list.html');

            //clearResults = jQuery("a#clearResults");
            clearResults = $j("a#clearResults");

            patientQuery = $j('input[name="patientQuery"]');
            startDate = jQuery('input[name="startDate"]');
            endDate = jQuery('input[name="endDate"]');
            patientQuery.val("Hornblower");
            startDate.val("2016-05-01");
            endDate.val("2016-05-31");
            patientQuery.val("XXX");

            //jQuery("tbody#radiologyOrdersTableBody").html("table data");

            jasmine.Ajax.useMock();

            request = mostRecentAjaxRequest();
          });

          it("should show the loading element", function() {
            expect(jQuery('div#openmrs_msg[name="loading"]')).toBeVisible();
          });

          //it("should call the orderSearch portlet url", function() {
          //expect(request.url).toEqual("portlets/orderSearch.portlet")
          //});

          it("should have define datatables", function() {
            //    expect(jQuery("<div><span></span></div>")).toHaveHtml("<span></span>");
            expect(jQuery("<div><span></span></div>")).toHaveHtml(
                    "<span></span>");
          });

          describe(
                  "clicking the clearResults anchor with values in search box text inputs",
                  function() {
                    it("should clear search box on clearResults click",
                            function() {
                              spyEvent = spyOnEvent("a#clearResults", 'click');
                              //clearResults.trigger("click");
                              clearResults.click();

                              expect('click').toHaveBeenTriggeredOn(
                                      "a#clearResults");
                              expect(spyEvent).toHaveBeenTriggered();

                              expect(patientQuery).toHaveValue("XXX");
                              //expect(patientQuery).not.toHaveValue("Hornblower");
                            });
                  });
        });
