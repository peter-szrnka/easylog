/**
 * @author Peter Szrnka
 */
export const environment = {
  apiUrl: `http://localhost:${import.meta.env.PORT || 8080}/api`,
  webSocketUrl: `ws://localhost:${import.meta.env.PORT || 8080}/topic/logs`
};
