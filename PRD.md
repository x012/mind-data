# Product Requirements Document: Mind Data

## 1. Executive Summary

Mind Data is a native Android mind mapping application that enables users to create, visualize, and interact with graph-based mind maps. Users construct networks of labeled nodes connected by styled edges, navigate the graph via multi-touch gestures, and persist maps locally in SQLite databases. The app targets Android 2.3.3+ (API 10) with zero external dependencies — using only Android SDK built-in APIs.

---

## 2. Product Overview

### 2.1 Product Vision

Provide a lightweight, responsive mind mapping tool for low-end Android devices that requires no network access, no account creation, and no third-party libraries. Data is owned entirely by the user on their device.

### 2.2 Target Audience

- Students and professionals who need quick, local mind mapping on older Android devices
- Users who prefer local-only, privacy-preserving tools
- Developers and tinkerers exploring force-directed graph visualization on constrained hardware

### 2.3 Platform Requirements

| Requirement | Value |
|---|---|
| Platform | Android |
| Min SDK | 10 (Gingerbread 2.3.3) |
| Target / Compile SDK | 10 / 18 (Jelly Bean 4.3) |
| Build System | Gradle 4.8 / AGP 3.6.1 |
| Permissions | `android.permission.VIBRATE` |
| Orientation | Portrait only |

---

## 3. Functional Requirements

### 3.1 Mind Map Index (Launcher)

| ID | Requirement | Priority |
|---|---|---|
| F1 | Display a scrollable list of all saved mind maps by title | P0 |
| F2 | Create a new blank mind map with an auto-generated name | P0 |
| F3 | Long-press a map entry to show a context menu with Delete and Edit Title options | P0 |
| F4 | Tap a map entry to open it in the graph editor | P0 |
| F5 | Rename a map via a dialog activity | P1 |

### 3.2 Graph Canvas (Editor)

| ID | Requirement | Priority |
|---|---|---|
| F6 | Render a full-screen black canvas with a randomly generated dark background color | P0 |
| F7 | Display all nodes as rounded rectangles with their label text | P0 |
| F8 | Display all edges as lines connecting their source and target nodes | P0 |
| F9 | Apply a force-directed physics simulation continuously (~30 FPS) | P0 |
| F10 | Highlight the currently selected node with a distinct border color | P0 |

### 3.3 Node Management

| ID | Requirement | Priority |
|---|---|---|
| F11 | Create a new node via the toolbar + button | P0 |
| F12 | New nodes auto-connect to the currently selected node (if any), else appear at center | P0 |
| F13 | Edit a node's label text via an inline EditText that appears on the pencil button | P0 |
| F14 | Delete a node and all its connected edges via the X toolbar button | P0 |
| F15 | Assign each node a random color derived from its label's hash code | P1 |
| F16 | Toggle a highlighted visual state on a node | P1 |

### 3.4 Edge Management

| ID | Requirement | Priority |
|---|---|---|
| F17 | Connect two nodes by tapping the Connect button, then tapping each node | P0 |
| F18 | Tapping two already-connected nodes in connect mode removes the edge | P0 |
| F19 | Cycle through 5 visual edge styles via the Arrow toolbar button | P1 |
| F20 | Edge styles: Plain (neutral gray), GreenV / RedV (colored arrowhead), GreenX / RedX (colored dashed X-head) | P1 |
| F21 | Long-press the Arrow button to invert edge direction | P2 |

### 3.5 Touch Gestures

| ID | Requirement | Priority |
|---|---|---|
| F22 | Tap to select / deselect the nearest node (40px hit radius) | P0 |
| F23 | Drag (single finger) to pan the canvas | P0 |
| F24 | Pinch (two fingers) to zoom in/out | P0 |
| F25 | Fling (fast swipe) for momentum-based inertial scrolling | P1 |
| F26 | No rotation allowed during pinch | P2 |

### 3.6 Physics Simulation

| ID | Requirement | Priority |
|---|---|---|
| F27 | Spring forces pull connected nodes toward a target distance of 35px | P0 |
| F28 | All nodes repel each other within a 250px radius | P0 |
| F29 | Centralizing gravity pulls distant nodes gently back toward center | P1 |
| F30 | Friction and air resistance decay velocities for natural settling | P0 |
| F31 | Simulation runs at approximately 30 FPS via Handler postDelayed | P0 |

### 3.7 Persistence

| ID | Requirement | Priority |
|---|---|---|
| F32 | Each mind map is persisted as a separate SQLite database file | P0 |
| F33 | The nodes table stores label, position (cx, cy), velocity (vx, vy), and highlighted state | P0 |
| F34 | The edges table stores source node, target node, and edge type | P0 |
| F35 | A global index database stores all map titles | P0 |
| F36 | Maps load their last-saved state on open | P0 |
| F37 | Camera zoom/pan state can be persisted (meta_data table) | P2 |

### 3.8 UI Components

| ID | Requirement | Priority |
|---|---|---|
| F38 | Toolbar with buttons for New, Edit, Delete, Connect, Highlight, Arrow | P0 |
| F39 | Inline EditText appears when editing a node label; dismissed on IME back-key or Done | P0 |
| F40 | Black no-title theme applied to all activities | P1 |

---

## 4. Non-Functional Requirements

| ID | Requirement | Target |
|---|---|---|
| N1 | Render target | 30 FPS minimum on API 10 devices |
| N2 | No external dependencies | Zero third-party libraries at runtime |
| N3 | Offline operation | 100% functional without network |
| N4 | Data privacy | All data stored locally; no network calls |
| N5 | Max map size | Support at least 200 nodes without perceptible lag |
| N6 | App size | Minimize APK size (no external resources) |

---

## 5. User Flows

### 5.1 Create and Explore a New Mind Map

1. User launches app → sees Index screen with empty list
2. User taps Menu → "New mind map" → map created with auto-generated name
3. User taps the new entry → opens editor on a blank canvas
4. User taps + button → a node appears at center with a random label
5. User taps the node → it becomes selected (highlighted border)
6. User taps + again → a second node appears, connected to the first
7. User drags nodes → physics simulation adjusts layout
8. User pinches to zoom out for overview
9. User taps back → returns to Index (map auto-saved)

### 5.2 Edit Node Label

1. User selects a node
2. User taps pencil button → EditText appears with current label
3. User types new label → taps Done or back-key
4. EditText dismisses, node displays new label

### 5.3 Connect Nodes

1. User selects a node
2. User taps Connect button → enters connection mode
3. User taps a second node → edge created
4. User taps Connect again to exit connection mode

### 5.4 Delete a Mind Map

1. User long-presses a map in the Index list
2. Context menu appears with "Delete" option
3. User taps Delete → map database deleted, entry removed from index

---

## 6. Architecture

### 6.1 Layers

```
Index Layer        IndexActivity ←→ IndexDbAdapter (index.db)
                         ↓
Editor Layer     MindDataActivity ←→ GraphDatabase (graphdata<id>.db)
                         ↓
Rendering Layer     GraphView (Canvas renderer + physics tick)
                         ↓
Sub-components     GestureAndTransformationHandler
                   EdgeRelaxer / Repulsor implementations
                   EdgePainter strategy hierarchy
```

### 6.2 Data Model

- **Node:** id, label, cx, cy, vx, vy, highlighted (boolean), color
- **Edge:** id, fromNode, toNode, edgeType (int 0–4 mapping to 5 painters)
- **MindMap (Index):** id (int), title (String)

### 6.3 Persistence Schema

**index.db / index_table:**
```
_id (INTEGER PRIMARY KEY), title (TEXT)
```

**graphdata<id>.db / nodes:**
```
_id (INTEGER PRIMARY KEY), data (BLOB)
```
BLOB serialization: label (UTF), cx (float), cy (float), vx (float), vy (float), highlighted (boolean).

**graphdata<id>.db / edges:**
```
_id (INTEGER PRIMARY KEY), from_id (INTEGER), to_id (INTEGER), data (BLOB)
```
BLOB serialization: edgeType (int).

**graphdata<id>.db / meta_data:**
```
data (BLOB)
```

---

## 7. Edge Style Definitions

| Style | ID | Description |
|---|---|---|
| Plain | 0 | Neutral gray line, no arrowhead |
| GreenV | 1 | Green line with V-shaped arrowhead |
| RedV | 2 | Red line with V-shaped arrowhead |
| GreenX | 3 | Green dashed line with X-shaped head |
| RedX | 4 | Red dashed line with X-shaped head |

---

## 8. Open Issues / Future Considerations

| Issue | Description |
|---|---|
| Bitmap caching | Node bitmap caching is implemented as a no-op; rendering performance could improve with proper caching |
| Camera persistence | Camera matrix (zoom/pan) is saved to meta_data but never loaded — restored on session resume |
| Edge type popup | A popup window for edge type selection is initialized but never shown |
| Undo/Redo | No undo functionality; deletions are permanent |
| Export/Share | No mechanism to export maps as images or share them |
| Accessibility | No content descriptions, no TalkBack support |
| Orientation | Locked to portrait; no landscape support |
| Configuration changes | Uses deprecated `onRetainNonConfigurationInstance()` instead of ViewModel/saved instance state |
| Modernization | Requires updates to target SDK>=31 for Play Store compliance, plus AndroidX migration |
| Testing | Zero automated tests across the entire codebase |

---

## 9. Glossary

| Term | Definition |
|---|---|
| Node | A labeled rounded rectangle representing a single idea/concept |
| Edge | A styled line connecting two nodes representing a relationship |
| Force-directed layout | A physics simulation that positions nodes so that connected ones attract and all nodes repel |
| Relaxer | Component that applies spring forces along edges |
| Repulsor | Component that applies repulsion forces between all node pairs |
| Edge painter | Strategy implementation that defines how an edge is visually drawn |
| Index | The launcher screen listing all saved mind maps |