# Monew - 1조 할머니 보쌈

![cover](src/main/resources/assets/raw.png)

## 프로젝트 소개

> 여러 뉴스 API를 통합하여 사용자에게 맞춤형 뉴스를 제공하고, 의견을 나눌 수 있는 소셜 기능을 갖춘 서비스
>
> 프로젝트 기간: 2025.07.08 ~ 2025.07.30

## 테스트 커버리지

[![codecov](https://codecov.io/gh/sb3-monew-team1/sb03-monew-team1/branch/dev/graph/badge.svg?token=QH0C73AD22)](https://codecov.io/gh/sb3-monew-team1/sb03-monew-team1)

## 팀원 구성

![image](src/main/resources/assets/Capture_2025-07-09_09-41-13.png)

<table>
  <thead>
    <tr>
      <th style="width: 25%;">조재구</th>
      <th style="width: 25%;">이지현</th>
      <th style="width: 25%;">이주용</th>
      <th style="width: 25%;">이채원</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td><img src="src/main/resources/assets/Capture_2025-07-09_09-51-00.png" style="width: 100%; height: auto;"></td>
      <td><img src="src/main/resources/assets/Capture_2025-07-09_09-40-37.png" style="width: 100%; height: auto;"></td>
      <td><img src="src/main/resources/assets/Capture_2025-07-09_09-40-22.png" style="width: 100%; height: auto;"></td>
      <td><img src="src/main/resources/assets/Capture_2025-07-09_09-40-03.png" style="width: 100%; height: auto;"></td>
    </tr>
    <tr>
      <td>BE / Infra</td>
      <td>BE / DevOps</td>
      <td>BE / Infra</td>
      <td>BE / DevOps</td>
    </tr>
    <tr>
      <td>뉴스 기사 관리, API 연동</td>
      <td>프로젝트 설정, 댓글 관리, CI/CD</td>
      <td>사용자 관리, 알림 관리, DB</td>
      <td>관심사 관리, 문서화</td>
    </tr>
    <tr>
      <td><a href="https://github.com/nine-j">nine-j</a></td>
      <td><a href="https://github.com/jhlee-codes">jhlee-codes</a></td>
      <td><a href="https://github.com/pureod">pureod</a></td>
      <td><a href="https://github.com/Chaewon3Lee">Chaewon3Lee</a></td>
    </tr>
  </tbody>
</table>

## 기술 스택 및 사용 도구

| 항목        | 사용 도구 / 기술                                                                                                                                                                                                                                                                                                                                                                                                                                                                                |
|-----------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Backend   | <img src="https://img.shields.io/badge/SpringBoot-6DB33F.svg?&logo=SpringBoot&logoColor=white"> <img src="https://img.shields.io/badge/SpringMVC-6DB33F.svg?&logo=Spring&logoColor=white"> <img src="https://img.shields.io/badge/SpringBatch-6DB33F.svg?&logo=SpringBatch&logoColor=white"> <img src="https://img.shields.io/badge/DataJPA-333333.svg?labelColor=6DB33F&logoColor=white"> <img src="https://img.shields.io/badge/QueryDSL-333333.svg?labelColor=088CD0&logoColor=white"> |
| Database  | <img src="https://img.shields.io/badge/PostgreSQL-17.5-333333.svg?labelColor=4169E1&logo=PostgreSQL&logoColor=white"> <img src="https://img.shields.io/badge/MongoDB-8.0-333333.svg?labelColor=082532&logo=MongoDB&logoColor=47A248"> <img src="https://img.shields.io/badge/H2DB-latest-333333.svg?labelColor=09476B&logo=h2database&logoColor=white">                                                                                                                                   |
| API 문서화   | <img src="https://img.shields.io/badge/swagger-000.svg?&logo=swagger&logoColor=white">                                                                                                                                                                                                                                                                                                                                                                                                    |
| 협업 도구     | <img src="https://img.shields.io/badge/GitKraken-179287.svg?&logo=gitkraken&logoColor=white"> <img src="https://img.shields.io/badge/GitHub-181717.svg?&logo=github&logoColor=white"> <img src="https://img.shields.io/badge/Discord-5865F2.svg?&logo=discord&logoColor=white"> <img src="https://img.shields.io/badge/Notion-000000.svg?&logo=Notion&logoColor=white">                                                                                                                   |
| 일정 관리     | <img src="https://img.shields.io/badge/Jira-0052CC.svg?&logo=jira&logoColor=white"> <img src="https://img.shields.io/badge/Notion-Timeline-333333.svg?&logo=Notion&labelColor=000000&logoColor=white">                                                                                                                                                                                                                                                                                    |
| 배포 & 모니터링 | <img src="https://img.shields.io/badge/AWS-ECR,ECS,RDS,S3-333333.svg?&logo=aws&labelColor=000000&logoColor=white"> <img src="https://img.shields.io/badge/Docker-2496ED.svg?&logo=docker&logoColor=white"> <img src="https://img.shields.io/badge/Prometheus-E6522C.svg?&logo=prometheus&logoColor=white"> <img src="https://img.shields.io/badge/Grafana-F46800.svg?&logo=grafana&logoColor=white">                                                                                      |
| IDE       | <img src="https://img.shields.io/badge/IntellijIDEA-000000.svg?&logo=intellijidea&logoColor=white">                                                                                                                                                                                                                                                                                                                                                                                       |

## 프로젝트 구조

```
src/main/java
└── com
    └── sprint
        └── mission
            └── sb03monewteam1
                ├── batch                # 배치 처리(스케줄, 대용량 작업)
                │   └── job              # 배치 작업(Job/Step 등) 구현
                ├── collector            # 외부 데이터 수집(크롤러, API 연동)
                ├── config               # 전역 설정(Spring, Swagger 등)
                ├── controller           # REST API 컨트롤러
                │   └── api              # API 엔드포인트 구현
                ├── dto                  # 데이터 전송 객체(DTO)
                │   ├── request          # 요청 DTO
                │   └── response         # 응답 DTO
                ├── entity               # JPA 엔티티(도메인 모델)
                │   └── base             # 공통 엔티티 속성
                ├── event                # 도메인 이벤트
                │   └── listener         # 이벤트 리스너
                ├── exception            # 예외 처리 클래스
                │   ├── article          # 게시글 관련 예외
                │   ├── comment          # 댓글 관련 예외
                │   ├── common           # 공통 예외
                │   ├── interest         # 관심사 관련 예외
                │   ├── notification     # 알림 관련 예외
                │   ├── user             # 사용자 관련 예외
                │   └── util             # 예외 유틸리티
                ├── interceptor          # HTTP 인터셉터
                ├── logging              # 로깅 관련
                │   └── aspect           # AOP 기반 로깅 등
                ├── mapper               # 엔티티-DTO 변환
                ├── repository           # 데이터 접근 레이어
                │   └── jpa              # JPA Repository
                ├── scheduler            # 스케줄러(정기 작업)
                ├── seeder               # 데이터 시드(seed) 기능
                ├── service              # 비즈니스 로직 서비스
                ├── storage              # 파일 등 외부 저장소 연동
                └── util                 # 공통
```

## 🛠 프로젝트 초기 셋업

프로젝트를 처음 클론하셨다면 다음 명령어를 실행해주세요:

```bash
npm install
```
