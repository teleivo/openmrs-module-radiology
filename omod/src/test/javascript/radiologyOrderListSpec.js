describe("The radiology order list", function() {

  jasmine.getFixtures().fixturesPath = "spec/fixtures";

  beforeEach(function() {
    jasmine.Clock.useMock();
    jasmine.Ajax.useMock();
    loadFixtures('radiologyOrder.list.html');
  });

  it("should have datatables defined", function() {
    expect(true).toBe(true);

    expect(jQuery("<div><span></span></div>")).toHaveHtml("<span></span>");
    // expect(jQuery.("<div><span></span></div>")).toHaveHtml("<span></span>");
  });
});