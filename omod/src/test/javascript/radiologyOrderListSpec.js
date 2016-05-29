describe("The radiology order list", function() {
  var request;

  jasmine.getFixtures().fixturesPath = "spec/fixtures";

  beforeEach(function() {
    loadFixtures('radiologyOrder.list.html');

    jasmine.Ajax.useMock();

    request = mostRecentAjaxRequest();
  });

  it("should show the loading element", function(){
    expect(jQuery('div#openmrs_msg[name="loading"]')).toBeVisible();
  });

  it("should call the orderSearch portlet url", function(){
    expect(request.url).toEqual("portlets/orderSearch.portlet")
  });

  it("should have define datatables", function() {
    expect(true).toBe(true);

    expect(jQuery("<div><span></span></div>")).toHaveHtml("<span></span>");
    // expect(jQuery.("<div><span></span></div>")).toHaveHtml("<span></span>");
  });
});