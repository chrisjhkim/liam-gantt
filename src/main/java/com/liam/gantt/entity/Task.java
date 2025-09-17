package com.liam.gantt.entity;

import com.liam.gantt.entity.enums.TaskStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * 태스크 엔티티 클래스
 * 
 * 이 클래스는 간트 차트 시스템에서 개별 작업 단위를 나타내는 JPA 엔티티입니다.
 * 각 태스크는 특정 프로젝트에 속하며, 계층 구조(상위-하위 태스크)와 의존성 관계를 지원합니다.
 * 
 * <h3>주요 특징:</h3>
 * <ul>
 *   <li>프로젝트와 다대일 관계를 가지며, 반드시 하나의 프로젝트에 속해야 합니다</li>
 *   <li>계층 구조를 지원하여 상위 태스크와 하위 태스크 관계를 형성할 수 있습니다</li>
 *   <li>태스크 간 의존성(선행/후행) 관계를 관리합니다</li>
 *   <li>진행률(0-100%)과 상태를 추적합니다</li>
 *   <li>시작일, 종료일, 작업 기간을 관리합니다</li>
 * </ul>
 * 
 * @author Liam
 * @since 1.0.0
 * @see BaseEntity 공통 엔티티 속성 상속
 * @see Project 태스크가 속한 프로젝트 엔티티
 * @see TaskStatus 태스크 상태 열거형
 * @see TaskDependency 태스크 간 의존성 엔티티
 */
@Entity
@Table(name = "tasks")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
@ToString(exclude = {"project", "parentTask", "subTasks", "predecessorDependencies", "successorDependencies"})
@EqualsAndHashCode(callSuper = true, exclude = {"project", "parentTask", "subTasks", "predecessorDependencies", "successorDependencies"})
public class Task extends BaseEntity {
    
    @NotNull(message = "프로젝트는 필수입니다")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_task_id")
    private Task parentTask;
    
    @NotBlank(message = "태스크명은 필수입니다")
    @Size(max = 200, message = "태스크명은 200자를 초과할 수 없습니다")
    @Column(name = "name", nullable = false, length = 200)
    private String name;
    
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    
    @NotNull(message = "시작일은 필수입니다")
    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;
    
    @NotNull(message = "종료일은 필수입니다")
    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;
    
    @NotNull(message = "기간은 필수입니다")
    @Positive(message = "기간은 양수여야 합니다")
    @Column(name = "duration", nullable = false)
    private Integer duration;
    
    @NotNull
    @DecimalMin(value = "0.0", message = "진행률은 0 이상이어야 합니다")
    @DecimalMax(value = "100.0", message = "진행률은 100 이하여야 합니다")
    @Column(name = "progress", precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal progress = BigDecimal.ZERO;
    
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private TaskStatus status = TaskStatus.NOT_STARTED;
    
    // 자기 참조 관계 (계층 구조)
    @OneToMany(mappedBy = "parentTask", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Task> subTasks = new ArrayList<>();
    
    // 의존성 관계 - 이 태스크가 선행자인 의존성들
    @OneToMany(mappedBy = "predecessor", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<TaskDependency> successorDependencies = new ArrayList<>();
    
    // 의존성 관계 - 이 태스크가 후행자인 의존성들
    @OneToMany(mappedBy = "successor", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<TaskDependency> predecessorDependencies = new ArrayList<>();
    
    // 헬퍼 메서드들
    
    /**
     * 하위 태스크 추가
     */
    public void addSubTask(Task subTask) {
        subTasks.add(subTask);
        subTask.setParentTask(this);
    }
    
    /**
     * 하위 태스크 제거
     */
    public void removeSubTask(Task subTask) {
        subTasks.remove(subTask);
        subTask.setParentTask(null);
    }
    
    /**
     * 선행 의존성 추가
     */
    public void addPredecessorDependency(TaskDependency dependency) {
        predecessorDependencies.add(dependency);
        dependency.setSuccessor(this);
    }
    
    /**
     * 후행 의존성 추가
     */
    public void addSuccessorDependency(TaskDependency dependency) {
        successorDependencies.add(dependency);
        dependency.setPredecessor(this);
    }
    
    /**
     * 태스크가 완료되었는지 확인
     */
    @Transient
    public boolean isCompleted() {
        return status == TaskStatus.COMPLETED || 
               (progress != null && progress.compareTo(BigDecimal.valueOf(100)) >= 0);
    }
    
    /**
     * 태스크가 지연되었는지 확인
     */
    @Transient
    public boolean isOverdue() {
        return endDate != null && 
               LocalDate.now().isAfter(endDate) && 
               !isCompleted();
    }
    
    /**
     * 태스크의 실제 기간 계산 (일 수)
     */
    @Transient
    public long getActualDuration() {
        if (startDate == null || endDate == null) {
            return 0;
        }
        return java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate) + 1;
    }
    
    /**
     * 계층 레벨 계산 (루트 태스크는 0)
     */
    @Transient
    public int getLevel() {
        int level = 0;
        Task parent = this.parentTask;
        while (parent != null) {
            level++;
            parent = parent.getParentTask();
        }
        return level;
    }
    
    /**
     * 루트 태스크인지 확인
     */
    @Transient
    public boolean isRootTask() {
        return parentTask == null;
    }
    
    /**
     * 리프 태스크인지 확인 (하위 태스크가 없는 경우)
     */
    @Transient
    public boolean isLeafTask() {
        return subTasks.isEmpty();
    }
    
    /**
     * 선행 태스크 목록 조회
     */
    @Transient
    public List<Task> getPredecessorTasks() {
        return predecessorDependencies.stream()
                .map(TaskDependency::getPredecessor)
                .toList();
    }
    
    /**
     * 후행 태스크 목록 조회
     */
    @Transient
    public List<Task> getSuccessorTasks() {
        return successorDependencies.stream()
                .map(TaskDependency::getSuccessor)
                .toList();
    }
    
    @PrePersist
    @PreUpdate
    private void validateDates() {
        if (startDate != null && endDate != null) {
            if (endDate.isBefore(startDate)) {
                throw new IllegalArgumentException("종료일은 시작일보다 같거나 늦어야 합니다");
            }
            
            // duration 자동 계산 (주말 제외 옵션은 추후 구현)
            long calculatedDuration = getActualDuration();
            if (duration == null) {
                duration = (int) calculatedDuration;
            }
        }
        
        // 진행률에 따른 상태 자동 업데이트
        if (progress != null) {
            if (progress.compareTo(BigDecimal.ZERO) == 0) {
                if (status == TaskStatus.COMPLETED) {
                    status = TaskStatus.NOT_STARTED;
                }
            } else if (progress.compareTo(BigDecimal.valueOf(100)) == 0) {
                status = TaskStatus.COMPLETED;
            } else if (progress.compareTo(BigDecimal.ZERO) > 0) {
                if (status == TaskStatus.NOT_STARTED) {
                    status = TaskStatus.IN_PROGRESS;
                }
            }
        }
    }
}