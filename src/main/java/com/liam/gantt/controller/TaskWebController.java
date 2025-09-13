package com.liam.gantt.controller;

import com.liam.gantt.dto.request.TaskRequestDto;
import com.liam.gantt.dto.response.ProjectResponseDto;
import com.liam.gantt.dto.response.TaskResponseDto;
import com.liam.gantt.service.ProjectService;
import com.liam.gantt.service.TaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Slf4j
@Controller
@RequestMapping("/web")
@RequiredArgsConstructor
public class TaskWebController {

    private final TaskService taskService;
    private final ProjectService projectService;

    /**
     * 프로젝트별 태스크 목록 페이지
     */
    @GetMapping("/projects/{projectId}/tasks")
    public String taskList(@PathVariable Long projectId,
                          @PageableDefault(size = 10, sort = "startDate") Pageable pageable,
                          Model model) {
        
        log.info("프로젝트 태스크 목록 페이지 요청 - projectId: {}", projectId);
        
        try {
            ProjectResponseDto project = projectService.findById(projectId);
            Page<TaskResponseDto> tasks = taskService.findByProjectIdWithPaging(projectId, pageable);
            
            model.addAttribute("project", project);
            model.addAttribute("tasks", tasks);
            model.addAttribute("pageTitle", project.getName() + " - 태스크 관리");
            model.addAttribute("pageIcon", "fas fa-tasks");
            
            return "tasks/list";
        } catch (Exception e) {
            log.error("프로젝트 태스크 목록 조회 실패 - projectId: {}", projectId, e);
            model.addAttribute("errorMessage", "프로젝트를 찾을 수 없습니다.");
            return "redirect:/web/projects";
        }
    }

    /**
     * 태스크 상세 페이지
     */
    @GetMapping("/tasks/{id}")
    public String taskDetail(@PathVariable Long id, Model model) {
        log.info("태스크 상세 페이지 요청 - id: {}", id);
        
        try {
            TaskResponseDto task = taskService.findByIdWithDependencies(id);
            ProjectResponseDto project = projectService.findById(task.getProjectId());
            
            model.addAttribute("task", task);
            model.addAttribute("project", project);
            model.addAttribute("pageTitle", task.getName());
            model.addAttribute("pageIcon", "fas fa-task");
            
            return "tasks/detail";
        } catch (Exception e) {
            log.error("태스크 조회 실패 - id: {}", id, e);
            model.addAttribute("errorMessage", "태스크를 찾을 수 없습니다.");
            return "redirect:/web/projects";
        }
    }

    /**
     * 새 태스크 생성 폼
     */
    @GetMapping("/projects/{projectId}/tasks/new")
    public String newTaskForm(@PathVariable Long projectId, Model model) {
        log.info("새 태스크 생성 폼 요청 - projectId: {}", projectId);
        
        try {
            ProjectResponseDto project = projectService.findById(projectId);
            List<TaskResponseDto> availableTasks = taskService.findByProjectId(projectId);
            
            model.addAttribute("task", new TaskRequestDto());
            model.addAttribute("project", project);
            model.addAttribute("availableTasks", availableTasks);
            model.addAttribute("pageTitle", "새 태스크 - " + project.getName());
            model.addAttribute("pageIcon", "fas fa-plus");
            
            return "tasks/form";
        } catch (Exception e) {
            log.error("프로젝트 조회 실패 - projectId: {}", projectId, e);
            model.addAttribute("errorMessage", "프로젝트를 찾을 수 없습니다.");
            return "redirect:/web/projects";
        }
    }

    /**
     * 태스크 생성 처리
     */
    @PostMapping("/projects/{projectId}/tasks")
    public String createTask(@PathVariable Long projectId,
                            @Valid @ModelAttribute("task") TaskRequestDto task,
                            BindingResult bindingResult,
                            RedirectAttributes redirectAttributes,
                            Model model) {
        
        log.info("태스크 생성 요청 - projectId: {}, name: {}", projectId, task.getName());
        
        if (bindingResult.hasErrors()) {
            try {
                ProjectResponseDto project = projectService.findById(projectId);
                List<TaskResponseDto> availableTasks = taskService.findByProjectId(projectId);
                
                model.addAttribute("project", project);
                model.addAttribute("availableTasks", availableTasks);
                model.addAttribute("pageTitle", "새 태스크 - " + project.getName());
                model.addAttribute("pageIcon", "fas fa-plus");
                
                return "tasks/form";
            } catch (Exception e) {
                log.error("프로젝트 조회 실패 - projectId: {}", projectId, e);
                return "redirect:/web/projects";
            }
        }
        
        try {
            TaskResponseDto createdTask = taskService.create(projectId, task);
            log.info("태스크 생성 완료 - id: {}, name: {}", createdTask.getId(), createdTask.getName());
            
            redirectAttributes.addFlashAttribute("successMessage", 
                "태스크 '" + createdTask.getName() + "'가 성공적으로 생성되었습니다.");
            
            return "redirect:/web/projects/" + projectId + "/tasks";
        } catch (Exception e) {
            log.error("태스크 생성 실패 - projectId: {}, name: {}", projectId, task.getName(), e);
            
            try {
                ProjectResponseDto project = projectService.findById(projectId);
                List<TaskResponseDto> availableTasks = taskService.findByProjectId(projectId);
                
                model.addAttribute("project", project);
                model.addAttribute("availableTasks", availableTasks);
                model.addAttribute("errorMessage", "태스크 생성 중 오류가 발생했습니다: " + e.getMessage());
                model.addAttribute("pageTitle", "새 태스크 - " + project.getName());
                model.addAttribute("pageIcon", "fas fa-plus");
                
                return "tasks/form";
            } catch (Exception ex) {
                return "redirect:/web/projects";
            }
        }
    }

    /**
     * 태스크 수정 폼
     */
    @GetMapping("/tasks/{id}/edit")
    public String editTaskForm(@PathVariable Long id, Model model) {
        log.info("태스크 수정 폼 요청 - id: {}", id);
        
        try {
            TaskResponseDto task = taskService.findByIdWithDependencies(id);
            ProjectResponseDto project = projectService.findById(task.getProjectId());
            List<TaskResponseDto> availableTasks = taskService.findByProjectId(task.getProjectId())
                .stream()
                .filter(t -> !t.getId().equals(id)) // 자기 자신 제외
                .toList();
            
            // ResponseDto를 RequestDto로 변환
            TaskRequestDto taskRequest = TaskRequestDto.builder()
                    .name(task.getName())
                    .description(task.getDescription())
                    .startDate(task.getStartDate())
                    .endDate(task.getEndDate())
                    .duration(task.getDuration())
                    .progress(task.getProgress())
                    .status(task.getStatus())
                    .parentTaskId(task.getParentTaskId())
                    .build();
            
            model.addAttribute("task", taskRequest);
            model.addAttribute("taskId", id);
            model.addAttribute("project", project);
            model.addAttribute("availableTasks", availableTasks);
            model.addAttribute("pageTitle", "태스크 수정: " + task.getName());
            model.addAttribute("pageIcon", "fas fa-edit");
            
            return "tasks/form";
        } catch (Exception e) {
            log.error("태스크 조회 실패 - id: {}", id, e);
            model.addAttribute("errorMessage", "태스크를 찾을 수 없습니다.");
            return "redirect:/web/projects";
        }
    }

    /**
     * 태스크 수정 처리
     */
    @PostMapping("/tasks/{id}")
    public String updateTask(@PathVariable Long id,
                            @Valid @ModelAttribute("task") TaskRequestDto task,
                            BindingResult bindingResult,
                            RedirectAttributes redirectAttributes,
                            Model model) {
        
        log.info("태스크 수정 요청 - id: {}, name: {}", id, task.getName());
        
        if (bindingResult.hasErrors()) {
            try {
                TaskResponseDto originalTask = taskService.findById(id);
                ProjectResponseDto project = projectService.findById(originalTask.getProjectId());
                List<TaskResponseDto> availableTasks = taskService.findByProjectId(originalTask.getProjectId())
                    .stream()
                    .filter(t -> !t.getId().equals(id))
                    .toList();
                
                model.addAttribute("taskId", id);
                model.addAttribute("project", project);
                model.addAttribute("availableTasks", availableTasks);
                model.addAttribute("pageTitle", "태스크 수정");
                model.addAttribute("pageIcon", "fas fa-edit");
                
                return "tasks/form";
            } catch (Exception e) {
                return "redirect:/web/projects";
            }
        }
        
        try {
            TaskResponseDto updatedTask = taskService.update(id, task);
            log.info("태스크 수정 완료 - id: {}, name: {}", updatedTask.getId(), updatedTask.getName());
            
            redirectAttributes.addFlashAttribute("successMessage", 
                "태스크 '" + updatedTask.getName() + "'가 성공적으로 수정되었습니다.");
            
            return "redirect:/web/tasks/" + updatedTask.getId();
        } catch (Exception e) {
            log.error("태스크 수정 실패 - id: {}, name: {}", id, task.getName(), e);
            
            try {
                TaskResponseDto originalTask = taskService.findById(id);
                ProjectResponseDto project = projectService.findById(originalTask.getProjectId());
                List<TaskResponseDto> availableTasks = taskService.findByProjectId(originalTask.getProjectId())
                    .stream()
                    .filter(t -> !t.getId().equals(id))
                    .toList();
                
                model.addAttribute("errorMessage", "태스크 수정 중 오류가 발생했습니다: " + e.getMessage());
                model.addAttribute("taskId", id);
                model.addAttribute("project", project);
                model.addAttribute("availableTasks", availableTasks);
                model.addAttribute("pageTitle", "태스크 수정");
                model.addAttribute("pageIcon", "fas fa-edit");
                
                return "tasks/form";
            } catch (Exception ex) {
                return "redirect:/web/projects";
            }
        }
    }

    /**
     * 태스크 삭제
     */
    @PostMapping("/tasks/{id}/delete")
    public String deleteTask(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        log.info("태스크 삭제 요청 - id: {}", id);
        
        try {
            TaskResponseDto task = taskService.findById(id);
            Long projectId = task.getProjectId();
            String taskName = task.getName();
            
            taskService.delete(id);
            log.info("태스크 삭제 완료 - id: {}, name: {}", id, taskName);
            
            redirectAttributes.addFlashAttribute("successMessage", 
                "태스크 '" + taskName + "'가 성공적으로 삭제되었습니다.");
            
            return "redirect:/web/projects/" + projectId + "/tasks";
        } catch (Exception e) {
            log.error("태스크 삭제 실패 - id: {}", id, e);
            redirectAttributes.addFlashAttribute("errorMessage", 
                "태스크 삭제 중 오류가 발생했습니다: " + e.getMessage());
            
            return "redirect:/web/projects";
        }
    }
}