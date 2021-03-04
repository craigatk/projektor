import * as React from "react";
import { ResultsProcessingFailure } from "../../model/AdminModel";
import AdminFailuresTable from "./AdminFailuresTable";

interface AdminFailuresDetailsProps {
  failures: ResultsProcessingFailure[];
}

const AdminFailuresDetails = ({ failures }: AdminFailuresDetailsProps) => {
  return (
    <div>
      <AdminFailuresTable failures={failures} />
    </div>
  );
};

export default AdminFailuresDetails;
