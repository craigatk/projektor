import { formatBytes } from "../byteFormat";

describe("formatBytes", () => {
  it("should format a file size into a human readable string", () => {
    expect(formatBytes(1024)).toBe("1.02 kB");
  });

  it("should return an empty string when file size is not provided", () => {
    expect(formatBytes(undefined)).toBe("");
  });

  it("should return an empty string when file size is 0", () => {
    expect(formatBytes(0)).toBe("");
  });
});
