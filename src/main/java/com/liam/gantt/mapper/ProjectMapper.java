package com.liam.gantt.mapper;

import com.liam.gantt.dto.request.ProjectRequestDto;
import com.liam.gantt.dto.response.ProjectResponseDto;
import com.liam.gantt.entity.Project;
import com.liam.gantt.entity.enums.ProjectStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class ProjectMapper {

    @Autowired
    private TaskMapper taskMapper;

    public Project toEntity(ProjectRequestDto dto) {
        if (dto == null) {
            return null;
        }

        return Project.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .startDate(dto.getStartDate())
                .endDate(dto.getEndDate())
                .status(ProjectStatus.PLANNING)
                .build();
    }

    public ProjectResponseDto toResponseDto(Project entity) {
        if (entity == null) {
            return null;
        }

        ProjectResponseDto.ProjectResponseDtoBuilder builder = ProjectResponseDto.builder()
                .id(entity.getId())
                .name(entity.getName())
                .description(entity.getDescription())
                .startDate(entity.getStartDate())
                .endDate(entity.getEndDate())
                .status(entity.getStatus())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt());

        double avgProgress = calculateAverageProgress(entity);
        
        if (entity.getTasks() != null) {
            builder.tasks(entity.getTasks().stream()
                    .map(taskMapper::toResponseDto)
                    .collect(Collectors.toList()));
            builder.taskCount(entity.getTasks().size());
        } else {
            builder.taskCount(0);
        }
        
        builder.averageProgress(avgProgress);
        builder.progress(avgProgress);

        return builder.build();
    }

    public void updateEntity(Project existingEntity, ProjectRequestDto dto) {
        if (dto == null || existingEntity == null) {
            return;
        }

        existingEntity.setName(dto.getName());
        existingEntity.setDescription(dto.getDescription());
        existingEntity.setStartDate(dto.getStartDate());
        existingEntity.setEndDate(dto.getEndDate());
    }

    public List<ProjectResponseDto> toResponseDtoList(List<Project> entities) {
        if (entities == null) {
            return null;
        }

        return entities.stream()
                .map(this::toResponseDto)
                .collect(Collectors.toList());
    }

    private double calculateAverageProgress(Project project) {
        if (project.getTasks() == null || project.getTasks().isEmpty()) {
            return 0.0;
        }

        return project.getTasks().stream()
                .mapToDouble(task -> task.getProgress() != null ? task.getProgress().doubleValue() : 0.0)
                .average()
                .orElse(0.0);
    }
}