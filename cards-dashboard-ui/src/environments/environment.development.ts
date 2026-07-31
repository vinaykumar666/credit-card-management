export const environment = {
  production: false,
  // Same-origin via Angular proxy (proxy.conf.json → localhost:8086) so CORS cannot drop headers.
  bffUrl: '',
  authUrl: 'http://localhost:8081',
  channelId: 'WEB',
  clientId: 'cards-dashboard-ui',
};
