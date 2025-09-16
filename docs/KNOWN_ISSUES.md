# Known Issues

## 1. Spring Boot DevTools Auto-reload Not Working
- **Issue**: DevTools auto-reload is not working properly. Changes to templates and code require manual server restart.
- **Expected**: DevTools should automatically reload changes without manual restart
- **Current Workaround**: Manually restart server with `./gradlew bootRun` after changes
- **TODO**: Investigate DevTools configuration and fix auto-reload functionality

## 2. Project Edit Page Rendering Issue
- **Issue**: Edit page briefly shows then turns white/blank
- **Symptoms**:
  - Page flashes briefly before going blank
  - Start date and end date fields may not be visible
- **Status**: Under investigation