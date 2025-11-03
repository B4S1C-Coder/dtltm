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
                except AssertionError as aerr:
                    fprint.fail(f"{test} - {aerr}", self.__log)
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
    self.token: str|None = None
    self.creds = {
        'email': 'test@email.com',
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
        
        temp_token = response.get('token', '')
        assert temp_token != '', "Token is absent in response body"

        self.token = temp_token

    def user_info(self):
        response, code = self.http_request(
            HttpTestType.POST, 'users/current-user-info', headers={
                "Content-Type": "application/json",
                "Authorization": f"Bearer {self.token}"
            }
        )

        assert type(response) == dict, f"Expected response type 'dict', got: '{type(response)}'"
        assert code >= 200 and code < 300, f"Response code violates 2XX range: {code}"

def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("action", choices=["up", "down", "server", "dev", "test"])

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
        login_tests = LoginAndUserInfoTests('backend', TestLogger)
        login_tests.execute_tests()

if __name__ == "__main__":
    main()

