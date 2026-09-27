export const formatCurrency = (amount) => {
  const value = Number(amount || 0);
  return new Intl.NumberFormat("en-IN", { style: "currency", currency: "INR", maximumFractionDigits: 2 }).format(value);
};

export const formatDate = (isoString) => {
  if (!isoString) return "-";
  const date = new Date(isoString);
  return date.toLocaleString("en-IN", {
    day: "2-digit",
    month: "short",
    year: "numeric",
    hour: "2-digit",
    minute: "2-digit",
  });
};

export const maskAccountNumber = (accountNumber) => {
  if (!accountNumber || accountNumber.length < 4) return accountNumber;
  return "•••• •••• " + accountNumber.slice(-4);
};

export const extractErrorMessage = (error) => {
  if (error?.response?.data?.message) {
    const details = error.response.data.details;
    if (Array.isArray(details) && details.length > 0) {
      return details.join(", ");
    }
    return error.response.data.message;
  }
  return error?.message || "Something went wrong. Please try again.";
};
