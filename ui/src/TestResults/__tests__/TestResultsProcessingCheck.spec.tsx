import "@testing-library/jest-dom/extend-expect";
import React from "react";
import { render, waitFor } from "@testing-library/react";
import { axiosInstanceWithoutCache } from "../../service/AxiosService";
import MockAdapter from "axios-mock-adapter";
import {
  TestResultsProcessing,
  TestResultsProcessingStatus,
} from "../../model/TestRunModel";
import TestResultsProcessingCheck from "../TestResultsProcessingCheck";

describe("TestResultsProcessingCheck", () => {
  let mockAxios;

  const publicId = "12345";

  beforeEach(() => {
    // @ts-ignore
    mockAxios = new MockAdapter(axiosInstanceWithoutCache);
  });

  afterEach(() => {
    mockAxios.restore();
  });

  const mockProcessingStatus = (status: TestResultsProcessingStatus) => {
    const processingStatus = {
      id: publicId,
      status,
    } as TestResultsProcessing;

    mockAxios
      .onGet(`http://localhost:8080/results/${publicId}/status`)
      .reply(200, processingStatus);
  };

  it("should display message when results still processing", async () => {
    const succeededFunc = jest.fn();

    mockProcessingStatus(TestResultsProcessingStatus.PROCESSING);

    const { findByTestId } = render(
      <TestResultsProcessingCheck
        publicId={publicId}
        processingSucceeded={succeededFunc}
        refreshInterval={5000}
        autoRefreshTimeout={60000}
      />
    );

    await findByTestId("results-still-processing");

    expect(succeededFunc).not.toHaveBeenCalled();
  });

  it("should refresh status when still processing up to max timeout", async () => {
    const succeededFunc = jest.fn();

    mockProcessingStatus(TestResultsProcessingStatus.PROCESSING);

    const { findByTestId } = render(
      <TestResultsProcessingCheck
        publicId={publicId}
        processingSucceeded={succeededFunc}
        refreshInterval={100}
        autoRefreshTimeout={400}
      />
    );

    await findByTestId("results-still-processing");

    await waitFor(() =>
      expect(mockAxios.history.get.length).toBeGreaterThan(1)
    );

    return setTimeout(() => {
      expect(mockAxios.history.get.length).toBeLessThanOrEqual(5);
    }, 1000);
  });

  it("should display failure message when results processing failed", async () => {
    const succeededFunc = jest.fn();

    mockProcessingStatus(TestResultsProcessingStatus.ERROR);

    const { findByTestId } = render(
      <TestResultsProcessingCheck
        publicId={publicId}
        processingSucceeded={succeededFunc}
        refreshInterval={5000}
        autoRefreshTimeout={60000}
      />
    );

    await findByTestId("results-processing-failed");

    expect(succeededFunc).not.toHaveBeenCalled();
  });

  it("should display message when results were deleted", async () => {
    const succeededFunc = jest.fn();

    mockProcessingStatus(TestResultsProcessingStatus.DELETED);

    const { findByTestId } = render(
      <TestResultsProcessingCheck
        publicId={publicId}
        processingSucceeded={succeededFunc}
        refreshInterval={5000}
        autoRefreshTimeout={60000}
      />
    );

    await findByTestId("results-deleted");

    expect(succeededFunc).not.toHaveBeenCalled();
  });

  it("should call success function when results successfully processed", async () => {
    const succeededFunc = jest.fn();

    mockProcessingStatus(TestResultsProcessingStatus.SUCCESS);

    render(
      <TestResultsProcessingCheck
        publicId={publicId}
        processingSucceeded={succeededFunc}
        refreshInterval={5000}
        autoRefreshTimeout={60000}
      />
    );

    await waitFor(() => expect(succeededFunc).toHaveBeenCalled());
  });
});
