# CLAUDE.md

## Git 규칙
- git 관련 명령어는 **사용자가 직접 실행**한다.
- Claude는 커밋 타이밍과 커밋 제목(메시지)만 알려준다.
- git 명령어를 직접 실행하지 않는다 (git add, git commit, git push 등 일체 금지).

## 보안 규칙
- 보안 관련 폴더 및 파일(credentials, secrets, keys, .env, token 등)은 **절대 내용을 출력하지 않는다**.
- 해당 파일의 존재 여부 확인 정도만 허용하며, 내용은 숨김 처리한다.

## 미션 8 마일스톤
1. 애플리케이션 컨테이너화 (Docker)
2. BinaryContentStorage 고도화 (AWS S3)
3. AWS 배포 (AWS ECS + RDS)
4. CI/CD 파이프라인 구축 (GitHub Actions)

## AWS 비용 유의사항
- 프리티어 할당량 내에서 진행
- 미션 종료 후 모든 AWS 리소스(EC2, RDS, S3) **삭제** 필수 (중지 X)
- 불필요한 리소스 생성 최소화