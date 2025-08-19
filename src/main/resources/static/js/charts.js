// Charts Management
class ChartsManager {
    constructor() {
        this.charts = {};
        this.data = {
            responseTimeData: [],
            requestVolumeData: [],
            apiPerformanceData: []
        };
        
        this.init();
    }
    
    init() {
        this.createResponseTimeChart();
        this.createRequestVolumeChart();
        this.loadChartData();
        this.startDataRefresh();
    }
    
    createResponseTimeChart() {
        const ctx = document.getElementById('responseTimeChart').getContext('2d');
        
        this.charts.responseTime = new Chart(ctx, {
            type: 'line',
            data: {
                labels: [],
                datasets: [{
                    label: 'Average Response Time (ms)',
                    data: [],
                    borderColor: '#667eea',
                    backgroundColor: 'rgba(102, 126, 234, 0.1)',
                    borderWidth: 2,
                    fill: true,
                    tension: 0.4
                }, {
                    label: 'Max Response Time (ms)',
                    data: [],
                    borderColor: '#e74c3c',
                    backgroundColor: 'rgba(231, 76, 60, 0.1)',
                    borderWidth: 2,
                    fill: false,
                    tension: 0.4
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    legend: {
                        position: 'top',
                    },
                    title: {
                        display: true,
                        text: 'Response Time Trends'
                    }
                },
                scales: {
                    y: {
                        beginAtZero: true,
                        title: {
                            display: true,
                            text: 'Response Time (ms)'
                        }
                    },
                    x: {
                        title: {
                            display: true,
                            text: 'Time'
                        }
                    }
                },
                interaction: {
                    intersect: false,
                    mode: 'index'
                }
            }
        });
    }
    
    createRequestVolumeChart() {
        const ctx = document.getElementById('requestVolumeChart').getContext('2d');
        
        this.charts.requestVolume = new Chart(ctx, {
            type: 'bar',
            data: {
                labels: [],
                datasets: [{
                    label: 'Requests per Minute',
                    data: [],
                    backgroundColor: [
                        'rgba(102, 126, 234, 0.8)',
                        'rgba(118, 75, 162, 0.8)',
                        'rgba(39, 174, 96, 0.8)',
                        'rgba(243, 156, 18, 0.8)',
                        'rgba(231, 76, 60, 0.8)'
                    ],
                    borderColor: [
                        'rgba(102, 126, 234, 1)',
                        'rgba(118, 75, 162, 1)',
                        'rgba(39, 174, 96, 1)',
                        'rgba(243, 156, 18, 1)',
                        'rgba(231, 76, 60, 1)'
                    ],
                    borderWidth: 1
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    legend: {
                        position: 'top',
                    },
                    title: {
                        display: true,
                        text: 'Request Volume'
                    }
                },
                scales: {
                    y: {
                        beginAtZero: true,
                        title: {
                            display: true,
                            text: 'Number of Requests'
                        }
                    },
                    x: {
                        title: {
                            display: true,
                            text: 'API Endpoints'
                        }
                    }
                }
            }
        });
    }
    
    async loadChartData() {
        try {
            const response = await fetch('/api/dashboard/chart-data');
            if (response.ok) {
                const data = await response.json();
                this.updateChartData(data);
            }
        } catch (error) {
            console.error('Error loading chart data:', error);
        }
    }
    
    startDataRefresh() {
        // Refresh chart data every 5 seconds for better responsiveness
        setInterval(() => {
            this.loadChartData();
        }, 5000);
    }
    
    updateChartData(data) {
        this.data = data;
        
        // Use requestAnimationFrame for smooth chart updates
        requestAnimationFrame(() => {
            this.updateResponseTimeChart();
            this.updateRequestVolumeChart();
            this.updateTopApis();
        });
    }
    
    updateResponseTimeChart() {
        const chart = this.charts.responseTime;
        const responseTimeData = this.data.responseTimeData || [];
        
        if (responseTimeData.length > 0) {
            chart.data.labels = responseTimeData.map(item => item.time);
            chart.data.datasets[0].data = responseTimeData.map(item => item.avgTime);
            chart.data.datasets[1].data = responseTimeData.map(item => item.maxTime);
            chart.update();
        }
    }
    
    updateRequestVolumeChart() {
        const chart = this.charts.requestVolume;
        const requestVolumeData = this.data.requestVolumeData || [];
        
        if (requestVolumeData.length > 0) {
            chart.data.labels = requestVolumeData.map(item => item.endpoint);
            chart.data.datasets[0].data = requestVolumeData.map(item => item.count);
            chart.update();
        }
    }
    
    updateTopApis() {
        const container = document.getElementById('topApis');
        const topApis = this.data.apiPerformanceData || [];
        
        if (topApis.length === 0) {
            container.innerHTML = '<div class="loading">No API data available...</div>';
            return;
        }
        
        const top5Apis = topApis.slice(0, 5);
        container.innerHTML = top5Apis.map(api => `
            <div class="api-item">
                <div class="api-name">${api.endpoint}</div>
                <div class="api-time">${api.avgTime}ms</div>
            </div>
        `).join('');
    }
    
    updateCharts(timeRange) {
        // Update charts based on time range
        this.loadChartDataForTimeRange(timeRange);
    }
    
    async loadChartDataForTimeRange(timeRange) {
        try {
            const response = await fetch(`/api/dashboard/chart-data?timeRange=${timeRange}`);
            if (response.ok) {
                const data = await response.json();
                this.updateChartData(data);
            }
        } catch (error) {
            console.error('Error loading chart data for time range:', error);
        }
    }
    
    // WebSocket event handlers for real-time chart updates
    handleWebSocketMessage(data) {
        try {
            const message = JSON.parse(data);
            
            if (message.type === 'chart_update') {
                this.updateChartData(message.data);
            }
        } catch (error) {
            console.error('Error handling chart WebSocket message:', error);
        }
    }
}

// Initialize charts when DOM is loaded
document.addEventListener('DOMContentLoaded', () => {
    window.chartsManager = new ChartsManager();
});
