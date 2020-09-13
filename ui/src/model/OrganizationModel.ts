import { Coverage } from "./TestRunModel";

interface RepositoryCoverage {
  publicId: string;
  repoName: string;
  projectName?: string;
  coverage?: Coverage;
}

interface OrganizationCoverage {
  repositories: RepositoryCoverage[];
}

export { OrganizationCoverage, RepositoryCoverage };
