package com.liam.gantt.entity;

import com.liam.gantt.entity.enums.DependencyType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.SuperBuilder;

/**
 * 태스크 간 의존성 관계 엔티티
 */
@Entity
@Table(name = "task_dependencies",
       uniqueConstraints = {
           @UniqueConstraint(
               name = "uk_task_dependency",
               columnNames = {"predecessor_id", "successor_id"}
           )
       })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@ToString(exclude = {"predecessor", "successor"})
@EqualsAndHashCode(callSuper = true, exclude = {"predecessor", "successor"})
public class TaskDependency extends BaseEntity {
    
    @NotNull(message = "선행 태스크는 필수입니다")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "predecessor_id", nullable = false)
    private Task predecessor;
    
    @NotNull(message = "후행 태스크는 필수입니다")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "successor_id", nullable = false)
    private Task successor;
    
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "dependency_type", nullable = false, length = 20)
    @Builder.Default
    private DependencyType dependencyType = DependencyType.FINISH_TO_START;
    
    @Column(name = "lag_days")
    @Builder.Default
    private Integer lagDays = 0;
    
    // 헬퍼 메서드들
    
    /**
     * 의존성 관계 설명 문자열 생성
     */
    @Transient
    public String getDescription() {
        String lagDescription = "";
        if (lagDays != null && lagDays != 0) {
            lagDescription = lagDays > 0 
                ? String.format(" + %d일", lagDays)
                : String.format(" - %d일", Math.abs(lagDays));
        }
        
        return String.format("[%s] %s → %s (%s%s)",
            dependencyType.getCode(),
            predecessor != null ? predecessor.getName() : "N/A",
            successor != null ? successor.getName() : "N/A",
            dependencyType.getDescription(),
            lagDescription
        );
    }
    
    /**
     * 순환 의존성 체크를 위한 메서드
     * 실제 구현은 Service 레이어에서 수행
     */
    @Transient
    public boolean createsCircularDependency() {
        // 간단한 자기 참조 체크만 수행
        if (predecessor != null && successor != null) {
            return predecessor.getId() != null && 
                   predecessor.getId().equals(successor.getId());
        }
        return false;
    }
    
    /**
     * 의존성이 유효한지 검증
     */
    @PrePersist
    @PreUpdate
    private void validate() {
        if (predecessor != null && successor != null) {
            // 자기 자신에 대한 의존성 방지
            if (predecessor.equals(successor)) {
                throw new IllegalArgumentException("태스크는 자기 자신에 의존할 수 없습니다");
            }
            
            // 같은 프로젝트의 태스크인지 확인
            if (predecessor.getProject() != null && 
                successor.getProject() != null &&
                !predecessor.getProject().equals(successor.getProject())) {
                throw new IllegalArgumentException("다른 프로젝트의 태스크 간에는 의존성을 설정할 수 없습니다");
            }
        }
    }
}