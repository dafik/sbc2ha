# Running sbc2ha without systemd

> Quick-start commands for running sbc2ha directly from the command line
> (dev workstation, ad-hoc BBB test, or manual systemd troubleshooting).

## Prerequisites

- JDK 25+
- On BBB: root/sudo access for GPIO (diozero requires it)

## Build

```bash
cd /home/zw/dev/sbc2ha
mvn clean package -DskipTests
```

Produces: `target/sbc2ha-0.0.1-SNAPSHOT.jar` (classes-only, no fat jar).

## Run — dev workstation (no hardware)

The app loads a YAML config and attempts hardware wiring. On a dev box
diozero will fail to find BBB GPIO pins, which is handled gracefully
(logged as warnings) — switches/inputs still initialise as empty.

```bash
# Minimal: just load config, no hardware
java \
  -Dsbc2ha.state=/tmp/sbc2ha-state.json \
  -Dsbc2ha.logdir=/tmp/sbc2ha-log \
  -cp target/sbc2ha-0.0.1-SNAPSHOT.jar \
  iot.sbc2ha.Main \
  .plan/config/bone1-converted.yaml
```

> **Config path** is a mandatory `args[0]` — no fallback, no system property.

## Run — BeagleBone (real hardware)

On the BBB the app needs root for `/dev/gpiomem` and `/dev/i2c-*`.

```bash
# Use production config path
sudo java \
  -Dsbc2ha.state=/var/lib/sbc2ha/state.json \
  -Dsbc2ha.logdir=/var/log/sbc2ha \
  -Djava.awt.headless=true \
  -cp target/sbc2ha-0.0.1-SNAPSHOT.jar \
  iot.sbc2ha.Main \
  /etc/sbc2ha/config.yaml
```

## Run as non-root user (partial hardware access)

If GPIO isn't available but i2c/devmem are exposed via udev rules:

```bash
sudo -u sbc2ha java \
  -Dsbc2ha.state=/var/lib/sbc2ha/state.json \
  -Dsbc2ha.logdir=/var/log/sbc2ha \
  -Djava.awt.headless=true \
  -cp target/sbc2ha-0.0.1-SNAPSHOT.jar \
  iot.sbc2ha.Main \
  /etc/sbc2ha/config.yaml
```

## Logging

Logback is configured via `src/main/resources/logback.xml`.
Override the log directory with:

```bash
java -Dsbc2ha.logdir=/tmp/mylog ...
```

If no system property is set, logs default to `/var/log/sbc2ha/sbc2ha.log`.

## State persistence

Default state file: `/var/lib/sbc2ha/state.json`
Override: `-Dsbc2ha.state=/path/to/state.json`

State is atomically written (temp file + rename) — survives crashes.

## Full lifecycle visible in logs

```
BOOTING → CONFIG_LOADED → STATE_RESTORED → HARDWARE_MINIMAL_READY → OFFLINE_READY
```

If hardware wiring fails, the app still reaches `OFFLINE_READY`
(gpio adapter errors are logged as warnings, not fatal).
