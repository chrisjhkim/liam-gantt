package com.liam.gantt;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Liam Gantt Chart Application의 메인 애플리케이션 클래스
 * 
 * 이 애플리케이션은 프로젝트 관리 및 간트 차트 시각화를 위한 웹 애플리케이션입니다.
 * Spring Boot를 기반으로 하며, 프로젝트와 태스크 관리, 간트 차트 생성 기능을 제공합니다.
 * 
 * <h3>주요 기능:</h3>
 * <ul>
 *   <li>프로젝트 CRUD 관리</li>
 *   <li>태스크 생성 및 관리</li>
 *   <li>태스크 간 의존성 관리</li>
 *   <li>간트 차트 시각화</li>
 *   <li>프로젝트 진행률 추적</li>
 * </ul>
 * 
 * @author Liam
 * @since 1.0.0
 * @see <a href="https://spring.io/projects/spring-boot">Spring Boot</a>
 */
@SpringBootApplication
public class LiamGanttApplication {

	public static void main(String[] args) {
		SpringApplication.run(LiamGanttApplication.class, args);
	}

}
