# Mission 8 체크리스트

---

## 코드 작업 Breakdown

### 커밋 계획

| # | 커밋 제목 | 대상 파일 |
|---|-----------|-----------|
| 1 | `feat: Docker 컨테이너화 구성` | `Dockerfile`, `.dockerignore`, `docker-compose.yml`, `.env.example`, `.gitignore` |
| 2 | `fix: PostgreSQL 헬스체크 및 S3 환경변수 추가` | `docker-compose.yml`, `.env.example` |
| 3 | `chore: AWS S3 SDK 의존성 추가` | `build.gradle` |
| 4 | `feat: storage 설정 환경변수화 및 S3 config 추가` | `application.yaml` |
| 5 | `feat: S3BinaryContentStorage 구현` | `S3BinaryContentStorage.java` |
| 6 | `test: AWSS3Test 작성` | `AWSS3Test.java` |
| 7 | `test: S3BinaryContentStorageTest 작성` | `S3BinaryContentStorageTest.java` |

### 커밋 없는 작업 (AWS 콘솔 직접 작업)
- S3 버킷 생성, IAM 사용자/키 발급
- RDS 인스턴스 생성, EC2(SSH 터널) + DataGrip으로 DB 초기화
- ECR 퍼블릭 레포 생성 + 멀티플랫폼 이미지 빌드 & push
- ECS 클러스터/태스크/서비스 구성
- `discodeit.env` 작성 & S3 업로드 (형상관리 제외)

### 주요 검토사항
- `docker-compose.yml`: PostgreSQL 헬스체크 + `condition: service_healthy` 추가 필요
- `BinaryContentControllerTest`: S3 적용 후 다운로드가 `302 FOUND` 반환 — 기존 테스트(`200 OK`)와 의도 확인 필요
- 환경변수 미설정 시 S3 프로퍼티는 빈 문자열 기본값으로 앱 기동 보장

---

## 1. 애플리케이션 컨테이너화

### Dockerfile 작성
- [x] Amazon Corretto 17 이미지를 베이스 이미지로 사용
- [x] 작업 디렉토리 설정 (`/app`)
- [x] 프로젝트 파일 복사 (불필요 파일은 `.dockerignore`로 제외)
- [x] Gradle Wrapper로 애플리케이션 빌드
- [x] 80 포트 노출
- [x] 프로젝트 정보 환경 변수 설정
  - `PROJECT_NAME=discodeit`
  - `PROJECT_VERSION=1.2-M8`
- [x] JVM 옵션 환경 변수 설정 (`JVM_OPTS`, 기본값 빈 문자열)
- [x] 환경 변수를 활용한 애플리케이션 실행 명령어 설정

### 이미지 빌드 및 실행 테스트
- [ ] Docker 이미지 빌드 및 태그(`local`) 지정
  ```bash
  docker build -t discodeit:local .
  ```
- [ ] 컨테이너 실행 및 애플리케이션 테스트
  - prod 프로필로 실행
  - 로컬 PostgreSQL 연결
  - http://localhost:8081 접속 확인
  ```bash
  docker run -p 8081:80 \
    -e SPRING_PROFILES_ACTIVE=prod \
    -e SPRING_DATASOURCE_URL=jdbc:postgresql://host.docker.internal:5432/discodeit \
    -e SPRING_DATASOURCE_USERNAME=discodeit_user \
    -e SPRING_DATASOURCE_PASSWORD=discodeit1234 \
    discodeit:local
  ```

### Docker Compose 구성
- [x] 애플리케이션 + PostgreSQL 서비스 포함
- [x] 모든 환경 변수 설정 (`.env` 파일 활용, `.env`는 형상관리 제외)
- [x] 애플리케이션 서비스를 로컬 Dockerfile에서 빌드하도록 구성
- [x] 앱 볼륨 구성 (BinaryContentStorage 데이터 유지)
- [x] PostgreSQL 볼륨 구성 (DB 데이터 유지)
- [x] PostgreSQL 시작 후 `schema.sql` 자동 실행 구성
- [x] 서비스 간 의존성 설정 (`depends_on`)
- [x] 포트 매핑 구성
- [ ] Docker Compose로 서비스 시작 및 테스트
  ```bash
  docker compose up --build
  ```

---

## 2. BinaryContentStorage 고도화 (AWS S3)

### AWS S3 버킷 구성 (AWS 콘솔 직접 작업)
- [ ] AWS S3 버킷 생성
- [ ] 버킷 이름: `discodeit-binary-content-storage-(사용자 이니셜)` 형식
- [ ] 퍼블릭 액세스 차단 설정 활성화 (모든 퍼블릭 액세스 차단)
- [ ] 버전 관리 비활성화 상태 유지

### AWS S3 접근을 위한 IAM 구성 (AWS 콘솔 직접 작업)
- [ ] IAM 사용자(`discodeit`) 생성
- [ ] `AmazonS3FullAccess` 권한 할당
- [ ] 생성된 사용자에 액세스 키 생성
- [ ] 발급받은 키 포함 AWS 관련 정보를 `.env` 파일에 추가
  ```
  AWS_S3_ACCESS_KEY=엑세스_키
  AWS_S3_SECRET_KEY=시크릿_키
  AWS_S3_REGION=ap-northeast-2
  AWS_S3_BUCKET=버킷_이름
  ```
- [ ] PR에 `.env` 파일 별도 첨부 (액세스 키·시크릿 키는 제외)

### AWS S3 테스트
- [ ] AWS S3 SDK 의존성 추가 (`software.amazon.awssdk:s3:2.31.7`)
- [ ] `AWSS3Test` 클래스 작성
  - 패키지: `com.sprint.mission.discodeit.storage.s3`
  - `Properties` 클래스로 `.env`의 AWS 정보 로드
  - 테스트 메소드: 업로드 / 다운로드 / PresignedUrl 생성

### AWS S3를 활용한 BinaryContentStorage 고도화 (코드 작업)
- [ ] `S3BinaryContentStorage` 구현
  - 패키지: `com.sprint.mission.discodeit.storage.s3`
  - `BinaryContentStorage` 인터페이스 구현
  - `discodeit.storage.type=s3` 일 때만 Bean 등록 (`@ConditionalOnProperty`)
  - 필드: `accessKey`, `secretKey`, `region`, `bucket`
  - private 메소드: `getS3Client()`, `generatePresignedUrl(String key, String contentType)`
  - `download()` 메소드: PresignedUrl 리다이렉트 방식으로 구현 (`302 FOUND`)
- [ ] `S3BinaryContentStorageTest` 작성 (구현과 함께 진행)
- [ ] `application.yaml` 수정 — 스토리지 설정을 환경 변수로 유연하게 제어
  ```yaml
  discodeit:
    storage:
      type: ${STORAGE_TYPE:local}          # local | s3 (기본값: local)
      local:
        root-path: ${STORAGE_LOCAL_ROOT_PATH:.discodeit/storage}
      s3:
        access-key: ${AWS_S3_ACCESS_KEY:}
        secret-key: ${AWS_S3_SECRET_KEY:}
        region: ${AWS_S3_REGION:ap-northeast-2}
        bucket: ${AWS_S3_BUCKET:}
        presigned-url-expiration: ${AWS_S3_PRESIGNED_URL_EXPIRATION:600}
  ```
- [ ] AWS 관련 정보는 `.env` 파일로 관리 (형상관리 제외)
- [ ] Docker Compose에서 스토리지 관련 환경 변수 주입 가능하도록 수정

---

## 3. AWS 배포 (AWS ECS, RDS)

### AWS RDS 구성 (AWS 콘솔 직접 작업)
- [ ] AWS RDS PostgreSQL 인스턴스 생성
  - 엔진: PostgreSQL 17.6-R2
  - 템플릿: **프리티어** ⚠️
  - DB 인스턴스 식별자: `discodeit-db`
  - 마스터 사용자 이름: `postgres`
  - 자격 증명 관리: 자체 관리 (암호 따로 메모)
  - DB 인스턴스 클래스: `db.t4g.micro`
  - **퍼블릭 액세스: 아니오** ⚠️
  - 데이터베이스 포트: 5432
  - **모니터링 보존기간: 7일** ⚠️
  - **추가 모니터링 설정: 모두 체크 해제** ⚠️
  - **자동 백업 활성화: 체크 해제** ⚠️
- [ ] 과금 항목 최종 확인
  - [ ] 템플릿: 프리티어
  - [ ] 퍼블릭 액세스: 아니오
  - [ ] 모니터링 보존기간: 7일
  - [ ] 추가 모니터링 설정: 모두 체크 해제
  - [ ] 자동 백업: 체크 해제

### SSH 터널링용 EC2 구성 (AWS 콘솔 직접 작업)
- [ ] EC2 인스턴스 생성 (`rds-ssh`)
  - 인스턴스 유형: `t3.micro` ⚠️
  - 키 페어: 새 키 페어 생성 (`.pem` 파일 저장 위치 기억)
  - 네트워크 설정: 기존 보안 그룹 선택
- [ ] 보안 그룹 인바운드 규칙 편집
  - 유형: SSH / 소스: 내 IP
  - ※ 네트워크(와이파이) 변경 시 재수정 필요
- [ ] DataGrip에서 SSH 터널링으로 RDS 연결
  - SSH/SSL > Use SSH tunnel 활성화 (`.pem` 파일 활용)
- [ ] 연결 성공 후 DB·유저·테이블 초기화
  ```sql
  CREATE USER discodeit_user WITH PASSWORD 'discodeit1234';
  GRANT discodeit_user TO postgres;
  CREATE DATABASE discodeit OWNER discodeit_user;
  -- 이후 schema.sql 실행
  ```
- [ ] 초기화 완료 후 `rds-ssh` EC2 인스턴스 **삭제** (중지 X) ⚠️

### AWS ECR 구성 (AWS 콘솔 + CLI)
- [ ] ECR 퍼블릭 레포지토리 생성 (`discodeit`)
  - ※ 프라이빗은 용량 제한 있으므로 퍼블릭으로 생성
- [ ] AWS CLI 설치
- [ ] `aws configure` 실행 후 `discodeit` IAM 사용자 정보 입력
  - 액세스 키 / 시크릿 키
  - region: `ap-northeast-2`
  - output format: `json`
- [ ] `discodeit` IAM 사용자에 ECR 권한 추가
  - `AmazonElasticContainerRegistryPublicFullAccess`
- [ ] Docker 클라이언트 레지스트리 인증 (ECR 콘솔 > 레포지토리 > 푸시 명령 참고)
  ```bash
  aws ecr-public get-login-password --region us-east-1 | \
    docker login --username AWS --password-stdin public.ecr.aws/...
  ```
- [ ] 멀티플랫폼 이미지 빌드 및 ECR push
  - 태그: `latest`, `1.2-M8`
  - 멀티플랫폼: `linux/amd64,linux/arm64`
- [ ] AWS 콘솔에서 푸시된 이미지 확인

### AWS ECS 구성 (AWS 콘솔 직접 작업)
- [ ] ECS용 환경 변수 파일 `discodeit.env` 작성 후 S3에 업로드
  ```
  SPRING_PROFILES_ACTIVE=prod
  STORAGE_TYPE=s3
  AWS_S3_ACCESS_KEY=엑세스_키
  AWS_S3_SECRET_KEY=시크릿_키
  AWS_S3_REGION=ap-northeast-2
  AWS_S3_BUCKET=버킷_이름
  AWS_S3_PRESIGNED_URL_EXPIRATION=600
  RDS_ENDPOINT=RDS_엔드포인트(포트 포함)
  SPRING_DATASOURCE_URL=jdbc:postgresql://${RDS_ENDPOINT}/discodeit
  SPRING_DATASOURCE_USERNAME=discodeit_user
  SPRING_DATASOURCE_PASSWORD=RDS_비밀번호
  JVM_OPTS="-Xmx384m -Xms256m -XX:MaxMetaspaceSize=64m -XX:+UseSerialGC"
  ```
  - ※ 이 파일은 형상관리하지 않도록 주의
- [ ] ECS 클러스터 생성 (`discodeit-cluster`)
  - 인프라: Fargate 체크 해제 / **EC2 인스턴스** 선택
  - EC2 인스턴스 유형: `t3.micro` ⚠️
  - 원하는 용량: 최소 0, 최대 1 ⚠️
  - SSH 키 페어: 새 키 페어 생성 후 지정
- [ ] 태스크 정의 (`discodeit-task`)
  - 시작 유형: Fargate 체크 해제 / **Amazon EC2 인스턴스** 선택
  - 네트워크 모드: `bridge`
  - 태스크 크기: CPU 0.25 vCPU / 메모리 0.5 GB
  - 컨테이너: 이름 `discodeit-app` / 이미지 URI: ECR 이미지
  - 포트 매핑: 호스트 80 → 컨테이너 80
  - 리소스 제한: CPU 0.25 vCPU / 메모리 하드 0.5 GB / 소프트 0.25 GB
  - 환경 변수: S3에 업로드한 `discodeit.env` 파일 지정
- [ ] 태스크 실행 역할에 S3 관련 권한 추가 (환경 변수 파일 읽기용)
- [ ] 서비스 생성 (`discodeit-service`)
  - 태스크 정의: `discodeit-task`
  - 원하는 태스크: 1
  - 상태 검사 유예 기간: 30초
- [ ] EC2 보안 그룹 인바운드 규칙 설정
  - 유형: HTTP / 소스: Anywhere-IPv4
- [ ] 태스크 실행 완료 후 EC2 퍼블릭 IP로 접속 확인

---

## 4. [심화 - 선택] 이미지 최적화 및 CI/CD 파이프라인 구축

> ⚠️ 필수 요구사항이 아닙니다. 기본 요구사항 완료 후 시간이 남을 경우 진행하세요.

### 이미지 최적화
- [ ] 멀티 스테이지 빌드 적용 (빌드 스테이지 + 런타임 스테이지)
  - 태그명: `local-slim`
  - 기존 이미지(`local` 또는 `1.2-M8`)와 크기 비교
- [ ] 이미지 레이어 캐시를 고려한 Dockerfile 수정

### CI — 지속적 통합 (GitHub Actions)
- [ ] `.github/workflows/test.yml` 파일 생성
  - `main` 브랜치에 PR 생성 시 실행
  - 테스트 실행 Job 정의
- [ ] CodeCov 연동하여 테스트 커버리지 뱃지를 README에 추가

### CD — 지속적 배포 (GitHub Actions)
- [ ] `.github/workflows/deploy.yml` 파일 생성
  - `release` 브랜치에 push 시 실행
- [ ] GitHub 레포지토리 시크릿 추가
  - `AWS_ACCESS_KEY`: IAM 사용자 액세스 키
  - `AWS_SECRET_KEY`: IAM 사용자 시크릿 키
- [ ] GitHub 레포지토리 변수 추가
  - `AWS_REGION`: `ap-northeast-2`
  - `ECR_REPOSITORY_URI`: ECR 레포지토리 URI
  - `ECS_CLUSTER`: `discodeit-cluster`
  - `ECS_SERVICE`: `discodeit-service`
  - `ECS_TASK_DEFINITION`: `discodeit-task`
- [ ] Docker 이미지 빌드 및 푸시 Job
  - AWS CLI 설정 Step (Public ECR이므로 리전 `us-east-1`)
  - Public ECR 로그인 Step
  - 이미지 빌드 및 푸시 Step
    - 멀티플랫폼 옵션 제외 (GitHub Actions 및 ECS 모두 x86_64)
    - 태그: `latest` + GitHub 커밋 해시
- [ ] ECS 서비스 업데이트 Job
  - AWS CLI 설정 Step (리전: `AWS_REGION`)
  - 새 이미지를 사용하도록 태스크 정의 업데이트 Step
  - 기존 구동 중인 서비스 중단 Step (프리티어 고려)
    - `aws ecs update-service --desired-count` 활용
  - 새 태스크 정의로 ECS 서비스 업데이트 Step
- [ ] AWS 콘솔에서 새 태스크 정의로 배포 확인

---

## 5. PR 제출 시 포함 항목

- [ ] `.env` 파일 첨부 (AWS 액세스 키·시크릿 키는 제외)
- [ ] **RDS**
  - [ ] AWS 콘솔 RDS 인스턴스 상세 페이지 스크린샷
  - [ ] DataGrip SSH 터널링 연결 스크린샷 (생성된 테이블 목록 포함)
- [ ] **ECR**
  - [ ] 푸시된 이미지가 보이는 AWS 콘솔 페이지 스크린샷
- [ ] **ECS**
  - [ ] 실행 중인 태스크 구성정보 AWS 콘솔 스크린샷
  - [ ] 배포된 EC2 엔드포인트 (URL)
- [ ] **VPC**
  - [ ] 보안 그룹 인바운드 규칙 AWS 콘솔 스크린샷
- [ ] **IAM**
  - [ ] 사용자 권한 정책 AWS 콘솔 스크린샷
