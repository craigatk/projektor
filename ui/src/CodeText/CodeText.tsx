import * as React from "react";
import { useQueryParam, NumberParam } from "use-query-params";
import { scroller, Element } from "react-scroll";
import CodeTextProgressiveRender from "./CodeTextProgressiveRender";
import CodeTextLine from "./CodeTextLine";
import { makeStyles } from "@material-ui/core";
import LinearProgress from "@material-ui/core/LinearProgress";

interface CodeTextProps {
  text: string;
}

const useStyles = makeStyles((theme) => ({
  renderIndicator: {
    marginLeft: "40px",
    marginRight: "40px",
    marginBottom: "20px",
  },
}));

const CodeText = ({ text }: CodeTextProps) => {
  if (text == null) {
    return null;
  }

  const classes = useStyles({});

  const [highlightedLine, setHighlightedLine] = useQueryParam("l", NumberParam);
  const [rendered, setRendered] = React.useState(false);
  const [renderProgress, setRenderProgress] = React.useState(0);

  const textLines = text.split(/\r?\n/g);
  const lastLine = textLines.pop();
  if (lastLine !== "") {
    textLines.push(lastLine);
  }

  function handleLineClick(event: React.MouseEvent, clickedIdx: number) {
    if (highlightedLine == null) {
      setHighlightedLine(clickedIdx);
    } else if (clickedIdx !== highlightedLine) {
      setHighlightedLine(clickedIdx);
    } else {
      setHighlightedLine(null);
    }
  }

  React.useEffect(() => {
    return () => {
      setHighlightedLine(null);
    };
  }, [setHighlightedLine]);

  const renderComplete = () => {
    if (highlightedLine != null && !rendered) {
      scroller.scrollTo(`line-${highlightedLine}-true`, {
        duration: 0,
        delay: 0,
        offset: -45,
        smooth: "easeInOutQuart",
      });
    }

    setRendered(true);
  };

  const renderProgressUpdate = (progress: number) =>
    setRenderProgress(progress);

  const listElements = textLines.map((line, idx) => {
    const lineIdx = idx + 1;
    const highlighted = lineIdx === highlightedLine;

    return React.useMemo(
      () => (
        <Element
          name={`line-${lineIdx}-${highlighted}`}
          key={`line-element-${lineIdx}-${highlighted}`}
        >
          <CodeTextLine
            key={`code-line-${lineIdx}-${highlighted}`}
            line={line}
            idx={lineIdx}
            highlighted={highlighted}
            handleLineClick={handleLineClick}
          />
        </Element>
      ),
      [lineIdx, highlighted]
    );
  });

  return (
    <pre data-testid="code-text">
      {!rendered && listElements.length > 1000 ? (
        <LinearProgress
          variant="determinate"
          value={renderProgress}
          className={classes.renderIndicator}
        />
      ) : null}
      <CodeTextProgressiveRender
        listElements={listElements}
        renderComplete={renderComplete}
        renderProgress={renderProgressUpdate}
        pageSize={500}
        lineHeight={13.333}
      />
    </pre>
  );
};

export default CodeText;
