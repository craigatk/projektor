import * as React from "react";
import {Alert} from "@material-ui/lab";

interface TestRunMessagesProps {
    publicId: string;
}

const TestRunMessages = ({publicId}: TestRunMessagesProps) => {
    const [messages, setMessages] = React.useState([])

    React.useEffect(() => {
        setMessages(["Starting on 6/14 reports older than 60 days will be cleaned up to save space. You can also 'pin' an important test report to keep it forever."])
    }, [setMessages])

    return (
        <div>{messages.map(message => <Alert severity="info">{message}</Alert>)}</div>
    )
}

export default TestRunMessages;