import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

export default defineConfig({
  plugins: [react()],
  server: {
    host: '0.0.0.0',
    port: 5173,
    watch: {
      usePolling: true,   // wichtig: Bind-Mounts über die Podman-VM auf Windows senden oft keine nativen Filesystem-Events
      interval: 300,
    },
    hmr: {
      clientPort: 5173,   // stellt sicher, dass der Browser die WebSocket-Verbindung über den richtigen (gemappten) Port aufbaut
    },
  },
})