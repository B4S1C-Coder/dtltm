import os
import json
import requests
import logging
import inspect
import glob
import time
import subprocess
from logging.handlers import RotatingFileHandler
from datetime import datetime
from pprint import pprint
from enum import Enum
from pathlib import Path
import argparse
import random
import uuid

# Configs for manage.py
BASE_DIR = Path(__file__).resolve().parent
COMPOSE_DEV_FILE = BASE_DIR / "infra" / "dev" / "docker-compose.yml"
MVNW = BASE_DIR / "mvnw"
INFRA_UP = ["docker", "compose", "-f", str(COMPOSE_DEV_FILE), "up", "-d"]
INFRA_DOWN = ["docker", "compose", "-f", str(COMPOSE_DEV_FILE), "down"]

def run_cmd(cmd: list[str]):
    print(f"RUN: {' '.join(cmd)}")
    result = subprocess.run(cmd, capture_output=True, text=True)
    print(result.stdout)
    print(result.stderr)

os.makedirs(".logs", exist_ok=True)

TestLogger = logging.getLogger()
TestLogger.setLevel(logging.INFO)

file_handler = RotatingFileHandler(
    ".logs/api-requests.log",
    maxBytes = 1_000_000,
    backupCount=10
)

file_handler.setFormatter(logging.Formatter(
    "%(asctime)s - %(levelname)s - %(message)s"
))

if not TestLogger.handlers:
    TestLogger.addHandler(file_handler)

def get_random_creds() -> dict:
    return {
        'email': f'{str(uuid.uuid4()).replace("-","")}',
        'password': '123'
    }

class fprint:
    @staticmethod
    def info(msg: str, log: logging.Logger | None=None):
        print(f"{datetime.now().isoformat()} - [ INFO  ] - {msg}")

        if log is not None:
            log.info(msg)

    @staticmethod
    def error(msg: str, log: logging.Logger | None=None):
        print(f"{datetime.now().isoformat()} - [ ERROR ] - {msg}")

        if log is not None:
            log.error(msg)

    @staticmethod
    def tpass(msg: str, log: logging.Logger | None=None):
        print(f"{datetime.now().isoformat()} - [ PASS  ] - {msg}")

        if log is not None:
            log.info(f"[ PASS  ] - {msg}")

    @staticmethod
    def tfail(msg: str, log: logging.Logger | None=None):
        print(f"{datetime.now().isoformat()} - [ FAIL  ] - {msg}")

        if log is not None:
            log.error(f"[ FAIL  ] - {msg}")

    @staticmethod
    def warn(msg: str, log: logging.Logger | None=None):
        print(f"{datetime.now().isoformat()} - [ WARN  ] - {msg}")

        if log is not None:
            log.warning(msg)

    @staticmethod
    def critical(msg: str, log: logging.Logger | None=None):
        print(f"{datetime.now().isoformat()} - [ XXXXX ] - {msg}")

        if log is not None:
            log.critical(msg)

class HttpTestType(Enum):
    GET = 1
    POST = 2
    PUT_OR_PATCH = 3
    DELETE = 4

class HttpTest:
    def __init__(self, servicename: str, tlogger: logging.Logger | None=None, print_cfg: bool = True):
        fprint.info("Loading config")

        with open("test.config.json", "r") as f:
            self.__conf = json.load(f).get(servicename)

            if self.__conf is None:
                fprint.error(f"{servicename} not found in config")
                exit(1)

        self.__servicename = servicename
        self.__log = tlogger

        if print_cfg:
            fprint.info(f"Configuration for {self.__servicename}:")
            pprint(self.__conf)

        fprint.info("Test object created.", self.__log)

    def getServiceName(self) -> str:
        return self.__servicename

    def http_request(
        self, test_type: HttpTestType, endpoint: str, headers: dict={
            "Content-Type": "application/json"
        }, **kwargs
    ) -> tuple[list[dict], int] | tuple[dict, int] | tuple[str, int]:

        url = f"{self.__conf['domain']}:{self.__conf['port']}/{endpoint}"
        url = "https://" + url if self.__conf["useHttps"] else "http://" + url

        response, result = None, None

        if test_type == HttpTestType.GET:
            response = requests.get(url=url, headers=headers, **kwargs)

        elif test_type == HttpTestType.POST:
            response = requests.post(url=url, headers=headers, **kwargs)

        elif test_type == HttpTestType.PUT_OR_PATCH:
            response = requests.put(url=url, headers=headers, **kwargs)

        elif test_type == HttpTestType.DELETE:
            response = requests.delete(url=url, headers=headers, **kwargs)

        else:
            fprint.error(f"Incompatible test type specified: {test_type}")
            exit(2)

        result = (
            response.json()
            if response.content and 'application/json'
            in response.headers.get('Content-Type', '') 
            else response.text
        )


        fprint.info(f"HTTP {test_type.name} {response.status_code} - {url}", self.__log)

        return result, response.status_code
    
    # Must be called on child classes ONLY
    def execute_tests(self, tests_to_run: list[str]=[]) -> tuple[int, int, int]:
        child_methods = {
            name for name, member in inspect.getmembers(self, predicate=inspect.ismethod)
            if not name.startswith('_')
        }

        base_methods = {
            name for name, member in inspect.getmembers(HttpTest, predicate=inspect.isfunction)
            if not name.startswith('_')
        }

        runnable_tests = list(child_methods - base_methods)
        tests_to_run = runnable_tests if len(tests_to_run) == 0 else tests_to_run
 
        passed, skipped, failed = 0, len(runnable_tests) - len(tests_to_run), 0
        
        print("--------------[ T E S T S ]--------------")
        print("Service Name: " + self.__servicename.upper() + "\n------------------------")

        print("Following tests are runnable: ", runnable_tests)
        print("Followng tests will be run  : ", tests_to_run)

        print("------------------------")

        for test in tests_to_run:
            method = getattr(self, test)

            if callable(method):
                try:
                    method()
                    passed += 1
                    fprint.tpass(f"{test} Succeeded")
                except AssertionError as aerr:
                    fprint.tfail(f"{test} - {aerr}", self.__log)
                    failed += 1
                except Exception as err:
                    fprint.critical(f"UNHANDLED EXCEPTION - {test} - {err}", self.__log)
                    failed += 1
            else:
                fprint.warn(f"{test} is not callable. Skipped.")
                skipped += 1
        
        print("------------------------")
        print(f"PASSED: {passed}, SKIPPED: {skipped}, FAILED: {failed}")
        return (passed, skipped, failed)

class LoginAndUserInfoTests(HttpTest):
    token: str|None = None
    creds = {
        'email': f'test{random.randint(0, 99999)}@email.com',
        'password': '123'
    }

    def health_check(self):
        response_str, code = self.http_request(HttpTestType.GET, 'health')
        assert code == 200, f"Unexpected status code: {code}"

    def create_user(self):
        response_str, code = self.http_request(
            HttpTestType.POST, 'auth/register', json=self.creds
        )
        assert type(response_str) == str, f"Expected response type 'str', got: '{type(response_str)}'"
        assert code >= 200 and code < 300, f"Response code violates 2XX range: {code}"

    def obtain_token(self):
        response, code = self.http_request(HttpTestType.POST, 'auth/login', json=self.creds)
        assert type(response) == dict, f"Expected response type 'dict', got; '{type(response)}'"
        assert code >= 200 and code < 300, f"Response code violates 2XX range: {code}"

        temp_token = response.get('token', '')
        assert temp_token != '', "Token is absent in response body"

        self.token = temp_token

    def user_info(self):
        response, code = self.http_request(HttpTestType.POST, 'auth/login', json=self.creds)
        assert type(response) == dict, f"Expected response type 'dict', got; '{type(response)}'"
        
        temp_token = response.get('token', '')
        assert temp_token != '', "Token is absent in response body"

        response, code = self.http_request(
            HttpTestType.GET, 'users/current-user-info', headers={
                "Content-Type": "application/json",
                "Authorization": f"Bearer {temp_token}"
            }
        )

        fprint.info(f"user_info - {temp_token}")

        in_use = {
            "Content-Type": "application/json",
            "Authorization": f"Bearer {temp_token}"
        }

        pprint(in_use)

        fprint.info(f"user_info - {response}")
        assert type(response) == dict, f"Expected response type 'dict', got: '{type(response)}'"
        assert code >= 200 and code < 300, f"Response code violates 2XX range: {code}"

class CourseAssignmentTests(HttpTest):
    admin_token: str|None = None
    admin_creds = {
        'email': f'admin{random.randint(0, 99999)}@email.com',
        'password': 'admin123'
    }
    faculty_id: str|None = None
    course_id: str|None = None
    term_id: str|None = None

    def create_admin_user(self):
        """Create an admin user for testing"""
        # First create a regular user
        creds = get_random_creds()
        response_str, code = self.http_request(
            HttpTestType.POST, 'auth/register', json=creds
        )
        assert code >= 200 and code < 300, f"Failed to create admin user: {code}"
        
        # Login to get token
        response, code = self.http_request(HttpTestType.POST, 'auth/login', json=creds)
        assert code >= 200 and code < 300, f"Failed to login: {code}"
        assert type(response) == dict, f"Expected response type 'dict', got: '{type(response)}'"
        
        self.admin_token = response.get('token', '')
        assert self.admin_token != '', "Token is absent in response body"

    def create_faculty(self):
        """Create a faculty member for testing"""
        faculty_data = {
            'name': 'Test Faculty',
            'email': f'faculty{random.randint(0, 99999)}@email.com',
            'password': 'faculty123',
            'dateOfJoin': '2020-01-01',
            'seniorityScore': 2,
            'mobilityScore': 1
        }
        
        response, code = self.http_request(
            HttpTestType.POST, 'faculty/create', json=faculty_data
        )
        assert code >= 200 and code < 300, f"Failed to create faculty: {code}"

    def create_course(self):
        """Create a course for testing"""
        course_data = {
            'code': f'CS{random.randint(100, 999)}',
            'name': 'Test Course',
            'taughtBy': []
        }
        
        headers = {
            "Content-Type": "application/json",
            "Authorization": f"Bearer {self.admin_token}"
        }
        
        response, code = self.http_request(
            HttpTestType.POST, 'courses/create', headers=headers, json=course_data
        )
        assert code == 201, f"Failed to create course: {code}"
        
        # Get course ID from response or list
        courses_response, code = self.http_request(
            HttpTestType.GET, 'admin/courses', headers=headers
        )
        if code == 200 and type(courses_response) == list and len(courses_response) > 0:
            self.course_id = courses_response[-1].get('id')

    def test_admin_crud_courses(self):
        """Test admin CRUD operations on courses"""
        self.create_admin_user()
        
        # Create course
        course_data = {
            'code': f'TEST{random.randint(1000, 9999)}',
            'name': 'Test Course for CRUD',
            'hoursRequiredPerWeek': 3
        }
        
        headers = {
            "Content-Type": "application/json",
            "Authorization": f"Bearer {self.admin_token}"
        }
        
        # Create
        response, code = self.http_request(
            HttpTestType.POST, 'courses/create', headers=headers, json=course_data
        )
        assert code == 201, f"Failed to create course: {code}"
        
        # Get all courses
        courses, code = self.http_request(
            HttpTestType.GET, 'admin/courses', headers=headers
        )
        assert code == 200, f"Failed to get courses: {code}"
        assert type(courses) == list, f"Expected list, got: {type(courses)}"
        
        # Find the course we just created
        created_course = None
        for course in courses:
            if course.get('code') == course_data['code']:
                created_course = course
                break
        
        assert created_course is not None, "Created course not found in list"
        course_id = created_course.get('id')
        assert course_id is not None, "Course ID is missing"
        
        # Get course by ID
        course, code = self.http_request(
            HttpTestType.GET, f'admin/courses/{course_id}', headers=headers
        )
        assert code == 200, f"Failed to get course by ID: {code}"
        assert course.get('code') == course_data['code'], "Course code mismatch"
        
        # Update course
        update_data = {
            'name': 'Updated Course Name',
            'hoursRequiredPerWeek': 4
        }
        updated_course, code = self.http_request(
            HttpTestType.PUT, f'admin/courses/{course_id}', headers=headers, json=update_data
        )
        assert code == 200, f"Failed to update course: {code}"
        assert updated_course.get('name') == update_data['name'], "Course name not updated"
        
        # Delete course
        response, code = self.http_request(
            HttpTestType.DELETE, f'admin/courses/{course_id}', headers=headers
        )
        assert code == 200, f"Failed to delete course: {code}"

    def test_admin_crud_faculties(self):
        """Test admin CRUD operations on faculties"""
        self.create_admin_user()
        
        headers = {
            "Content-Type": "application/json",
            "Authorization": f"Bearer {self.admin_token}"
        }
        
        # Get all faculties
        faculties, code = self.http_request(
            HttpTestType.GET, 'admin/faculties', headers=headers
        )
        assert code == 200, f"Failed to get faculties: {code}"
        assert type(faculties) == list, f"Expected list, got: {type(faculties)}"
        
        if len(faculties) > 0:
            faculty_id = faculties[0].get('id')
            assert faculty_id is not None, "Faculty ID is missing"
            
            # Get faculty by ID
            faculty, code = self.http_request(
                HttpTestType.GET, f'admin/faculties/{faculty_id}', headers=headers
            )
            assert code == 200, f"Failed to get faculty by ID: {code}"
            
            # Update faculty
            update_data = {
                'rating': 4.5,
                'maxHoursPerWeek': 15
            }
            updated_faculty, code = self.http_request(
                HttpTestType.PUT, f'admin/faculties/{faculty_id}', headers=headers, json=update_data
            )
            assert code == 200, f"Failed to update faculty: {code}"
            assert updated_faculty.get('rating') == update_data['rating'], "Faculty rating not updated"

    def test_automatic_assignment(self):
        """Test automatic course assignment flow"""
        # This is a placeholder test - actual implementation would require:
        # 1. Creating academic term
        # 2. Creating preference sets
        # 3. Submitting preferences
        # 4. Running assignment
        # For now, just verify the endpoint exists
        self.create_admin_user()
        
        headers = {
            "Content-Type": "application/json",
            "Authorization": f"Bearer {self.admin_token}"
        }
        
        # This would need a valid term ID
        assignment_request = {
            'termId': '00000000-0000-0000-0000-000000000000'  # Placeholder
        }
        
        # We expect this to fail with a proper error, not crash
        response, code = self.http_request(
            HttpTestType.POST, 'admin/assignments/run', headers=headers, json=assignment_request
        )
        # Should return 400 (Bad Request) for invalid term, not 500
        assert code >= 400 and code < 500, f"Expected 4xx error for invalid term, got: {code}"

def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("action", choices=["up", "down", "server", "dev", "test"])
    parser.add_argument("--test-suite", choices=["login", "assignment"], help="Which test suite to run")

    args = parser.parse_args()

    if args.action == "up":
        run_cmd(INFRA_UP)
    elif args.action == "down":
        run_cmd(INFRA_DOWN)
    elif args.action == "server":
        os.execvp(str(MVNW), [str(MVNW), "spring-boot:run"])
    elif args.action == "dev":
        run_cmd(INFRA_UP)
        print("Waiting a bit for containers to initialize...")
        time.sleep(5)
        print("Handing over control to Spring Boot ...")
        os.execvp(str(MVNW), [str(MVNW), "spring-boot:run"])
    elif args.action == "test":
        test_suite = getattr(args, 'test_suite', None)
        if test_suite == "assignment" or test_suite is None:
            assignment_tests = CourseAssignmentTests('backend', TestLogger)
            assignment_tests.execute_tests()
        if test_suite == "login" or test_suite is None:
            login_tests = LoginAndUserInfoTests('backend', TestLogger)
            login_tests.execute_tests()

if __name__ == "__main__":
    main()

