package com.liam.gantt.dto.request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDate;

/**
 * 프로젝트 생성/수정 요청 DTO
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class ProjectRequestDto {
    
    @NotBlank(message = "프로젝트명은 필수입니다")
    @Size(min = 1, max = 200, message = "프로젝트명은 1-200자 사이여야 합니다")
    private String name;
    
    @Size(max = 1000, message = "설명은 1000자를 초과할 수 없습니다")
    private String description;
    
    @NotNull(message = "시작일은 필수입니다")
    private LocalDate startDate;
    
    @NotNull(message = "종료일은 필수입니다")
    private LocalDate endDate;
    
    // 상태는 서버에서 관리하므로 요청에는 포함하지 않음
    
    /**
     * 날짜 유효성 검증
     */
    public boolean isValidDateRange() {
        if (startDate == null || endDate == null) {
            return false;
        }
        return !endDate.isBefore(startDate);
    }
}