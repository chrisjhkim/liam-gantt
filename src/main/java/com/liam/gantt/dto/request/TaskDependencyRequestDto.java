package com.liam.gantt.dto.request;

import com.liam.gantt.entity.enums.DependencyType;
import jakarta.validation.constraints.NotNull;
import lombok.*;

/**
 * 태스크 의존성 생성 요청 DTO
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class TaskDependencyRequestDto {
    
    @NotNull(message = "선행 태스크 ID는 필수입니다")
    private Long predecessorId;
    
    @NotNull(message = "후행 태스크 ID는 필수입니다")
    private Long successorId;
    
    @NotNull(message = "의존성 유형은 필수입니다")
    @Builder.Default
    private DependencyType dependencyType = DependencyType.FINISH_TO_START;
    
    @Builder.Default
    private Integer lagDays = 0;
    
    /**
     * 자기 참조 검증
     */
    public boolean isSelfReference() {
        if (predecessorId == null || successorId == null) {
            return false;
        }
        return predecessorId.equals(successorId);
    }
}