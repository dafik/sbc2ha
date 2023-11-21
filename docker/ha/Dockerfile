FROM eclipse-temurin:17-jre

RUN apt-get update && apt-get install -y --no-install-recommends \
    ca-certificates telnet iputils-ping curl netbase net-tools iproute2 wget jq \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /data
RUN mkdir /app
COPY sbc2ha.jar  /app
COPY sbc.yaml ./config.yaml
COPY run.sh /


CMD /run.sh

