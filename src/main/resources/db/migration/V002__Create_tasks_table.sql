-- V002: Create tasks table

CREATE TABLE tasks (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    project_id BIGINT NOT NULL,
    parent_task_id BIGINT NULL,
    name VARCHAR(200) NOT NULL,
    description TEXT,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    duration INT NOT NULL,
    progress DECIMAL(5,2) DEFAULT 0.00,
    status VARCHAR(20) NOT NULL DEFAULT 'NOT_STARTED',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    -- Foreign keys
    FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE,
    FOREIGN KEY (parent_task_id) REFERENCES tasks(id) ON DELETE SET NULL,
    
    -- Business constraints
    CONSTRAINT chk_task_dates CHECK (end_date >= start_date),
    CONSTRAINT chk_task_progress CHECK (progress >= 0 AND progress <= 100),
    CONSTRAINT chk_task_duration CHECK (duration > 0),
    CONSTRAINT chk_task_status CHECK (status IN ('NOT_STARTED', 'IN_PROGRESS', 'COMPLETED', 'ON_HOLD', 'CANCELLED'))
);

-- Performance indexes
CREATE INDEX idx_tasks_project ON tasks(project_id);
CREATE INDEX idx_tasks_parent ON tasks(parent_task_id);
CREATE INDEX idx_tasks_dates ON tasks(start_date, end_date);
CREATE INDEX idx_tasks_status ON tasks(status);
CREATE INDEX idx_tasks_progress ON tasks(progress);

-- Composite indexes
CREATE INDEX idx_tasks_project_status ON tasks(project_id, status);
CREATE INDEX idx_tasks_project_dates ON tasks(project_id, start_date, end_date);
CREATE INDEX idx_tasks_parent_status ON tasks(parent_task_id, status);

-- Gantt chart optimization index
CREATE INDEX idx_tasks_project_dates_status ON tasks(project_id, start_date, end_date, status);

-- Hierarchy query index
CREATE INDEX idx_tasks_hierarchy ON tasks(project_id, parent_task_id, id);