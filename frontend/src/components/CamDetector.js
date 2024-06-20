import React, { useRef, useCallback, useState } from "react";
import Webcam from "react-webcam";
import usePlateDetection from "../hooks/usePlateDetection";

export default function CamDetector() {
  const webcamRef = useRef(null);
  const [capturing, setCapturing] = useState(false);
  const { result, detectPlate, loading, error } = usePlateDetection();

  const capture = useCallback(() => {
    const imageSrc = webcamRef.current.getScreenshot();
    if (imageSrc) {
      detectPlate(imageSrc);
    }
  }, [webcamRef, detectPlate]);

  const handleStartCapture = () => {
    setCapturing(true);
    setInterval(() => {
      if (capturing) {
        capture();
      }
    }, 2000); // Capture every 2 seconds
  };

  const handleStopCapture = () => {
    setCapturing(false);
  };

  return (
    <div className="flex flex-col items-center min-h-screen bg-gray-900 text-white p-4">
      <div className="w-full max-w-lg p-6 bg-gray-800 rounded-lg shadow-lg">
        <Webcam
          audio={false}
          ref={webcamRef}
          screenshotFormat="image/jpeg"
          className="w-full rounded"
        />
        <div className="mt-4">
          <button
            onClick={handleStartCapture}
            className="w-full px-4 py-2 mb-2 text-white bg-blue-500 rounded-md hover:bg-blue-600 focus:outline-none focus:ring-2 focus:ring-blue-500"
          >
            Start Capture
          </button>
          <button
            onClick={handleStopCapture}
            className="w-full px-4 py-2 text-white bg-red-500 rounded-md hover:bg-red-600 focus:outline-none focus:ring-2 focus:ring-red-500"
          >
            Stop Capture
          </button>
        </div>
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
          </div>
        )}
      </div>
    </div>
  );
}
