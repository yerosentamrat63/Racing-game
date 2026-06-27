# City Taxi 3D — a Java 3D taxi game

A 3D taxi driving game built in **pure Java** using the open-source **jMonkeyEngine 3** engine and real CC0 3D asset packs from Kenney (City Kit Roads, City Kit Suburban, Car Kit, Blocky Characters).

You spawn in the city and just **drive around**. Passengers wait on sidewalks (marked with a yellow ring) — drive into one to pick them up automatically, then a green ring appears at their random destination. Drive to the green ring, stop, deliver, and keep driving to find the next passenger. There's **no timer, no game over** — open-ended driving gameplay.

## Features

- True 3D world rendered with LWJGL 3 / OpenGL
- **Real CC0 3D asset packs** (not built from primitives):
  - 4 road tile types from Kenney City Kit - Roads
  - 21 building models from Kenney City Kit - Suburban
  - 6 car models from Kenney Car Kit (taxi, sedan, suv, van, police, hatchback)
  - 5 character models from Kenney Blocky Characters (used as passengers)
- Procedurally-built city on a regular road grid with proper intersections
- Driveable taxi with arcade-style physics (acceleration, braking, drag, steering, collisions)
- **Open-ended driving gameplay** - no timer, no game-over
- Pickup + drop-off loop: drive into yellow ring to pick up, drive to green ring to deliver
- 6 AI obstacle cars wandering the road grid
- Smooth third-person chase camera
- Title menu, instructions screen, in-game HUD, pause
- Score tracking: fare earned, trips completed, crash count, time alive

## Controls

| Key              | Action                |
|------------------|-----------------------|
| `W` / `Up`       | Accelerate            |
| `S` / `Down`     | Brake / Reverse       |
| `A` / `Left`     | Steer left            |
| `D` / `Right`    | Steer right           |
| `R`              | Reset position        |
| `P`              | Pause / resume        |
| `Enter`          | Confirm / start game  |
| `I`              | Show instructions     |
| `Esc`            | Pause / back to menu  |

## How the gameplay works

1. **Spawn** at a road intersection in the city.
2. **Drive around** exploring - WASD or arrow keys.
3. Look for **yellow rings** on sidewalks - those mark waiting passengers.
4. Drive close to a yellow ring (within ~3.5m) to **auto-pickup** the passenger. You earn $25.
5. A **green ring** appears somewhere else in the city - that's their destination.
6. Drive to the green ring and **slow down** (under 5 m/s) to drop them off. You earn $75.
7. A new passenger spawns somewhere else - **keep driving** and find them.
8. Avoid hitting other cars - each collision costs $10.
9. There's **no time limit and no game-over** - drive as long as you want, deliver as many passengers as you want.

## Requirements

- **JDK 17 or newer** (tested with OpenJDK 21)
- **Maven 3.6+**
- A desktop with OpenGL 3.3+ support (any PC made after ~2010 works)

## Build & Run

```bash
cd taxi-game
mvn package
java -jar target/city-taxi-3d.jar
```

(Or just `./run.sh` which does both.)

Audio is **disabled by default** so the game runs everywhere (including VMs
without a sound card). If you add sound effects later, re-enable audio with:

```bash
java -Dtaxigame.audio=true -jar target/city-taxi-3d.jar
```

## Project Layout

```
taxi-game/
├── pom.xml                       Maven build + jMonkeyEngine deps
├── README.md                     this file
├── run.sh                        convenience build+run script
└── src/main/
    ├── java/com/taxigame/
    │   ├── Main.java             entry point - boots the engine
    │   ├── TaxiGame.java         state machine, menu, HUD, game loop
    │   ├── City.java             procedural city using real road + building GLB assets
    │   ├── Taxi.java             player taxi - loads real taxi.glb model
    │   ├── Passenger.java        passenger - loads real character GLB + yellow ring
    │   ├── Obstacle.java         AI obstacle car - loads real car GLB
    │   └── tools/
    │       ├── GameTest.java     headless integration test
    │       ├── InspectModels.java  prints GLB asset bounding boxes
    │       └── ScreenshotTool.java renders the scene to PNG
    └── resources/Models/
        ├── Roads/                road-straight/crossroad/curve/intersection.glb
        ├── Buildings/            21 building-type-*.glb
        ├── Cars/                 taxi/sedan/suv/van/police/hatchback-sports.glb
        └── Passengers/           character-a..e.glb
```

## Tech Stack

- **Java 17+**
- **jMonkeyEngine 3.6.1** — open-source Java 3D game engine
- **LWJGL 3** — desktop OpenGL / input backend
- **Maven Shade Plugin** — produces a single runnable fat-jar (14 MB)

## Asset Licenses

All 3D models and textures are from [Kenney.nl](https://kenney.nl) and are
licensed under the **Creative Commons Zero (CC0 1.0)** license - effectively
public domain. You can use them for any purpose, including commercial.

- [City Kit - Roads](https://kenney.nl/assets/city-kit-roads)
- [City Kit - Suburban](https://kenney.nl/assets/city-kit-suburban)
- [Car Kit](https://kenney.nl/assets/car-kit)
- [Blocky Characters](https://kenney.nl/assets/blocky-characters)

## Code License

MIT-style - do whatever you want, just don't blame me.
