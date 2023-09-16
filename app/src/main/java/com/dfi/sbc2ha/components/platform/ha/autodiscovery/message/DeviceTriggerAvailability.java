package com.dfi.sbc2ha.components.platform.ha.autodiscovery.message;

import com.dfi.sbc2ha.components.platform.ha.autodiscovery.HaDeviceType;
import com.dfi.sbc2ha.components.platform.ha.autodiscovery.SbcDeviceType;
import com.dfi.sbc2ha.services.state.sensor.ButtonState;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.util.List;

@Getter
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties({"uniqueId", "stateTopic", "name", "availability"})
public class DeviceTriggerAvailability extends IterableAvailability {

    //<discovery_prefix>/device_automation/[<node_id>/]<object_id>/config

    /**
     * The type of automation, must be ‘trigger’
     * REQUIRED
     */
    @JsonProperty("automation_type")
    final String automationType = "trigger";

    /**
     * The MQTT topic subscribed to receive trigger events.
     * REQUIRED
     */
    final String topic;
    /**
     * The subtype of the trigger, e.g. button_1.
     * Entries supported by the frontend:
     * * turn_on,
     * * turn_off,
     * * button_1,
     * * button_2,
     * * button_3,
     * * button_4,
     * * button_5,
     * * button_6.
     * If set to an unsupported value, will render as subtype type,
     * e.g. left_button pressed with type set to button_short_press and subtype set to left_button
     * REQUIRED
     */
    final String subtype;
    @JsonIgnore()
    private final List<ButtonState> states;
    /**
     * The type of the trigger, e.g. button_short_press.
     * Entries supported by the frontend:
     * * button_short_press,
     * * button_short_release,
     * * button_long_press,
     * * button_long_release,
     * * button_double_press,
     * * button_triple_press,
     * * button_quadruple_press,
     * * button_quintuple_press.
     * If set to an unsupported value, will render as subtype type, e.g. button_1 spammed with type set to spammed and subtype set to button_1
     * REQUIRED
     */
    String type;
    /**
     * Optional payload to match the payload being sent over the topic.
     * optional
     */
    String payload;
    /**
     * The maximum QoS level to be used when receiving messages.
     * (optional, default: 0)
     */
    @JsonIgnore()
    int qos;
    /**
     * Defines a template to extract the value.
     * template (optional)
     */
    @JsonProperty("value_template")
    String valueTemplate;

    public DeviceTriggerAvailability(String id, String name, List<ButtonState> states) {
        super(id, name, HaDeviceType.DEVICE_AUTOMATION, SbcDeviceType.INPUT);

        this.topic = formatTopic(Availability.topicPrefix, getStateDeviceTypeName(), id);


        subtype = name;
        this.states = states;
        setUniqueId(null);
        setStateTopic(null);
        setName(null);
        setAvailability(null);
    }

    @Override
    public String getStateTopic() {
        return topic;
    }

    @JsonIgnore
    public String getName() {
        return subtype;
    }

    @Override
    public String getNodeName() {
        return super.getNodeName() + "_" + type;
    }


    @Override
    public IterableAvailability iterator() {
        payload = null;
        return this;
    }

    @Override
    public boolean hasNext() {
        if (states.size() == 0) {
            return false;
        }
        if (payload == null) {
            return true;
        } else {
            ButtonState state = ButtonState.valueOf(payload.toUpperCase());
            return states.indexOf(state) < states.size() - 1;
        }
    }

    @Override
    public IterableAvailability next() {
        ButtonState state;
        if (payload == null) {
            state = states.get(0);
        } else {
            state = ButtonState.valueOf(payload.toUpperCase());
            state = states.get(states.indexOf(state) + 1);
        }
        switch (state) {
            default:
            case SINGLE:
                type = "button_short_press";
                break;
            case DOUBLE:
                type = "button_double_press";
                break;
            case LONG:
                type = "button_long_press";
                break;
            case RELEASE:
                type = "button_long_release";
                break;
        }
        payload = state.toString().toLowerCase();
        return this;
    }
}
