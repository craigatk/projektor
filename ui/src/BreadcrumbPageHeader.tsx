import * as React from "react";
import { makeStyles } from "@mui/material/styles";
import Breadcrumbs from "@mui/material/Breadcrumbs";
import Typography from "@mui/material/Typography";
import NavigateNextIcon from "@mui/icons-material/NavigateNext";

const useStyles = makeStyles((theme) => ({
  wrapper: {
    padding: theme.spacing(1, 2),
    marginBottom: "10px",
  },
}));

interface BreadcrumbPageHeaderProps {
  intermediateLinks?: React.ReactNode[];
  endingText: string;
}

const BreadcrumbPageHeader = ({
  intermediateLinks,
  endingText,
}: BreadcrumbPageHeaderProps) => {
  const classes = useStyles({});

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
