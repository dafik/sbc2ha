export class UrlHelper {
  static getApiUrl() {
    return UrlHelper.getAppUrl() + 'api/'
  }

  static getWsUrl() {
    return UrlHelper.getAppUrl().replace('http', 'ws') + 'ws/'
  }

  static getAppUrl() {
    const protocol = window.location.protocol
    const host = window.location.host;
    const pathname = window.location.pathname;

    return `${protocol}//${host}${pathname}`
  }
}
