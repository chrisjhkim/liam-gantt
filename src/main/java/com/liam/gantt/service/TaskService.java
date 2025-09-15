package com.liam.gantt.service;

import com.liam.gantt.dto.request.TaskRequestDto;
import com.liam.gantt.dto.response.TaskResponseDto;
import com.liam.gantt.entity.enums.TaskStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;

/**
 * 태스크 관련 비즈니스 로직 인터페이스
 */
public interface TaskService {
    
    /**
     * 프로젝트별 태스크 목록 조회
     */
    List<TaskResponseDto> findByProjectId(Long projectId);

    /**
     * 프로젝트별 태스크 목록 페이징 조회
     */
    Page<TaskResponseDto> findByProjectIdWithPaging(Long projectId, Pageable pageable);

    /**
     * 태스크 단건 조회
     */
    TaskResponseDto findById(Long id);

    /**
     * 의존성 포함 태스크 조회
     */
    TaskResponseDto findByIdWithDependencies(Long id);

    /**
     * 태스크 생성
     */
    TaskResponseDto create(Long projectId, TaskRequestDto request);

    /**
     * 태스크 수정
     */
    TaskResponseDto update(Long id, TaskRequestDto request);

    /**
     * 태스크 삭제
     */
    void delete(Long id);

    /**
     * 태스크 계층 구조 조회
     */
    List<TaskResponseDto> findTaskHierarchyByProjectId(Long projectId);

    /**
     * 태스크 진행률 업데이트
     */
    TaskResponseDto updateProgress(Long id, BigDecimal progress);

    /**
     * 태스크 상태 변경
     */
    TaskResponseDto updateStatus(Long id, TaskStatus status);

    /**
     * 하위 태스크 추가
     */
    TaskResponseDto addSubTask(Long parentTaskId, TaskRequestDto requestDto);

    /**
     * 루트 태스크 조회
     */
    List<TaskResponseDto> findRootTasks(Long projectId);

    /**
     * 지연된 태스크 조회
     */
    List<TaskResponseDto> findOverdueTasks(Long projectId);

    /**
     * 태스크 이동
     */
    TaskResponseDto moveTask(Long id, Integer dayOffset);

    /**
     * 태스크 검색
     */
    List<TaskResponseDto> search(Long projectId, String name, String status);
    
    /**
     * 프로젝트 ID와 상태별 태스크 조회
     */
    List<TaskResponseDto> findByProjectIdAndStatus(Long projectId, TaskStatus status);
    
    /**
     * 프로젝트 내 태스크 이름으로 검색
     */
    List<TaskResponseDto> searchByName(Long projectId, String keyword);

    /**
     * 부모 태스크 ID로 하위 태스크 조회
     */
    List<TaskResponseDto> findByParentTaskId(Long parentTaskId);
}