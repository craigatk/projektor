import * as React from "react";
import classes from "./BreadcrumbPageHeader.module.css";
import Breadcrumbs from "@material-ui/core/Breadcrumbs";
import Typography from "@material-ui/core/Typography";
import NavigateNextIcon from "@mui/icons-material/NavigateNext";

interface BreadcrumbPageHeaderProps {
  intermediateLinks?: React.ReactNode[];
  endingText: string;
}

const BreadcrumbPageHeader = ({
  intermediateLinks,
  endingText,
}: BreadcrumbPageHeaderProps) => {
  return (
    <div className={classes.wrapper}>
      <Breadcrumbs
        separator={<NavigateNextIcon fontSize="small" />}
        aria-label="breadcrumb"
      >
        {intermediateLinks != null ? intermediateLinks : null}
        <Typography color="textPrimary" data-testid="breadcrumb-ending-text">
          {endingText}
        </Typography>
      </Breadcrumbs>
    </div>
  );
};

export default BreadcrumbPageHeader;
