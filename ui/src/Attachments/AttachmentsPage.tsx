import * as React from "react";
import { RouteComponentProps } from "@reach/router";
import LoadingState from "../Loading/LoadingState";
import LoadingSection from "../Loading/LoadingSection";
import { fetchAttachments, fetchTestSuite } from "../service/TestRunService";
import AttachmentsList from "./AttachmentsList";
import { Attachments } from "../model/TestRunModel";
import PageTitle from "../PageTitle";

interface AttachmentsPageProps extends RouteComponentProps {
  publicId: string;
}

const AttachmentsPage = ({ publicId }: AttachmentsPageProps) => {
  const [loadingState, setLoadingState] = React.useState<LoadingState>(
    LoadingState.Loading
  );
  const [attachments, setAttachments] = React.useState<Attachments>(null);

  React.useEffect(() => {
    fetchAttachments(publicId)
      .then((response) => {
        setAttachments(response.data);
        setLoadingState(LoadingState.Success);
      })
      .catch(() => setLoadingState(LoadingState.Error));
  }, [setAttachments, setLoadingState]);

  return (
    <div>
      <PageTitle title="Attachments" testid="attachments-title" />

      <LoadingSection
        loadingState={loadingState}
        successComponent={
          <AttachmentsList publicId={publicId} attachments={attachments} />
        }
      />
    </div>
  );
};

export default AttachmentsPage;
