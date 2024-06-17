import { useNavigate } from "react-router-dom";
import { useState } from "react";

export default function Header() {
  const [activeSection, setActiveSection] = useState("");

  const navigate = useNavigate();

  return (
    <header className="w-full bg-blue-950">
      <div className="w-full flex justify-between p-4 text-white">
        <button
          className={`text-lg hover:text-gray-300 ${
            activeSection === "with-image" ? "text-gray-300" : ""
          }`}
          onClick={() =>
            navigate("/with-image") || setActiveSection("with-image")
          }
        >
          Detect with Image
        </button>
        <button
          className="text-center font-bold text-2xl hover:text-gray-300"
          onClick={() => navigate("/") || setActiveSection("")}
        >
          Vehicle Plate Detector
        </button>
        <button
          className={`text-lg hover:text-gray-300 ${
            activeSection === "with-cam" ? "text-gray-300" : ""
          }`}
          onClick={() => navigate("/with-cam") || setActiveSection("with-cam")}
        >
          Detect with Cam
        </button>
      </div>
    </header>
  );
}
