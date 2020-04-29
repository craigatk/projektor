import "@testing-library/jest-dom/extend-expect";
import React from "react";
import MockAdapter from "axios-mock-adapter";
import { render, getNodeText, waitFor } from "@testing-library/react";
import { axiosInstanceWithoutCache } from "../../service/AxiosService";
import TestRunCleanupDate from "../TestRunCleanupDate";
import {
  ServerCleanupConfig,
  ServerConfig,
} from "../../model/ServerConfigModel";
import { TestRunSystemAttributes } from "../../model/TestRunModel";
import { act } from "react-dom/test-utils";
import { PinState } from "../../Pin/PinState";
import moment from "moment";

describe("TestRunCleanupDate", () => {
  let mockAxios;

  const publicId = "12345";

  beforeEach(() => {
    // @ts-ignore
    mockAxios = new MockAdapter(axiosInstanceWithoutCache);
  });

  afterEach(() => {
    mockAxios.restore();
  });

  it("when cleanup enabled and not pinned should toggle to pinned", async () => {
    const cleanupConfig = {
      enabled: true,
      maxReportAgeInDays: 30,
    } as ServerCleanupConfig;

    const serverConfig = {
      cleanup: cleanupConfig,
    } as ServerConfig;

    mockAxios.onGet(`http://localhost:8080/config`).reply(200, serverConfig);

    const attributes = {
      pinned: false,
    } as TestRunSystemAttributes;

    mockAxios
      .onGet(`http://localhost:8080/run/${publicId}/attributes`)
      .reply(200, attributes);

    const createdTimestamp = moment("2020-04-25").toDate();

    const { getByTestId, findByTestId } = render(
      <PinState publicId={publicId}>
        <TestRunCleanupDate createdTimestamp={createdTimestamp} />
      </PinState>
    );

    expect(
      getNodeText(await findByTestId("test-run-report-cleanup-date"))
    ).toContain("May 25th 2020");

    mockAxios
      .onPost(`http://localhost:8080/run/${publicId}/attributes/pin`)
      .reply(200);

    act(() => getByTestId("test-run-header-pin-link").click());

    await findByTestId("test-run-header-unpin-link");
  });

  it("when cleanup enabled and pinned should toggle to unpinned", async () => {
    const cleanupConfig = {
      enabled: true,
      maxReportAgeInDays: 30,
    } as ServerCleanupConfig;

    const serverConfig = {
      cleanup: cleanupConfig,
    } as ServerConfig;

    mockAxios.onGet(`http://localhost:8080/config`).reply(200, serverConfig);

    const attributes = {
      pinned: true,
    } as TestRunSystemAttributes;

    mockAxios
      .onGet(`http://localhost:8080/run/${publicId}/attributes`)
      .reply(200, attributes);

    const createdTimestamp = moment("2020-04-25").toDate();

    const { getByTestId, findByTestId } = render(
      <PinState publicId={publicId}>
        <TestRunCleanupDate createdTimestamp={createdTimestamp} />
      </PinState>
    );

    await findByTestId("test-run-header-unpin-link");

    mockAxios
      .onPost(`http://localhost:8080/run/${publicId}/attributes/unpin`)
      .reply(200);

    act(() => getByTestId("test-run-header-unpin-link").click());

    await findByTestId("test-run-header-pin-link");
  });

  it("when cleanup not enabled should not display anything", async () => {
    const cleanupConfig = {
      enabled: false,
    } as ServerCleanupConfig;

    const serverConfig = {
      cleanup: cleanupConfig,
    } as ServerConfig;

    mockAxios.onGet(`http://localhost:8080/config`).reply(200, serverConfig);

    const createdTimestamp = moment("2020-04-25").toDate();

    const { queryByTestId } = render(
      <PinState publicId={publicId}>
        <TestRunCleanupDate createdTimestamp={createdTimestamp} />
      </PinState>
    );

    await waitFor(() =>
      expect(queryByTestId("test-run-header-unpin-link")).toBeNull()
    );
    await waitFor(() =>
      expect(queryByTestId("test-run-header-pin-link")).toBeNull()
    );
  });
});
