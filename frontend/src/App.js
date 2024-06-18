import React, { useState } from "react";
import { Route, Routes } from "react-router-dom";
import ImageDetector from "./components/ImageDetector";
import CamDetector from "./components/CamDetector";
import Home from "./components/Home";
import Header from "./layout/Header";
import Footer from "./layout/Footer";

export default function App() {
  const [hoveredSection, setHoveredSection] = useState("");

  return (
    <div className="bg-slate-950">
      <Header
        hoveredSection={hoveredSection}
        setHoveredSection={setHoveredSection}
      />
      <Routes>
        <Route
          path="/"
          element={
            <Home
              hoveredSection={hoveredSection}
              setHoveredSection={setHoveredSection}
            />
          }
        />
        <Route path="/with-image/" element={<ImageDetector />} />
        <Route path="/with-cam/" element={<CamDetector />} />
      </Routes>
      <Footer />
    </div>
  );
}
