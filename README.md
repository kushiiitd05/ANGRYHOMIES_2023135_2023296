
AngryHomies

AngryHomies is a LibGDX-powered game inspired by Angry Birds. It features multiple screens, interactive game elements, and a static GUI that presents the basic gameplay mechanics and UI flow.

This project was generated using gdx-liftoff and includes a template with basic application launchers and a main class extending Game that sets the first screen.

Platforms

core: Main module with shared game logic across all platforms.
lwjgl3: Primary desktop platform using LWJGL3.
Gradle
This project uses Gradle for dependency management and build automation. For Gradle documentation, visit Gradle.org.

## Gradle

This project uses [Gradle](https://gradle.org/) to manage dependencies.
The Gradle wrapper was included, so you can run Gradle tasks using `gradlew.bat` or `./gradlew` commands.
Useful Gradle tasks and flags:

- `--continue`: when using this flag, errors will not stop the tasks from running.
- `--daemon`: thanks to this flag, Gradle daemon will be used to run chosen tasks.
- `--offline`: when using this flag, cached dependency archives will be used.
- `--refresh-dependencies`: this flag forces validation of all dependencies. Useful for snapshot versions.
- `build`: builds sources and archives of every project.
- `cleanEclipse`: removes Eclipse project data.
- `cleanIdea`: removes IntelliJ project data.
- `clean`: removes `build` folders, which store compiled classes and built archives.
- `eclipse`: generates Eclipse project data.
- `idea`: generates IntelliJ project data.
- `lwjgl3:jar`: builds application's runnable jar, which can be found at `lwjgl3/build/libs`.
- `lwjgl3:run`: starts the application.
- `test`: runs unit tests (if any).

Note that most tasks that are not specific to a single project can be run with `name:` prefix, where the `name` should be replaced with the ID of a specific project.
For example, `core:clean` removes `build` folder only from the `core` project.


## Game Overview
In AngryHomies, players start with a loading screen before moving to interactive gameplay where they can launch birds to hit structures composed of pigs and blocks.

## Table of Contents

Game Setup
Running the Game
Game Flow
Class Structure
References
 
## Game Setup

To set up and run AngryHomies:

Clone the Repository:
bash
Copy code
git clone <repository_url>
Navigate to the Project Directory:
bash
Copy code
cd AngryHomies
Run the Game Using Gradle:
bash
Copy code
./gradlew lwjgl3:run
Ensure Gradle and Java are installed.


## Running the Game

Main Game Flow
First Run: Starts with a FirstScreen (loading screen) transitioning to the Home Screen.

Home Screen:

Start Game: Opens Level Select.
Load Game: Opens saved games.
Quit Game: Exits.
Start Game:

Level Select Screen lets the player select a level. Currently, redirects to Level One.
Level One: Begins gameplay with blocks, pigs, and birds.

Pause Menu: In Level One, the player can resume, restart, save, or exit.

Step-by-Step Gameplay Flow
New Game:

Select Start Game.
Choose any level (redirects to Level One for now).
In Level One:

Pause Menu:
Resume: Resumes gameplay.
Restart: Reloads and resets elements.
Save Game: Saves state and returns to Home Screen.
Win/Lose Screen:
After game completion, Win/Lose buttons allow return to Home Screen or retry.
Load Game:

Select Load Game from Home Screen.
Choose a saved game to continue (redirects to Level One).
Quit Game: Closes the game.

## Class Structure
The game’s classes manage screens, gameplay components, and user interactions.

Main Class: Manages main game loop and screen transitions.
Screen Interface: Defines lifecycle methods for each screen.
Screens:

FirstScreen (loading screen)
HomeScreen (main menu)
LevelSelectScreen (level choice menu)
WinScreen and LoseScreen (end screens)
SaveSuccessScreen (confirmation for saves)
LoadScreen (saved games)

Level Hierarchy:

Level (Abstract Class) and LevelOne handle game elements, pause menu, and mechanics.
Game Elements:

Bird (subclasses for each type), Block (subclasses), and Pig (subclasses).

Slingshot: Manages launching mechanics.

Utility Classes:
AssetManager: Manages textures, sounds, etc.


Quick Start Guide for AngryHomies
Launching the Game
Run the game with:
bash

Copy code
./gradlew lwjgl3:run

A loading screen appears briefly, then transitions to the Home Screen.
Home Screen
Options:

Start Game: Go to the Level Select screen.

Load Game: Open saved games.
Quit: Exit the game.
Starting a New Game
Select Start Game.
In Level Select, pick any level to proceed to Level One.
Level One - Gameplay
Birds: Three types (Red, Black, Yellow).
Pigs: Three types (Small, Big, King) in a castle.
Blocks: Made of Wood, Stone, and Glass.
Slingshot: Launches birds toward the pigs.
Pause Menu
Resume: Continue gameplay.
Restart: Reset Level One.
Save Game: Save and return to the Level Select Screen.
Exit to Home: Return to the Home Screen.
Win/Lose Screens


Win Button : Win Screen (level cleared ) -> LevelSelecct.
Lose Screen: LooseScreen -> LevelSelectScreen.
Each screen offers options to restart the level or return home.

Loading a Saved Game:

Select Load Game from the Home Screen to resume a previous session in Level One.
Exiting the Game:

Choose Quit from the Home Screen to exit.


Enjoy playing AngryHomies!




##References


OpenAI GPT: Used to assist with  Understanding basic error that we were not aware of .
Google: Searched for UI/UX and coding references.
GitHub: Examined samples for UI design and dynamic gameplay.
YouTube: Tutorials for understanding basic syntax and LibGDX setup(GameFromScratch).


