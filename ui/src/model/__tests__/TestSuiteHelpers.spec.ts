import { TestSuite } from "../TestRunModel";
import { fullTestSuiteName } from "../TestSuiteHelpers";

describe("TestSuiteHelpers", () => {
  describe("fullTestSuiteName", () => {
    it("should return class name when no package name", () => {
      const testSuite = {
        className: "MyClass",
      } as TestSuite;

      expect(fullTestSuiteName(testSuite)).toEqual("MyClass");
    });

    it("should return package name and class name when package name set and it does not include class name", () => {
      const testSuite = {
        className: "MyClass",
        packageName: "test.package",
      } as TestSuite;

      expect(fullTestSuiteName(testSuite)).toEqual("test.package.MyClass");
    });

    it("should return package name when package name set and it does include class name", () => {
      const testSuite = {
        className: "RepositoryCoverageTimelineGraph",
        packageName:
          "src/Repository/Coverage/__tests__/RepositoryCoverageTimelineGraph.spec.tsx",
      } as TestSuite;

      expect(fullTestSuiteName(testSuite)).toEqual(
        "src/Repository/Coverage/__tests__/RepositoryCoverageTimelineGraph.spec.tsx",
      );
    });
  });
});
