import React, { useState } from "react";
import usePlateDetection from "../hooks/usePlateDetection";

export default function ImageDetector() {
  const [file, setFile] = useState(null);
  const { result, detectPlate, loading, error } = usePlateDetection();

  const handleFileChange = (event) => {
    setFile(event.target.files[0]);
  };

  const handleSubmit = (event) => {
    event.preventDefault();
    if (file) {
      detectPlate(file);
    }
  };

  return (
    <div className="text-white">
      <h1 className="">Vehicle Plate Detector</h1>
      <form onSubmit={handleSubmit}>
        <input type="file" onChange={handleFileChange} />
        <button type="submit">Upload and Detect</button>
      </form>
      {loading && <p>Loading...</p>}
      {error && <p>{error}</p>}
      {result && (
        <div>
          <h2>Detection Result:</h2>
          <p>{result}</p>
        </div>
      )}
    </div>
  );
}
