import * as React from "react";
import { fetchRepositoryCoverageBadge } from "../service/RepositoryService";
import { CopyToClipboard } from "react-copy-to-clipboard";
import {
  repositoryLinkUrlAPI,
  repositoryLinkUrlUI,
} from "../Repository/RepositoryLink";
import { makeStyles } from "@material-ui/styles";
import FileCopyOutlinedIcon from "@material-ui/icons/FileCopyOutlined";
import { Chip, Fade, Link, Tooltip } from "@material-ui/core";
import { useLocation } from "@reach/router";

interface RepositoryCoverageBadgeProps {
  repoName: string;
  projectName?: string;
}

const useStyles = makeStyles(() => ({
  badgeLink: {
    display: "inline-block",
    marginLeft: "8px",
    cursor: "pointer",
  },
  copied: {
    marginLeft: "5px",
    verticalAlign: "top",
  },
}));

const RepositoryCoverageBadge = ({
  repoName,
  projectName,
}: RepositoryCoverageBadgeProps) => {
  const classes = useStyles({});

  const [badgeSvg, setBadgeSvg] = React.useState<string>(null);
  const [showCopied, setShowCopied] = React.useState<boolean>(false);

  const location = useLocation();

  React.useEffect(() => {
    fetchRepositoryCoverageBadge(repoName, projectName)
      .then((response) => {
        setBadgeSvg(response.data);
      })
      .catch((_) => {});
  }, [setBadgeSvg]);

  const badgeContents = { __html: badgeSvg };

  const coveragePageLink =
    location.origin + repositoryLinkUrlUI(repoName, projectName, "/coverage");
  const badgeSvgUrl =
    location.origin +
    repositoryLinkUrlAPI(repoName, projectName, "/badge/coverage");
  const badgeMarkdown = `[![Code coverage percentage](${badgeSvgUrl})](${coveragePageLink})`;

  const onCopy = () => {
    setShowCopied(true);

    setTimeout(() => setShowCopied(false), 3000);
  };

  if (badgeSvg) {
    return (
      <div>
        <span
          data-testid="coverage-badge-contents"
          dangerouslySetInnerHTML={badgeContents}
        />
        <span className={classes.badgeLink}>
          <Tooltip title="Copy coverage badge readme code to clipboard">
            <CopyToClipboard onCopy={onCopy} text={badgeMarkdown}>
              <Link
                data-testid="coverage-badge-copy-link"
                data-badge={badgeMarkdown}
              >
                <FileCopyOutlinedIcon />
              </Link>
            </CopyToClipboard>
          </Tooltip>
          <span>
            <Fade in={showCopied}>
              <Chip
                label="Copied to clipboard"
                variant="outlined"
                size="small"
                className={classes.copied}
                data-testid="coverage-badge-copied"
              />
            </Fade>
          </span>
        </span>
      </div>
    );
  } else {
    return null;
  }
};

export default RepositoryCoverageBadge;
