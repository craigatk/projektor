import { TestCase } from "../../model/TestRunModel";
import { sortTestSuiteTestCases } from "../sort";

describe("test suite sort util", () => {
  it("should sort failed test cases first, then passed, then skipped", () => {
    const failed1 = {
      name: "failed1",
      failure: { message: "" },
      skipped: false,
      passed: false,
    } as unknown as TestCase;
    const failed2 = {
      name: "failed2",
      failure: { message: "" },
      skipped: false,
      passed: false,
    } as unknown as TestCase;
    const passed1 = {
      name: "passed1",
      failure: null,
      skipped: false,
      passed: true,
    } as unknown as TestCase;
    const passed2 = {
      name: "passed2",
      failure: null,
      skipped: false,
      passed: true,
    } as unknown as TestCase;
    const skipped1 = {
      name: "skipped1",
      failure: null,
      skipped: true,
      passed: false,
    } as unknown as TestCase;
    const skipped2 = {
      name: "skipped2",
      failure: null,
      skipped: true,
      passed: false,
    } as unknown as TestCase;

    const sortedTestCases = sortTestSuiteTestCases([
      skipped1,
      passed1,
      failed1,
      failed2,
      passed2,
      skipped2,
    ]);

    expect(sortedTestCases[0].name).toContain("failed");
    expect(sortedTestCases[1].name).toContain("failed");
    expect(sortedTestCases[2].name).toContain("passed");
    expect(sortedTestCases[3].name).toContain("passed");
    expect(sortedTestCases[4].name).toContain("skipped");
    expect(sortedTestCases[5].name).toContain("skipped");
  });
});
