
# AngryHomies

**AngryHomies** is a LibGDX-powered game inspired by Angry Birds. It features multiple screens, interactive game elements, and a dynamic GUI that presents fully functional gameplay mechanics, interactive features, and a comprehensive UI flow. This project has evolved from an initial static GUI submission into a fully operational game with enhanced logic, dynamic asset handling, and serialized game states.

---

## Github Repository

[Link to AngryHomies Repository](https://github.com/kushiiitd05/ANGRYHOMIES_2023135_2023296)

---

## Platforms

- **Core:** Main module with shared game logic across all platforms.
- **LWJGL3:** Primary desktop platform using LWJGL3 for execution.

---

## Gradle

AngryHomies uses [Gradle](https://gradle.org/) for dependency management and build automation. The Gradle wrapper (`gradlew.bat` or `./gradlew`) allows for seamless task execution.

#### Useful Gradle Tasks:
- `build`: Builds sources and archives for all projects.
- `clean`: Removes build folders containing compiled classes and archives.
- `lwjgl3:run`: Starts the application.
- `test`: Executes unit tests (if any).
- `--refresh-dependencies`: Validates and refreshes dependencies.

---

## Game Overview

AngryHomies invites players into an interactive experience where birds launch from a slingshot to collide with blocks and pigs. The goal is to eliminate all pigs while navigating the obstacles created by block structures.

---

## **Table of Contents**
1. [Game Setup](#game-setup)
2. [Running the Game](#running-the-game)
3. [New Features and Enhancements](#new-features-and-enhancements)
4. [Class Structure](#class-structure)
5. [References](#references)

---

## Game Setup

#### Steps to Set Up and Run AngryHomies:
1. **Clone the Repository**:
   ```bash
   git clone https://github.com/kushiiitd05/ANGRYHOMIES_2023135_2023296
   ```

2. **Navigate to the Project Directory**:
   ```bash
   cd AngryHomies
   ```

3. **Run the Game Using Gradle**:
   ```bash
   ./gradlew lwjgl3:run
   ```

4. **Prerequisites**:
   - Ensure **Java 21** and **Gradle** are installed on your system.
   - Follow instructions to configure required dependencies.

---

## Running the Game

#### **Main Game Flow**
1. **First Run**: A loading screen (`FirstScreen`) transitions to the **Home Screen**.
2. **Home Screen**:
   - **Start Game**: Opens the Level Select screen.
   - **Load Game**: Loads a previously saved game.
   - **Quit Game**: Exits the application.
3. **Gameplay**:
   - Select a level from the Level Select screen to proceed to gameplay (Level One by default).

#### **Pause Menu**:
Accessible during gameplay to:
- **Resume**: Continue the game.
- **Restart**: Reset and reload the level.
- **Save Game**: Save the current state and return to the Home Screen.
- **Exit**: Return to the Home Screen.

#### **Win/Lose Screens**:
- **Win Screen**: Transition to Level Select upon clearing the level.
- **Lose Screen**: Retry the level or return to Level Select.

#### **Loading a Saved Game**:
1. Select **Load Game** from the Home Screen.
2. Resume gameplay from a saved state.

#### **Exiting the Game**:
- Quit from the Home Screen to exit the application.

---

## New Features and Enhancements

1. **Dynamic Gameplay Features**:
   - Transitioned from a static GUI to a functional game with **Box2D physics**.
   - Collision detection and object interactions are dynamically managed using a **CollisionDetector** class.

2. **Advanced Game Elements**:
   - Birds, pigs, and blocks now include enhanced attributes and methods.
   - Overloaded constructors for `Bird`, `Pig`, and `Block` allow flexible initialization.

3. **Serialization**:
   - Implemented a **Serialization Package** for saving and loading game states:
     - `SerializableBirds`
     - `SerializableBlocks`
     - `SerializableBodies`
   - The `LevelState` class tracks level-specific attributes, ensuring continuity between sessions.

4. **Texture Management**:
   - Utilized a **Texture Factory** for managing textures dynamically.
   - Efficient mapping of textures for streamlined asset management.

5. **Test Suite**:
   - Added a **Test Package** for verifying logic and game physics:
     - `CollisionDetectorTest`
     - `LogicTest`
     - `SlingshotTest`

6. **Updated Class Structure**:
   - **Packages**:
     - `collision`: Manages collision detection.
     - `logic`: Handles slingshot mechanics and trajectory logic.
     - `assets`: Centralized asset management.
     - `serializable`: Manages serialized game elements.

7. **Enhanced UI/UX**:
   - Improved transition animations between screens.
   - Redesigned pause and win/lose menus for clarity and interactivity.
   - Added custom fonts and skins for buttons and in-game text.

8. **Gradle Updates**:
   - Updated `build.gradle` for new dependencies and test configurations.

---

## Class Structure

1. **Core Classes**:
   - `Main`: Handles the main game loop and screen transitions.
   - `Slingshot`: Manages bird launching mechanics with trajectory calculations.

2. **Screens**:
   - `FirstScreen`: Loading screen.
   - `HomeScreen`: Main menu.
   - `LevelSelectScreen`: Level selection.
   - `WinScreen` and `LoseScreen`: End screens for game outcomes.
   - `SaveSuccessScreen`: Confirmation for successful saves.
   - `LoadScreen`: Displays saved games.

3. **Game Elements**:
   - `Bird` (and subclasses): Defines bird behavior and attributes.
   - `Pig` (and subclasses): Defines pig attributes and damage handling.
   - `Block` (and subclasses): Handles block destruction upon collisions.

4. **Utility Classes**:
   - `AssetManager`: Handles textures, sounds, and game assets.
   - `CollisionDetector`: Detects and processes object collisions.

---

## References

1. **OpenAI GPT**:
   - Assisted in debugging errors and providing optimization suggestions.
2. **Google**:
   - Researched UI/UX improvements and Box2D integrations.
3. **GitHub**:
   - Referenced sample repositories for dynamic gameplay and serialization implementation.
4. **YouTube**:
   - Tutorials on LibGDX setup (GameFromScratch).
   - Box2D tutorials for understanding dependencies and root causes of errors.

---

## Quick Start Guide

#### Launching the Game
```bash
./gradlew lwjgl3:run
```

#### Playing the Game
- Start with the **Home Screen**.
- Select **Start Game** to proceed to gameplay or **Load Game** to resume a saved session.
- Use the **Pause Menu** during gameplay to manage your session.

#### Saving Progress
- Save game states from the **Pause Menu**.
- Resume saved games from the **Load Screen**.

Enjoy playing **AngryHomies** – a game that blends creativity, logic, and physics for a delightful experience!
