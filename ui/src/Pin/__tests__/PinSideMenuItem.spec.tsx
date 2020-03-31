import "@testing-library/jest-dom/extend-expect";
import React from "react";
import MockAdapter from "axios-mock-adapter";
import { render, wait } from "@testing-library/react";
import { axiosInstanceWithoutCache } from "../../service/AxiosService";
import PinSideMenuItem from "../PinSideMenuItem";
import {
  ServerCleanupConfig,
  ServerConfig,
} from "../../model/ServerConfigModel";
import { TestRunSystemAttributes } from "../../model/TestRunModel";
import { act } from "react-dom/test-utils";

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

    mockAxios
      .onGet(`http://localhost:8080/run/${publicId}/config`)
      .reply(200, serverConfig);

    const attributes = {
      pinned: false,
    } as TestRunSystemAttributes;

    mockAxios
      .onGet(`http://localhost:8080/run/${publicId}/attributes`)
      .reply(200, attributes);

    const { getByTestId } = render(<PinSideMenuItem publicId={publicId} />);

    await wait(() => getByTestId("nav-link-pin"));

    mockAxios
      .onPost(`http://localhost:8080/run/${publicId}/attributes/pin`)
      .reply(200);

    act(() => getByTestId("nav-link-pin").click());

    await wait(() => getByTestId("nav-link-unpin"));
  });

  it("when cleanup enabled and pinned should toggle to unpinned", async () => {
    const cleanupConfig = {
      enabled: true,
    } as ServerCleanupConfig;

    const serverConfig = {
      cleanup: cleanupConfig,
    } as ServerConfig;

    mockAxios
      .onGet(`http://localhost:8080/run/${publicId}/config`)
      .reply(200, serverConfig);

    const attributes = {
      pinned: true,
    } as TestRunSystemAttributes;

    mockAxios
      .onGet(`http://localhost:8080/run/${publicId}/attributes`)
      .reply(200, attributes);

    const { getByTestId } = render(<PinSideMenuItem publicId={publicId} />);

    await wait(() => getByTestId("nav-link-unpin"));

    mockAxios
      .onPost(`http://localhost:8080/run/${publicId}/attributes/unpin`)
      .reply(200);

    act(() => getByTestId("nav-link-unpin").click());

    await wait(() => getByTestId("nav-link-pin"));
  });

  it("when cleanup not enabled should not display anything", () => {
    const cleanupConfig = {
      enabled: false,
    } as ServerCleanupConfig;

    const serverConfig = {
      cleanup: cleanupConfig,
    } as ServerConfig;

    mockAxios
      .onGet(`http://localhost:8080/run/${publicId}/config`)
      .reply(200, serverConfig);

    const { queryByTestId } = render(<PinSideMenuItem publicId={publicId} />);

    expect(queryByTestId("nav-link-unpin")).toBeNull();
    expect(queryByTestId("nav-link-pin")).toBeNull();
  });
});
