import * as React from "react";
import { useLocation } from "@reach/router";
import { CopyToClipboard } from "react-copy-to-clipboard";
import { repositoryLinkUrlAPI } from "../../Repository/RepositoryLink";
import { Chip, Fade, Link, Tooltip } from "@material-ui/core";
import FileCopyOutlinedIcon from "@mui/icons-material/FileCopyOutlined";
import classes from "./TestsBadge.module.css";

interface TestsBadgeProps {
  badgeSvg: string;
  repoName: string;
  projectName?: string;
}

const TestsBadge = ({ badgeSvg, repoName, projectName }: TestsBadgeProps) => {
  const [showCopied, setShowCopied] = React.useState<boolean>(false);

  const location = useLocation();

  const badgeContents = { __html: badgeSvg };

  const latestTestRunPageLink =
    location.origin +
    repositoryLinkUrlAPI(repoName, projectName, "/run/latest");
  const badgeSvgUrl =
    location.origin +
    repositoryLinkUrlAPI(repoName, projectName, "/badge/tests");
  const badgeMarkdown = `[![Test results](${badgeSvgUrl})](${latestTestRunPageLink})`;

  const onCopy = () => {
    setShowCopied(true);

    setTimeout(() => setShowCopied(false), 3000);
  };

  if (badgeSvg) {
    return (
      <div>
        <span
          data-testid="tests-badge-contents"
          dangerouslySetInnerHTML={badgeContents}
        />
        <span className={classes.badgeLink}>
          <Tooltip title="Copy tests badge Markdown readme code to clipboard">
            <CopyToClipboard onCopy={onCopy} text={badgeMarkdown}>
              <Link
                data-testid="tests-badge-copy-link"
                data-badge={badgeMarkdown}
              >
                <FileCopyOutlinedIcon fontSize="small" />
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
                data-testid="tests-badge-copied"
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

export default TestsBadge;
