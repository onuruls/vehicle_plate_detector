import React, { useState } from "react";
import axios from "axios";

export default function CamDetector() {
  const [file, setFile] = useState(null);
  const [result, setResult] = useState("");

  const handleFileChange = (event) => {
    setFile(event.target.files[0]);
  };

  const handleSubmit = async (event) => {
    event.preventDefault();
    const formData = new FormData();
    formData.append("file", file);

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
      setResult("Error detecting plate");
    }
  };

  return (
    <div className="">
      <h1 className="">Vehicle Plate Detector</h1>
      <form onSubmit={handleSubmit}>
        <input type="file" onChange={handleFileChange} />
        <button type="submit">Upload and Detect</button>
      </form>
      {result && (
        <div>
          <h2>Detection Result:</h2>
          <p>{result}</p>
        </div>
      )}
    </div>
  );
}
