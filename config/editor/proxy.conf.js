const winston = require('winston'); //(1)

function logProvider() { //(2)
  return winston.createLogger({
    level: 'debug',
    format: winston.format.combine(
        winston.format.splat(),
        winston.format.simple()
    ),
    transports: [new winston.transports.Console()],
  });
}
const PROXY_CONF = {
  "/api": {
    target: "http://localhost:8080",
    secure: false
  },
  "/ws": {
    target: "ws://localhost:8080",
    secure: false,
    ws: true,
    logLevel: 'debug',
    logProvider: logProvider,
  },
};

module.exports = PROXY_CONF;
