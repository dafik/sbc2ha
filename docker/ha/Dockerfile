FROM eclipse-temurin:17-jre

RUN apt-get update && apt-get install -y --no-install-recommends \
    ca-certificates telnet iputils-ping curl netbase net-tools iproute2 wget \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /app

COPY sbc2ha.jar  ./
COPY sbc.yaml ./config.yaml

CMD java -Ddiozero.devicefactory=com.diozero.internal.provider.pigpioj.PigpioJDeviceFactory  -DusePwmFadingLed=true -jar sbc2ha.jar /app/config.yaml

