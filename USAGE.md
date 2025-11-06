# Faculty Course Preference Capture - API Usage

This document explains how to use the newly added endpoints for per-semester faculty course preference capture, including request/response formats and cURL examples.

Note: Examples assume JWT-based auth. Replace `YOUR_TOKEN` with a valid token. Admin routes require an admin token. Faculty routes require a faculty token. All IDs are UUIDs.

## Data Model Overview
- AcademicTerm: `{ id, year, season }` where `season` ∈ `SPRING|SUMMER|FALL|WINTER`.
- PreferenceSet: `{ id, facultyId, term, status, createdAt, updatedAt }` with `status` ∈ `DRAFT|OPEN|CLOSED`.
- Candidate courses are linked to a PreferenceSet.
- FacultyCoursePreference rows store ordered preferences with unique `(preference_set_id, rank)` and `(preference_set_id, course_id)`.

## Common Response Shapes
- Error (handled by GlobalExceptionHandler):
```json
{
  "timeStamp": "2025-10-31T10:00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Detailed message",
  "path": "/request/path"
}
```
- PreferenceSetResponse:
```json
{
  "id": "SET_ID",
  "facultyId": "FACULTY_ID",
  "term": { "year": 2025, "season": "FALL" },
  "candidateCourseIds": ["COURSE_ID_1", "COURSE_ID_2"],
  "preferences": [
    { "courseId": "COURSE_ID_1", "rank": 1 },
    { "courseId": "COURSE_ID_2", "rank": 2 }
  ],
  "status": "OPEN"
}
```

## Admin Endpoints
Base path: `/admin/preference-sets`

### Create preference set (DRAFT)
- Request (CreatePreferenceSetRequest):
```json
{
  "facultyId": "FACULTY_ID",
  "termId": "TERM_ID",
  "candidateCourseIds": ["COURSE_ID_1", "COURSE_ID_2", "COURSE_ID_3"]
}
```
- Response: `200 OK` with `setId` (UUID)

cURL:
```bash
curl -X POST \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
        "facultyId": "FACULTY_ID",
        "termId": "TERM_ID",
        "candidateCourseIds": ["COURSE_ID_1", "COURSE_ID_2"]
      }' \
  http://localhost:8080/admin/preference-sets
```

Errors:
- 400 Bad Request: invalid IDs or courses do not exist
- 409 Conflict: set already exists for (faculty, term)

### Update candidate courses
- Request (UpdateCandidateCoursesRequest):
```json
{ "candidateCourseIds": ["COURSE_ID_1", "COURSE_ID_3"] }
```
- Response: `200 OK`

cURL:
```bash
curl -X PUT \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{ "candidateCourseIds": ["COURSE_ID_1", "COURSE_ID_3"] }' \
  http://localhost:8080/admin/preference-sets/SET_ID/candidates
```

Errors:
- 400 Bad Request: non-existent courses
- 409 Conflict: candidates cannot be changed because preferences reference removed courses, or set is CLOSED

### Open a preference set
- Transitions status to `OPEN`.
- Response: `200 OK`

cURL:
```bash
curl -X POST \
  -H "Authorization: Bearer YOUR_TOKEN" \
  http://localhost:8080/admin/preference-sets/SET_ID/open
```

### Close a preference set
- Transitions status to `CLOSED`.
- Response: `200 OK`

cURL:
```bash
curl -X POST \
  -H "Authorization: Bearer YOUR_TOKEN" \
  http://localhost:8080/admin/preference-sets/SET_ID/close
```

### Get a preference set (detailed)
- Response: `200 OK` with `PreferenceSetResponse` (includes candidates and current preferences)

cURL:
```bash
curl -X GET \
  -H "Authorization: Bearer YOUR_TOKEN" \
  http://localhost:8080/admin/preference-sets/SET_ID
```

## Faculty Endpoints
Base path: `/faculty/preference-sets`

Authentication: Uses `@AuthenticationPrincipal User user` to resolve faculty from the logged-in user. Do not pass `facultyId` in requests.

### List open sets for current faculty
- Response: `200 OK` with `PreferenceSetResponse[]`

cURL:
```bash
curl -X GET \
  -H "Authorization: Bearer YOUR_TOKEN" \
  http://localhost:8080/faculty/preference-sets
```

Errors:
- 404 Not Found: no faculty associated with current user

### Submit preferences for a set
- Request (SubmitPreferencesRequest): ordered list of course IDs; no duplicates; must be subset of candidates.
```json
{ "rankedCourseIds": ["COURSE_ID_2", "COURSE_ID_1", "COURSE_ID_3"] }
```
- Response: `200 OK`

cURL:
```bash
curl -X POST \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{ "rankedCourseIds": ["COURSE_ID_2", "COURSE_ID_1"] }' \
  http://localhost:8080/faculty/preference-sets/SET_ID/submit
```

Errors:
- 400 Bad Request: duplicate course IDs or ranked course not in candidate set
- 403 Forbidden: submitting for another faculty's set (ownership)
- 409 Conflict: set not OPEN (or other state conflicts)

## Validation Rules (enforced server-side)
- Faculty can only submit to their own `PreferenceSet` when `status = OPEN`.
- Ranked courses must be a subset of candidate courses; duplicates not allowed.
- Ranks are assigned consecutively in submission order (1..k). DB uniqueness prevents duplicate ranks/courses within a set.
- Updating candidates on a set with existing preferences that would be invalid is rejected.

## Course Assignment System

The system automatically assigns courses to faculties based on priority criteria (seniority, rating, max hours). If these criteria fail, LLM-based fallback can be used.

### Data Model Updates

- **Faculty**: Added `rating` (Double, 1.0-5.0, higher is better) and `maxHoursPerWeek` (Integer, default 20).
- **Course**: Added `hoursRequiredPerWeek` (Integer, default 3).

### Assignment Priorities

1. **Seniority**: Lower `seniorityScore` = higher priority (1 = most senior, 5 = least senior).
2. **Rating**: Higher `rating` = higher priority (1.0-5.0 scale).
3. **Max Hours**: More available hours = higher priority.
4. **LLM Fallback**: If criteria 1-3 fail, Kafka event is published for LLM-based recommendation.
5. **Manual Assignment**: Admin can manually assign courses if all automatic methods fail.

### Constraints

- Maximum 2 courses per faculty.
- Total hours assigned cannot exceed faculty's `maxHoursPerWeek`.
- Assignments respect faculty preferences when possible.

## Admin Assignment Endpoints

Base path: `/admin/assignments`

All endpoints require ADMIN role.

### Run automatic assignment for a term

- Request (AssignmentRequest):
```json
{
  "termId": "TERM_ID"
}
```
- Response: `200 OK` with `AssignmentResponse[]`

AssignmentResponse:
```json
[
  {
    "facultyId": "FACULTY_ID",
    "facultyName": "Faculty Name",
    "assignedCourses": [
      {
        "courseId": "COURSE_ID",
        "courseCode": "CS101",
        "courseName": "Introduction to Computer Science",
        "hoursRequiredPerWeek": 3
      }
    ],
    "unassignedCourseIds": []
  }
]
```

cURL:
```bash
curl -X POST \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{ "termId": "TERM_ID" }' \
  http://localhost:20001/admin/assignments/run
```

Errors:
- 400 Bad Request: term not found or no closed preference sets found
- 403 Forbidden: not an admin user

### Get assignments for a term

Runs the assignment algorithm and returns current assignments for the term.

- Response: `200 OK` with `AssignmentResponse[]`

cURL:
```bash
curl -X GET \
  -H "Authorization: Bearer YOUR_TOKEN" \
  http://localhost:20001/admin/assignments/term/TERM_ID
```

Errors:
- 400 Bad Request: term not found or no closed preference sets found
- 403 Forbidden: not an admin user

### Update faculty course assignments

- Request (UpdateAssignmentRequest):
```json
{
  "courseIds": ["COURSE_ID_1", "COURSE_ID_2"]
}
```
- Response: `200 OK`

cURL:
```bash
curl -X PUT \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{ "courseIds": ["COURSE_ID_1", "COURSE_ID_2"] }' \
  http://localhost:20001/admin/assignments/FACULTY_ID/courses
```

Errors:
- 400 Bad Request: faculty/course not found, more than 2 courses, or exceeds max hours
- 403 Forbidden: not an admin user

### Remove a course assignment

- Response: `200 OK`

cURL:
```bash
curl -X DELETE \
  -H "Authorization: Bearer YOUR_TOKEN" \
  http://localhost:20001/admin/assignments/FACULTY_ID/courses/COURSE_ID
```

Errors:
- 400 Bad Request: course not found
- 403 Forbidden: not an admin user

### Get unassigned courses for a term

- Response: `200 OK` with `Course[]`

cURL:
```bash
curl -X GET \
  -H "Authorization: Bearer YOUR_TOKEN" \
  http://localhost:20001/admin/assignments/unassigned/TERM_ID
```

### Get faculty assignments for a term

- Response: `200 OK` with `Course[]`

cURL:
```bash
curl -X GET \
  -H "Authorization: Bearer YOUR_TOKEN" \
  http://localhost:20001/admin/assignments/faculty/FACULTY_ID/term/TERM_ID
```

### Submit LLM assignment result

Endpoint for Python consumer to submit LLM recommendation results.

- Request (LLMResultRequest):
```json
{
  "facultyId": "FACULTY_ID",
  "courseId": "COURSE_ID",
  "recommended": true
}
```
- Response: `200 OK`

cURL:
```bash
curl -X POST \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
        "facultyId": "FACULTY_ID",
        "courseId": "COURSE_ID",
        "recommended": true
      }' \
  http://localhost:20001/admin/assignments/llm-result
```

Errors:
- 400 Bad Request: faculty/course not found or constraints violated
- 403 Forbidden: not an admin user

## Admin Course Management Endpoints

Base path: `/admin/courses`

All endpoints require ADMIN role.

### List all courses

- Response: `200 OK` with `Course[]`

Course:
```json
{
  "id": "COURSE_ID",
  "code": "CS101",
  "name": "Introduction to Computer Science",
  "hoursRequiredPerWeek": 3,
  "taughtBy": [
    {
      "id": "FACULTY_ID",
      "name": "Faculty Name",
      ...
    }
  ]
}
```

cURL:
```bash
curl -X GET \
  -H "Authorization: Bearer YOUR_TOKEN" \
  http://localhost:20001/admin/courses
```

Errors:
- 403 Forbidden: not an admin user

### Get course by ID

- Response: `200 OK` with `Course`

cURL:
```bash
curl -X GET \
  -H "Authorization: Bearer YOUR_TOKEN" \
  http://localhost:20001/admin/courses/COURSE_ID
```

Errors:
- 404 Not Found: course not found
- 403 Forbidden: not an admin user

### Update course

- Request (CourseUpdateDTO):
```json
{
  "name": "Updated Course Name",
  "code": "CS102",
  "hoursRequiredPerWeek": 4,
  "taughtBy": ["FACULTY_ID_1", "FACULTY_ID_2"]
}
```
All fields are optional. Only provided fields will be updated.

- Response: `200 OK` with updated `Course`

cURL:
```bash
curl -X PUT \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
        "name": "Updated Course Name",
        "hoursRequiredPerWeek": 4
      }' \
  http://localhost:20001/admin/courses/COURSE_ID
```

Errors:
- 400 Bad Request: invalid faculty IDs or code already exists
- 404 Not Found: course not found
- 403 Forbidden: not an admin user
- 409 Conflict: course code already exists

### Delete course

- Response: `200 OK`

cURL:
```bash
curl -X DELETE \
  -H "Authorization: Bearer YOUR_TOKEN" \
  http://localhost:20001/admin/courses/COURSE_ID
```

Errors:
- 404 Not Found: course not found
- 403 Forbidden: not an admin user

## Admin Faculty Management Endpoints

Base path: `/admin/faculties`

All endpoints require ADMIN role.

### List all faculties

- Response: `200 OK` with `Faculty[]`

Faculty:
```json
{
  "id": "FACULTY_ID",
  "name": "Faculty Name",
  "dateOfJoin": "2020-01-01",
  "seniorityScore": 2,
  "mobilityScore": 1,
  "rating": 4.5,
  "maxHoursPerWeek": 20,
  "user": {
    "id": "USER_ID",
    "email": "faculty@example.com",
    ...
  }
}
```

cURL:
```bash
curl -X GET \
  -H "Authorization: Bearer YOUR_TOKEN" \
  http://localhost:20001/admin/faculties
```

Errors:
- 403 Forbidden: not an admin user

### Get faculty by ID

- Response: `200 OK` with `Faculty`

cURL:
```bash
curl -X GET \
  -H "Authorization: Bearer YOUR_TOKEN" \
  http://localhost:20001/admin/faculties/FACULTY_ID
```

Errors:
- 404 Not Found: faculty not found
- 403 Forbidden: not an admin user

### Update faculty

- Request (FacultyUpdateDTO):
```json
{
  "name": "Updated Faculty Name",
  "seniorityScore": 1,
  "mobilityScore": 2,
  "rating": 4.8,
  "maxHoursPerWeek": 15
}
```
All fields are optional. Only provided fields will be updated.

- Response: `200 OK` with updated `Faculty`

cURL:
```bash
curl -X PUT \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
        "rating": 4.8,
        "maxHoursPerWeek": 15
      }' \
  http://localhost:20001/admin/faculties/FACULTY_ID
```

Errors:
- 400 Bad Request: invalid values (e.g., rating out of range)
- 404 Not Found: faculty not found
- 403 Forbidden: not an admin user

### Delete faculty

- Response: `200 OK`

cURL:
```bash
curl -X DELETE \
  -H "Authorization: Bearer YOUR_TOKEN" \
  http://localhost:20001/admin/faculties/FACULTY_ID
```

Errors:
- 404 Not Found: faculty not found
- 403 Forbidden: not an admin user

## LLM Assignment Consumer

A Python consumer (`llm_assignment_consumer.py`) processes LLM assignment events from Kafka.

### Configuration

Set environment variables:
- `KAFKA_BROKER`: Kafka bootstrap servers (default: `localhost:9094`)
- `BACKEND_API_URL`: Backend API URL (default: `http://localhost:20001`)
- `CEREBRAS_API_URL`: Cerebras Inference API URL (required)
- `CEREBRAS_API_KEY`: Cerebras API key (required)
- `FACULTY_EXPERIENCE_API_URL`: External API for faculty experience (required)

### Running the Consumer

```bash
python llm_assignment_consumer.py
```

### How It Works

1. Consumer listens to `course-assignment-llm` Kafka topic.
2. For each event:
   - Fetches faculty experience/publications from external API.
   - Fetches course details from backend API.
   - Calls Cerebras Inference API with prompt to determine if faculty should teach the course.
   - Submits result to backend via `POST /admin/assignments/llm-result`.

### Kafka Event Format

```json
{
  "facultyId": "FACULTY_ID",
  "courseId": "COURSE_ID",
  "termId": "TERM_ID",
  "preferenceRank": 1
}
```

## Validation Rules (enforced server-side)

### Assignment Constraints
- Maximum 2 courses per faculty.
- Total assigned hours cannot exceed faculty's `maxHoursPerWeek`.
- Faculty must have submitted preferences for the term (preference set must be CLOSED).

### Priority Ranking
- When multiple faculties want the same course, they are ranked by:
  1. Seniority (lower score = better)
  2. Rating (higher = better)
  3. Available hours (more = better)
  4. Preference rank (lower = better, as tie-breaker)

### Course Updates
- Course code must be unique across all courses.
- Faculty IDs in `taughtBy` must exist.

### Faculty Updates
- `seniorityScore`: 1-5 (1 = most senior)
- `mobilityScore`: 1-3
- `rating`: 1.0-5.0 (higher = better)
- `maxHoursPerWeek`: Must be at least 1

## Notes
- Endpoint paths reflect current implementation (no `/api` prefix).
- If your deployment uses a different base URL or port, adjust cURL accordingly.
- Admin-only vs Faculty-only access should be enforced by your Spring Security configuration.
- Default port is `20001` (not `8080`).
- LLM fallback is only triggered when automatic assignment (priorities 1-3) fails for a course-faculty pair.
