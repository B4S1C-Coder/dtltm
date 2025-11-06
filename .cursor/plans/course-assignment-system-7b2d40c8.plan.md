<!-- 7b2d40c8-aa72-4af6-bfff-c76f5f70129a 92fd9f50-bdb6-420b-9e9a-0be37d9fcba2 -->
# Course Assignment and Admin Management System

## Overview

Implement automatic course assignment system that assigns up to 2 courses per faculty based on priority criteria, with LLM fallback via Kafka. Add admin CRUD endpoints for course allocations, faculty, and course management. Create integration tests in manage.py.

## Data Model Changes

### Faculty Model Updates

- Add `rating` field (Double, 1.0-5.0, higher is better)
- Add `maxHoursPerWeek` field (Integer, hours faculty can teach per week)

### Course Model Updates

- Add `hoursRequiredPerWeek` field (Integer, hours required per week for the course)

## Core Assignment Service

### AssignmentService

Location: `src/main/java/edu/thapar/dtltm/service/AssignmentService.java`

Methods:

- `assignCoursesAutomatic(termId)`: Main assignment algorithm
- Priority 1: Seniority (lower seniorityScore = higher priority)
- Priority 2: Rating (higher rating = higher priority)
- Priority 3: Max hours capacity (assign courses based on available hours)
- Priority 4: LLM fallback (publish Kafka event for unassigned courses)
- Priority 5: Leave unassigned for manual admin assignment
- `getUnassignedCourses(termId)`: Get courses not yet assigned
- `getFacultyAssignments(facultyId, termId)`: Get assigned courses for a faculty
- Logic:
- Use faculty preferences from `FacultyCoursePreference` (ranked by preference)
- Enforce max 2 courses per faculty
- Check `hoursRequiredPerWeek` sum doesn't exceed `maxHoursPerWeek`
- Use existing `Course.taughtBy` relationship for assignments

## Kafka Integration

### Kafka Producer

Location: `src/main/java/edu/thapar/dtltm/kafka/AssignmentKafkaProducer.java`

- Publish events to topic `course-assignment-llm` when criteria 1-3 fail
- Event payload: `{ facultyId, courseId, termId, preferenceRank }`

### Kafka Topic Configuration

- Add topic `course-assignment-llm` in `KafkaConfig.java`

### Python Consumer

Location: `llm_assignment_consumer.py` (project root)

- Consume from `course-assignment-llm` topic
- Call external API to get faculty experience/publications
- Call Cerebras Inference API for LLM recommendation
- Call backend API endpoint to update assignment (POST `/admin/assignments/llm-result`)

## Admin Endpoints

### AdminAssignmentController

Location: `src/main/java/edu/thapar/dtltm/controller/AdminAssignmentController.java`

- POST `/admin/assignments/run`: Trigger automatic assignment for a term
- GET `/admin/assignments/term/{termId}`: Get all assignments for a term
- PUT `/admin/assignments/{facultyId}/courses`: Update course assignments for a faculty
- DELETE `/admin/assignments/{facultyId}/courses/{courseId}`: Remove a course assignment
- POST `/admin/assignments/llm-result`: Endpoint for Python consumer to submit LLM result

### AdminCourseController (extend existing CourseController or create new)

Location: `src/main/java/edu/thapar/dtltm/controller/AdminCourseController.java`

- GET `/admin/courses`: List all courses
- GET `/admin/courses/{id}`: Get course details
- PUT `/admin/courses/{id}`: Update course (name, code, hoursRequiredPerWeek, taughtBy)
- DELETE `/admin/courses/{id}`: Delete course

### AdminFacultyController

Location: `src/main/java/edu/thapar/dtltm/controller/AdminFacultyController.java`

- GET `/admin/faculties`: List all faculties
- GET `/admin/faculties/{id}`: Get faculty details
- PUT `/admin/faculties/{id}`: Update faculty (name, seniorityScore, mobilityScore, rating, maxHoursPerWeek)
- DELETE `/admin/faculties/{id}`: Delete faculty

## DTOs

### Assignment DTOs

- `AssignmentRequest.java`: `{ termId }`
- `AssignmentResponse.java`: `{ facultyId, facultyName, assignedCourses[], unassignedCourses[] }`
- `LLMResultRequest.java`: `{ facultyId, courseId, recommended: boolean }`
- `UpdateAssignmentRequest.java`: `{ courseIds[] }`

### Update DTOs

- `CourseUpdateDTO.java`: `{ name?, code?, hoursRequiredPerWeek?, taughtBy[]? }`
- `FacultyUpdateDTO.java`: `{ name?, seniorityScore?, mobilityScore?, rating?, maxHoursPerWeek? }`

## Service Updates

### CourseService

- Add `updateCourse(UUID id, CourseUpdateDTO dto)`
- Add `deleteCourse(UUID id)`
- Add `getAllCourses()`
- Add `getCourseById(UUID id)`

### FacultyService

- Add `updateFaculty(UUID id, FacultyUpdateDTO dto)`
- Add `deleteFaculty(UUID id)`
- Add `getAllFaculties()`
- Add `getFacultyById(UUID id)`

## Integration Tests

### manage.py Updates

- Fix existing broken tests in `LoginAndUserInfoTests`
- Add `CourseAssignmentTests` class:
- `test_automatic_assignment`: Test automatic assignment flow
- `test_assignment_by_seniority`: Verify seniority priority
- `test_assignment_by_rating`: Verify rating priority
- `test_assignment_by_max_hours`: Verify hours capacity
- `test_admin_update_assignment`: Test admin update endpoint
- `test_admin_crud_courses`: Test course CRUD
- `test_admin_crud_faculties`: Test faculty CRUD

## Implementation Notes

1. Assignment algorithm should prefer assigning from faculty preferences when possible
2. When multiple faculties have same priority, prefer faculty with higher preference rank for that course
3. LLM fallback only triggers when no faculty can be assigned using criteria 1-3
4. Admin endpoints must check `User.role == "ADMIN"`
5. All updates should be transactional
6. Add validation for rating (1-5), maxHoursPerWeek (>0), hoursRequiredPerWeek (>0)

## Files to Create/Modify

### New Files

- `src/main/java/edu/thapar/dtltm/service/AssignmentService.java`
- `src/main/java/edu/thapar/dtltm/kafka/AssignmentKafkaProducer.java`
- `src/main/java/edu/thapar/dtltm/controller/AdminAssignmentController.java`
- `src/main/java/edu/thapar/dtltm/controller/AdminCourseController.java`
- `src/main/java/edu/thapar/dtltm/controller/AdminFacultyController.java`
- `src/main/java/edu/thapar/dtltm/dto/AssignmentRequest.java`
- `src/main/java/edu/thapar/dtltm/dto/AssignmentResponse.java`
- `src/main/java/edu/thapar/dtltm/dto/LLMResultRequest.java`
- `src/main/java/edu/thapar/dtltm/dto/UpdateAssignmentRequest.java`
- `src/main/java/edu/thapar/dtltm/dto/CourseUpdateDTO.java`
- `src/main/java/edu/thapar/dtltm/dto/FacultyUpdateDTO.java`
- `llm_assignment_consumer.py`

### Modified Files

- `src/main/java/edu/thapar/dtltm/model/Faculty.java` (add rating, maxHoursPerWeek)
- `src/main/java/edu/thapar/dtltm/model/Course.java` (add hoursRequiredPerWeek)
- `src/main/java/edu/thapar/dtltm/config/KafkaConfig.java` (add topic)
- `src/main/java/edu/thapar/dtltm/service/CourseService.java` (add CRUD methods)
- `src/main/java/edu/thapar/dtltm/service/FacultyService.java` (add CRUD methods)
- `manage.py` (fix broken tests, add assignment tests)

### To-dos

- [ ] Add rating and maxHoursPerWeek fields to Faculty model, add hoursRequiredPerWeek to Course model
- [ ] Create AssignmentService with automatic assignment algorithm (priorities 1-3)
- [ ] Implement Kafka producer for LLM fallback and create Python consumer with Cerebras API integration
- [ ] Create AdminAssignmentController with endpoints for assignments and LLM result submission
- [ ] Create AdminCourseController and AdminFacultyController with full CRUD operations
- [ ] Extend CourseService and FacultyService with update/delete/getAll methods
- [ ] Fix broken tests in manage.py and add integration tests for assignment system and admin CRUD