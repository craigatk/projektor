const repositoryLinkUrl = (
  prefix: string,
  repoName: string,
  projectName?: string,
  uri?: string
): string => {
  const projectPart = projectName ? `/project/${projectName}` : "";

  let uriPart = "";

  if (uri) {
    if (uri.startsWith("/")) {
      uriPart = uri;
    } else {
      uriPart = `/${uri}`;
    }
  }

  const fullPrefix = prefix.startsWith("/") ? prefix : `/${prefix}`;

  return `${fullPrefix}/${repoName}${projectPart}${uriPart}`;
};

const repositoryLinkUrlUI = (
  repoName: string,
  projectName?: string,
  uri?: string
): string => repositoryLinkUrl("repository", repoName, projectName, uri);

const repositoryLinkUrlAPI = (
  repoName: string,
  projectName?: string,
  uri?: string
): string => repositoryLinkUrl("repo", repoName, projectName, uri);

export { repositoryLinkUrlUI, repositoryLinkUrlAPI };
