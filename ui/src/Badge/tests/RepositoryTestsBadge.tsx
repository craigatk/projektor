import * as React from "react";
import { fetchRepositoryTestsBadge } from "../../service/RepositoryService";
import TestsBadge from "./TestsBadge";

interface RepositoryTestsBadgeProps {
  repoName: string;
  projectName?: string;
}

const RepositoryTestsBadge = ({
  repoName,
  projectName,
}: RepositoryTestsBadgeProps) => {
  const [badgeSvg, setBadgeSvg] = React.useState<string>(null);

  React.useEffect(() => {
    fetchRepositoryTestsBadge(repoName, projectName)
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

export default RepositoryTestsBadge;
