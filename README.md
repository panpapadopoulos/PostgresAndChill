<p align="center">
  <img src="assets/banner.gif"/>
</p>

<h1 align="center">Postgres and Chill</h1>
<p align="center"><b>Smarter Movie Recommendations • Full-Stack • Dockerized</b></p>

<p align="center">
<p align="center">
  <img src="https://img.shields.io/badge/PostgreSQL-4169E1?logo=postgresql&logoColor=white&style=for-the-badge"/>
  <img src="https://img.shields.io/badge/Spring%20Boot-6DB33F?logo=springboot&logoColor=white&style=for-the-badge"/>
  <img src="https://img.shields.io/badge/Java-ED8B00?logo=openjdk&logoColor=white&style=for-the-badge"/>
  <img src="https://img.shields.io/badge/Docker-2496ED?logo=docker&logoColor=white&style=for-the-badge"/>
  <img src="https://img.shields.io/badge/MovieLens-Dataset-blue?style=for-the-badge"/>
  <img src="https://img.shields.io/badge/HTML5-E34F26?logo=html5&logoColor=white&style=for-the-badge"/>
  <img src="https://img.shields.io/badge/CSS3-1572B6?logo=css3&logoColor=white&style=for-the-badge"/>
  <img src="https://img.shields.io/badge/JavaScript-F7DF1E?logo=javascript&logoColor=black&style=for-the-badge"/>
</p>

---

# 🎬 Overview

**Postgres and Chill** is a fully Dockerized full-stack movie recommendation platform built as part of  
**CS 51550 — Database Systems, Purdue University Northwest (Fall 2025)**.

The application integrates:

- **PostgreSQL** for data storage  
- **Spring Boot REST API** backend  
- **Custom collaborative filtering recommendation engine**  
- **Static HTML/CSS/JS frontend (Netflix-inspired UI)**  
- **Docker Compose** for one-click deployment  

Users can:

- Browse *all* movies  
- View *popular* movies (Bayesian weighted ranking)  
- Receive *personalized recommendations*  
- Rate movies (0–5 stars)  
- Complete a *cold-start onboarding* experience  

---

# 📸 Demo & Screenshots

### 🎞 Demo GIF
<p align="center">
  <img src="assets/demo.gif" width="700"/>
</p>

# 🚀 Running the Application

## 1️⃣ Install Prerequisites
- Install **Docker Desktop**
- !!! Docker needs to be running !!!
## 2️⃣ Start the Entire App

start.bat

This will:

- Start the PostgreSQL container  
- Load MovieLens data  
- Start the Spring Boot backend  
- Open the frontend in your browser  

## 3️⃣ Stop the Application

stop.bat


## 4️⃣ Reset Database (Fresh Load)

cleanStart.bat

## 5️⃣ View Logs

console.bat
---

# 🧠 Features

### ⭐ Personalized Recommendations  
Collaborative filtering with weighted correlation & rating-based similarity.

### ⭐ Smart Popular Movies Ranking  
Weighted (Bayesian) IMDB-style formula:  weighted = (v/(v+m)) * R + (m/(v+m)) * C

Ensures fairness for movies with fewer ratings.

### ⭐ Movie Browsing  
- Popular  
- All movies  
- Genres  
- Sorting options  
- Average & weighted rating display

### ⭐ Cold Start Onboarding  
New users rate initial movies → system learns preferences.

### ⭐ Fully Dockerized  
Zero manual setup. One click → app runs.

---

# 🏛 System Architecture

<p align="center">
  <img src="assets/system.png" width="700"/>
</p>

---

# 🗄️ Database Schema
<p align="center">
  <img src="assets/schema.png" width="700"/>
<p>

# 🧮 Recommendation Engine (How It Works)

### Step-by-Step Logic

1. Collect all ratings from the user  
2. Identify unrated movies  
3. Compute similarity between unrated and rated movies  
   - Weighted correlation  
4. Predict a score for each movie  
5. Rank movies → return **Top-N recommendations**

### Scoring Logic

- If predicted score > user average → “likely to enjoy”  
- If predicted score < user average → “likely to dislike”  

This mirrors classical collaborative filtering used by platforms like Netflix.


# 🧪 Results & Evaluation

- ✔ Fully functional end-to-end recommendation system  
- ✔ Accurate REST API behavior  
- ✔ Stable Docker deployment  
- ✔ Good recommendation quality  
- ✔ Clean UI and user flow  
- ✔ Achieved all goals from the project proposal  


# ⚠️ Limitations & Future Improvements

### Current Limitations
- No JWT authentication  
- Frontend not using React/Vue  
- Recommendation engine is classical, not ML-based  
- No pagination or infinite scroll  
- Tags table unused  

### Future Improvements
- Neural network–based recommender  
- React frontend  
- JWT security  
- Watchlists + favorites  
- Caching for faster queries  


# 📥 Download

This repository includes:

- Full source code  
- Project report (PDF)  
- Docker setup  
- Database schema + loaders  
- Screenshots & demo GIF  
- Presentation Video
- Demo Video
# 🎥 Project Videos

## 📘 Presentation Video
<p align="center">
  <a href="https://drive.google.com/file/d/1uzaf4vCgtJArSsP0MM4foU_iYkwjD2Ts/view?usp=drive_link">
    <img src="assets/presentation-thumbnail.png" width="700" alt="Presentation Video Thumbnail">
  </a>
</p>

<p align="center"><b>▶ Click the thumbnail to watch the Presentation Video</b></p>


## 🛠️ Demo Video
<p align="center">
  <a href="https://drive.google.com/file/d/1MAuR2AKhWPFniS_8Qqj5WAItxcWokkKk/view?usp=sharing">
    <img src="assets/demo-thumbnail.png" width="700" alt="Demo Video Thumbnail">
  </a>
</p>

<p align="center"><b>▶ Click the thumbnail to watch the Demo Video</b></p>

### UI Previews
<p align="center">
  <img src="assets/index.png" width="700"/><br/>
  <i>Home Page — Popular & All Movies</i><br/><br/>
</p>
<p align="center">
  <img src="assets/login.png" width="700"/><br/>
  <i>Login</i><br/><br/>
</p>
<p align="center">
  <img src="assets/register.png" width="700"/><br/>
  <i>Register</i><br/><br/>
</p>
<p align="center">
  <img src="assets/coldstart.png" width="700"/><br/>
  <i>Cold-Start: Rate a Few Movies</i><br/><br/>
</p>
<p align="center">
  <img src="assets/dashboard.png" width="700"/><br/>
  <i>User Dashboard + Recommendations</i><br/><br/>
</p>
<p align="center">
  <img src="assets/loading.png" width="700"/><br/>
  <i>Loading / System Startup</i>
</p>
<p align="center">
  <img src="assets/systemdown.png" width="700"/><br/>
  <i>System Down</i>
</p>

---

# 👤 About the Author

**Panagiotis Papadopoulos**  
*M.S. Computer Science — Purdue University Northwest*  

I am a graduate student in Computer Science.  
Postgres and Chill was developed as part of **CS 51550 — Database Systems**, combining full-stack engineering with practical database design and recommendation-system techniques.

### 📬 Contact
- **Email:** papadop@pnw.edu  
- **LinkedIn:** [Panagiotis Papadopoulos](https://www.linkedin.com/in/panagiotis-papadopoulos-0b1a291ab/)
- **GitHub:** panpapadopoulos

---

# 📄 License (MIT)

MIT License

Copyright (c) 2025

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files...


# 🙌 Thank You

If you found this project helpful, consider starring ⭐ the repository!