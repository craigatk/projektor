import * as React from "react";
import TestsBadge from "./TestsBadge";
import { fetchTestsBadge } from "../../service/TestRunService";

interface TestRunTestsBadgeProps {
  publicId: string;
  repoName: string;
  projectName?: string;
}

const TestRunTestsBadge = ({
  publicId,
  repoName,
  projectName,
}: TestRunTestsBadgeProps) => {
  const [badgeSvg, setBadgeSvg] = React.useState<string>(null);

  React.useEffect(() => {
    fetchTestsBadge(publicId)
      .then((response) => {
        setBadgeSvg(response.data);
      })
      .catch((_) => {});
  }, [setBadgeSvg]);

  if (badgeSvg) {
    return (
      <TestsBadge
        badgeSvg={badgeSvg}
        repoName={repoName}
        projectName={projectName}
      />
    );
  } else {
    return null;
  }
};

export default TestRunTestsBadge;
