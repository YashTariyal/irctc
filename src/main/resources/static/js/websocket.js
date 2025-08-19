// WebSocket Management for Real-time Updates
class WebSocketManager {
    constructor() {
        this.socket = null;
        this.reconnectAttempts = 0;
        this.maxReconnectAttempts = 5;
        this.reconnectDelay = 1000; // Start with 1 second
        this.isConnected = false;
        
        this.init();
    }
    
    init() {
        this.connect();
        this.updateConnectionStatus();
    }
    
    connect() {
        try {
            // Create WebSocket connection
            const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:';
            const wsUrl = `${protocol}//${window.location.host}/ws/dashboard`;
            
            this.socket = new WebSocket(wsUrl);
            
            this.socket.onopen = () => {
                console.log('WebSocket connected');
                this.isConnected = true;
                this.reconnectAttempts = 0;
                this.reconnectDelay = 1000;
                this.updateConnectionStatus();
            };
            
            this.socket.onmessage = (event) => {
                this.handleMessage(event.data);
            };
            
            this.socket.onclose = (event) => {
                console.log('WebSocket disconnected:', event.code, event.reason);
                this.isConnected = false;
                this.updateConnectionStatus();
                
                if (!event.wasClean && this.reconnectAttempts < this.maxReconnectAttempts) {
                    this.scheduleReconnect();
                }
            };
            
            this.socket.onerror = (error) => {
                console.error('WebSocket error:', error);
                this.isConnected = false;
                this.updateConnectionStatus();
            };
            
        } catch (error) {
            console.error('Error creating WebSocket connection:', error);
            this.scheduleReconnect();
        }
    }
    
    scheduleReconnect() {
        this.reconnectAttempts++;
        const delay = this.reconnectDelay * Math.pow(2, this.reconnectAttempts - 1); // Exponential backoff
        
        console.log(`Scheduling WebSocket reconnect attempt ${this.reconnectAttempts} in ${delay}ms`);
        
        setTimeout(() => {
            if (this.reconnectAttempts < this.maxReconnectAttempts) {
                this.connect();
            } else {
                console.error('Max WebSocket reconnect attempts reached');
                this.updateConnectionStatus('Max reconnection attempts reached');
            }
        }, delay);
    }
    
    handleMessage(data) {
        try {
            const message = JSON.parse(data);
            
            // Route message to appropriate handlers
            if (window.dashboard) {
                window.dashboard.handleWebSocketMessage(data);
            }
            
            if (window.chartsManager) {
                window.chartsManager.handleWebSocketMessage(data);
            }
            
        } catch (error) {
            console.error('Error handling WebSocket message:', error);
        }
    }
    
    sendMessage(message) {
        if (this.socket && this.socket.readyState === WebSocket.OPEN) {
            try {
                this.socket.send(JSON.stringify(message));
            } catch (error) {
                console.error('Error sending WebSocket message:', error);
            }
        } else {
            console.warn('WebSocket is not connected, cannot send message');
        }
    }
    
    updateConnectionStatus(message = null) {
        const statusElement = document.getElementById('connectionStatus');
        
        if (!statusElement) return;
        
        if (this.isConnected) {
            statusElement.className = 'connection-status connected';
            statusElement.innerHTML = '<i class="fas fa-wifi"></i><span>Connected</span>';
        } else {
            statusElement.className = 'connection-status disconnected';
            const statusText = message || 'Disconnected';
            statusElement.innerHTML = `<i class="fas fa-wifi-slash"></i><span>${statusText}</span>`;
        }
    }
    
    disconnect() {
        if (this.socket) {
            this.socket.close();
        }
    }
    
    // Utility methods for sending specific types of messages
    subscribeToActivity() {
        this.sendMessage({
            type: 'subscribe',
            channel: 'activity'
        });
    }
    
    subscribeToStats() {
        this.sendMessage({
            type: 'subscribe',
            channel: 'stats'
        });
    }
    
    subscribeToAlerts() {
        this.sendMessage({
            type: 'subscribe',
            channel: 'alerts'
        });
    }
    
    subscribeToCharts() {
        this.sendMessage({
            type: 'subscribe',
            channel: 'charts'
        });
    }
    
    // Subscribe to all channels
    subscribeToAll() {
        this.subscribeToActivity();
        this.subscribeToStats();
        this.subscribeToAlerts();
        this.subscribeToCharts();
    }
}

// Initialize WebSocket when DOM is loaded
document.addEventListener('DOMContentLoaded', () => {
    window.webSocketManager = new WebSocketManager();
    
    // Subscribe to all channels once connected
    setTimeout(() => {
        if (window.webSocketManager && window.webSocketManager.isConnected) {
            window.webSocketManager.subscribeToAll();
        }
    }, 1000);
});

// Clean up WebSocket on page unload
window.addEventListener('beforeunload', () => {
    if (window.webSocketManager) {
        window.webSocketManager.disconnect();
    }
});
