/**
 * Gantt Chart Core Module
 * React 마이그레이션을 위한 기반 모듈
 *
 * @version 1.0.0
 * @author Claude AI
 */

class GanttChartCore {
    constructor(options = {}) {
        this.options = {
            container: null,
            projectData: null,
            tasksData: [],
            apiUrl: '/api/v1',
            ...options
        };

        this.state = {
            filteredTasks: [],
            currentView: 'basic', // 'basic' | 'd3'
            filters: {
                search: '',
                status: '',
                progress: '',
                dateFrom: '',
                dateTo: ''
            }
        };

        this.events = new EventTarget();
    }

    // 이벤트 시스템
    on(event, handler) {
        this.events.addEventListener(event, handler);
    }

    emit(event, data) {
        this.events.dispatchEvent(new CustomEvent(event, { detail: data }));
    }

    // 상태 관리
    setState(newState) {
        this.state = { ...this.state, ...newState };
        this.emit('stateChange', this.state);
        this.render();
    }

    getState() {
        return { ...this.state };
    }

    // 데이터 관리
    async loadProjectData(projectId) {
        try {
            const response = await fetch(`${this.options.apiUrl}/projects/${projectId}`);
            const projectData = await response.json();

            const tasksResponse = await fetch(`${this.options.apiUrl}/projects/${projectId}/tasks`);
            const tasksData = await tasksResponse.json();

            this.options.projectData = projectData;
            this.options.tasksData = tasksData;
            this.state.filteredTasks = [...tasksData];

            this.emit('dataLoaded', { projectData, tasksData });
            return { projectData, tasksData };
        } catch (error) {
            this.emit('error', { type: 'dataLoad', error });
            throw error;
        }
    }

    // 필터링
    applyFilters(filters) {
        this.setState({ filters });

        const filteredTasks = this.options.tasksData.filter(task => {
            // 텍스트 검색
            if (filters.search && !task.name.toLowerCase().includes(filters.search.toLowerCase())) {
                return false;
            }

            // 상태 필터
            if (filters.status && task.status !== filters.status) {
                return false;
            }

            // 진행률 필터
            if (filters.progress) {
                const progress = task.progress || 0;
                if (!this.matchProgressFilter(progress, filters.progress)) {
                    return false;
                }
            }

            // 날짜 필터
            if (filters.dateFrom) {
                const taskStart = new Date(task.startDate);
                const filterFrom = new Date(filters.dateFrom);
                if (taskStart < filterFrom) return false;
            }

            if (filters.dateTo) {
                const taskEnd = new Date(task.endDate);
                const filterTo = new Date(filters.dateTo);
                if (taskEnd > filterTo) return false;
            }

            return true;
        });

        this.setState({ filteredTasks });
        this.emit('filtered', { filters, filteredTasks });
    }

    matchProgressFilter(progress, filterValue) {
        switch(filterValue) {
            case '0': return progress === 0;
            case '1-25': return progress >= 1 && progress <= 25;
            case '26-50': return progress >= 26 && progress <= 50;
            case '51-75': return progress >= 51 && progress <= 75;
            case '76-99': return progress >= 76 && progress <= 99;
            case '100': return progress === 100;
            default: return true;
        }
    }

    // 통계 계산
    calculateStatistics(tasks = null) {
        const data = tasks || this.state.filteredTasks;

        if (!data || data.length === 0) {
            return {
                totalTasks: 0,
                completedTasks: 0,
                inProgressTasks: 0,
                notStartedTasks: 0,
                overallProgress: 0
            };
        }

        const totalTasks = data.length;
        const completedTasks = data.filter(t => t.status === 'COMPLETED').length;
        const inProgressTasks = data.filter(t => t.status === 'IN_PROGRESS').length;
        const notStartedTasks = data.filter(t => t.status === 'NOT_STARTED').length;

        const totalProgress = data.reduce((sum, task) => sum + (task.progress || 0), 0);
        const overallProgress = totalTasks > 0 ? Math.round(totalProgress / totalTasks) : 0;

        return {
            totalTasks,
            completedTasks,
            inProgressTasks,
            notStartedTasks,
            overallProgress,
            completedPercent: totalTasks > 0 ? (completedTasks / totalTasks) * 100 : 0,
            inProgressPercent: totalTasks > 0 ? (inProgressTasks / totalTasks) * 100 : 0
        };
    }

    // 뷰 전환
    switchView(viewType) {
        this.setState({ currentView: viewType });
        this.emit('viewChanged', { view: viewType });
    }

    // 렌더링 (React에서 오버라이드 예정)
    render() {
        // 기본 구현 - React에서 이 메서드를 오버라이드할 예정
        console.log('GanttChartCore: render() called', this.state);
    }

    // 초기화
    async init(containerId, projectId) {
        this.options.container = document.getElementById(containerId);

        if (!this.options.container) {
            throw new Error(`Container with id "${containerId}" not found`);
        }

        if (projectId) {
            await this.loadProjectData(projectId);
        }

        this.render();
        this.emit('initialized', this.getState());
    }

    // 정리
    destroy() {
        this.emit('destroyed');
        // 이벤트 리스너 정리
        this.events = new EventTarget();
    }
}

// Utils 모듈
class GanttUtils {
    static formatDate(date, format = 'yyyy-MM-dd') {
        const d = new Date(date);
        const year = d.getFullYear();
        const month = String(d.getMonth() + 1).padStart(2, '0');
        const day = String(d.getDate()).padStart(2, '0');

        return format
            .replace('yyyy', year)
            .replace('MM', month)
            .replace('dd', day);
    }

    static calculateDuration(startDate, endDate) {
        const start = new Date(startDate);
        const end = new Date(endDate);
        return Math.ceil((end - start) / (1000 * 60 * 60 * 24)) + 1;
    }

    static getStatusColor(status) {
        const colors = {
            'NOT_STARTED': '#6c757d',
            'IN_PROGRESS': '#17a2b8',
            'COMPLETED': '#28a745',
            'ON_HOLD': '#ffc107',
            'CANCELLED': '#dc3545'
        };
        return colors[status] || '#6c757d';
    }

    static getStatusLabel(status) {
        const labels = {
            'NOT_STARTED': '시작 전',
            'IN_PROGRESS': '진행중',
            'COMPLETED': '완료',
            'ON_HOLD': '보류',
            'CANCELLED': '취소'
        };
        return labels[status] || '알 수 없음';
    }

    static debounce(func, wait) {
        let timeout;
        return function executedFunction(...args) {
            const later = () => {
                clearTimeout(timeout);
                func(...args);
            };
            clearTimeout(timeout);
            timeout = setTimeout(later, wait);
        };
    }

    static throttle(func, limit) {
        let inThrottle;
        return function(...args) {
            if (!inThrottle) {
                func.apply(this, args);
                inThrottle = true;
                setTimeout(() => inThrottle = false, limit);
            }
        };
    }
}

// React 컴포넌트를 위한 Hook 스타일 API
class GanttHooks {
    static useGanttState(ganttCore) {
        return {
            state: ganttCore.getState(),
            setState: ganttCore.setState.bind(ganttCore),
            statistics: ganttCore.calculateStatistics()
        };
    }

    static useGanttFilters(ganttCore) {
        return {
            filters: ganttCore.state.filters,
            applyFilters: ganttCore.applyFilters.bind(ganttCore),
            filteredTasks: ganttCore.state.filteredTasks
        };
    }

    static useGanttData(ganttCore) {
        return {
            projectData: ganttCore.options.projectData,
            tasksData: ganttCore.options.tasksData,
            loadData: ganttCore.loadProjectData.bind(ganttCore)
        };
    }
}

// Export for React migration
if (typeof module !== 'undefined' && module.exports) {
    module.exports = { GanttChartCore, GanttUtils, GanttHooks };
} else {
    window.GanttChartCore = GanttChartCore;
    window.GanttUtils = GanttUtils;
    window.GanttHooks = GanttHooks;
}