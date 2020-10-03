import moment from "moment";

const formatSecondsDuration = (
  durationInSeconds: number,
  includeMS: boolean = true
): string => {
  const secondsFormat = includeMS ? "s.SSS[s]" : "s[s]";

  let timeFormat = secondsFormat;

  if (durationInSeconds >= 60 * 60) {
    timeFormat = "H[h] m[m]";
  } else if (durationInSeconds >= 60) {
    timeFormat = `m[m] ${secondsFormat}`;
  }

  return moment("2015-01-01")
    .startOf("day")
    .milliseconds(durationInSeconds * 1000)
    .format(timeFormat);
};

const formatSecondsDurationWithoutMS = (durationInSeconds: number): string =>
  formatSecondsDuration(durationInSeconds, false);

export { formatSecondsDuration, formatSecondsDurationWithoutMS };
