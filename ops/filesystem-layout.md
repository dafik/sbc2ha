# Filesystem layout

> Production paths for sbc2ha on a BeagleBone Black (or similar SBC).

## Paths

| Purpose | Path | Owner | Rationale |
|---------|------|-------|-----------|
| Config | `/etc/sbc2ha/config.yaml` | `root:sbc2ha` 0640 | Standard Linux config dir; group-readable for runtime user |
| State | `/var/lib/sbc2ha/state.json` | `sbc2ha:sbc2ha` 0600 | Persistent app data; atomic writes prevent corruption |
| HA cache | `/var/lib/sbc2ha/ha-discovery-cache.json` | `sbc2ha:sbc2ha` 0600 | Planned later (Phase 2); co-located with state for now |
| Logs | `/var/log/sbc2ha/sbc2ha.log` | `sbc2ha:adm` 0640 | Standard log dir; group-readable for log collectors |

## Defaults in code

Main.java uses these defaults when no system properties are set:

- Config: mandatory CLI argument (`args[0]`)
- State: `/var/lib/sbc2ha/state.json` (via `-Dsbc2ha.state`)
- Logs: `/var/log/sbc2ha/sbc2ha.log` (via logback configuration)

All paths can be overridden via JVM system properties:

```bash
java -Dsbc2ha.state=/tmp/state.json -jar sbc2ha.jar /etc/sbc2ha/config.yaml
```

## systemd service

See `sbc2ha.service` for the full unit file.

Key points:

- Runs as dedicated `sbc2ha:sbc2ha` user (no root).
- `ProtectSystem=strict` — read-only filesystem except explicitly allowed paths.
- `ReadWritePaths=/var/lib/sbc2ha /var/log/sbc2ha` — state and logs are the only writable dirs.
- `Restart=on-failure` with 5s backoff.
- `NoNewPrivileges=true` — cannot escalate.

## Directory setup

```bash
# Create runtime user (if not already present)
sudo useradd --system --no-create-home --shell /usr/sbin/nologin sbc2ha

# Create directories
sudo mkdir -p /etc/sbc2ha
sudo mkdir -p /var/lib/sbc2ha
sudo mkdir -p /var/log/sbc2ha

# Set ownership
sudo chown root:sbc2ha /etc/sbc2ha
sudo chmod 0640 /etc/sbc2ha   # only group can read

sudo chown sbc2ha:sbc2ha /var/lib/sbc2ha
sudo chmod 0750 /var/lib/sbc2ha

sudo chown sbc2ha:adm /var/log/sbc2ha
sudo chmod 0750 /var/log/sbc2ha

# Deploy config
sudo cp config.yaml /etc/sbc2ha/config.yaml
sudo chown root:sbc2ha /etc/sbc2ha/config.yaml
sudo chmod 0640 /etc/sbc2ha/config.yaml

# Enable and start
sudo systemctl daemon-reload
sudo systemctl enable --now sbc2ha
```
