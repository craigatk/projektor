import "@testing-library/jest-dom";
import React from "react";
import MockAdapter from "axios-mock-adapter";
import { render, waitFor } from "@testing-library/react";
import { axiosInstanceWithoutCache } from "../../service/AxiosService";
import PinSideMenuItem from "../PinSideMenuItem";
import {
  ServerCleanupConfig,
  ServerConfig,
} from "../../model/ServerConfigModel";
import { TestRunSystemAttributes } from "../../model/TestRunModel";
import { act } from "react-dom/test-utils";
import { PinState } from "../PinState";

describe("PinSideMenuItem", () => {
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

    const { getByTestId, findByTestId } = render(
      <PinState publicId={publicId}>
        <PinSideMenuItem />
      </PinState>,
    );

    await findByTestId("nav-link-pin");

    mockAxios
      .onPost(`http://localhost:8080/run/${publicId}/attributes/pin`)
      .reply(200);

    act(() => getByTestId("nav-link-pin").click());

    await findByTestId("nav-link-unpin");
  });

  it("when cleanup enabled and pinned should toggle to unpinned", async () => {
    const cleanupConfig = {
      enabled: true,
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

    const { getByTestId, findByTestId } = render(
      <PinState publicId={publicId}>
        <PinSideMenuItem />
      </PinState>,
    );

    await findByTestId("nav-link-unpin");

    mockAxios
      .onPost(`http://localhost:8080/run/${publicId}/attributes/unpin`)
      .reply(200);

    act(() => getByTestId("nav-link-unpin").click());

    await findByTestId("nav-link-pin");
  });

  it("when cleanup not enabled should not display anything", async () => {
    const cleanupConfig = {
      enabled: false,
    } as ServerCleanupConfig;

    const serverConfig = {
      cleanup: cleanupConfig,
    } as ServerConfig;

    mockAxios.onGet(`http://localhost:8080/config`).reply(200, serverConfig);

    const { queryByTestId } = render(
      <PinState publicId={publicId}>
        <PinSideMenuItem />
      </PinState>,
    );

    await waitFor(() => expect(queryByTestId("nav-link-unpin")).toBeNull());
    await waitFor(() => expect(queryByTestId("nav-link-pin")).toBeNull());
  });
});
