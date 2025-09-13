package com.liam.gantt.dto.request;

import com.liam.gantt.entity.enums.TaskStatus;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 태스크 생성/수정 요청 DTO
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class TaskRequestDto {
    
    @NotBlank(message = "태스크명은 필수입니다")
    @Size(min = 1, max = 200, message = "태스크명은 1-200자 사이여야 합니다")
    private String name;
    
    @Size(max = 1000, message = "설명은 1000자를 초과할 수 없습니다")
    private String description;
    
    @NotNull(message = "시작일은 필수입니다")
    private LocalDate startDate;
    
    @NotNull(message = "종료일은 필수입니다")
    private LocalDate endDate;
    
    @Positive(message = "기간은 양수여야 합니다")
    private Integer duration;
    
    @DecimalMin(value = "0.0", message = "진행률은 0 이상이어야 합니다")
    @DecimalMax(value = "100.0", message = "진행률은 100 이하여야 합니다")
    @Builder.Default
    private BigDecimal progress = BigDecimal.ZERO;
    
    @Builder.Default
    private TaskStatus status = TaskStatus.NOT_STARTED;
    
    // 상위 태스크 ID (계층 구조를 위해)
    private Long parentTaskId;
    
    /**
     * 날짜 유효성 검증
     */
    public boolean isValidDateRange() {
        if (startDate == null || endDate == null) {
            return false;
        }
        return !endDate.isBefore(startDate);
    }
    
    /**
     * 기간 자동 계산 (일 단위)
     */
    public int calculateDuration() {
        if (startDate == null || endDate == null) {
            return 0;
        }
        return (int) java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate) + 1;
    }
}