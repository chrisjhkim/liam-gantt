package com.liam.gantt.entity;

import com.liam.gantt.entity.enums.ProjectStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * 프로젝트 엔티티 클래스
 * 
 * 이 클래스는 간트 차트 시스템에서 프로젝트 정보를 담는 JPA 엔티티입니다.
 * 프로젝트는 여러 개의 태스크를 포함할 수 있으며, 프로젝트의 전체적인 일정과 상태를 관리합니다.
 * 
 * <h3>주요 특징:</h3>
 * <ul>
 *   <li>프로젝트명은 고유해야 하며 200자 이내로 제한됩니다</li>
 *   <li>프로젝트는 시작일과 종료일을 가지며, 종료일은 시작일 이후여야 합니다</li>
 *   <li>프로젝트 상태(PLANNING, IN_PROGRESS, COMPLETED, ON_HOLD, CANCELLED)를 추적합니다</li>
 *   <li>프로젝트에 속한 태스크들과 일대다 관계를 가집니다</li>
 * </ul>
 * 
 * @author Liam
 * @since 1.0.0
 * @see BaseEntity 공통 엔티티 속성 상속
 * @see Task 프로젝트에 속한 태스크 엔티티
 * @see ProjectStatus 프로젝트 상태 열거형
 */
@Entity
@Table(name = "projects")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
@ToString(exclude = {"tasks"})
@EqualsAndHashCode(callSuper = true, exclude = {"tasks"})
public class Project extends BaseEntity {
    
    @NotBlank(message = "프로젝트명은 필수입니다")
    @Size(max = 200, message = "프로젝트명은 200자를 초과할 수 없습니다")
    @Column(name = "name", nullable = false, unique = true, length = 200)
    private String name;
    
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    
    @NotNull(message = "시작일은 필수입니다")
    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;
    
    @NotNull(message = "종료일은 필수입니다")
    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;
    
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private ProjectStatus status = ProjectStatus.PLANNING;
    
    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Task> tasks = new ArrayList<>();
    
    // 헬퍼 메서드들
    
    /**
     * 프로젝트에 태스크 추가
     */
    public void addTask(Task task) {
        tasks.add(task);
        task.setProject(this);
    }
    
    /**
     * 프로젝트에서 태스크 제거
     */
    public void removeTask(Task task) {
        tasks.remove(task);
        task.setProject(null);
    }
    
    /**
     * 프로젝트의 전체 진행률 계산
     */
    @Transient
    public Double calculateProgress() {
        if (tasks.isEmpty()) {
            return 0.0;
        }
        
        double totalProgress = tasks.stream()
                .mapToDouble(task -> task.getProgress().doubleValue())
                .sum();
        
        return totalProgress / tasks.size();
    }
    
    /**
     * 프로젝트 기간 (일 수) 계산
     */
    @Transient
    public long getDurationInDays() {
        if (startDate == null || endDate == null) {
            return 0;
        }
        return java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate) + 1;
    }
    
    /**
     * 프로젝트가 지연되었는지 확인
     */
    @Transient
    public boolean isOverdue() {
        return endDate != null && 
               LocalDate.now().isAfter(endDate) && 
               status != ProjectStatus.COMPLETED;
    }
    
    @PrePersist
    @PreUpdate
    private void validateDates() {
        if (startDate != null && endDate != null && endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("종료일은 시작일보다 같거나 늦어야 합니다");
        }
    }
}