import * as React from "react";
import { Alert } from "@material-ui/lab";
import { fetchMessages } from "../service/TestRunService";

interface TestRunMessagesProps {
  publicId: string;
}

const TestRunMessages = ({ publicId }: TestRunMessagesProps) => {
  const [messages, setMessages] = React.useState([]);

  React.useEffect(() => {
    fetchMessages(publicId)
      .then((response) => {
        if (response.data) {
          setMessages(response.data.messages);
        }
      })
      .catch(() => {});
  }, [setMessages]);

  return (
    <div data-testid="test-run-messages">
      {messages &&
        messages.map((message, idx) => (
          <Alert
            severity="info"
            data-testid={`test-run-message-${idx + 1}`}
            key={`test-run0message-${idx + 1}`}
          >
            {message}
          </Alert>
        ))}
    </div>
  );
};

export default TestRunMessages;
