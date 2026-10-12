import * as React from "react";
import {
  Button,
  Dialog,
  DialogActions,
  DialogContent,
  DialogContentText,
  DialogTitle,
  TextField,
} from "@mui/material";
import DashboardSummaryItem from "../Dashboard/DashboardSummaryItem";
import { fetchServerConfig } from "../service/ServerConfigService";
import {
  deleteRepositorySlackChannel,
  fetchRepositorySlackChannel,
  saveRepositorySlackChannel,
} from "../service/RepositorySlackService";
import classes from "./SlackChannelListItem.module.css";

interface SlackChannelListItemProps {
  repoName: string;
  projectName?: string;
}

const SlackChannelListItem = ({
  repoName,
  projectName,
}: SlackChannelListItemProps) => {
  const [slackEnabled, setSlackEnabled] = React.useState<boolean>(false);
  const [channelName, setChannelName] = React.useState<string>(null);
  const [dialogOpen, setDialogOpen] = React.useState<boolean>(false);
  const [channelInput, setChannelInput] = React.useState<string>("");
  const [errorMessage, setErrorMessage] = React.useState<string>(null);
  const [saving, setSaving] = React.useState<boolean>(false);

  React.useEffect(() => {
    fetchServerConfig()
      .then((response) => {
        const enabled = !!response.data.slack && response.data.slack.enabled;
        setSlackEnabled(enabled);

        if (enabled) {
          fetchRepositorySlackChannel(repoName, projectName)
            .then((channelResponse) => {
              setChannelName(
                channelResponse.data ? channelResponse.data.channelName : null,
              );
            })
            .catch(() => {});
        }
      })
      .catch(() => {});
  }, [repoName, projectName]);

  if (!slackEnabled) {
    return null;
  }

  const openDialog = () => {
    setChannelInput(channelName || "");
    setErrorMessage(null);
    setDialogOpen(true);
  };

  const closeDialog = () => setDialogOpen(false);

  const showError = (error, defaultMessage: string) => {
    const responseData = error && error.response && error.response.data;

    setErrorMessage(
      responseData && responseData.errorMessage
        ? responseData.errorMessage
        : defaultMessage,
    );
  };

  const saveChannel = () => {
    setSaving(true);
    setErrorMessage(null);

    saveRepositorySlackChannel(repoName, projectName, channelInput)
      .then((response) => {
        setChannelName(response.data.channelName);
        setDialogOpen(false);
      })
      .catch((error) => showError(error, "Unable to save Slack channel"))
      .finally(() => setSaving(false));
  };

  const removeChannel = () => {
    setSaving(true);
    setErrorMessage(null);

    deleteRepositorySlackChannel(repoName, projectName)
      .then(() => {
        setChannelName(null);
        setDialogOpen(false);
      })
      .catch((error) => showError(error, "Unable to remove Slack channel"))
      .finally(() => setSaving(false));
  };

  return (
    <React.Fragment>
      <DashboardSummaryItem
        label="Slack channel"
        testId="dashboard-summary-slack-channel"
        value={
          <span>
            {channelName && (
              <span data-testid="slack-channel-name">#{channelName}</span>
            )}
            <Button
              size="small"
              onClick={openDialog}
              className={channelName ? classes.editButton : null}
              data-testid="slack-channel-edit"
            >
              {channelName ? "Edit" : "Set channel"}
            </Button>
          </span>
        }
      />
      <Dialog
        open={dialogOpen}
        onClose={closeDialog}
        fullWidth
        maxWidth="xs"
        data-testid="slack-channel-dialog"
      >
        <DialogTitle>Slack notifications</DialogTitle>
        <DialogContent>
          <DialogContentText data-testid="slack-channel-dialog-description">
            Slack channel to notify when mainline goes red, coverage drops, or a
            new flaky test appears in {repoName}
            {projectName ? ` project ${projectName}` : ""}.
          </DialogContentText>
          <TextField
            autoFocus
            fullWidth
            margin="normal"
            label="Channel"
            placeholder="build-alerts"
            value={channelInput}
            onChange={(event) => setChannelInput(event.target.value)}
            error={!!errorMessage}
            helperText={errorMessage}
            slotProps={{
              htmlInput: { "data-testid": "slack-channel-input" },
              formHelperText: { "data-testid": "slack-channel-error" } as any,
            }}
          />
        </DialogContent>
        <DialogActions>
          {channelName && (
            <Button
              color="error"
              onClick={removeChannel}
              disabled={saving}
              data-testid="slack-channel-remove"
            >
              Remove
            </Button>
          )}
          <Button onClick={closeDialog} data-testid="slack-channel-cancel">
            Cancel
          </Button>
          <Button
            variant="contained"
            onClick={saveChannel}
            disabled={saving || channelInput.trim().length === 0}
            data-testid="slack-channel-save"
          >
            Save
          </Button>
        </DialogActions>
      </Dialog>
    </React.Fragment>
  );
};

export default SlackChannelListItem;
