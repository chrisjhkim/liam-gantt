-- V004: Add updated_at column to task_dependencies table

ALTER TABLE task_dependencies 
ADD COLUMN updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;

-- Update existing records to have proper updated_at values
UPDATE task_dependencies 
SET updated_at = created_at 
WHERE updated_at IS NULL;