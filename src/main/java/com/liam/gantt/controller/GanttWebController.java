package com.liam.gantt.controller;

import com.liam.gantt.dto.response.GanttChartDto;
import com.liam.gantt.dto.response.ProjectResponseDto;
import com.liam.gantt.dto.response.TaskResponseDto;
import com.liam.gantt.service.GanttService;
import com.liam.gantt.service.ProjectService;
import com.liam.gantt.service.TaskService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 간트차트 웹 페이지 컨트롤러
 */
@Slf4j
@Controller
@RequestMapping("/web/gantt")
@RequiredArgsConstructor
public class GanttWebController {

    private final GanttService ganttService;
    private final ProjectService projectService;
    private final TaskService taskService;

    /**
     * 간트차트 목록 페이지 (프로젝트 선택)
     */
    @GetMapping
    public String ganttList(Model model) {
        log.info("간트차트 목록 페이지 요청");
        
        try {
            List<ProjectResponseDto> projects = projectService.findAll();
            
            model.addAttribute("projects", projects);
            model.addAttribute("pageTitle", "간트차트");
            model.addAttribute("pageIcon", "fas fa-chart-gantt");
            
            return "gantt/list";
            
        } catch (Exception e) {
            log.error("간트차트 목록 페이지 로드 실패: {}", e.getMessage(), e);
            model.addAttribute("errorMessage", "프로젝트 목록을 불러올 수 없습니다: " + e.getMessage());
            return "error/404";
        }
    }

    /**
     * 간트차트 페이지 표시
     */
    @GetMapping("/{projectId}")
    public String ganttChart(@PathVariable Long projectId, Model model) {
        log.info("간트차트 페이지 요청 - projectId: {}", projectId);
        
        try {
            ProjectResponseDto project = projectService.findById(projectId);
            List<TaskResponseDto> tasks = taskService.findByProjectId(projectId);

            model.addAttribute("project", project);
            model.addAttribute("tasks", tasks);
            model.addAttribute("pageTitle", "간트차트: " + project.getName());
            model.addAttribute("pageIcon", "fas fa-chart-gantt");

            return "gantt/chart";
            
        } catch (Exception e) {
            log.error("간트차트 페이지 로드 실패: {}", e.getMessage(), e);
            model.addAttribute("errorMessage", "간트차트를 불러올 수 없습니다: " + e.getMessage());
            return "redirect:/web/projects?error=" + e.getMessage();
        }
    }

    /**
     * 간트차트 데이터 API (AJAX 호출용)
     */
    @GetMapping("/{projectId}/data")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> ganttChartData(@PathVariable Long projectId) {
        log.info("간트차트 데이터 API 호출 - projectId: {}", projectId);
        
        try {
            GanttChartDto ganttData = ganttService.getGanttChart(projectId);
            
            // 프론트엔드에서 사용할 수 있는 형태로 데이터 변환
            Map<String, Object> response = new HashMap<>();
            response.put("project", ganttData.getProject());
            response.put("tasks", enhanceTasksForView(ganttData.getTasks(), ganttData.getProject()));
            response.put("dependencies", ganttData.getDependencies());
            response.put("criticalPath", ganttData.getCriticalPath());
            response.put("timeline", createTimelineInfo(ganttData));
            response.put("statistics", createStatistics(ganttData));
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("간트차트 데이터 로드 실패: {}", e.getMessage(), e);
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", true);
            errorResponse.put("message", "간트차트 데이터를 불러올 수 없습니다: " + e.getMessage());
            
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * 태스크 데이터를 뷰에서 사용하기 위해 추가 정보 포함
     */
    private List<Map<String, Object>> enhanceTasksForView(List<TaskResponseDto> tasks, ProjectResponseDto project) {
        List<Map<String, Object>> enhancedTasks = new ArrayList<>();
        
        if (tasks != null && project != null) {
            for (TaskResponseDto task : tasks) {
                Map<String, Object> enhancedTask = new HashMap<>();
                // 기본 태스크 정보 복사
                enhancedTask.put("id", task.getId());
                enhancedTask.put("name", task.getName());
                enhancedTask.put("description", task.getDescription());
                enhancedTask.put("startDate", task.getStartDate());
                enhancedTask.put("endDate", task.getEndDate());
                enhancedTask.put("duration", task.getDuration());
                enhancedTask.put("progress", task.getProgress());
                enhancedTask.put("status", task.getStatus());
                
                // ganttInfo 계산 및 추가 (프로젝트 날짜 기준)
                enhancedTask.put("ganttInfo", calculateGanttInfo(task, project.getStartDate(), project.getEndDate()));
                enhancedTasks.add(enhancedTask);
            }
        }
        
        return enhancedTasks;
    }

    /**
     * 간트차트 바 표시를 위한 정보 계산
     */
    private Map<String, Object> calculateGanttInfo(TaskResponseDto task, LocalDate projectStartDate, LocalDate projectEndDate) {
        Map<String, Object> ganttInfo = new HashMap<>();
        
        // CSS 클래스 결정
        String cssClass = "gantt-task ";
        if (task.getStatus() != null) {
            switch (task.getStatus()) {
                case NOT_STARTED -> cssClass += "not-started";
                case IN_PROGRESS -> cssClass += "in-progress";
                case COMPLETED -> cssClass += "completed";
                case ON_HOLD -> cssClass += "on-hold";
                case CANCELLED -> cssClass += "cancelled";
                default -> cssClass += "not-started";
            }
        } else {
            cssClass += "not-started";
        }
        
        // 실제 날짜 기반 위치 및 폭 계산
        long totalProjectDays = ChronoUnit.DAYS.between(projectStartDate, projectEndDate) + 1;
        long taskStartOffset = ChronoUnit.DAYS.between(projectStartDate, task.getStartDate());
        long taskDuration = task.getDuration();
        
        // 최소값 보장 (0% 미만이나 100% 초과 방지)
        double leftPercent = Math.max(0, (double) taskStartOffset / totalProjectDays * 100);
        double widthPercent = Math.min(100 - leftPercent, (double) taskDuration / totalProjectDays * 100);
        
        // 최소 표시 크기 보장 (너무 작으면 클릭하기 어려움)
        widthPercent = Math.max(2.0, widthPercent);
        
        ganttInfo.put("cssClass", cssClass);
        ganttInfo.put("leftPercent", Math.round(leftPercent * 10.0) / 10.0);
        ganttInfo.put("widthPercent", Math.round(widthPercent * 10.0) / 10.0);
        ganttInfo.put("progressStyle", task.getStatus() != null && task.getStatus().name().equals("IN_PROGRESS"));
        
        return ganttInfo;
    }

    /**
     * 타임라인 정보 생성
     */
    private Map<String, Object> createTimelineInfo(GanttChartDto ganttData) {
        Map<String, Object> timeline = new HashMap<>();
        
        if (ganttData.getTimeline() != null) {
            timeline.put("startDate", ganttData.getTimeline().getStartDate());
            timeline.put("endDate", ganttData.getTimeline().getEndDate());
            timeline.put("totalDays", ganttData.getTimeline().getTotalDays());
            timeline.put("workingDays", ganttData.getTimeline().getWorkingDays());
            
            // 주별 헤더 생성
            timeline.put("weeks", createWeekHeaders(
                ganttData.getTimeline().getStartDate(),
                ganttData.getTimeline().getEndDate()
            ));
        }
        
        return timeline;
    }

    /**
     * 주별 헤더 생성
     */
    private List<Map<String, String>> createWeekHeaders(LocalDate startDate, LocalDate endDate) {
        List<Map<String, String>> weeks = new ArrayList<>();
        
        if (startDate != null && endDate != null) {
            LocalDate current = startDate;
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd");
            
            while (!current.isAfter(endDate)) {
                Map<String, String> week = new HashMap<>();
                LocalDate weekEnd = current.plusDays(6);
                if (weekEnd.isAfter(endDate)) {
                    weekEnd = endDate;
                }
                
                week.put("label", current.format(formatter) + "~" + weekEnd.format(formatter));
                week.put("startDate", current.toString());
                week.put("endDate", weekEnd.toString());
                
                weeks.add(week);
                current = current.plusWeeks(1);
            }
        }
        
        return weeks;
    }

    /**
     * 통계 정보 생성
     */
    private Map<String, Object> createStatistics(GanttChartDto ganttData) {
        Map<String, Object> statistics = new HashMap<>();
        
        if (ganttData.getTasks() != null) {
            List<TaskResponseDto> tasks = ganttData.getTasks();
            
            long completedTasks = tasks.stream()
                    .mapToLong(task -> "COMPLETED".equals(task.getStatus().name()) ? 1 : 0)
                    .sum();
            
            long inProgressTasks = tasks.stream()
                    .mapToLong(task -> "IN_PROGRESS".equals(task.getStatus().name()) ? 1 : 0)
                    .sum();
            
            double averageProgress = tasks.stream()
                    .filter(task -> task.getProgress() != null)
                    .mapToDouble(task -> task.getProgress().doubleValue())
                    .average()
                    .orElse(0.0);
            
            statistics.put("totalTasks", tasks.size());
            statistics.put("completedTasks", completedTasks);
            statistics.put("inProgressTasks", inProgressTasks);
            statistics.put("overallProgress", Math.round(averageProgress * 10.0) / 10.0);
        } else {
            statistics.put("totalTasks", 0);
            statistics.put("completedTasks", 0);
            statistics.put("inProgressTasks", 0);
            statistics.put("overallProgress", 0.0);
        }
        
        return statistics;
    }
}