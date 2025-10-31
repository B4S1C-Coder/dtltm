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

## Notes
- Endpoint paths reflect current implementation (no `/api` prefix).
- If your deployment uses a different base URL or port, adjust cURL accordingly.
- Admin-only vs Faculty-only access should be enforced by your Spring Security configuration.
