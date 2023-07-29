import "@testing-library/jest-dom/extend-expect";
import React from "react";
import { render } from "@testing-library/react";
import MockAdapter from "axios-mock-adapter";
import { axiosInstanceWithoutCache } from "../../service/AxiosService";
import { Attachment, Attachments } from "../../model/TestRunModel";
import AttachmentsPage from "../AttachmentsPage";

describe("AttachmentsPage", () => {
  let mockAxios;

  beforeEach(() => {
    // @ts-ignore
    mockAxios = new MockAdapter(axiosInstanceWithoutCache);
  });

  afterEach(() => {
    mockAxios.restore();
  });

  it("should fetch attachments and render list", async () => {
    const publicId = "ATT12345";

    const attachments = {
      attachments: [
        {
          fileName: "attachment1.txt",
          objectName: "attachmentObject1",
        } as Attachment,
        {
          fileName: "attachment2.txt",
          objectName: "attachmentObject2",
        } as Attachment,
      ],
    } as Attachments;

    mockAxios
      .onGet(`http://localhost:8080/run/${publicId}/attachments`)
      .reply(200, attachments);

    const { findByTestId } = render(<AttachmentsPage publicId={publicId} />);

    expect(
      await findByTestId("attachment-file-name-attachment1.txt"),
    ).toHaveTextContent("attachment1.txt");
    expect(
      await findByTestId("attachment-file-name-attachment2.txt"),
    ).toHaveTextContent("attachment2.txt");
  });
});
