import React from "react";
import { Route, Routes } from "react-router-dom";
import ImageDetector from "./components/ImageDetector";
import PlateDetectorWithCam from "./components/CamDetector";
import Header from "./layout/Header";

export default function App() {
  return (
    <div className="h-screen w-screen bg-gray-950">
      <Header />
      <Routes>
        <Route path="/with-image/" element={<ImageDetector />} />
        <Route path="/with-cam/" element={<PlateDetectorWithCam />} />
      </Routes>
    </div>
  );
}
