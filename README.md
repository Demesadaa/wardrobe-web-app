# Wardrobe Web App

A Spring Boot + React web app for saving clothing photos and remixing outfits on a playful lobby character.

## Features

- Account registration, login, logout, and profile editing.
- Browser camera capture for clothing photos.
- Four wardrobe tags: footwear, pants, torso, and headwear.
- Backend image storage with clothing metadata in H2.
- Lobby character that wears one saved piece from each category.
- Random reroll plus left/right controls per category.

## Run Locally

Backend:

```powershell
cd backend
mvn spring-boot:run
```

Frontend:

```powershell
cd frontend
npm install
npm run dev
```

The frontend expects the backend at `http://localhost:8080`. Camera access usually requires `localhost` or HTTPS.
