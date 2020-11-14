import moment from "moment";

const formatSecondsDuration = (
  durationInSeconds: number,
  includeMSThreshold: number = 60 * 60
): string => {
  const secondsFormat =
    durationInSeconds < includeMSThreshold ? "s.SSS[s]" : "s[s]";

  let timeFormat = secondsFormat;

  if (durationInSeconds >= 60 * 60) {
    timeFormat = "H[h] m[m]";
  } else if (durationInSeconds >= 60) {
    timeFormat = `m[m] ${secondsFormat}`;
  } else if (durationInSeconds === 0) {
    timeFormat = "s[s]";
  }

  return moment("2015-01-01")
    .startOf("day")
    .milliseconds(durationInSeconds * 1000)
    .format(timeFormat);
};

const formatSecondsDurationWithoutMS = (durationInSeconds: number): string =>
  formatSecondsDuration(durationInSeconds, 0);

export { formatSecondsDuration, formatSecondsDurationWithoutMS };
