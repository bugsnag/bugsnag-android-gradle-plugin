import { AppRegistry } from 'react-native';
import App from './App';
import { Client } from 'bugsnag-react-native';
const bugsnag = new Client();

AppRegistry.registerComponent('rnapp', () => App);
