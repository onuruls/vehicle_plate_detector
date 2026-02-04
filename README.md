# German License Plate Detector

A full-stack application for detecting German license plates and resolving the registration prefix (Kürzel) to the corresponding city or district.

## Features

- 🚗 **License Plate Detection** — Upload an image or use your webcam to detect German plates
- 🏙️ **City Lookup** — Automatically resolves the plate prefix to the registration district (220+ prefixes included)
- 📷 **Multiple Input Methods** — Supports file upload and base64 encoded images
- 🎯 **Template Matching** — Uses OpenCV for character recognition
- 🌐 **REST API** — Clean JSON responses for easy integration

## Architecture

```
┌─────────────┐     HTTP/JSON     ┌─────────────────┐
│   Frontend  │ ◄───────────────► │     Backend     │
│   (React)   │                   │  (Spring Boot)  │
└─────────────┘                   └────────┬────────┘
                                           │
                                  ┌────────▼────────┐
                                  │     OpenCV      │
                                  │ Template Match  │
                                  └─────────────────┘
```

- **Frontend**: React with TailwindCSS, webcam support
- **Backend**: Java 17 + Spring Boot 3, OpenCV for plate detection
- **Data**: Local JSON file with German plate prefixes (no database required)

## Quick Start

### Prerequisites

- Java 17+
- Node.js 18+
- Maven (or use included wrapper)

### Backend

```bash
cd backend
./mvnw spring-boot:run
```

The API will be available at `http://localhost:8080`.

### Frontend

```bash
cd frontend
npm install
npm start
```

Open `http://localhost:3000` in your browser.

### Using Docker (Recommended)

```bash
docker compose up --build
```

- Frontend: http://localhost:3000
- Backend: http://localhost:8080

## API Reference

### Detect Plate (File Upload)

```bash
curl -X POST -F "file=@plate.jpg" http://localhost:8080/api/detect
```

**Response:**
```json
{
  "plateText": "KI AB 123",
  "prefix": "KI",
  "city": "Kiel",
  "status": "OK",
  "error": null
}
```

### Detect Plate (Base64)

```bash
curl -X POST \
  -H "Content-Type: text/plain" \
  -d "data:image/jpeg;base64,/9j/4AAQ..." \
  http://localhost:8080/api/detect-base64
```

### Lookup City Code

```bash
curl http://localhost:8080/api/city-codes/HH
```

**Response:**
```json
{
  "code": "HH",
  "city": "Hamburg"
}
```

### Response Status Codes

| Status | Description |
|--------|-------------|
| `OK` | Plate detected successfully |
| `NO_PLATE` | No license plate found in image |
| `ERROR` | Processing error occurred |

## Configuration

### Backend (`application.properties`)

| Property | Default | Description |
|----------|---------|-------------|
| `server.port` | `8080` | API server port |
| `app.debug.enabled` | `false` | Save debug images |
| `app.debug.dir` | `${java.io.tmpdir}/vehicle-plate-debug` | Debug output directory |

### Frontend

Create a `.env` file (see `.env.example`):

```env
REACT_APP_API_BASE_URL=http://localhost:8080
```

## Adding More City Codes

The city codes are stored in `backend/src/main/resources/city_codes.json`. To add more:

```json
[
  {"code": "NEW", "city": "New City Name"},
  ...
]
```

The file currently includes 220+ German registration prefixes.

## Troubleshooting

### OpenCV Issues

The backend uses [openpnp-opencv](https://github.com/openpnp/opencv) which includes native libraries for common platforms. If you encounter issues:

1. Ensure you're using Java 17+
2. Check your OS/architecture is supported (Windows/Linux/macOS, x64/arm64)

### Port Conflicts

If port 8080 or 3000 is in use:

```bash
# Backend: change in application.properties
server.port=8081

# Frontend: set PORT env variable
PORT=3001 npm start
```

### CORS Issues

CORS is configured in `WebConfig.java`. For production, update the allowed origins.

## Project Structure

```
├── backend/
│   ├── src/main/java/.../
│   │   ├── controller/     # REST endpoints
│   │   ├── service/        # Business logic
│   │   ├── model/          # DTOs
│   │   └── utils/          # OpenCV utilities
│   └── src/main/resources/
│       ├── city_codes.json # German plate prefixes
│       └── templates/      # Character templates
├── frontend/
│   └── src/
│       ├── components/     # React components
│       └── hooks/          # Custom hooks
├── docker-compose.yml
└── README.md
```

## Example Images

Sample plate images for testing are available in the `examplePlates/` directory.

## Authors

- Onur
- Markus

## License

This project is for educational purposes.
