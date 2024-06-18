import React from "react";
import { useNavigate } from "react-router-dom";
import img1 from "../imgs/withImage.png";
import img2 from "../imgs/withCam.webp";

export default function Home({ hoveredSection, setHoveredSection }) {
  const navigate = useNavigate();

  return (
    <div className="w-full min-h-screen flex">
      <div
        className={`flex-1 relative group cursor-pointer ${
          hoveredSection === "image" ? "flex-2" : ""
        }`}
        onMouseEnter={() => setHoveredSection("image")}
        onMouseLeave={() => setHoveredSection("")}
        onClick={() => navigate("/with-image")}
      >
        <img
          src={img1}
          alt="withImage"
          className={`w-full h-screen object-cover ${
            hoveredSection === "image" ? "opacity-100" : "opacity-50"
          } group-hover:opacity-100 transition-opacity duration-500 ease-in-out `}
        />
        <div
          className={`absolute inset-0 bg-slate-950 ${
            hoveredSection === "image" ? "opacity-0" : "opacity-25"
          } group-hover:opacity-0 transition-opacity duration-500 ease-in-out`}
        ></div>
      </div>
      <div
        className={`flex-1 relative group cursor-pointer ${
          hoveredSection === "cam" ? "flex-2" : ""
        }`}
        onMouseEnter={() => setHoveredSection("cam")}
        onMouseLeave={() => setHoveredSection("")}
        onClick={() => navigate("/with-cam")}
      >
        <img
          src={img2}
          alt="withCam"
          className={`w-full h-screen object-cover ${
            hoveredSection === "cam" ? "opacity-100" : "opacity-50"
          } group-hover:opacity-100 transition-opacity duration-500 ease-in-out`}
        />
        <div
          className={`absolute inset-0 bg-slate-950 ${
            hoveredSection === "cam" ? "opacity-0" : "opacity-25"
          } group-hover:opacity-0 transition-opacity duration-500 ease-in-out`}
        ></div>
      </div>
    </div>
  );
}
