import React, { useRef, useCallback, useState } from "react";
import Webcam from "react-webcam";
import usePlateDetection from "../hooks/usePlateDetection";
import DetectionResult from "./DetectionResult";

export default function CamDetector() {
  const webcamRef = useRef(null);
  const [capturing, setCapturing] = useState(false);
  const [intervalId, setIntervalId] = useState(null);
  const { result, detectPlate, loading, error } = usePlateDetection();

  const capture = useCallback(() => {
    const imageSrc = webcamRef.current.getScreenshot();
    if (imageSrc) detectPlate(imageSrc);
  }, [webcamRef, detectPlate]);

  const handleStartCapture = () => {
    if (!capturing) {
      setCapturing(true);
      const id = setInterval(() => capture(), 2000);
      setIntervalId(id);
    }
  };

  const handleStopCapture = () => {
    setCapturing(false);
    if (intervalId) {
      clearInterval(intervalId);
      setIntervalId(null);
    }
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
            className={`w-full px-4 py-2 mb-2 text-white rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 ${
              capturing
                ? "bg-gray-500 cursor-not-allowed"
                : "bg-blue-500 hover:bg-blue-600"
            }`}
            disabled={capturing}
          >
            Start Capture
          </button>
          <button
            onClick={handleStopCapture}
            className={`w-full px-4 py-2 text-white rounded-md focus:outline-none focus:ring-2 focus:ring-red-500 ${
              capturing
                ? "bg-red-500 hover:bg-red-600"
                : "bg-gray-500 cursor-not-allowed"
            }`}
            disabled={!capturing}
          >
            Stop Capture
          </button>
        </div>
        <DetectionResult result={result} loading={loading} error={error} />
      </div>
    </div>
  );
}
