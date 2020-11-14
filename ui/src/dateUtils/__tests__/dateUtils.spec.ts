import {
  formatSecondsDuration,
  formatSecondsDurationWithoutMS,
} from "../dateUtils";

describe("dateUtils", () => {
  describe("formatSecondsDuration", () => {
    it("should format duration less than 1 second", () => {
      expect(formatSecondsDuration(0.423, 1)).toEqual("0.423s");
    });

    it("should format duration less than 1 minute with MS threshold of 1", () => {
      expect(formatSecondsDuration(23.423, 1)).toEqual("23s");
    });

    it("should format duration less than 1 minute", () => {
      expect(formatSecondsDuration(34.023)).toEqual("34.023s");
    });

    it("should format duration over 1 minute and less than 1 hour", () => {
      expect(formatSecondsDuration(125.041)).toEqual("2m 5.041s");
    });

    it("should format duration over 1 hour", () => {
      expect(formatSecondsDuration(60 * 60 + 67)).toEqual("1h 1m");
    });

    it("should format 0 seconds without any milliseconds", () => {
      expect(formatSecondsDuration(0)).toEqual("0s");
    });
  });

  describe("formatSecondsDurationWithoutMS", () => {
    it("should format duration less than 1 minute", () => {
      expect(formatSecondsDurationWithoutMS(34.02)).toEqual("34s");
    });

    it("should format duration over 1 minute and less than 1 hour", () => {
      expect(formatSecondsDurationWithoutMS(125.04)).toEqual("2m 5s");
    });

    it("should format duration over 10 minutes and less than 1 hour", () => {
      expect(formatSecondsDurationWithoutMS(60 * 10 + 144)).toEqual("12m 24s");
    });
  });
});
