import React, { useState, useEffect } from "react";
import usePlateDetection from "../hooks/usePlateDetection";
import useCityCodes from "../hooks/useCityCodes";

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

  useEffect(() => {
    if (result) {
      const code = result.split(" ")[0];
      if (code) {
        getCityNameByCode(code);
      }
    }
  }, [result]);

  const handleFileChange = (event) => {
    setFile(event.target.files[0]);
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
        {loading && (
          <p className="text-center text-blue-500 mt-4">Loading...</p>
        )}
        {error && <p className="text-center text-red-500 mt-4">{error}</p>}
        {result && (
          <div className="w-full mt-6 text-center bg-gray-700 p-4 rounded-md">
            <h2 className="text-2xl font-bold mb-2 text-blue-500">
              Detection Result:
            </h2>
            <p className="text-lg">{result}</p>
            {result !== "Not found" && !cityLoading && cityName && (
              <div className="mt-4">
                <p className="text-green-500">City: {cityName.city}</p>
              </div>
            )}
            {result !== "Not found" && !cityLoading && !cityName && (
              <div className="mt-4">
                <p className="text-red-500">
                  City information not found. Please input the city name:
                </p>
                <input
                  type="text"
                  value={cityNameInput}
                  onChange={(e) => setCityNameInput(e.target.value)}
                  className="w-full px-4 py-2 text-black bg-white rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                  placeholder="Enter city name"
                />
                <button
                  type="button"
                  onClick={handleCityNameSubmit}
                  className="mt-2 px-4 py-2 text-white bg-blue-500 rounded-md hover:bg-blue-600 focus:outline-none focus:ring-2 focus:ring-blue-500"
                >
                  Submit
                </button>
              </div>
            )}
          </div>
        )}
      </div>

      {/* Input Image Section */}
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
