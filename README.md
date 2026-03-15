# Hyprv 🕹️ — Velocity Packet Logger

> A low-level Velocity plugin that intercepts packets and logs player activity in real time. Built for use with the [Hyprx](https://github.com/zPleum/Hyprx) toolkit.

<h1 align="center">
  <img src="https://i.postimg.cc/4ywzHV6J/Hyprv.png" alt="Hyprv" style="width:30%; max-width:400px;"/>
</h1>

<p align="center">
  <img src="https://img.shields.io/badge/Java-21+-orange?style=for-the-badge&logo=openjdk"/>
  <img src="https://img.shields.io/badge/Velocity-3.4.0--SNAPSHOT-blue?style=for-the-badge"/>
  <img src="https://img.shields.io/badge/License-GPLv3-green?style=for-the-badge"/>
  <img src="https://img.shields.io/badge/Build-Gradle-lightgrey?style=for-the-badge&logo=gradle"/>
</p>

---

## What is Hyprv?

Hyprv hooks directly into Velocity's internal packet registry using reflection, allowing it to intercept raw Minecraft packets **before** they are processed by the proxy. It logs connection data, player activity, and commands — all transparently, without modifying gameplay.

---

## Features

| Feature | Description |
|---------|-------------|
| Packet Hooking | Intercepts raw packets via `StateRegistry` reflection |
| Handshake Hook | Rewrites server address to redirect traffic to backend |
| Login Hook | Captures usernames from `ServerLoginPacket` before backend |
| Player Logging | Logs join IP, port, chat, and commands via event listeners |

---

## Logged Data

| Event | Captured |
|-------|----------|
| Player Join | Username · IP address · Port |
| Player Chat | Username · Message content |
| Command | Username · Full command string |
| Login Packet | Username (raw packet level, pre-auth) |

---

## Requirements

- **Java** 21+
- **Velocity** `3.4.0-SNAPSHOT-503` — place jar in `libs/`
- **Gradle** (included wrapper)

---

## Installation

```bash
# 1. Clone the repo
git clone https://github.com/zPleum/Hyprv.git
cd Hyprv

# 2. Build
./gradlew build

# 3. Copy jar to Velocity plugins folder
cp build/libs/hyprv-1.0.jar /path/to/velocity/plugins/

# 4. Restart Velocity
```

---

## Project Structure

```
src/main/java/me/zpleum/hyprv/
├── Hyprv.java              — Plugin entry point, registers hooks & listeners
├── GenericListener.java    — Handles PostLoginEvent, PlayerChatEvent, CommandExecuteEvent
└── hook/
    ├── PacketHook.java     — Interface defining hook contract
    ├── HandshakeHook.java  — Intercepts & rewrites handshake packets
    └── LoginHook.java      — Logs usernames from raw login packets
```

---

## How It Works

Velocity stores packet mappings in `StateRegistry.PacketRegistry.ProtocolRegistry` as two private fields:

- `packetIdToSupplier` — maps packet ID → constructor
- `packetClassToId` — maps class → packet ID

Hyprv uses `MethodHandles.privateLookupIn` to access these fields at startup, then **replaces** the original packet suppliers with custom hook classes. Every packet of that type now passes through the hook before being handled by Velocity.

```
Client → [Hyprv Hook] → Velocity → Backend
              ↓
           Log / Modify
```

---

## Dependencies

| Dependency | Version | Source |
|------------|---------|--------|
| `velocity` | `3.4.0-SNAPSHOT-503` | Local `libs/` |
| `velocity-api` | `3.4.0-SNAPSHOT` | PaperMC Maven (annotation processor) |
| `io.netty` | bundled | Velocity |
| `it.unimi.dsi.fastutil` | bundled | Velocity |

---

## Notes

- Uses **Velocity internal APIs** — may break on Velocity version updates
- `PACKET_HOOKS` must be initialized as `new ArrayList<>()` before adding hooks
- `PostLoginEvent` is used instead of `LoginEvent` — `LoginEvent` does not expose `getRemoteAddress()`

---

## License

Based on [Plantain](https://github.com/x3fication/Plantain) by x5ten.
Distributed under the **GNU General Public License v3.0** — free to use and modify, provided derivatives carry the same license and credits.

---

> **Disclaimer** — This plugin is intended for use in environments where you have explicit permission to monitor traffic. The developer is not responsible for misuse or damage caused by this software.