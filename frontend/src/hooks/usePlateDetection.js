import { useState } from "react";
import axios from "axios";

export default function usePlateDetection() {
  const [result, setResult] = useState("");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  const detectPlate = async (file) => {
    const formData = new FormData();
    formData.append("file", file);

    setLoading(true);
    setError(null);

    try {
      const response = await axios.post(
        "http://localhost:8080/api/detect",
        formData,
        {
          headers: {
            "Content-Type": "multipart/form-data",
          },
        }
      );
      setResult(response.data);
    } catch (error) {
      console.error("Error uploading file:", error);
      setError("Error detecting plate");
    } finally {
      setLoading(false);
    }
  };

  return { result, detectPlate, loading, error };
}
