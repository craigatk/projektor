import * as React from "react";
import { CoverageStat } from "../model/TestRunModel";
import { Typography } from "@material-ui/core";
import styled from '@emotion/styled'
import CoveragePercentage from "./CoveragePercentage";
import CleanLink from "../Link/CleanLink";
import CoverageGraphImpl from "./CoverageGraphImpl";

interface CoverageGraphProps {
  coverageStat: CoverageStat;
  type: string;
  height: number;
  inline: boolean;
  coveredPercentageLink?: string;
  previousTestRunId?: string;
  testIdPrefix?: string;
}

interface CoverageGraphStyleProps {
  inline: boolean;
  children: React.ReactElement[];
}

const CoverageGraphWrapper = styled.div<CoverageGraphStyleProps>`
  margin-right: ${({ inline }) => (inline ? "10px" : "50px")};
  display: inline-block;
`;

const CoverageGraphLabel = styled(Typography)`
  margin-bottom: 10px;
  display: inline-block;
`;

const CoverageGraph = ({
  coverageStat,
  type,
  height,
  inline,
  coveredPercentageLink,
  previousTestRunId,
  testIdPrefix,
}: CoverageGraphProps) => {
  if (coverageStat.total > 0) {
    return (
      <CoverageGraphWrapper inline={inline}>
        {!inline && (
          <CoverageGraphLabel
            data-testid={`coverage-graph-title-${type.toLowerCase()}`}
          >
            {type}{" "}
            <CoveragePercentage
              coverageStat={coverageStat}
              previousTestRunId={previousTestRunId}
            />
          </CoverageGraphLabel>
        )}
        {coveredPercentageLink && (
          <CleanLink
            to={coveredPercentageLink}
            data-testid={`${testIdPrefix}-covered-percentage-link`}
          >
            <CoverageGraphImpl
              coverageStat={coverageStat}
              height={height}
              inline={inline}
              previousTestRunId={previousTestRunId}
              testIdPrefix={testIdPrefix}
            />
          </CleanLink>
        )}
        {!coveredPercentageLink && (
          <CoverageGraphImpl
            coverageStat={coverageStat}
            height={height}
            inline={inline}
            previousTestRunId={previousTestRunId}
            testIdPrefix={testIdPrefix}
          />
        )}
      </CoverageGraphWrapper>
    );
  } else {
    return null;
  }
};

export default CoverageGraph;
