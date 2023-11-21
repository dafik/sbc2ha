PIGPIOD_HOST=$(jq -r .pigpio_host /data/options.json)
export PIGPIOD_HOST

java -Ddiozero.devicefactory=com.diozero.internal.provider.pigpioj.PigpioJDeviceFactory  -DusePwmFadingLed=true -jar /app/sbc2ha.jar /data/config.yaml
