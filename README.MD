# Fahrzeug-Kennzeichen-Erkennung

Dies ist ein Projekt zur Erkennung von Fahrzeug-Kennzeichen mithilfe einer Live-Kamera oder eines Bildes. Wenn das Kennzeichen erkannt wird, gibt die Anwendung auch den Namen der Stadt aus.

## Bibliotheken und Frameworks

### Frontend

- React
- TailwindCSS

### Backend

- Java
- Spring Boot
- OpenCV
- MongoDB

## Installation und Ausführung

### Backend

1. Öffnen Sie das `backend` Projekt in Ihrem bevorzugten Java-Compiler (z.B. IntelliJ IDEA, Eclipse).

2. Stellen Sie sicher, dass Sie die OpenCV-Bibliothek in Ihrem Projektpfad hinzufügen. Fügen Sie die folgende Konfiguration (VM Option/Argument) hinzu:
    ```sh
    -Djava.library.path=X:\path\to\opencv\build\java\x64
    ```

4. Starten Sie die Spring Boot Anwendung über die Hauptklasse des Projekts.

### Backend über die Kommandozeile starten

Falls Sie das Backend über die Kommandozeile starten möchten, führen Sie folgende Schritte aus:

1. Navigieren Sie in das `backend` Verzeichnis:
    ```sh
    cd backend
    ```

2. Stellen Sie sicher, dass Sie die OpenCV-Bibliothek in Ihrem Projektpfad hinzufügen. Fügen Sie die folgende Konfiguration hinzu:
    ```sh
    export JAVA_OPTS="-Djava.library.path=X:\path\to\opencv\build\java\x64"
    ```

3. Bauen und starten Sie die Spring Boot Anwendung:
    ```sh
    ./mvnw clean install
    ./mvnw spring-boot:run
    ```

### Frontend

1. Navigieren Sie in das `frontend` Verzeichnis:
    ```sh
    cd frontend
    ```

2. Installieren Sie die Abhängigkeiten:
    ```sh
    npm install
    ```

3. Starten Sie die Anwendung:
    ```sh
    npm start
    ```

## Nutzung

### Frontend

- Öffnen Sie Ihren Browser und gehen Sie zu `http://localhost:3000`.
- Laden Sie ein Bild hoch oder starten Sie die Kamera, um das Fahrzeug-Kennzeichen zu erkennen.

### Backend

- Das Backend läuft auf `http://localhost:8080`.
- Die API-Endpunkte können verwendet werden, um Fahrzeug-Kennzeichen zu erkennen und Stadtinformationen abzurufen.

### Beispielbilder

Im Ordner `examplePlates` finden Sie Beispielbilder zum Testen der Anwendung.

## Ersteller

Dieses Projekt wurde erstellt von:
- [Onur]
- [Markus]

