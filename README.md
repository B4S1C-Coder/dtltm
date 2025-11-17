[![Ask DeepWiki](https://deepwiki.com/badge.svg)](https://deepwiki.com/B4S1C-Coder/dtltm)
# Departmental Load & Timetable Management System
A centralized dynamic system to manage teaching loads and manage timetables on the fly. More details soon.

## Architecture
Architecture and techstack of the project is as follows:
![Architecture](docs/dbms_hld.jpg)

## Build & Containerization Process (for a service)
A fat `.jar` file is built on the host machine outside docker (`BUILDER STAGE`). Then the fat `.jar` is placed in the container and run via `gcr.io/distroless/java21` (`RUNNER STAGE`).

![BuildSystem](docs/build.jpg)
