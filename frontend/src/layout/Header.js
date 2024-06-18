import { useNavigate } from "react-router-dom";

export default function Header({ hoveredSection, setHoveredSection }) {
  const navigate = useNavigate();

  return (
    <header className="w-full bg-slate-950">
      <div className="w-full flex justify-evenly p-4 text-gray-400 text-lg">
        <button
          className={`${hoveredSection === "image" ? "text-white" : ""}`}
          onClick={() => navigate("/with-image")}
          onMouseEnter={() => setHoveredSection("image")}
          onMouseLeave={() => setHoveredSection("")}
        >
          Detect with Image
        </button>
        <button
          className="text-center font-bold text-2xl text-white hover:text-gray-300"
          onClick={() => navigate("/")}
        >
          Vehicle Plate Detector
        </button>
        <button
          className={`${hoveredSection === "cam" ? "text-white" : ""}`}
          onClick={() => navigate("/with-cam")}
          onMouseEnter={() => setHoveredSection("cam")}
          onMouseLeave={() => setHoveredSection("")}
        >
          Detect with Cam
        </button>
      </div>
    </header>
  );
}
