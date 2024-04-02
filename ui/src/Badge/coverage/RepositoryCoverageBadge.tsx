import * as React from "react";
import { fetchRepositoryCoverageBadge } from "../../service/RepositoryService";
import CoverageBadge from "./CoverageBadge";

interface RepositoryCoverageBadgeProps {
  repoName: string;
  projectName?: string;
}

const RepositoryCoverageBadge = ({
  repoName,
  projectName,
}: RepositoryCoverageBadgeProps) => {
  const [badgeSvg, setBadgeSvg] = React.useState<string>(null);

  React.useEffect(() => {
    fetchRepositoryCoverageBadge(repoName, projectName)
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

export default RepositoryCoverageBadge;
