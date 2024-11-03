import { Link } from "@material-ui/core";
import styled from '@emotion/styled'

interface CleanLinkProps {
  color?: string;
}

const CleanLinkText = styled(Link)<CleanLinkProps>`
  text-decoration: none;
  color: ${(props) => (props.color ? props.color : "blue")};
  &:hover {
    text-decoration: underline;
    cursor: pointer;
  }
  &:visited {
    color: ${(props) => (props.color ? props.color : "blue")};
  }
`;

export default CleanLinkText;
