import React from "react";

const MAP = {
  ACTIVE: "success",
  SUCCESS: "success",
  INACTIVE: "warning",
  PENDING: "warning",
  CLOSED: "danger",
  FAILED: "danger",
  DEPOSIT: "success",
  WITHDRAWAL: "warning",
  TRANSFER: "info",
};

export default function StatusBadge({ value }) {
  const variant = MAP[value] || "neutral";
  return <span className={`badge badge-${variant}`}>{value}</span>;
}
