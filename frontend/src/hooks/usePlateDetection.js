import { useState } from "react";
import axios from "axios";

const usePlateDetection = () => {
  const [result, setResult] = useState("");
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
        `http://localhost:8080/api/detect${isBase64 ? "-base64" : ""}`,
        isBase64 ? data : data,
        {
          headers: {
            "Content-Type": isBase64 ? "text/plain" : "multipart/form-data",
          },
        }
      );
      setResult(response.data);
      return response.data;
    } catch (error) {
      console.error("Error uploading file:", error);
      setError("Error detecting plate");
      return null;
    } finally {
      setLoading(false);
    }
  };

  return { result, detectPlate, loading, error };
};

export default usePlateDetection;
