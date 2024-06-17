export default function Footer() {
  return (
    <footer className="w-full bg-blue-950 mt-auto bottom-0">
      <div className="w-full flex justify-between p-4 text-white">
        <div>
          <p>
            &copy; {new Date().getFullYear()} Vehicle Plate Detector. All rights
            reserved.
          </p>
        </div>
        <div className="flex space-x-4">
          <a href="#" className="hover:text-gray-300">
            Markus Artemov - Onur Ulusoy
          </a>
        </div>
      </div>
    </footer>
  );
}
