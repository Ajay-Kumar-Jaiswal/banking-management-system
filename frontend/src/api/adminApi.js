import axiosClient from "./axiosClient";

export const getAllCustomers = (search = "") =>
  axiosClient.get("/admin/customers", { params: search ? { search } : {} });

export const getAllAccounts = (search = "") =>
  axiosClient.get("/admin/accounts", { params: search ? { search } : {} });

export const getAllTransactions = () => axiosClient.get("/admin/transactions");

export const updateAccountStatus = (accountId, status) =>
  axiosClient.put(`/admin/accounts/${accountId}/status`, { status });
