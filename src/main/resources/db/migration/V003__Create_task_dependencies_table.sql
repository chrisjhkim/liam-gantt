-- V003: Create task_dependencies table

CREATE TABLE task_dependencies (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    predecessor_id BIGINT NOT NULL,
    successor_id BIGINT NOT NULL,
    dependency_type VARCHAR(20) NOT NULL DEFAULT 'FINISH_TO_START',
    lag_days INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    -- Foreign keys
    FOREIGN KEY (predecessor_id) REFERENCES tasks(id) ON DELETE CASCADE,
    FOREIGN KEY (successor_id) REFERENCES tasks(id) ON DELETE CASCADE,
    
    -- Business constraints
    CONSTRAINT chk_no_self_dependency CHECK (predecessor_id != successor_id),
    CONSTRAINT chk_dependency_type CHECK (dependency_type IN ('FINISH_TO_START', 'START_TO_START', 'FINISH_TO_FINISH', 'START_TO_FINISH')),
    
    -- Prevent duplicate dependencies
    CONSTRAINT uk_task_dependency UNIQUE (predecessor_id, successor_id)
);

-- Performance indexes
CREATE INDEX idx_dependencies_predecessor ON task_dependencies(predecessor_id);
CREATE INDEX idx_dependencies_successor ON task_dependencies(successor_id);
CREATE INDEX idx_dependencies_type ON task_dependencies(dependency_type);

-- Dependency chain tracking indexes
CREATE INDEX idx_dependencies_both ON task_dependencies(predecessor_id, successor_id);
CREATE INDEX idx_dependencies_type_lag ON task_dependencies(dependency_type, lag_days);

-- Circular dependency check index
CREATE INDEX idx_dependencies_chain ON task_dependencies(predecessor_id, successor_id, dependency_type);