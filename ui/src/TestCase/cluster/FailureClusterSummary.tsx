import * as React from "react";
import classes from "./FailureClusterSummary.module.css";
import { FailureClusters } from "../../model/TestRunModel";
import { fetchFailureClusters } from "../../service/TestRunService";
import FailureClusterPanel from "./FailureClusterPanel";
import { Typography } from "@mui/material";

interface FailureClusterSummaryProps {
  publicId: string;
}

const pluralize = (count: number, singular: string, plural: string) =>
  `${count} ${count === 1 ? singular : plural}`;

const FailureClusterSummary = ({ publicId }: FailureClusterSummaryProps) => {
  const [failureClusters, setFailureClusters] =
    React.useState<FailureClusters>(null);

  React.useEffect(() => {
    fetchFailureClusters(publicId)
      .then((response) => setFailureClusters(response.data))
      .catch(() => setFailureClusters(null));
  }, [publicId, setFailureClusters]);

  // Grouping only adds information when at least two failures share a root cause
  const hasSharedRootCause =
    failureClusters &&
    failureClusters.clusters.some((cluster) => cluster.testCaseCount > 1);

  if (!hasSharedRootCause) {
    return null;
  }

  const { clusters, totalFailedTestCount } = failureClusters;

  const headline =
    clusters.length === 1
      ? `${pluralize(totalFailedTestCount, "test", "tests")} failed from 1 root cause: ${clusters[0].title}`
      : `${pluralize(totalFailedTestCount, "test", "tests")} failed from ${clusters.length} root causes`;

  return (
    <div className={classes.section} data-testid="failure-cluster-summary">
      <Typography
        variant="subtitle1"
        className={classes.headline}
        data-testid="failure-cluster-headline"
      >
        {headline}
      </Typography>
      {clusters.map((cluster) => (
        <FailureClusterPanel
          cluster={cluster}
          publicId={publicId}
          key={cluster.key}
        />
      ))}
    </div>
  );
};

export default FailureClusterSummary;
