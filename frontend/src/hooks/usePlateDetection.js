import { useState } from "react";
import axios from "axios";

const API_BASE_URL = process.env.REACT_APP_API_BASE_URL || "http://localhost:8080";

const usePlateDetection = () => {
  const [result, setResult] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  const detectPlate = async (fileOrBase64) => {
    const isBase64 = typeof fileOrBase64 === "string";
    const data = isBase64 ? fileOrBase64 : new FormData();
    if (!isBase64) {
      data.append("file", fileOrBase64);
    }
    setLoading(true);
    setError(null);

    try {
      const response = await axios.post(
        `${API_BASE_URL}/api/detect${isBase64 ? "-base64" : ""}`,
        data,
        {
          headers: {
            "Content-Type": isBase64 ? "text/plain" : "multipart/form-data",
          },
        }
      );
      setResult(response.data);
      return response.data;
    } catch (err) {
      console.error("Error detecting plate:", err);
      const errorMessage = err.response?.data?.error || "Error detecting plate";
      setError(errorMessage);
      return null;
    } finally {
      setLoading(false);
    }
  };

  const resetResult = () => {
    setResult(null);
    setError(null);
  };

  return { result, detectPlate, loading, error, resetResult };
};

export default usePlateDetection;
