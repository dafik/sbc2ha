package com.dalsemi.onewire.container;

import java.util.HashMap;
import java.util.Map;

public enum Family {
    F01((byte) 0x01),
    F02((byte) 0x02),
    F04((byte) 0x04),
    F05((byte) 0x05),
    F06((byte) 0x06),
    F08((byte) 0x08),
    F09((byte) 0x09),
    F0A((byte) 0x0A),
    F0B((byte) 0x0B),
    F0C((byte) 0x0C),
    F0F((byte) 0x0F),
    F10((byte) 0x10),
    F12((byte) 0x12),
    F13((byte) 0x13),
    F14((byte) 0x14),
    F18((byte) 0x18),
    F1A((byte) 0x1A),
    F1C((byte) 0x1C),
    F1D((byte) 0x1D),
    F1F((byte) 0x1F),
    F20((byte) 0x20),
    F21((byte) 0x21),
    F22((byte) 0x22),
    F23((byte) 0x23),
    F24((byte) 0x24),
    F26((byte) 0x26),
    F27((byte) 0x27),
    F28((byte) 0x28),
    F29((byte) 0x29),
    F2C((byte) 0x2C),
    F2D((byte) 0x2D),
    F30((byte) 0x30),
    F33((byte) 0x33),
    F37((byte) 0x37),
    F3A((byte) 0x3A),
    F41((byte) 0x41),
    F42((byte) 0x42),
    F43((byte) 0x43),
    F53((byte) 0x53);

    private static final Map<Byte, Family> lookup = new HashMap<>();

    static {
        for (Family family : Family.values()) {
            lookup.put(family.familyByte, family);
        }
    }

    public final byte familyByte;

    Family(byte familyByte) {
        this.familyByte = familyByte;
    }

    public static Family ofValue(byte family) {
        return lookup.get(family);
    }
    public Class<? extends OneWireContainer> getContainerClass() {
        return getClass(this);
    }

    public static Class<? extends OneWireContainer> getClass(Family family) {
        switch (family) {
            case F01:
                return OneWireContainer01.class;
            case F02:
                return OneWireContainer02.class;
            case F04:
                return OneWireContainer04.class;
            case F05:
                return OneWireContainer05.class;
            case F06:
                return OneWireContainer06.class;
            case F08:
                return OneWireContainer08.class;
            case F09:
                return OneWireContainer09.class;
            case F0A:
                return OneWireContainer0A.class;
            case F0B:
                return OneWireContainer0B.class;
            case F0C:
                return OneWireContainer0C.class;
            case F0F:
                return OneWireContainer0F.class;
            case F10:
                return OneWireContainer10.class;
            case F12:
                return OneWireContainer12.class;
            case F13:
                return OneWireContainer13.class;
            case F14:
                return OneWireContainer14.class;
            case F18:
                return OneWireContainer18.class;
            case F1A:
                return OneWireContainer1A.class;
            case F1C:
                return OneWireContainer1C.class;
            case F1D:
                return OneWireContainer1D.class;
            case F1F:
                return OneWireContainer1F.class;
            case F20:
                return OneWireContainer20.class;
            case F21:
                return OneWireContainer21.class;
            case F22:
                return OneWireContainer22.class;
            case F23:
                return OneWireContainer23.class;
            case F24:
                return OneWireContainer24.class;
            case F26:
                return OneWireContainer26.class;
            case F27:
                return OneWireContainer27.class;
            case F28:
                return OneWireContainer28.class;
            case F29:
                return OneWireContainer29.class;
            case F2C:
                return OneWireContainer2C.class;
            case F2D:
                return OneWireContainer2D.class;
            case F30:
                return OneWireContainer30.class;
            case F33:
                return OneWireContainer33.class;
            case F37:
                return OneWireContainer37.class;
            case F3A:
                return OneWireContainer3A.class;
            case F41:
                return OneWireContainer41.class;
            case F42:
                return OneWireContainer42.class;
            case F43:
                return OneWireContainer43.class;
            case F53:
                return OneWireContainer53.class;
            default:
                return OneWireContainerGeneric.class;
        }

    }


    public <T extends OneWireContainer> Class<T> getContainerClass3() {
        return null;
    }
}
