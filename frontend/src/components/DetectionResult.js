import React from "react";

const DetectionResult = ({ result, loading, error }) => {
  const hasResult = result && result.status === "OK";
  const noPlate = result && result.status === "NO_PLATE";
  const hasError = result && result.status === "ERROR";

  return (
    <div className="w-full mt-6 text-center bg-gray-700 p-4 rounded-md">
      <h2 className="text-2xl font-bold mb-2 text-blue-500">
        Detection Result
      </h2>

      {loading && <p className="text-blue-500 mt-4">Detecting...</p>}

      {error && <p className="text-red-500 mt-4">{error}</p>}

      {hasResult && (
        <div className="mt-4 space-y-2">
          <p className="text-lg font-mono bg-gray-800 p-2 rounded">
            {result.plateText}
          </p>
          {result.prefix && (
            <p className="text-gray-300">
              Prefix: <span className="text-white font-semibold">{result.prefix}</span>
            </p>
          )}
          {result.city ? (
            <p className="text-green-500">
              City: <span className="font-semibold">{result.city}</span>
            </p>
          ) : (
            result.prefix && (
              <p className="text-yellow-500">City: Unknown</p>
            )
          )}
        </div>
      )}

      {noPlate && (
        <p className="text-yellow-500 mt-4">No license plate detected</p>
      )}

      {hasError && (
        <p className="text-red-500 mt-4">{result.error}</p>
      )}
    </div>
  );
};

export default DetectionResult;
