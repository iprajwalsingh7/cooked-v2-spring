# Cooked V2

The ultimate roasting platform. Select your victim (GitHub, Reddit, Spotify) and let the AI humble you.

Built with **Spring Boot 2.7.x**, **Gemini 1.5 Pro**, and **Vanilla HTML/Tailwind**.

## Local Development & Setup

1. **Clone the repository:**
   ```bash
   git clone git@github.com:iprajwalsingh7/cooked-v2-spring.git
   cd cooked-v2-springboot
   ```

2. **Configure API Keys safely:**
   Do **not** hardcode your keys in the `application.properties` file! Instead, create a new file located at `src/main/resources/application-dev.properties`. This file is ignored by Git to protect your secrets.

   Inside `application-dev.properties`, add your keys:
   ```properties
   gemini.api.key=YOUR_GEMINI_API_KEY
   github.token=YOUR_GITHUB_TOKEN
   spotify.client.id=YOUR_SPOTIFY_CLIENT_ID
   spotify.client.secret=YOUR_SPOTIFY_CLIENT_SECRET
   # Important for local Spotify auth
   spotify.redirect.uri=http://127.0.0.1:8080/ 
   ```

3. **Run the Application Locally with the Dev Profile:**
   ```bash
   mvn spring-boot:run -Dspring-boot.run.profiles=dev
   ```

4. **Access the application** at `http://127.0.0.1:8080/`.

## Public Deployment (Render, Railway, Fly.io, etc.)

When deploying this to a public web host, you do not need the `application-dev.properties` file.

1. Tell your host to build using Maven (`mvn clean install`) and run the generated jar.
2. In your host's dashboard, provide the following **Environment Variables**:
   * `GEMINI_API_KEY`
   * `GITHUB_TOKEN`
   * `SPOTIFY_CLIENT_ID`
   * `SPOTIFY_CLIENT_SECRET`
   * `SPOTIFY_REDIRECT_URI` (This must match the URL of your deployed site, e.g. `https://your-custom-app.onrender.com/`. Make sure this exact string is registered in your Spotify Developer Dashboard allowed redirect URIs).
