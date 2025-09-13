package com.liam.gantt.controller;

import com.liam.gantt.dto.response.ProjectResponseDto;
import com.liam.gantt.service.ProjectService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Slf4j
@Controller
@RequiredArgsConstructor
public class HomeController {

    private final ProjectService projectService;

    @GetMapping("/")
    public String home(Model model) {
        log.info("메인 페이지 요청");
        
        try {
            // 최근 프로젝트 5개 조회
            Pageable pageable = PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "startDate"));
            Page<ProjectResponseDto> recentProjects = projectService.findAllWithPaging(pageable);
            
            // 전체 통계 정보
            long totalProjects = projectService.countAll();
            long activeProjects = projectService.countByStatus("IN_PROGRESS");
            long completedProjects = projectService.countByStatus("COMPLETED");
            
            model.addAttribute("recentProjects", recentProjects.getContent());
            model.addAttribute("totalProjects", totalProjects);
            model.addAttribute("activeProjects", activeProjects);
            model.addAttribute("completedProjects", completedProjects);
            model.addAttribute("pageTitle", "대시보드");
            model.addAttribute("pageIcon", "fas fa-tachometer-alt");
            model.addAttribute("activePage", "home");
            
        } catch (Exception e) {
            log.error("메인 페이지 데이터 조회 실패", e);
            model.addAttribute("errorMessage", "데이터 로딩 중 오류가 발생했습니다.");
            model.addAttribute("activePage", "home");
        }
        
        return "home";
    }

    @GetMapping("/web")
    public String webHome() {
        return "redirect:/";
    }
}