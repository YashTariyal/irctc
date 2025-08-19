// Dashboard Main JavaScript
class Dashboard {
    constructor() {
        this.data = {
            activities: [],
            apiStats: {},
            alerts: [],
            stats: {
                totalRequests: 0,
                successCount: 0,
                errorCount: 0,
                slowCount: 0,
                avgResponseTime: 0
            }
        };
        
        this.isPaused = false;
        this.maxActivities = 100;
        this.maxAlerts = 50;
        
        this.init();
    }
    
    init() {
        this.bindEvents();
        
        // Optimize: Load data after DOM is ready
        if (document.readyState === 'loading') {
            document.addEventListener('DOMContentLoaded', () => {
                this.loadInitialData();
                this.startDataRefresh();
            });
        } else {
            this.loadInitialData();
            this.startDataRefresh();
        }
    }
    
    bindEvents() {
        // Clear logs button
        document.getElementById('clearLogs').addEventListener('click', () => {
            this.clearActivities();
        });
        
        // Pause logs button
        document.getElementById('pauseLogs').addEventListener('click', () => {
            this.togglePause();
        });
        
        // Search functionality with debouncing
        const searchInput = document.getElementById('searchApi');
        let searchTimeout;
        searchInput.addEventListener('input', (e) => {
            clearTimeout(searchTimeout);
            searchTimeout = setTimeout(() => {
                this.filterApiTable(e.target.value);
            }, 150); // Debounce for 150ms
        });
        
        // Sort functionality
        document.getElementById('sortBy').addEventListener('change', (e) => {
            this.sortApiTable(e.target.value);
        });
        
        // Time range selector
        document.getElementById('timeRange').addEventListener('change', (e) => {
            this.updateCharts(parseInt(e.target.value));
        });
    }
    
    loadInitialData() {
        // Show loading indicator
        this.showLoadingIndicator();
        
        // Load initial data from backend
        Promise.all([
            this.fetchApiStats(),
            this.fetchActivities(),
            this.fetchAlerts()
        ]).then(() => {
            // Hide loading indicator and show dashboard
            this.hideLoadingIndicator();
        }).catch(() => {
            // Hide loading indicator even if there's an error
            this.hideLoadingIndicator();
        });
    }
    
    showLoadingIndicator() {
        const loadingIndicator = document.getElementById('loadingIndicator');
        const dashboardContainer = document.getElementById('dashboardContainer');
        
        if (loadingIndicator) loadingIndicator.style.display = 'flex';
        if (dashboardContainer) dashboardContainer.style.display = 'none';
    }
    
    hideLoadingIndicator() {
        const loadingIndicator = document.getElementById('loadingIndicator');
        const dashboardContainer = document.getElementById('dashboardContainer');
        
        if (loadingIndicator) loadingIndicator.style.display = 'none';
        if (dashboardContainer) dashboardContainer.style.display = 'grid';
    }
    
    startDataRefresh() {
        // Refresh data every 3 seconds for better responsiveness
        setInterval(() => {
            if (!this.isPaused) {
                this.fetchApiStats();
                this.fetchActivities();
                this.fetchAlerts();
            }
        }, 3000);
    }
    
    async fetchApiStats() {
        try {
            const response = await fetch('/api/dashboard/stats');
            if (response.ok) {
                const stats = await response.json();
                this.updateStats(stats);
            }
            return Promise.resolve();
        } catch (error) {
            console.error('Error fetching API stats:', error);
            return Promise.resolve(); // Don't fail the entire load
        }
    }
    
    async fetchActivities() {
        try {
            const response = await fetch('/api/dashboard/activities');
            if (response.ok) {
                const activities = await response.json();
                this.updateActivities(activities);
            }
            return Promise.resolve();
        } catch (error) {
            console.error('Error fetching activities:', error);
            return Promise.resolve(); // Don't fail the entire load
        }
    }
    
    async fetchAlerts() {
        try {
            const response = await fetch('/api/dashboard/alerts');
            if (response.ok) {
                const alerts = await response.json();
                this.updateAlerts(alerts);
            }
            return Promise.resolve();
        } catch (error) {
            console.error('Error fetching alerts:', error);
            return Promise.resolve(); // Don't fail the entire load
        }
    }
    
    updateStats(stats) {
        this.data.stats = stats;
        
        // Update header stats
        document.getElementById('activeApis').textContent = stats.activeApis || 0;
        document.getElementById('avgResponseTime').textContent = `${stats.avgResponseTime || 0}ms`;
        document.getElementById('totalRequests').textContent = stats.totalRequests || 0;
        
        // Update sidebar stats
        document.getElementById('successCount').textContent = stats.successCount || 0;
        document.getElementById('slowCount').textContent = stats.slowCount || 0;
        document.getElementById('errorCount').textContent = stats.errorCount || 0;
    }
    
    updateActivities(activities) {
        if (this.isPaused) return;
        
        // Optimize: Only update if there are new activities
        if (activities.length === 0) return;
        
        // Add new activities to the beginning
        this.data.activities = [...activities, ...this.data.activities].slice(0, this.maxActivities);
        
        // Use requestAnimationFrame for smooth rendering
        requestAnimationFrame(() => {
            this.renderActivities();
            this.updateRecentActivity();
        });
    }
    
    updateAlerts(alerts) {
        this.data.alerts = alerts.slice(0, this.maxAlerts);
        this.renderAlerts();
    }
    
    renderActivities() {
        const container = document.getElementById('activityFeed');
        
        if (this.data.activities.length === 0) {
            container.innerHTML = '<div class="loading">No activities yet...</div>';
            return;
        }
        
        // Optimize: Use DocumentFragment for better performance
        const fragment = document.createDocumentFragment();
        this.data.activities.forEach(activity => {
            const div = document.createElement('div');
            div.innerHTML = this.createActivityHTML(activity);
            fragment.appendChild(div.firstElementChild);
        });
        
        // Clear and append in one operation
        container.innerHTML = '';
        container.appendChild(fragment);
    }
    
    createActivityHTML(activity) {
        const icon = this.getActivityIcon(activity.type);
        const className = this.getActivityClass(activity.type);
        const time = this.formatTime(activity.timestamp);
        
        return `
            <div class="activity-item ${className}">
                <div class="activity-icon">${icon}</div>
                <div class="activity-content">
                    <div class="activity-title">${activity.title}</div>
                    <div class="activity-details">${activity.details}</div>
                </div>
                <div class="activity-time">${time}</div>
            </div>
        `;
    }
    
    getActivityIcon(type) {
        const icons = {
            'api_request': '<i class="fas fa-globe"></i>',
            'api_response': '<i class="fas fa-check-circle"></i>',
            'api_error': '<i class="fas fa-times-circle"></i>',
            'method_start': '<i class="fas fa-play"></i>',
            'method_complete': '<i class="fas fa-check"></i>',
            'method_error': '<i class="fas fa-exclamation-triangle"></i>',
            'slow_operation': '<i class="fas fa-clock"></i>'
        };
        return icons[type] || '<i class="fas fa-info-circle"></i>';
    }
    
    getActivityClass(type) {
        const classes = {
            'api_request': '',
            'api_response': 'success',
            'api_error': 'error',
            'method_start': '',
            'method_complete': 'success',
            'method_error': 'error',
            'slow_operation': 'warning'
        };
        return classes[type] || '';
    }
    
    renderAlerts() {
        const container = document.getElementById('alertsContainer');
        
        if (this.data.alerts.length === 0) {
            container.innerHTML = '<div class="no-alerts">No performance alerts at the moment.</div>';
            return;
        }
        
        container.innerHTML = this.data.alerts.map(alert => this.createAlertHTML(alert)).join('');
    }
    
    createAlertHTML(alert) {
        const icon = alert.type === 'warning' ? 'exclamation-triangle' : 'times-circle';
        const className = alert.type;
        const time = this.formatTime(alert.timestamp);
        
        return `
            <div class="alert-item ${className}">
                <div class="alert-icon">
                    <i class="fas fa-${icon}"></i>
                </div>
                <div class="alert-content">
                    <div class="alert-title">${alert.title}</div>
                    <div class="alert-details">${alert.details}</div>
                    <div class="alert-time">${time}</div>
                </div>
            </div>
        `;
    }
    
    updateRecentActivity() {
        const container = document.getElementById('recentActivity');
        const recentActivities = this.data.activities.slice(0, 5);
        
        if (recentActivities.length === 0) {
            container.innerHTML = '<div class="loading">No recent activity...</div>';
            return;
        }
        
        container.innerHTML = recentActivities.map(activity => this.createMiniActivityHTML(activity)).join('');
    }
    
    createMiniActivityHTML(activity) {
        const icon = this.getActivityIcon(activity.type);
        const time = this.formatTime(activity.timestamp);
        
        return `
            <div class="activity-mini">
                <div class="activity-mini-icon">${icon}</div>
                <div class="activity-mini-text">${activity.title}</div>
                <div class="activity-mini-time">${time}</div>
            </div>
        `;
    }
    
    clearActivities() {
        this.data.activities = [];
        this.renderActivities();
        this.updateRecentActivity();
    }
    
    togglePause() {
        this.isPaused = !this.isPaused;
        const button = document.getElementById('pauseLogs');
        
        if (this.isPaused) {
            button.innerHTML = '<i class="fas fa-play"></i> Resume';
            button.classList.remove('btn-warning');
            button.classList.add('btn-success');
        } else {
            button.innerHTML = '<i class="fas fa-pause"></i> Pause';
            button.classList.remove('btn-success');
            button.classList.add('btn-warning');
        }
    }
    
    filterApiTable(searchTerm) {
        const rows = document.querySelectorAll('#apiTableBody tr');
        const term = searchTerm.toLowerCase();
        
        rows.forEach(row => {
            const text = row.textContent.toLowerCase();
            row.style.display = text.includes(term) ? '' : 'none';
        });
    }
    
    sortApiTable(sortBy) {
        // Implementation for sorting API table
        console.log('Sorting by:', sortBy);
    }
    
    updateCharts(timeRange) {
        // Trigger chart updates
        if (window.chartsManager) {
            window.chartsManager.updateCharts(timeRange);
        }
    }
    
    formatTime(timestamp) {
        const date = new Date(timestamp);
        const now = new Date();
        const diff = now - date;
        
        if (diff < 60000) { // Less than 1 minute
            return 'Just now';
        } else if (diff < 3600000) { // Less than 1 hour
            const minutes = Math.floor(diff / 60000);
            return `${minutes}m ago`;
        } else if (diff < 86400000) { // Less than 1 day
            const hours = Math.floor(diff / 3600000);
            return `${hours}h ago`;
        } else {
            return date.toLocaleDateString();
        }
    }
    
    // WebSocket event handlers
    handleWebSocketMessage(data) {
        try {
            const message = JSON.parse(data);
            
            switch (message.type) {
                case 'activity':
                    this.addActivity(message.data);
                    break;
                case 'stats':
                    this.updateStats(message.data);
                    break;
                case 'alert':
                    this.addAlert(message.data);
                    break;
            }
        } catch (error) {
            console.error('Error handling WebSocket message:', error);
        }
    }
    
    addActivity(activity) {
        if (this.isPaused) return;
        
        this.data.activities.unshift(activity);
        this.data.activities = this.data.activities.slice(0, this.maxActivities);
        
        this.renderActivities();
        this.updateRecentActivity();
    }
    
    addAlert(alert) {
        this.data.alerts.unshift(alert);
        this.data.alerts = this.data.alerts.slice(0, this.maxAlerts);
        
        this.renderAlerts();
    }
}

// Initialize dashboard when DOM is loaded
document.addEventListener('DOMContentLoaded', () => {
    window.dashboard = new Dashboard();
});
