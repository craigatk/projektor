import * as React from "react";
import type { Meta, StoryObj } from "@storybook/react-vite";
import MockAdapter from "axios-mock-adapter";
import {
  createHistory,
  createMemorySource,
  LocationProvider,
  RouteComponentProps,
  Router,
} from "@reach/router";
import TestRunDataWrapper from "./TestRunDataWrapper";
import {
  axiosInstance,
  axiosInstanceWithoutCache,
} from "../service/AxiosService";
import testRunSummary from "../../cypress/fixtures/test_run_summary.json";
import failedTestCases from "../../cypress/fixtures/failed_test_cases.json";
import gitMetadata from "../../cypress/fixtures/metadata/git-metadata-with-pr-and-commit.json";
import slackEnabledConfig from "../../cypress/fixtures/config/slack_config_enabled.json";

interface TestRunSummaryPageProps {
  publicId: string;
  projectName?: string;
  slackEnabled: boolean;
  slackChannelName?: string;
}

const TestRunRoute = ({
  publicId,
}: RouteComponentProps<{ publicId: string }>) => (
  <TestRunDataWrapper key={publicId} publicId={publicId} />
);

// The app's routes are relative to /tests/:publicId so render the page at that location
const TestRunSummaryPage = ({ publicId }: TestRunSummaryPageProps) => (
  <LocationProvider
    history={createHistory(createMemorySource(`/tests/${publicId}/`))}
  >
    <Router>
      <TestRunRoute path="/tests/:publicId/*" />
    </Router>
  </LocationProvider>
);

// Responses are in the same snake case format the server sends
const mockServer = ({
  publicId,
  projectName,
  slackEnabled,
  slackChannelName,
}: TestRunSummaryPageProps) => {
  const endsWith = (uri: string) => new RegExp(`${uri}$`);
  const runUrl = (uri: string) => endsWith(`run/${publicId}/${uri}`);

  let savedSlackChannelName = slackChannelName;

  const mocks = [axiosInstance, axiosInstanceWithoutCache].map((instance) => {
    // @ts-ignore
    const mock = new MockAdapter(instance);

    mock.onGet(runUrl("summary")).reply(200, testRunSummary);
    mock
      .onGet(runUrl("metadata/git"))
      .reply(200, { ...gitMetadata, project_name: projectName });
    mock.onGet(runUrl("cases/failed")).reply(200, failedTestCases);
    mock.onGet(runUrl("attachments")).reply(200, { attachments: [] });
    mock.onGet(runUrl("coverage/exists")).reply(200, { exists: false });
    mock.onGet(runUrl("attributes")).reply(200, { pinned: false });
    mock.onGet(endsWith("config")).reply(200, {
      ...slackEnabledConfig,
      slack: { enabled: slackEnabled },
    });

    const slackChannelUrl = endsWith(
      projectName
        ? `repo/${gitMetadata.repo_name}/project/${projectName}/notification/slack`
        : `repo/${gitMetadata.repo_name}/notification/slack`,
    );

    mock
      .onGet(slackChannelUrl)
      .reply(() =>
        savedSlackChannelName
          ? [200, { channel_name: savedSlackChannelName }]
          : [204],
      );
    mock.onPut(slackChannelUrl).reply((config) => {
      const channelName = JSON.parse(config.data).channel_name.replace(
        /^#/,
        "",
      );

      if (channelName === "not-invited") {
        return [
          400,
          {
            error_message: `Unable to post to Slack channel ${channelName}: Slack API returned error: not_in_channel`,
          },
        ];
      }

      savedSlackChannelName = channelName;

      return [200, { channel_name: channelName }];
    });
    mock.onDelete(slackChannelUrl).reply(() => {
      savedSlackChannelName = undefined;

      return [200];
    });

    return mock;
  });

  return () => mocks.forEach((mock) => mock.restore());
};

const meta = {
  title: "Test run/Summary page",
  component: TestRunSummaryPage,
  beforeEach: ({ args }) => mockServer(args),
} satisfies Meta<typeof TestRunSummaryPage>;

export default meta;

type Story = StoryObj<typeof meta>;

export const SlackEnabledChannelNotSet: Story = {
  args: {
    publicId: "SLACKNOTSET1",
    slackEnabled: true,
  },
};

export const SlackEnabledChannelSet: Story = {
  args: {
    publicId: "SLACKSET0001",
    slackEnabled: true,
    slackChannelName: "build-alerts",
  },
};

export const SlackEnabledChannelSetForProject: Story = {
  args: {
    publicId: "SLACKPROJECT",
    projectName: "ui",
    slackEnabled: true,
    slackChannelName: "ui-build-alerts",
  },
};

export const SlackDisabled: Story = {
  args: {
    publicId: "SLACKDISABLE",
    slackEnabled: false,
  },
};
