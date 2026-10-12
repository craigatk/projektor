import "@testing-library/jest-dom";
import React from "react";
import MockAdapter from "axios-mock-adapter";
import { fireEvent, render, waitFor } from "@testing-library/react";
import { axiosInstanceWithoutCache } from "../../service/AxiosService";
import SlackChannelListItem from "../SlackChannelListItem";

vi.mock("../../service/EnvService", () => ({
  baseUrl: (): string => "http://localhost:8080/",
}));

describe("SlackChannelListItem", () => {
  let mockAxios;

  const repoName = "my-org/my-repo";
  const repoUrl = `http://localhost:8080/repo/${repoName}/notification/slack`;
  const projectUrl = `http://localhost:8080/repo/${repoName}/project/ui/notification/slack`;

  const mockSlackEnabled = (enabled: boolean) =>
    mockAxios
      .onGet("http://localhost:8080/config")
      .reply(200, { slack: { enabled } });

  beforeEach(() => {
    // @ts-ignore
    mockAxios = new MockAdapter(axiosInstanceWithoutCache);
  });

  afterEach(() => {
    mockAxios.restore();
  });

  it("should not render when Slack is not enabled", async () => {
    mockSlackEnabled(false);

    const { queryByTestId } = render(
      <SlackChannelListItem repoName={repoName} />,
    );

    await waitFor(() => expect(mockAxios.history.get.length).toBe(1));

    expect(queryByTestId("dashboard-summary-slack-channel")).toBeNull();
  });

  it("should not render when server config does not include Slack", async () => {
    mockAxios.onGet("http://localhost:8080/config").reply(200, {});

    const { queryByTestId } = render(
      <SlackChannelListItem repoName={repoName} />,
    );

    await waitFor(() => expect(mockAxios.history.get.length).toBe(1));

    expect(queryByTestId("dashboard-summary-slack-channel")).toBeNull();
  });

  it("should show existing channel", async () => {
    mockSlackEnabled(true);
    mockAxios.onGet(repoUrl).reply(200, { channel_name: "build-alerts" });

    const { findByTestId, getByTestId } = render(
      <SlackChannelListItem repoName={repoName} />,
    );

    expect(await findByTestId("slack-channel-name")).toHaveTextContent(
      "#build-alerts",
    );
    expect(getByTestId("slack-channel-edit")).toHaveTextContent("Edit");
  });

  it("should set channel when none is mapped", async () => {
    mockSlackEnabled(true);
    mockAxios.onGet(repoUrl).reply(204);
    mockAxios.onPut(repoUrl).reply(200, { channel_name: "build-alerts" });

    const { findByTestId, getByTestId, queryByTestId } = render(
      <SlackChannelListItem repoName={repoName} />,
    );

    const editButton = await findByTestId("slack-channel-edit");
    expect(editButton).toHaveTextContent("Set channel");
    expect(queryByTestId("slack-channel-name")).toBeNull();

    fireEvent.click(editButton);

    expect(getByTestId("slack-channel-save")).toBeDisabled();
    expect(queryByTestId("slack-channel-remove")).toBeNull();

    fireEvent.change(getByTestId("slack-channel-input"), {
      target: { value: "#build-alerts" },
    });
    fireEvent.click(getByTestId("slack-channel-save"));

    expect(await findByTestId("slack-channel-name")).toHaveTextContent(
      "#build-alerts",
    );

    expect(mockAxios.history.put.length).toBe(1);
    expect(JSON.parse(mockAxios.history.put[0].data)).toEqual({
      channel_name: "#build-alerts",
    });
  });

  it("should use project URL when project name is set", async () => {
    mockSlackEnabled(true);
    mockAxios.onGet(projectUrl).reply(200, { channel_name: "ui-alerts" });
    mockAxios.onPut(projectUrl).reply(200, { channel_name: "ui-builds" });

    const { findByTestId, getByTestId } = render(
      <SlackChannelListItem repoName={repoName} projectName="ui" />,
    );

    expect(await findByTestId("slack-channel-name")).toHaveTextContent(
      "#ui-alerts",
    );

    fireEvent.click(getByTestId("slack-channel-edit"));

    expect(getByTestId("slack-channel-dialog-description")).toHaveTextContent(
      "my-org/my-repo project ui",
    );
    expect(getByTestId("slack-channel-input")).toHaveValue("ui-alerts");

    fireEvent.change(getByTestId("slack-channel-input"), {
      target: { value: "ui-builds" },
    });
    fireEvent.click(getByTestId("slack-channel-save"));

    await waitFor(() =>
      expect(getByTestId("slack-channel-name")).toHaveTextContent("#ui-builds"),
    );
  });

  it("should show error from server when saving channel fails", async () => {
    mockSlackEnabled(true);
    mockAxios.onGet(repoUrl).reply(204);
    mockAxios.onPut(repoUrl).reply(400, {
      error_message: "Unable to post to Slack channel nope: not_in_channel",
    });

    const { findByTestId, getByTestId, queryByTestId } = render(
      <SlackChannelListItem repoName={repoName} />,
    );

    fireEvent.click(await findByTestId("slack-channel-edit"));
    fireEvent.change(getByTestId("slack-channel-input"), {
      target: { value: "nope" },
    });
    fireEvent.click(getByTestId("slack-channel-save"));

    expect(await findByTestId("slack-channel-error")).toHaveTextContent(
      "not_in_channel",
    );
    expect(queryByTestId("slack-channel-name")).toBeNull();
  });

  it("should remove channel", async () => {
    mockSlackEnabled(true);
    mockAxios.onGet(repoUrl).reply(200, { channel_name: "build-alerts" });
    mockAxios.onDelete(repoUrl).reply(200);

    const { findByTestId, getByTestId, queryByTestId } = render(
      <SlackChannelListItem repoName={repoName} />,
    );

    await findByTestId("slack-channel-name");

    fireEvent.click(getByTestId("slack-channel-edit"));
    fireEvent.click(getByTestId("slack-channel-remove"));

    await waitFor(() => expect(queryByTestId("slack-channel-name")).toBeNull());

    expect(mockAxios.history.delete.length).toBe(1);
    expect(getByTestId("slack-channel-edit")).toHaveTextContent("Set channel");
  });
});
