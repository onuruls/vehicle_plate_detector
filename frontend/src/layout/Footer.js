export default function Footer() {
  return (
    <footer className="w-full bg-blue-950 mt-auto bottom-0">
      <div className="w-full flex flex-col md:flex-row text-center justify-between p-4 text-white">
        <div>
          <p>
            &copy; {new Date().getFullYear()} Vehicle Plate Detector. All rights
            reserved.
          </p>
        </div>
        <div>
          <p className="hover:text-gray-300">Markus Artemov - Onur Ulusoy</p>
        </div>
      </div>
    </footer>
  );
}
