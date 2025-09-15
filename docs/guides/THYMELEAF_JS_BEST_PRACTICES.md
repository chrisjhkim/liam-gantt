# Thymeleaf & JavaScript Best Practices Guide

> Thymeleaf 3.1+ 버전과 JavaScript를 함께 사용할 때 발생하는 보안 및 호환성 문제 해결 가이드

## 📋 목차
1. [핵심 원칙](#핵심-원칙)
2. [보안 제약사항](#보안-제약사항)
3. [올바른 데이터 전달 방법](#올바른-데이터-전달-방법)
4. [일반적인 오류와 해결법](#일반적인-오류와-해결법)
5. [베스트 프랙티스](#베스트-프랙티스)
6. [체크리스트](#체크리스트)

## 🎯 핵심 원칙

### Thymeleaf 3.1+ 보안 정책
- **이벤트 핸들러**에서 문자열 변수 직접 사용 금지
- **숫자**와 **boolean** 타입만 이벤트 핸들러에서 직접 사용 가능
- 문자열 데이터는 반드시 **data-* 속성**을 통해 전달

## 🔒 보안 제약사항

### ❌ 잘못된 예시 (오류 발생)
```html
<!-- 문자열을 직접 이벤트 핸들러에 사용 - 보안 오류! -->
<button th:onclick="'deleteItem(' + ${item.id} + ', \'' + ${item.name} + '\')'">
    삭제
</button>

<!-- th:onclick에서 문자열 연결 - 오류! -->
<a th:onclick="'showDetails(\'' + ${user.email} + '\')'">상세보기</a>

<!-- JavaScript 함수 호출에 문자열 직접 전달 - 오류! -->
<div th:onclick="'handleClick(\'' + ${data.value} + '\')'">클릭</div>
```

### ✅ 올바른 예시
```html
<!-- data-* 속성으로 데이터 전달 -->
<button type="button"
        th:data-item-id="${item.id}"
        th:data-item-name="${item.name}"
        onclick="deleteItem(this)">
    삭제
</button>

<!-- 숫자는 직접 사용 가능 -->
<button th:onclick="'selectPage(' + ${pageNumber} + ')'">
    페이지 선택
</button>

<!-- boolean도 직접 사용 가능 -->
<input type="checkbox"
       th:onclick="'toggleStatus(' + ${item.active} + ')'" />
```

## 📤 올바른 데이터 전달 방법

### 1. data-* 속성 사용법
```html
<!-- HTML -->
<button class="btn-delete"
        th:data-project-id="${project.id}"
        th:data-project-name="${project.name}"
        th:data-project-status="${project.status}"
        onclick="handleDelete(this)">
    삭제
</button>

<!-- JavaScript -->
<script>
function handleDelete(button) {
    // data-* 속성에서 값 읽기
    const projectId = button.getAttribute('data-project-id');
    const projectName = button.getAttribute('data-project-name');
    const projectStatus = button.getAttribute('data-project-status');

    // 또는 dataset 사용
    const projectId2 = button.dataset.projectId;
    const projectName2 = button.dataset.projectName;
    const projectStatus2 = button.dataset.projectStatus;

    console.log(`Deleting project: ${projectName} (ID: ${projectId})`);
}
</script>
```

### 2. Hidden Input 사용법
```html
<!-- 폼 데이터로 전달 -->
<form id="deleteForm">
    <input type="hidden" th:value="${item.id}" name="itemId" />
    <input type="hidden" th:value="${item.name}" name="itemName" />
    <button type="submit">삭제</button>
</form>

<script>
document.getElementById('deleteForm').addEventListener('submit', function(e) {
    e.preventDefault();
    const formData = new FormData(this);
    const itemId = formData.get('itemId');
    const itemName = formData.get('itemName');
    // 처리 로직
});
</script>
```

### 3. JavaScript 변수로 전달
```html
<!-- Thymeleaf 인라인 JavaScript -->
<script th:inline="javascript">
    // 전역 변수 또는 네임스페이스에 저장
    const projectData = {
        id: /*[[${project.id}]]*/ null,
        name: /*[[${project.name}]]*/ '',
        status: /*[[${project.status}]]*/ '',
        tasks: /*[[${project.tasks}]]*/ []
    };

    // 배열 데이터
    const taskList = /*[[${tasks}]]*/ [];
</script>
```

### 4. JSON 데이터 속성
```html
<!-- 복잡한 객체를 JSON으로 전달 -->
<div id="chartContainer"
     th:data-chart-config="${#strings.escapeJavaScript(#jsonMapper.writeValueAsString(chartConfig))}">
</div>

<script>
    const container = document.getElementById('chartContainer');
    const config = JSON.parse(container.dataset.chartConfig);
</script>
```

## 🚨 일반적인 오류와 해결법

### 오류 1: "Only variable expressions returning numbers or booleans are allowed"
**원인**: 이벤트 핸들러에 문자열 변수 직접 사용

**해결법**:
```html
<!-- Before (오류) -->
<button th:onclick="'confirm(\'' + ${message} + '\')'">확인</button>

<!-- After (정상) -->
<button th:data-message="${message}" onclick="confirm(this.dataset.message)">확인</button>
```

### 오류 2: JavaScript 이스케이프 문제
**원인**: 특수문자 포함된 문자열 처리

**해결법**:
```html
<!-- 이스케이프 처리 -->
<script th:inline="javascript">
    const message = /*[[${#strings.escapeJavaScript(message)}]]*/ '';
</script>
```

### 오류 3: null 값 처리
**원인**: null 가능한 값 처리 미흡

**해결법**:
```html
<!-- null 안전 처리 -->
<div th:data-value="${item?.value ?: 'default'}"
     onclick="handleClick(this)">
</div>
```

## 📚 베스트 프랙티스

### 1. 이벤트 위임 사용
```html
<!-- 개별 이벤트 대신 이벤트 위임 사용 -->
<table id="dataTable">
    <tbody>
        <tr th:each="item : ${items}">
            <td th:text="${item.name}"></td>
            <td>
                <button class="btn-edit"
                        th:data-item-id="${item.id}">수정</button>
                <button class="btn-delete"
                        th:data-item-id="${item.id}">삭제</button>
            </td>
        </tr>
    </tbody>
</table>

<script>
// 테이블 레벨에서 이벤트 처리
document.getElementById('dataTable').addEventListener('click', function(e) {
    if (e.target.classList.contains('btn-edit')) {
        const itemId = e.target.dataset.itemId;
        editItem(itemId);
    } else if (e.target.classList.contains('btn-delete')) {
        const itemId = e.target.dataset.itemId;
        deleteItem(itemId);
    }
});
</script>
```

### 2. 모듈화된 JavaScript
```javascript
// 네임스페이스 패턴
const ProjectManager = {
    init: function() {
        this.bindEvents();
    },

    bindEvents: function() {
        document.querySelectorAll('[data-action]').forEach(element => {
            element.addEventListener('click', this.handleAction.bind(this));
        });
    },

    handleAction: function(e) {
        const action = e.target.dataset.action;
        const id = e.target.dataset.id;

        switch(action) {
            case 'edit':
                this.editProject(id);
                break;
            case 'delete':
                this.deleteProject(id);
                break;
        }
    },

    editProject: function(id) {
        // 수정 로직
    },

    deleteProject: function(id) {
        // 삭제 로직
    }
};

// 초기화
document.addEventListener('DOMContentLoaded', function() {
    ProjectManager.init();
});
```

### 3. 타입 안전성 확보
```javascript
// 데이터 타입 검증
function safeGetNumber(element, attribute) {
    const value = element.getAttribute(attribute);
    const number = parseInt(value, 10);
    return isNaN(number) ? 0 : number;
}

function safeGetString(element, attribute, defaultValue = '') {
    return element.getAttribute(attribute) || defaultValue;
}

// 사용 예
const projectId = safeGetNumber(button, 'data-project-id');
const projectName = safeGetString(button, 'data-project-name', 'Unknown');
```

### 4. XSS 방지
```html
<!-- 항상 텍스트 콘텐츠로 설정 -->
<script>
function displayMessage(message) {
    // XSS 위험: innerHTML 사용 금지
    // document.getElementById('message').innerHTML = message;

    // 안전: textContent 사용
    document.getElementById('message').textContent = message;
}
</script>
```

## ✅ 체크리스트

### Thymeleaf 템플릿 작성 시
- [ ] 이벤트 핸들러에 문자열 직접 전달하지 않기
- [ ] data-* 속성으로 데이터 전달하기
- [ ] null 값 처리 고려하기
- [ ] 특수문자 이스케이프 처리하기

### JavaScript 작성 시
- [ ] data-* 속성에서 값 읽기
- [ ] 이벤트 위임 활용하기
- [ ] 타입 검증 수행하기
- [ ] XSS 방지 (textContent 사용)

### 코드 리뷰 시
- [ ] th:onclick에 문자열 변수 사용 확인
- [ ] data-* 속성 네이밍 일관성
- [ ] JavaScript 에러 핸들링
- [ ] 보안 취약점 검토

## 📌 요약

### 절대 하지 말아야 할 것
1. `th:onclick`에 문자열 변수 직접 사용
2. `innerHTML`로 사용자 입력 렌더링
3. 이스케이프 없이 특수문자 처리

### 항상 해야 할 것
1. `data-*` 속성으로 데이터 전달
2. `textContent`로 텍스트 설정
3. 타입 검증 및 null 체크
4. 이벤트 위임 패턴 활용

## 🔗 참고 자료
- [Thymeleaf 3.1 Security](https://www.thymeleaf.org/doc/articles/thymeleaf31whatsnew.html)
- [MDN: data-* attributes](https://developer.mozilla.org/en-US/docs/Web/HTML/Global_attributes/data-*)
- [OWASP XSS Prevention](https://cheatsheetseries.owasp.org/cheatsheets/Cross_Site_Scripting_Prevention_Cheat_Sheet.html)