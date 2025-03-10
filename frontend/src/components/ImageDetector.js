import React, { useState } from "react";
import DetectionResult from "./DetectionResult";
import usePlateDetection from "../hooks/usePlateDetection";

export default function ImageDetector() {
  const [file, setFile] = useState(null);
  const { result, detectPlate, loading, error } = usePlateDetection();

  const handleFileChange = (event) => {
    setFile(event.target.files[0]);
  };

  const handleSubmit = async (event) => {
    event.preventDefault();
    if (file) {
      await detectPlate(file);
    }
  };

  return (
    <div className="flex flex-col items-center min-h-screen bg-gray-900 text-white p-4">
      <div className="w-full max-w-lg p-6 bg-gray-800 rounded-lg shadow-lg">
        <form
          onSubmit={handleSubmit}
          className="flex flex-col items-center mb-4"
        >
          <input
            type="file"
            onChange={handleFileChange}
            className="mb-4 px-4 py-2 text-white bg-gray-700 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
          />
          <button
            type="submit"
            className="w-full px-4 py-2 text-white bg-blue-500 rounded-md hover:bg-blue-600 focus:outline-none focus:ring-2 focus:ring-blue-500"
          >
            Upload and Detect
          </button>
        </form>
        <DetectionResult result={result} loading={loading} error={error} />
      </div>
      {file && !result && (
        <div className="mt-6 w-full max-w-lg p-4 bg-gray-800 rounded-lg shadow-lg">
          <h2 className="text-center text-lg mb-2">Input Image</h2>
          <img
            src={URL.createObjectURL(file)}
            alt="Input"
            className="w-full rounded"
          />
        </div>
      )}
    </div>
  );
}
