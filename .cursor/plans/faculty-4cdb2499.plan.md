<!-- 4cdb2499-48bd-4591-a32b-f53b7fd3ef68 395490cb-9e03-4c78-a987-b587c0d8857d -->
# Faculty Course Preference Capture

### Scope

Implement per-semester faculty course preference capture with strict rankings (no ties), over an admin-provided candidate set. Provide REST endpoints for admins to manage candidate sets and for faculty to submit/view preferences. Persist with JPA and enforce constraints at DB and service levels.

### Data Model (JPA)

- `AcademicTerm` (new): `id`, `year`, `season` (ENUM: SPRING, SUMMER, FALL, WINTER), unique `(year, season)`.
- `PreferenceSet` (new): `id`, `faculty` (ManyToOne), `term` (ManyToOne), `status` (ENUM: DRAFT, OPEN, CLOSED), `createdAt`, `updatedAt`.
- `PreferenceSetCourse` (new): `id`, `preferenceSet` (ManyToOne), `course` (ManyToOne). Defines the admin-approved candidate set.
- `FacultyCoursePreference` (new): `id`, `preferenceSet` (ManyToOne), `course` (ManyToOne), `rank` (int). Unique constraints: `(preference_set_id, rank)` and `(preference_set_id, course_id)`.

Minimal illustrative entity skeletons:

```startLine:1:src/main/java/edu/thapar/dtltm/model/FacultyCoursePreference.java
@Entity
@Table(name = "faculty_course_preference",
  uniqueConstraints = {
    @UniqueConstraint(columnNames = {"preference_set_id", "rank"}),
    @UniqueConstraint(columnNames = {"preference_set_id", "course_id"})
  }
)
public class FacultyCoursePreference {
  @Id @GeneratedValue private Long id;
  @ManyToOne(optional=false) private PreferenceSet preferenceSet;
  @ManyToOne(optional=false) private Course course;
  @Column(nullable=false) private Integer rank; // 1..N, no gaps
}
```

### Repositories

- `AcademicTermRepository`, `PreferenceSetRepository`, `PreferenceSetCourseRepository`, `FacultyCoursePreferenceRepository` with finders by facultyId+term, and batch operations.

### DTOs + Validation

- Admin
  - `CreatePreferenceSetRequest { facultyId, termId, candidateCourseIds[] }`
  - `UpdateCandidateCoursesRequest { candidateCourseIds[] }`
- Faculty
  - `SubmitPreferencesRequest { rankedCourseIds[] }` (strict order)
- Responses: `PreferenceSetResponse { id, facultyId, term, candidates[], preferences[] }`
- Validation rules in service layer:
  - All `rankedCourseIds` ⊆ candidate set
  - No duplicates; ranks are contiguous 1..k
  - Optional min length (e.g., require ranking all or allow subset)
  - PreferenceSet must be `OPEN`

### Services

- `PreferenceService`
  - `createPreferenceSet(facultyId, termId, candidateCourseIds)`
  - `updateCandidates(setId, candidateCourseIds)` (if OPEN or DRAFT)
  - `openSet(setId)` / `closeSet(setId)`
  - `submitPreferences(setId, facultyId, rankedCourseIds)` transactional: upsert, replace existing
  - `getPreferencesByFacultyAndTerm(facultyId, termId)`
- Integrate with `FacultyService` only for lookups; keep preferences in dedicated service.

### Controllers (REST)

- `AdminPreferenceController` (role ADMIN)
  - POST `/api/admin/preference-sets` create set
  - PUT `/api/admin/preference-sets/{id}/candidates` update candidates
  - POST `/api/admin/preference-sets/{id}/open` / `/close`
  - GET `/api/admin/preference-sets/{id}` view
- `FacultyPreferenceController` (role FACULTY)
  - POST `/api/faculty/preference-sets/{id}/submit`
  - GET `/api/faculty/preference-sets` list open sets for the faculty and their current preferences

### Security/Ownership

- Ensure faculty can only submit to their own `PreferenceSet`.
- Admin-only endpoints guarded by role checks.

### Transactionality & Constraints

- Use `@Transactional` for submit/update methods.
- DB unique constraints prevent duplicate course or rank within a set.
- On candidate change, if preferences include removed courses, drop or require resubmission (decide: auto-drop and renumber is NOT recommended; reject update if conflicts).

### Optional/Future

- Deadlines: add `opensAt`, `closesAt` to `PreferenceSet`; auto-close.
- Audit: `createdBy`, `updatedBy` and history table if needed.
- Export preferences for assignment service.

### Files to Add/Change

- Add: `edu/thapar/dtltm/model/{AcademicTerm.java, PreferenceSet.java, PreferenceSetCourse.java, FacultyCoursePreference.java}`
- Add: `edu/thapar/dtltm/repository/{AcademicTermRepository.java, PreferenceSetRepository.java, PreferenceSetCourseRepository.java, FacultyCoursePreferenceRepository.java}`
- Add: `edu/thapar/dtltm/service/PreferenceService.java`
- Add: `edu/thapar/dtltm/web/{AdminPreferenceController.java, FacultyPreferenceController.java}`
- Add: `edu/thapar/dtltm/dto/{CreatePreferenceSetRequest.java, UpdateCandidateCoursesRequest.java, SubmitPreferencesRequest.java, PreferenceSetResponse.java}`
- Change: wire into `FacultyService` if needed for helper methods only
- Optional: DB migration scripts (Flyway/Liquibase)

### Testing

- Unit tests for validation and service methods.
- Integration tests for controller flows and constraint enforcement.

### To-dos

- [ ] Create JPA entities for term, preference set, candidate link, and preference rows
- [ ] Add repositories with finders by faculty and term
- [ ] Define admin/faculty DTOs and response models
- [ ] Implement PreferenceService with validation and transactions
- [ ] Add admin and faculty controllers with security guards
- [ ] Provide helper lookups in FacultyService as needed
- [ ] Add DB migrations for new tables and constraints (if using Flyway)
- [ ] Write unit and integration tests for core flows