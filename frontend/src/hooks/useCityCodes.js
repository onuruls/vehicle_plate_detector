import { useState } from "react";
import axios from "axios";

const useCityCodes = () => {
  const [cityName, setCityName] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  const getCityNameByCode = async (code) => {
    setLoading(true);
    setError(null);
    try {
      const response = await axios.get(
        `http://localhost:8080/api/city-codes/${code}`
      );
      setCityName(response.data);
    } catch (error) {
      console.error("Error fetching city name:", error);
      setError("Error fetching city name");
    } finally {
      setLoading(false);
    }
  };

  const addCityName = async (newCityCode) => {
    setLoading(true);
    setError(null);
    try {
      const response = await axios.post(
        "http://localhost:8080/api/city-codes",
        newCityCode,
        {
          headers: {
            "Content-Type": "application/json",
          },
        }
      );
      return response.data;
    } catch (error) {
      console.error("Error adding city name:", error);
      setError("Error adding city name");
      return null;
    } finally {
      setLoading(false);
    }
  };

  return { cityName, getCityNameByCode, addCityName, loading, error };
};

export default useCityCodes;
