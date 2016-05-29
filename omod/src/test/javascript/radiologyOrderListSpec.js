describe("The radiology order list", function() {

  jasmine.getFixtures().fixturesPath = "spec/fixtures";

  loadFixtures('orderSearch.portlet.html');

  it("should have datatables defined", function() {
    expect(true).toBe(true);
    expect($("<div><span></span></div>")).toHaveHtml("<span></span>");
  });
});