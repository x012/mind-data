# Mind Data

Simple mind mapping application for Android. Create, visualize, and edit interactive force-directed graphs with labeled nodes and edges.

<img src="./screenshots/Screenshot_20200302-145552.png?raw=true" width="250">
<img src="./screenshots/Screenshot_20200302-152650.png?raw=true" width="250">

## Features

- **Force-directed graph layout** — nodes repel each other, edges act as springs, central gravity pulls the graph together with friction damping
- **Multi-touch gestures** — tap to select, drag to pan, pinch to zoom
- **Node CRUD** — add nodes (with auto-generated random labels), edit labels inline, delete nodes with cascading edge deletion
- **Edge creation** — toggle connect mode, tap source then target to create edges; tapping an existing pair removes the edge
- **5 edge visual styles** — plain, green V, red V, green X, red X (cycled via the arrow button)
- **Edge direction** — long-press the arrow button to flip connection direction
- **Node highlighting** — toggle highlight color on selected nodes
- **Persistence** — each mind map stored in its own SQLite database; node and edge data serialized as BLOBs
- **Mind map index** — list of saved maps with create, rename, and delete operations
- **Haptic feedback** — vibration on long-press actions
- **Custom fonts** — 7 bundled TTF fonts (charm, diablo, primitive, Roboto, under, yuma)

## Build

```bash
./gradlew assembleDebug
```

## Technical

| | |
|---|---|
| **Package** | `com.angel.mind` |
| **Min SDK** | 10 (Android 2.3.3 Gingerbread) |
| **Target SDK** | 10 |
| **Compile SDK** | 18 (Jelly Bean 4.3) |
| **Build tools** | 29.0.3 |
| **Dependencies** | None — pure Android SDK |

The graph engine runs a physics simulation loop at ~30 FPS via `Handler`. Nodes are rendered as rounded rectangles with text. Edges are drawn with configurable arrowhead styles using a strategy pattern (`EdgePainter` subclasses). Multi-touch is handled custom (no gesture detector library) with GC-efficient `Matrix` pooling.

## Architecture

- `IndexActivity` — launcher listing saved mind maps from an SQLite index
- `MindDataActivity` — main graph editor with a bottom toolbar
- `GraphView` — custom `View` managing rendering, physics, gestures, and graph commands
- `GraphDatabase` — per-graph SQLite persistence layer
- `Node` / `Edge` — data models for graph elements
- `EdgePainter` + `Plain`, `GreenV`, `RedV`, `GreenX`, `RedX` — strategy pattern for edge rendering
- `FastRepulsor` / `NormalRepulsor` — node repulsion physics
- `EdgeRelaxer` / `VisibleEdgeRelaxer` / `CenterEdgeRelaxer` — edge spring physics

## License

This is an open source project. See the source files for license information.