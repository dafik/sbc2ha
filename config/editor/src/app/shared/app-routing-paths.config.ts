import {RoutingPath} from "./routing-path.type";


export const appRoutingPaths: Record<string, RoutingPath> = {
  home: {
    label: 'Home',
    path: '',
    key: 'home',
  },

  states: {
    label: 'Device states',
    path: 'device',
    key: 'states',
  },

  settings: {
    label: 'Settings',
    path: 'settings',
    key: 'settings',
  },

  logs: {
    label: 'Logs',
    path: 'logs',
    key: 'logs',
  },
  config: {
    label: 'Config',
    path: 'config',
    key: 'config home',
  },
  creator: {
    label: 'Config creator',
    path: 'editor/creator',
    key: 'creator',
  },
  editor: {
    label: 'Config editor',
    path: 'editor',
    key: 'editor',
  },
};

export const editorRoutingPaths = {
  creator: {
    label: 'Config creator',
    path: 'creator',
    key: 'creator',

  } as RoutingPath,
};

