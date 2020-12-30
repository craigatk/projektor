import * as React from "react";
import CoverageBadge from "./CoverageBadge";
import { fetchCoverageBadge } from "../service/TestRunService";

interface TestRunCoverageBadgeProps {
  publicId: string;
  repoName: string;
  projectName?: string;
}

const TestRunCoverageBadge = ({
  publicId,
  repoName,
  projectName,
}: TestRunCoverageBadgeProps) => {
  const [badgeSvg, setBadgeSvg] = React.useState<string>(null);

  React.useEffect(() => {
    fetchCoverageBadge(publicId)
      .then((response) => {
        setBadgeSvg(response.data);
      })
      .catch((_) => {});
  }, [setBadgeSvg]);

  if (badgeSvg) {
    return (
      <CoverageBadge
        badgeSvg={badgeSvg}
        repoName={repoName}
        projectName={projectName}
      />
    );
  } else {
    return null;
  }
};

export default TestRunCoverageBadge;
