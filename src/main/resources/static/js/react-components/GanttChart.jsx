/**
 * React Gantt Chart Component
 * 향후 React 마이그레이션 시 사용할 컴포넌트 예시
 */

import React, { useState, useEffect, useRef } from 'react';
import { GanttChartCore, GanttUtils } from '../gantt-core.js';

// Main Gantt Chart Component
const GanttChart = ({ projectId, apiUrl }) => {
    const ganttRef = useRef(null);
    const [ganttCore, setGanttCore] = useState(null);
    const [state, setState] = useState({
        projectData: null,
        tasksData: [],
        filteredTasks: [],
        currentView: 'basic',
        filters: {
            search: '',
            status: '',
            progress: '',
            dateFrom: '',
            dateTo: ''
        },
        statistics: {
            totalTasks: 0,
            completedTasks: 0,
            inProgressTasks: 0,
            overallProgress: 0
        }
    });

    // Initialize Gantt Core
    useEffect(() => {
        const core = new GanttChartCore({
            apiUrl: apiUrl || '/api/v1'
        });

        // Event listeners
        core.on('stateChange', (event) => {
            setState(prevState => ({
                ...prevState,
                ...event.detail
            }));
        });

        core.on('dataLoaded', (event) => {
            const { projectData, tasksData } = event.detail;
            setState(prevState => ({
                ...prevState,
                projectData,
                tasksData,
                filteredTasks: tasksData,
                statistics: core.calculateStatistics(tasksData)
            }));
        });

        core.on('filtered', (event) => {
            const { filteredTasks } = event.detail;
            setState(prevState => ({
                ...prevState,
                filteredTasks,
                statistics: core.calculateStatistics(filteredTasks)
            }));
        });

        setGanttCore(core);

        return () => {
            core.destroy();
        };
    }, [apiUrl]);

    // Load project data
    useEffect(() => {
        if (ganttCore && projectId) {
            ganttCore.loadProjectData(projectId);
        }
    }, [ganttCore, projectId]);

    // Filter handlers
    const handleFilterChange = (filterType, value) => {
        const newFilters = {
            ...state.filters,
            [filterType]: value
        };

        if (ganttCore) {
            ganttCore.applyFilters(newFilters);
        }
    };

    const clearFilters = () => {
        const emptyFilters = {
            search: '',
            status: '',
            progress: '',
            dateFrom: '',
            dateTo: ''
        };

        if (ganttCore) {
            ganttCore.applyFilters(emptyFilters);
        }
    };

    const switchView = (viewType) => {
        if (ganttCore) {
            ganttCore.switchView(viewType);
        }
    };

    return (
        <div className="gantt-chart-container">
            {/* Header */}
            <GanttHeader
                projectData={state.projectData}
                onPrint={() => window.print()}
            />

            {/* Statistics */}
            <GanttStatistics statistics={state.statistics} />

            {/* Controls */}
            <GanttControls
                currentView={state.currentView}
                filters={state.filters}
                onViewChange={switchView}
                onFilterChange={handleFilterChange}
                onClearFilters={clearFilters}
            />

            {/* Chart */}
            <div ref={ganttRef}>
                {state.currentView === 'basic' ? (
                    <BasicGanttChart
                        projectData={state.projectData}
                        tasks={state.filteredTasks}
                    />
                ) : (
                    <D3GanttChart
                        projectData={state.projectData}
                        tasks={state.filteredTasks}
                    />
                )}
            </div>
        </div>
    );
};

// Header Component
const GanttHeader = ({ projectData, onPrint }) => (
    <div className="d-flex justify-content-between align-items-center mb-4">
        <h1 className="h3 mb-0">
            <i className="fas fa-chart-gantt me-2"></i>
            간트차트: {projectData?.name || '로딩중...'}
        </h1>
        <div>
            <a href={`/web/projects/${projectData?.id}`} className="btn btn-outline-primary">
                <i className="fas fa-arrow-left me-2"></i>프로젝트로 돌아가기
            </a>
            <button className="btn btn-outline-secondary ms-2" onClick={onPrint}>
                <i className="fas fa-print me-2"></i>인쇄
            </button>
        </div>
    </div>
);

// Statistics Component
const GanttStatistics = ({ statistics }) => (
    <div className="stats-card mb-4">
        <div className="row">
            <div className="col-md-3 text-center">
                <div className="h4 mb-1">
                    <i className="fas fa-tasks me-2 text-light"></i>
                    {statistics.totalTasks}
                </div>
                <div className="small opacity-75">전체 태스크</div>
                <div className="progress mt-2" style={{ height: '4px' }}>
                    <div className="progress-bar bg-light" style={{ width: '100%' }}></div>
                </div>
            </div>
            <div className="col-md-3 text-center">
                <div className="h4 mb-1">
                    <i className="fas fa-check-circle me-2 text-success"></i>
                    {statistics.completedTasks}
                </div>
                <div className="small opacity-75">완료됨</div>
                <div className="progress mt-2" style={{ height: '4px' }}>
                    <div
                        className="progress-bar bg-success"
                        style={{ width: `${statistics.completedPercent}%` }}
                    ></div>
                </div>
            </div>
            <div className="col-md-3 text-center">
                <div className="h4 mb-1">
                    <i className="fas fa-play-circle me-2 text-info"></i>
                    {statistics.inProgressTasks}
                </div>
                <div className="small opacity-75">진행중</div>
                <div className="progress mt-2" style={{ height: '4px' }}>
                    <div
                        className="progress-bar bg-info"
                        style={{ width: `${statistics.inProgressPercent}%` }}
                    ></div>
                </div>
            </div>
            <div className="col-md-3 text-center">
                <div className="h4 mb-1">
                    <i className="fas fa-chart-line me-2 text-warning"></i>
                    {statistics.overallProgress}%
                </div>
                <div className="small opacity-75">전체 진행률</div>
                <div className="progress mt-2" style={{ height: '6px' }}>
                    <div
                        className="progress-bar bg-gradient progress-bar-striped progress-bar-animated"
                        style={{ width: `${statistics.overallProgress}%` }}
                    ></div>
                </div>
            </div>
        </div>
    </div>
);

// Controls Component
const GanttControls = ({
    currentView,
    filters,
    onViewChange,
    onFilterChange,
    onClearFilters
}) => {
    const [showFilters, setShowFilters] = useState(false);

    return (
        <div className="card mb-3">
            <div className="card-header">
                <div className="row align-items-center">
                    <div className="col-md-6">
                        <h6 className="mb-0">간트차트 컨트롤</h6>
                    </div>
                    <div className="col-md-6">
                        <div className="d-flex justify-content-end gap-2">
                            {/* View Toggle */}
                            <div className="btn-group">
                                <button
                                    className={`btn btn-outline-primary btn-sm ${currentView === 'basic' ? 'active' : ''}`}
                                    onClick={() => onViewChange('basic')}
                                >
                                    <i className="fas fa-table me-1"></i>기본 보기
                                </button>
                                <button
                                    className={`btn btn-outline-primary btn-sm ${currentView === 'd3' ? 'active' : ''}`}
                                    onClick={() => onViewChange('d3')}
                                >
                                    <i className="fas fa-chart-bar me-1"></i>고급 시각화
                                </button>
                            </div>

                            {/* Filter Toggle */}
                            <button
                                className="btn btn-outline-secondary btn-sm"
                                onClick={() => setShowFilters(!showFilters)}
                            >
                                <i className="fas fa-filter me-1"></i>필터
                            </button>
                        </div>
                    </div>
                </div>
            </div>

            {/* Filter Panel */}
            {showFilters && (
                <div className="card-body border-top">
                    <GanttFilters
                        filters={filters}
                        onFilterChange={onFilterChange}
                        onClearFilters={onClearFilters}
                    />
                </div>
            )}
        </div>
    );
};

// Filter Component
const GanttFilters = ({ filters, onFilterChange, onClearFilters }) => (
    <div className="row g-3">
        <div className="col-md-3">
            <label className="form-label">태스크 검색</label>
            <div className="input-group">
                <input
                    type="text"
                    className="form-control form-control-sm"
                    placeholder="태스크명 검색..."
                    value={filters.search}
                    onChange={(e) => onFilterChange('search', e.target.value)}
                />
                <button
                    className="btn btn-outline-secondary btn-sm"
                    onClick={() => onFilterChange('search', '')}
                >
                    <i className="fas fa-times"></i>
                </button>
            </div>
        </div>

        <div className="col-md-2">
            <label className="form-label">상태 필터</label>
            <select
                className="form-select form-select-sm"
                value={filters.status}
                onChange={(e) => onFilterChange('status', e.target.value)}
            >
                <option value="">전체 상태</option>
                <option value="NOT_STARTED">시작 전</option>
                <option value="IN_PROGRESS">진행중</option>
                <option value="COMPLETED">완료</option>
                <option value="ON_HOLD">보류</option>
                <option value="CANCELLED">취소</option>
            </select>
        </div>

        <div className="col-md-2">
            <label className="form-label">진행률 필터</label>
            <select
                className="form-select form-select-sm"
                value={filters.progress}
                onChange={(e) => onFilterChange('progress', e.target.value)}
            >
                <option value="">전체 진행률</option>
                <option value="0">0%</option>
                <option value="1-25">1-25%</option>
                <option value="26-50">26-50%</option>
                <option value="51-75">51-75%</option>
                <option value="76-99">76-99%</option>
                <option value="100">100%</option>
            </select>
        </div>

        <div className="col-md-3">
            <label className="form-label">기간 필터</label>
            <div className="input-group">
                <input
                    type="date"
                    className="form-control form-control-sm"
                    value={filters.dateFrom}
                    onChange={(e) => onFilterChange('dateFrom', e.target.value)}
                />
                <span className="input-group-text">~</span>
                <input
                    type="date"
                    className="form-control form-control-sm"
                    value={filters.dateTo}
                    onChange={(e) => onFilterChange('dateTo', e.target.value)}
                />
            </div>
        </div>

        <div className="col-md-2">
            <label className="form-label">&nbsp;</label>
            <div className="d-grid">
                <button className="btn btn-outline-danger btn-sm" onClick={onClearFilters}>
                    <i className="fas fa-times me-1"></i>초기화
                </button>
            </div>
        </div>
    </div>
);

// Basic Gantt Chart Component
const BasicGanttChart = ({ projectData, tasks }) => {
    // Basic chart implementation
    return (
        <div className="card">
            <div className="card-body p-0">
                <div className="text-center py-4">
                    <p>기본 간트차트 컴포넌트 (React 구현 예정)</p>
                    <small className="text-muted">
                        현재 {tasks.length}개의 태스크가 표시됩니다.
                    </small>
                </div>
            </div>
        </div>
    );
};

// D3 Gantt Chart Component
const D3GanttChart = ({ projectData, tasks }) => {
    const d3Ref = useRef(null);

    useEffect(() => {
        // D3 implementation here
        if (d3Ref.current && tasks.length > 0) {
            // D3 차트 렌더링 로직
        }
    }, [tasks]);

    return (
        <div className="card">
            <div className="card-header">
                <h6 className="mb-0">고급 간트차트 (D3.js)</h6>
            </div>
            <div className="card-body">
                <div ref={d3Ref} className="d3-gantt-container">
                    <div className="text-center py-4">
                        <p>D3.js 간트차트 컴포넌트 (React 구현 예정)</p>
                        <small className="text-muted">
                            현재 {tasks.length}개의 태스크가 표시됩니다.
                        </small>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default GanttChart;
export {
    GanttHeader,
    GanttStatistics,
    GanttControls,
    GanttFilters,
    BasicGanttChart,
    D3GanttChart
};