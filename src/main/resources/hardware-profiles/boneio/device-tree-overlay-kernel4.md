# Device Tree Overlay for BBB (Kernel 4.x)

## Overview

On **kernel 4.x**, the BBB uses **cape-universal** which handles pin muxing automatically.
**No custom device tree overlay is needed.** Cape-universal is loaded by default from
`uEnv.txt` and exposes ALL pins as GPIO regardless of their default function.

## Kernel 4.x Setup

### uEnv.txt Configuration

On the BBB's FAT partition (`/boot/uEnv.txt`), ensure these lines are present:

```
enable_uboot_overlays=1
uboot_overlay_addr0=/lib/firmware/BB-BONE-UNIV-00A0.dtbo
```

The `BB-BONE-UNIV-00A0.dtbo` is cape-universal. It comes pre-installed in
BeagleBoard.org Debian 9 (Stretch) and Debian 10 (Buster) images.

### Verification

After boot, verify cape-universal is loaded:

```bash
dmesg | grep -i cape
# Expected: "gpio-of-helper ocp:cape-universal: ready"
```

### GPIO Chip Assignments (kernel 4.x)

With cape-universal, the gpiochip assignments are:

| gpiochip | GPIO bank | Pins served |
|----------|-----------|-------------|
| 0        | GPIO0     | Pins in GPIO0 bank |
| 1        | GPIO1     | Pins in GPIO1 bank |
| 2        | GPIO2     | Pins in GPIO2 bank |
| 3        | GPIO3     | Pins in GPIO3 bank |

Each pin's line offset matches the GPIO bank offset (e.g., GPIO0_14 → chip0:line14).

This is why diozero's `ti_am335x-bone.txt` board definitions work correctly on kernel 4.x
— they were written against this exact chip/line assignment scheme.

## Kernel 6.x Setup (comparison)

On **kernel 6.x**, cape-universal does **not** exist. Instead, the boneIO boards use the
**BONEIO-BLACK-PINS** device tree overlay:

```
uboot_overlay_addr0=BONEIO-BLACK-PINS.dtbo
```

This overlay is built from source at `github.com/boneIO-eu/black-pins-overlay` and is
pre-installed in boneIO's Debian 13 (Trixie) images.

**Important differences on kernel 6.x:**
- gpiochip numbers CHANGE (e.g., P8_37 goes from chip0 to chip1)
- Line offsets also change for some pins
- Diozero's board definitions are WRONG for kernel 6.x
- Greenfield app uses `PinModeOverrides` + `PinInfoView` to inject overrides at runtime

See `bbb-chip-mappings.txt` for the kernel 6.x chip+line override map.

## Config-pin (legacy, kernel 4.x only)

On kernel 4.x, `/usr/bin/config-pin` was available and used by the old app to set pin modes
at runtime (e.g., `config-pin P8.37 gpio_pu`). The greenfield app does NOT use config-pin —
it only sets PinModeOverrides which controls the pull-up/pull-down mask via the gpiod library.
