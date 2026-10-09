# 🤖 My AI Robot — Android AI Personal Assistant

**My AI Robot** is an intelligent, user-controlled digital personal assistant built for Android using Kotlin and Jetpack Compose. It automates product research across Temu and AliExpress, generates verified affiliate tracking links, creates 1000 × 1500 vertical Pinterest images, generates SEO copy, and publishes Pinterest pins to your boards.

---

## 📱 How to Build & Install from Your Android Phone

You do **not** need a computer or laptop. The automated cloud build pipeline runs directly on GitHub.

### Step 1: Run the Cloud Build on GitHub
1. Open your GitHub repository in your phone's browser (e.g. Chrome) or the GitHub Mobile App.
2. Tap the **Actions** tab (located near Code / Issues / Pull requests).
3. Tap **"Build My AI Robot Android APK"** from the workflow list.
4. Tap **Run workflow** -> Select the branch (e.g. `main`) -> Tap the green **Run workflow** button.
5. The build will start automatically and takes approximately 2–3 minutes.

### Step 2: Download the APK
There are two easy ways to download the APK directly on your phone:

#### Method A: Direct Download via GitHub Releases (Recommended — No Unzipping)
1. On your repository homepage, scroll down or tap **Releases** on the right/bottom menu.
2. Tap the latest release (e.g., `My AI Robot v1.0 (Build #...)`).
3. Under **Assets**, tap **`MyAIRobot-v1.0.apk`**.
4. Your browser will download the APK file directly to your phone's `Downloads` folder.

#### Method B: Via Actions Artifacts
1. Tap the completed run in the **Actions** tab.
2. Scroll to the bottom to the **Artifacts** section.
3. Tap **`My-AI-Robot-APK`** to download. (Note: GitHub packages artifacts in a `.zip` file; use your phone's Files app to extract the `.apk` file).

### Step 3: Install on Your Android Device
1. Open your phone's **Files** or **Downloads** app.
2. Tap **`MyAIRobot-v1.0.apk`**.
3. If Android prompts *"For your security, your phone is not allowed to install unknown apps from this source"*:
   - Tap **Settings** -> Toggle **Allow from this source** ON.
4. Tap **Install** -> Tap **Open**.

---

## 🔒 Security & Android Permissions

The app operates under a strict user-controlled security perimeter:
- **Never stores passwords in plaintext or sends credentials to AI models.**
- **Emergency Stop Button:** Instantly halts all background automation and web tasks.
- **Safety Pause:** Pauses automation immediately if a lock screen, CAPTCHA, or 2-factor authentication prompt is detected.
- **Android Accessibility Service (Optional):** Can be enabled in Android Settings to allow authorized taps, scrolling, and navigation in supported apps. You can disable it at any time.

---

## 🛠 Features Included

- **Assistant:** AI text and voice chat powered by `gemini-3.1-pro-preview` with High Thinking reasoning.
- **Control Center:** 10-step automation pipeline runner, pause/resume/stop controls, human approval checkpoint, and real-time execution logs.
- **Web Automator:** Embedded in-app automated browser with DOM scraper and quick portal shortcuts for Temu, AliExpress, Pinterest, and Google.
- **Affiliate & Pins:** Researched products table, 1000 × 1500 vertical Pinterest pin cards with verified affiliate tracking URLs and live publish verification.
- **Security & Accounts:** Individual authorization toggles for all 8 supported services and daily publishing limit safeguards.
