# 1-sprint-mission

[![codecov](https://codecov.io/gh/gim00001/1-sprint-mission/branch/main/graph/badge.svg?token=48406464-18ac-4b05-9764-0b90961d8543)](https://codecov.io/gh/gim00001/1-sprint-mission)

# 🎮 Discodeit

[![codecov](https://codecov.io/gh/gim00001/1-sprint-mission/branch/main/graph/badge.svg?token=48406464-18ac-4b05-9764-0b90961d8543)](https://codecov.io/gh/gim00001/1-sprint-mission)
![Java](https://img.shields.io/badge/Java-17-orange?logo=openjdk)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.11-brightgreen?logo=springboot)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-17-blue?logo=postgresql)
![Docker](https://img.shields.io/badge/Docker-Containerized-2496ED?logo=docker)
![AWS](https://img.shields.io/badge/AWS-ECS%20%7C%20RDS%20%7C%20S3-FF9900?logo=amazonaws)

> Discord를 모티브로 한 채팅 애플리케이션 백엔드 서버

---

## 🚀 배포 정보

| 항목     | 값                                          |
|--------|--------------------------------------------|
| 배포 환경  | AWS ECS (EC2 t3.micro)                     |
| 엔드포인트  | http://15.165.44.183                       |
| API 문서 | http://15.165.44.183/swagger-ui/index.html |

---

## 🛠️ 기술 스택

| 분류        | 기술                          |
|-----------|-----------------------------|
| Language  | Java 17                     |
| Framework | Spring Boot 3.5.11          |
| Database  | PostgreSQL 17 (AWS RDS)     |
| ORM       | Spring Data JPA / Hibernate |
| Storage   | AWS S3                      |
| Container | Docker / AWS ECS            |
| CI/CD     | GitHub Actions              |
| Docs      | Swagger (SpringDoc OpenAPI) |

## 🛠️ Tech Stack

### ☕ Language

![Java 17](https://img.shields.io/badge/Java-17-007396?style=for-the-badge&logo=openjdk&logoColor=white)

### 🌱 Framework

![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.11-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)

### 🗄️ Database

![PostgreSQL](https://img.shields.io/badge/PostgreSQL-17%20%28AWS%20RDS%29-4169E1?style=for-the-badge&logo=postgresql&logoColor=white)

### 🔗 ORM

![Spring Data JPA](https://img.shields.io/badge/Spring%20Data%20JPA-6DB33F?style=for-the-badge&logo=spring&logoColor=white)
![Hibernate](https://img.shields.io/badge/Hibernate-59666C?style=for-the-badge&logo=hibernate&logoColor=white)

### ☁️ Storage

![AWS S3](https://img.shields.io/badge/AWS%20S3-569A31?style=for-the-badge&logo=amazons3&logoColor=white)

### 📦 Container

![Docker](https://img.shields.io/badge/Docker-2496ED?style=for-the-badge&logo=docker&logoColor=white)
![AWS ECS](https://img.shields.io/badge/AWS%20ECS-FF9900?style=for-the-badge&logo=amazonecs&logoColor=white)

### 🚀 CI/CD

![GitHub Actions](https://img.shields.io/badge/GitHub%20Actions-2088FF?style=for-the-badge&logo=githubactions&logoColor=white)

### 📚 API Docs

![Swagger](https://img.shields.io/badge/Swagger-85EA2D?style=for-the-badge&logo=swagger&logoColor=black)
![SpringDoc OpenAPI](https://img.shields.io/badge/SpringDoc%20OpenAPI-6DB33F?style=for-the-badge&logo=spring&logoColor=white)

## 🏗️ 아키텍처

```mermaid
graph TD
    A[🔧 GitHub Actions<br/>CI / CD] --> B[📦 Amazon ECR<br/>Docker Registry]
    B --> C[🚀 Amazon ECS<br/>EC2 t3.micro]
    C --> D[🗄️ Amazon RDS<br/>PostgreSQL 17]
    C --> E[🪣 Amazon S3<br/>Binary Storage]
```
