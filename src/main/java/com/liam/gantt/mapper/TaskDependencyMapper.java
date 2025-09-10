package com.liam.gantt.mapper;

import com.liam.gantt.dto.request.TaskDependencyRequestDto;
import com.liam.gantt.dto.response.TaskDependencyResponseDto;
import com.liam.gantt.entity.TaskDependency;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class TaskDependencyMapper {

    public TaskDependency toEntity(TaskDependencyRequestDto dto) {
        if (dto == null) {
            return null;
        }

        return TaskDependency.builder()
                .dependencyType(dto.getDependencyType())
                .lagDays(dto.getLagDays() != null ? dto.getLagDays() : 0)
                .build();
    }

    public TaskDependencyResponseDto toResponseDto(TaskDependency entity) {
        if (entity == null) {
            return null;
        }

        TaskDependencyResponseDto.TaskDependencyResponseDtoBuilder builder = TaskDependencyResponseDto.builder()
                .id(entity.getId())
                .dependencyType(entity.getDependencyType())
                .lagDays(entity.getLagDays())
                .createdAt(entity.getCreatedAt());

        if (entity.getPredecessor() != null) {
            builder.predecessorId(entity.getPredecessor().getId());
            builder.predecessorName(entity.getPredecessor().getName());
        }

        if (entity.getSuccessor() != null) {
            builder.successorId(entity.getSuccessor().getId());
            builder.successorName(entity.getSuccessor().getName());
        }

        return builder.build();
    }

    public void updateEntity(TaskDependency existingEntity, TaskDependencyRequestDto dto) {
        if (dto == null || existingEntity == null) {
            return;
        }

        existingEntity.setDependencyType(dto.getDependencyType());
        existingEntity.setLagDays(dto.getLagDays() != null ? dto.getLagDays() : 0);
    }

    public List<TaskDependencyResponseDto> toResponseDtoList(List<TaskDependency> entities) {
        if (entities == null) {
            return null;
        }

        return entities.stream()
                .map(this::toResponseDto)
                .collect(Collectors.toList());
    }
}