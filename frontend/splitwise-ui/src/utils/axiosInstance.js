import axios from "axios";
import { API_BASE_URL } from "./config";
import { getToken, clearToken } from "../utils/auth";
const axiosInstance = axios.create({
  baseURL: API_BASE_URL,
});

axiosInstance.interceptors.request.use(
  (config) => {
    const token = getToken();
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);
axiosInstance.interceptors.response.use(
  (response) => response,
  (error) => {
    if (
      error.code === "ERR_NETWORK" ||
      error.message === "Network Error"
    ) {
      console.error("Backend unreachable");
      clearToken();
      window.location.replace("/login");
      return Promise.reject(error);
    }
    if (error.response?.status === 403) {
      clearToken()
      window.location.replace("/login");
    }
    return Promise.reject(error);
  }
);

export default axiosInstance;

