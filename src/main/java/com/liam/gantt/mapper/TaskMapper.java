package com.liam.gantt.mapper;

import com.liam.gantt.dto.request.TaskRequestDto;
import com.liam.gantt.dto.response.TaskResponseDto;
import com.liam.gantt.entity.Task;
import com.liam.gantt.entity.enums.TaskStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.temporal.ChronoUnit;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class TaskMapper {

    // TaskDependencyMapper dependencyMapper;  // 현재 사용되지 않으므로 주석 처리

    public Task toEntity(TaskRequestDto dto) {
        if (dto == null) {
            return null;
        }

        Integer duration = dto.getDuration();
        if (duration == null && dto.getStartDate() != null && dto.getEndDate() != null) {
            duration = (int) ChronoUnit.DAYS.between(dto.getStartDate(), dto.getEndDate()) + 1;
        }

        return Task.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .startDate(dto.getStartDate())
                .endDate(dto.getEndDate())
                .duration(duration != null ? duration : 1)
                .progress(dto.getProgress() != null ? dto.getProgress() : BigDecimal.ZERO)
                .status(TaskStatus.NOT_STARTED)
                .build();
    }

    public TaskResponseDto toResponseDto(Task entity) {
        if (entity == null) {
            return null;
        }

        TaskResponseDto.TaskResponseDtoBuilder builder = TaskResponseDto.builder()
                .id(entity.getId())
                .name(entity.getName())
                .description(entity.getDescription())
                .startDate(entity.getStartDate())
                .endDate(entity.getEndDate())
                .duration(entity.getDuration())
                .progress(entity.getProgress() != null ? entity.getProgress() : BigDecimal.ZERO)
                .status(entity.getStatus())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt());

        if (entity.getProject() != null) {
            builder.projectId(entity.getProject().getId());
        }

        if (entity.getParentTask() != null) {
            builder.parentTaskId(entity.getParentTask().getId());
            builder.parentTaskName(entity.getParentTask().getName());
        }

        if (entity.getSubTasks() != null && !entity.getSubTasks().isEmpty()) {
            builder.subTasks(entity.getSubTasks().stream()
                    .map(this::toResponseDto)
                    .collect(Collectors.toList()));
        }

        // Dependencies는 현재 순환 참조를 피하기 위해 별도 서비스에서 처리
        // if (entity.getPredecessorDependencies() != null && !entity.getPredecessorDependencies().isEmpty()) {
        //     builder.dependencies(entity.getPredecessorDependencies().stream()
        //             .map(dependencyMapper::toResponseDto)
        //             .collect(Collectors.toList()));
        // }

        return builder.build();
    }

    public void updateEntity(Task existingEntity, TaskRequestDto dto) {
        if (dto == null || existingEntity == null) {
            return;
        }

        existingEntity.setName(dto.getName());
        existingEntity.setDescription(dto.getDescription());
        existingEntity.setStartDate(dto.getStartDate());
        existingEntity.setEndDate(dto.getEndDate());

        if (dto.getDuration() != null) {
            existingEntity.setDuration(dto.getDuration());
        } else if (dto.getStartDate() != null && dto.getEndDate() != null) {
            int calculatedDuration = (int) ChronoUnit.DAYS.between(dto.getStartDate(), dto.getEndDate()) + 1;
            existingEntity.setDuration(calculatedDuration);
        }

        if (dto.getProgress() != null) {
            existingEntity.setProgress(dto.getProgress());
            updateTaskStatusByProgress(existingEntity, dto.getProgress());
        }
    }

    public List<TaskResponseDto> toResponseDtoList(List<Task> entities) {
        if (entities == null) {
            return null;
        }

        return entities.stream()
                .map(this::toResponseDto)
                .collect(Collectors.toList());
    }

    private void updateTaskStatusByProgress(Task task, BigDecimal progress) {
        if (progress == null) {
            return;
        }

        if (progress.compareTo(BigDecimal.ZERO) == 0) {
            task.setStatus(TaskStatus.NOT_STARTED);
        } else if (progress.compareTo(BigDecimal.valueOf(100)) >= 0) {
            task.setStatus(TaskStatus.COMPLETED);
        } else {
            task.setStatus(TaskStatus.IN_PROGRESS);
        }
    }
}