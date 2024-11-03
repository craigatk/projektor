import { Link } from "@reach/router";
import styled from '@emotion/styled'

interface CleanLinkProps {
  color?: string;
}

const CleanLink = styled(Link)<CleanLinkProps>`
  text-decoration: none;
  color: ${(props) => (props.color ? props.color : "blue")};
  &:hover {
    text-decoration: underline;
  }
  &:visited {
    color: ${(props) => (props.color ? props.color : "blue")};
  }
`;

export default CleanLink;
