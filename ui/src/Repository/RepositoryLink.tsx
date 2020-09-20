const repositoryLinkUrlUI = (
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

  return `/repository/${repoName}${projectPart}${uriPart}`;
};

export { repositoryLinkUrlUI };
