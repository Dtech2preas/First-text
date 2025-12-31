# The Archive - Memory Restoration Project

This is a special React Native app designed as a gift. It parses WhatsApp chat logs and creates a "Memory Restoration" game where the player must answer questions about their relationship history to unlock a final message.

## How it works

1.  **Chat Parsing**: The app reads `assets/chat.txt` and identifies the two participants.
2.  **Quizzes**:
    *   "Who Said It?": Guess who sent a specific message.
    *   "When Was It?": Guess the date of a conversation.
3.  **Unlock**: After collecting 5 keys (correct answers), the "Archive" is restored, revealing a letter.

## Customization

### 1. Chat Log
Replace `assets/chat.txt` with your own WhatsApp export file.
*   **Android**: Export Chat -> Without Media -> Email/Save.
*   **Format**: The app expects standard format: `YYYY/MM/DD, HH:mm pm - Name: Message`.
*   **Names**: The code is currently customized for "lefa" and "owami" (who uses a blank name). If you use a different file, you may need to update `utils/chatParser.js` to match the new names.

### 2. Music
Replace `assets/music.mp3` with your preferred song. The file must be named `music.mp3`.

### 3. Letter
Edit `App.js` (search for `FINALE`) to change the final love letter text.

## Building the APK (Android)

This repository includes a GitHub Action to automatically build the APK.

1.  Push your changes to GitHub.
2.  Go to the "Actions" tab in your repository.
3.  Click "Build Android APK".
4.  Once finished, download the `TheArchive-debug` artifact (a zip file containing the APK).
5.  Install the APK on your Android phone.

## Local Development

1.  Install dependencies: `npm install`
2.  Run the app: `npx expo start`
