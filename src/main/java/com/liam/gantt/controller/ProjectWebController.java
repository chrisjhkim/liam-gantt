package com.liam.gantt.controller;

import com.liam.gantt.dto.request.ProjectRequestDto;
import com.liam.gantt.dto.response.ProjectResponseDto;
import com.liam.gantt.dto.response.TaskResponseDto;
import com.liam.gantt.entity.enums.TaskStatus;
import com.liam.gantt.service.ProjectService;
import com.liam.gantt.service.TaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Slf4j
@Controller
@RequestMapping("/web/projects")
@RequiredArgsConstructor
public class ProjectWebController {

    private final ProjectService projectService;
    private final TaskService taskService;

    @GetMapping
    public String projectList(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status,
            @PageableDefault(size = 10, sort = "startDate") Pageable pageable,
            Model model) {
        
        log.info("프로젝트 목록 페이지 요청 - search: {}, status: {}", search, status);
        
        Page<ProjectResponseDto> projects;
        if (search != null || status != null) {
            projects = projectService.searchWithPaging(search, status, pageable);
        } else {
            projects = projectService.findAllWithPaging(pageable);
        }
        
        model.addAttribute("projects", projects);
        model.addAttribute("pageTitle", "프로젝트 관리");
        model.addAttribute("pageIcon", "fas fa-project-diagram");
        
        return "projects/list";
    }

    @GetMapping("/{id}")
    public String projectDetail(@PathVariable Long id, Model model) {
        log.info("프로젝트 상세 페이지 요청 - id: {}", id);
        
        try {
            // 프로젝트 기본 정보 조회
            ProjectResponseDto project = projectService.findById(id);
            
            // 프로젝트의 태스크 목록 조회 (최근 5개만 미리보기용)
            Page<TaskResponseDto> tasks = taskService.findByProjectIdWithPaging(
                id, PageRequest.of(0, 10)
            );
            
            // 태스크 상태별 통계 계산
            List<TaskResponseDto> allTasks = taskService.findByProjectId(id);
            long completedTaskCount = allTasks.stream()
                .filter(task -> task.getStatus() == TaskStatus.COMPLETED)
                .count();
            long inProgressTaskCount = allTasks.stream()
                .filter(task -> task.getStatus() == TaskStatus.IN_PROGRESS)
                .count();
            long notStartedTaskCount = allTasks.stream()
                .filter(task -> task.getStatus() == TaskStatus.NOT_STARTED)
                .count();
            
            // 프로젝트 일정 계산
            LocalDate today = LocalDate.now();
            LocalDate startDate = project.getStartDate();
            LocalDate endDate = project.getEndDate();
            
            long elapsedDays = ChronoUnit.DAYS.between(startDate, today);
            if (elapsedDays < 0) elapsedDays = 0;
            
            long remainingDays = ChronoUnit.DAYS.between(today, endDate);
            
            // 모델에 데이터 추가
            model.addAttribute("project", project);
            model.addAttribute("tasks", tasks);
            model.addAttribute("completedTaskCount", completedTaskCount);
            model.addAttribute("inProgressTaskCount", inProgressTaskCount);
            model.addAttribute("notStartedTaskCount", notStartedTaskCount);
            model.addAttribute("elapsedDays", elapsedDays);
            model.addAttribute("remainingDays", remainingDays);
            model.addAttribute("pageTitle", project.getName());
            model.addAttribute("pageIcon", "fas fa-project-diagram");
            
            return "projects/detail";
        } catch (Exception e) {
            log.error("프로젝트 조회 실패 - id: {}", id, e);
            model.addAttribute("errorMessage", "프로젝트를 찾을 수 없습니다.");
            return "redirect:/web/projects";
        }
    }

    @GetMapping("/new")
    public String newProjectForm(Model model) {
        log.info("새 프로젝트 생성 폼 요청");
        
        model.addAttribute("project", new ProjectRequestDto());
        model.addAttribute("pageTitle", "새 프로젝트");
        model.addAttribute("pageIcon", "fas fa-plus");
        
        return "projects/form";
    }

    @PostMapping
    public String createProject(
            @Valid @ModelAttribute("project") ProjectRequestDto project,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            Model model) {
        
        log.info("프로젝트 생성 요청 - name: {}", project.getName());
        
        if (bindingResult.hasErrors()) {
            model.addAttribute("pageTitle", "새 프로젝트");
            model.addAttribute("pageIcon", "fas fa-plus");
            return "projects/form";
        }
        
        try {
            ProjectResponseDto createdProject = projectService.create(project);
            log.info("프로젝트 생성 완료 - id: {}, name: {}", createdProject.getId(), createdProject.getName());
            
            redirectAttributes.addFlashAttribute("successMessage", 
                "프로젝트 '" + createdProject.getName() + "'가 성공적으로 생성되었습니다.");
            
            return "redirect:/web/projects/" + createdProject.getId();
        } catch (Exception e) {
            log.error("프로젝트 생성 실패 - name: {}", project.getName(), e);
            model.addAttribute("errorMessage", "프로젝트 생성 중 오류가 발생했습니다: " + e.getMessage());
            model.addAttribute("pageTitle", "새 프로젝트");
            model.addAttribute("pageIcon", "fas fa-plus");
            return "projects/form";
        }
    }

    @GetMapping("/{id}/edit")
    public String editProjectForm(@PathVariable Long id, Model model) {
        log.info("프로젝트 수정 폼 요청 - id: {}", id);
        
        try {
            ProjectResponseDto project = projectService.findById(id);
            
            // ResponseDto를 RequestDto로 변환
            ProjectRequestDto projectRequest = ProjectRequestDto.builder()
                    .name(project.getName())
                    .description(project.getDescription())
                    .startDate(project.getStartDate())
                    .endDate(project.getEndDate())
                    .status(project.getStatus())
                    .build();
            
            model.addAttribute("project", projectRequest);
            model.addAttribute("projectId", id);
            model.addAttribute("pageTitle", "프로젝트 수정: " + project.getName());
            model.addAttribute("pageIcon", "fas fa-edit");
            
            return "projects/form";
        } catch (Exception e) {
            log.error("프로젝트 조회 실패 - id: {}", id, e);
            model.addAttribute("errorMessage", "프로젝트를 찾을 수 없습니다.");
            return "redirect:/web/projects";
        }
    }

    @PostMapping("/{id}")
    public String updateProject(
            @PathVariable Long id,
            @Valid @ModelAttribute("project") ProjectRequestDto project,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            Model model) {
        
        log.info("프로젝트 수정 요청 - id: {}, name: {}", id, project.getName());
        
        if (bindingResult.hasErrors()) {
            model.addAttribute("projectId", id);
            model.addAttribute("pageTitle", "프로젝트 수정");
            model.addAttribute("pageIcon", "fas fa-edit");
            return "projects/form";
        }
        
        try {
            ProjectResponseDto updatedProject = projectService.update(id, project);
            log.info("프로젝트 수정 완료 - id: {}, name: {}", updatedProject.getId(), updatedProject.getName());
            
            redirectAttributes.addFlashAttribute("successMessage", 
                "프로젝트 '" + updatedProject.getName() + "'가 성공적으로 수정되었습니다.");
            
            return "redirect:/web/projects/" + updatedProject.getId();
        } catch (Exception e) {
            log.error("프로젝트 수정 실패 - id: {}, name: {}", id, project.getName(), e);
            model.addAttribute("errorMessage", "프로젝트 수정 중 오류가 발생했습니다: " + e.getMessage());
            model.addAttribute("projectId", id);
            model.addAttribute("pageTitle", "프로젝트 수정");
            model.addAttribute("pageIcon", "fas fa-edit");
            return "projects/form";
        }
    }

    @PostMapping("/{id}/delete")
    public String deleteProject(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        log.info("프로젝트 삭제 요청 - id: {}", id);
        
        try {
            ProjectResponseDto project = projectService.findById(id);
            String projectName = project.getName();
            
            projectService.delete(id);
            log.info("프로젝트 삭제 완료 - id: {}, name: {}", id, projectName);
            
            redirectAttributes.addFlashAttribute("successMessage", 
                "프로젝트 '" + projectName + "'가 성공적으로 삭제되었습니다.");
            
        } catch (Exception e) {
            log.error("프로젝트 삭제 실패 - id: {}", id, e);
            redirectAttributes.addFlashAttribute("errorMessage", 
                "프로젝트 삭제 중 오류가 발생했습니다: " + e.getMessage());
        }
        
        return "redirect:/web/projects";
    }

    @GetMapping("/search")
    @ResponseBody
    public List<ProjectResponseDto> searchProjectsAjax(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String status) {
        log.info("AJAX 프로젝트 검색 요청 - name: {}, status: {}", name, status);
        
        return projectService.search(name, status);
    }
}