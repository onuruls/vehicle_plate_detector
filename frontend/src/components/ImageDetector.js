import React, { useState } from "react";
import usePlateDetection from "../hooks/usePlateDetection";
import useCityCodes from "../hooks/useCityCodes";
import DetectionResult from "./DetectionResult";

export default function ImageDetector() {
  const [file, setFile] = useState(null);
  const [cityNameInput, setCityNameInput] = useState("");
  const { result, detectPlate, loading, error } = usePlateDetection();
  const {
    cityName,
    getCityNameByCode,
    addCityName,
    loading: cityLoading,
    error: cityError,
  } = useCityCodes();

  const handleFileChange = (event) => {
    setFile(event.target.files[0]);
    setCityNameInput("");
  };

  const handleSubmit = async (event) => {
    event.preventDefault();
    if (file) {
      const detectionResult = await detectPlate(file);
      if (detectionResult && detectionResult !== "Not found") {
        const code = detectionResult.split(" ")[0];
        getCityNameByCode(code);
      }
    }
  };

  const handleCityNameSubmit = async () => {
    if (cityNameInput.trim()) {
      const newCity = {
        code: result.split(" ")[0],
        city: cityNameInput.trim(),
      };
      await addCityName(newCity);
      setCityNameInput("");
      getCityNameByCode(newCity.code);
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
        <DetectionResult
          result={result}
          loading={loading}
          error={error || cityError}
          cityName={cityName}
          cityLoading={cityLoading}
          cityNameInput={cityNameInput}
          setCityNameInput={setCityNameInput}
          handleCityNameSubmit={handleCityNameSubmit}
        />
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
