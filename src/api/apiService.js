import axiosInstance from "./axiosInstance";

// ---------- Auth ----------
export const registerUser = (payload) => axiosInstance.post("/auth/register", payload);
export const loginUser = (payload) => axiosInstance.post("/auth/login", payload);

// ---------- User / Profile ----------
export const getMyProfile = () => axiosInstance.get("/users/me");
export const updateMyProfile = (payload) => axiosInstance.put("/users/me", payload);

// ---------- Accounts ----------
export const createAccount = (payload) => axiosInstance.post("/accounts", payload);
export const getMyAccounts = () => axiosInstance.get("/accounts");
export const getAccountById = (accountId) => axiosInstance.get(`/accounts/${accountId}`);
export const getAccountTransactions = (accountId, page = 0, size = 10) =>
  axiosInstance.get(`/accounts/${accountId}/transactions`, { params: { page, size } });

// ---------- Transactions ----------
export const depositMoney = (payload) => axiosInstance.post("/transactions/deposit", payload);
export const withdrawMoney = (payload) => axiosInstance.post("/transactions/withdraw", payload);
export const transferMoney = (payload) => axiosInstance.post("/transactions/transfer", payload);
export const getMyTransactions = (page = 0, size = 10) =>
  axiosInstance.get("/transactions/me", { params: { page, size } });

// ---------- Beneficiaries ----------
export const getMyBeneficiaries = () => axiosInstance.get("/beneficiaries");
export const addBeneficiary = (payload) => axiosInstance.post("/beneficiaries", payload);
export const deleteBeneficiary = (id) => axiosInstance.delete(`/beneficiaries/${id}`);

// ---------- Admin ----------
export const getAdminDashboard = () => axiosInstance.get("/admin/dashboard");
export const getAdminCustomers = (keyword = "", page = 0, size = 10) =>
  axiosInstance.get("/admin/customers", { params: { keyword, page, size } });
export const setCustomerStatus = (userId, enabled) =>
  axiosInstance.patch(`/admin/customers/${userId}/status`, null, { params: { enabled } });
export const getAdminAccounts = (keyword = "", page = 0, size = 10) =>
  axiosInstance.get("/admin/accounts", { params: { keyword, page, size } });
export const updateAccountStatus = (accountId, status) =>
  axiosInstance.patch(`/admin/accounts/${accountId}/status`, null, { params: { status } });
export const getAdminTransactions = (type, status, page = 0, size = 10) =>
  axiosInstance.get("/admin/transactions", { params: { type, status, page, size } });
