# Hyprv 🕹️ - A velocity log plugin for the Hyprx tool

Hyprv is a velocity plugin that logs player activity and connection data using low-level packet hooks.

<h1 align="center">
  <img src="https://i.postimg.cc/4ywzHV6J/Hyprv.png" alt="Header Image" style="width:30%; max-width:600px;"/>
</h1>

---

## 📜 License Notice

**Hyprv** is a modified version of the original project **Plantain**.

- **Original Project:** Plantain
- **Original Repository:** [https://github.com/x3fication/Plantain](https://github.com/x3fication/Plantain)

This project continues to be distributed under the **GNU General Public License v3.0**. 
In accordance with the GPLv3 and the original author's request, this software remains free to use and modify, provided that all derivatives also carry the same license and proper credits.

---

## ⚙️ Features

- **Packet Hooking** — Intercepts raw Minecraft packets via Velocity's internal `StateRegistry` using reflection and `MethodHandles`
- **Handshake Hook** — Rewrites the server address in handshake packets to redirect connections to a registered backend server
- **Login Hook** — Intercepts `ServerLoginPacket` to log usernames before they reach the backend
- **Player Logging** — Logs player join with IP address and port, chat messages, and commands
- **Event Listeners** — Listens to `PostLoginEvent`, `PlayerChatEvent`, and `CommandExecuteEvent`

---

## 🛠️ Requirements

- Java 21+
- Velocity `3.4.0-SNAPSHOT-503` (placed in `libs/`)
- Gradle

---

## 📂 Project Structure

```
src/main/java/me/zpleum/hyprv/
├── Hyprv.java                  # Main plugin class, registers hooks and listeners
├── GenericListener.java        # Event listeners for login, chat, commands
└── hook/
    ├── PacketHook.java         # Interface for packet hooks
    ├── HandshakeHook.java      # Rewrites handshake server address
    └── LoginHook.java          # Intercepts login packets
```

---

## 🔨 Build

```bash
./gradlew build
```

Output jar will be in `build/libs/`.

---

## 📦 Installation

1. Build the plugin
2. Place the jar in your Velocity `plugins/` folder
3. Restart Velocity

---

## ⚡ How It Works

### Packet Hooks

On proxy initialization, the plugin uses `MethodHandles.privateLookupIn` to access private fields inside `StateRegistry.PacketRegistry.ProtocolRegistry`:

- `packetIdToSupplier` — maps packet ID → packet supplier
- `packetClassToId` — maps packet class → packet ID

Each `PacketHook` replaces the original packet supplier with a custom one, allowing the plugin to intercept and modify packets before they are processed.

### HandshakeHook

Extends `HandshakePacket`. On `encode`, rewrites `serverAddress` and `port` to point to the first registered backend server.

### LoginHook

Extends `ServerLoginPacket`. On `decode`, logs the connecting player's username from the raw login packet.

---

## 📊 Logged Data

| Event | Data Logged |
|-------|-------------|
| Player Join | Username, IP address, port |
| Player Chat | Username, message |
| Command | Username, command string |
| Login Packet | Username (raw packet level) |

---

## 📚 Dependencies

| Dependency | Source |
|-----------|--------|
| `velocity-3.4.0-SNAPSHOT-503` | Local `libs/` folder |
| `velocity-api:3.4.0-SNAPSHOT` | Annotation processor via PaperMC Maven |
| `io.netty` | Bundled with Velocity |
| `it.unimi.dsi.fastutil` | Bundled with Velocity |

---

## 📝 Notes

- This plugin uses Velocity **internal APIs** (`com.velocitypowered.proxy.*`) which are not part of the public API and may break across Velocity versions
- `PACKET_HOOKS` must be initialized as `new ArrayList<>()` before hooks are added
- `PostLoginEvent` is used instead of `LoginEvent` for player IP access since `LoginEvent` does not expose a `Player` object with `getRemoteAddress()`

### ⚠️ Disclaimer

**This tool is for educational and ethical testing purposes only.** The developer of **Hyprv** is not responsible for any misuse, damage, or illegal activities caused by this software. Use it at your own risk. By using this tool, you agree to comply with your local laws and regulations regarding cybersecurity and network testing.

This tool is intended strictly for security research and testing environments where you have permission.

This project is based on Plantain by x5ten.
Modifications and improvements were made in Hyprv.
