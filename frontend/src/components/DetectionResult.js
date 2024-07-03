import React, { useState, useEffect } from "react";
import useCityCodes from "../hooks/useCityCodes";

const DetectionResult = ({ result, loading, error }) => {
  const { cityName, getCityNameByCode, addCityName, cityLoading } =
    useCityCodes();

  const [cityNameInput, setCityNameInput] = useState("");

  useEffect(() => {
    if (result && result !== "Not found") {
      const code = result.split(" ")[0];
      getCityNameByCode(code);
    }
  }, [result]);

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
    <div className="w-full mt-6 text-center bg-gray-700 p-4 rounded-md">
      <h2 className="text-2xl font-bold mb-2 text-blue-500">
        Detection Result:
      </h2>
      {loading ? <p className="text-blue-500 mt-4">Loading...</p> : null}
      {error ? <p className="text-red-500 mt-4">{error}</p> : null}
      {result && <p className="text-lg">{result}</p>}
      {result && result !== "Not found" && !cityLoading && cityName && (
        <p className="text-green-500 mt-4">City: {cityName.city}</p>
      )}
      {result && result !== "Not found" && !cityLoading && !cityName && (
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
  );
};

export default DetectionResult;
