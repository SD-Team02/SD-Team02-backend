# 코드 컨벤션 & Git 전략

## 1. 패키지 구조

요구사항 문서에서 "Controller-Service-Repository 계층으로 구성된 클린 아키텍쳐 권장, 헥사고날 등 다른 아키텍처는 학습만 하고 적용은 지양"이라고 명시하고 있어, 도메인 패키지 내부는 전통적인 계층형(Layered) 구조를 사용한다. (application/domain/infrastructure/presentation처럼 세분화하지 않는다.)

```
com.example.delivery
├── global                     # 공통/인프라 담당 전용 (다른 도메인은 여기 직접 수정 금지, PR로 요청)
│   ├── common
│   │   ├── entity/BaseEntity.java, BaseTimeEntity.java
│   │   ├── response/ApiResponse.java, PageResponse.java
│   │   └── util/PageableFactory.java
│   ├── config/                # Security, Swagger, JPA Auditing 등
│   └── exception/              # ErrorCode, BusinessException, GlobalExceptionHandler
│
├── {domain}                    # 예: order, store, menu, user, payment, review
│   ├── controller/              # {Domain}Controller
│   ├── service/                 # {Domain}Service (트랜잭션 경계)
│   ├── repository/              # Spring Data JPA Repository + QueryDSL 구현체(OOORepositoryImpl)
│   ├── entity/                  # JPA Entity (BaseEntity 상속)
│   ├── dto/
│   │   ├── request/              # Req{Action}{Domain}Dto
│   │   └── response/             # Res{Action}{Domain}Dto
│   └── client/                  # 외부 API 클라이언트가 필요한 도메인만 (예: review/client/GeminiClient)
└── DeliveryApplication.java
```

- 각 도메인 패키지는 위 6개 하위 패키지(controller/service/repository/entity/dto/client) 밖으로 더 쪼개지 않는다. 로직이 복잡해져도 `service` 안에서 클래스를 나누는 식으로 해결하고, 새로운 계층 개념(예: `usecase`, `facade`)을 임의로 추가하지 않는다.
- 도메인 간 직접 참조는 지양한다. 다른 도메인의 엔티티가 필요하면 해당 도메인의 Repository/Service를 주입받아 사용하고, `global`은 모든 도메인이 공유하는 코드만 둔다.

## 2. 네이밍 규칙

- 클래스: `PascalCase`. Entity는 접미사 없이 도메인명 그대로 (`Order`, `Store`), 필요시 `OrderEntity`처럼 통일 (팀 합의 필요 - 기본은 접미사 없음 권장)
- DTO: `Req{Action}{Domain}Dto` / `Res{Action}{Domain}Dto` 형태 권장
    - 예: `ReqCreateOrderDto`, `ResGetOrderDto`
- Service: `{Domain}Service`
- Controller: `{Domain}Controller`, 엔드포인트는 `/api/{domain}`
- `V1` 같은 버전 접미사는 붙이지 않는다. 실제로 버전 분기가 필요해지면 그때 팀 합의로 도입한다.
- 상수: `UPPER_SNAKE_CASE`
- 테이블명: `p_` 접두어 + snake_case (예: `p_order_item`)
- 컬럼명: snake_case (Hibernate 기본 전략 사용, Entity 필드는 camelCase 그대로 두면 자동 변환됨)
- JSON 요청/응답 필드명: camelCase (예: `storeName`, `createdAt`). Jackson 기본 직렬화 방식 그대로 사용하며 별도 Naming Strategy 설정을 추가하지 않는다. DB 컬럼(snake_case)과는 Hibernate가 자동 매핑하므로 신경 쓸 필요 없음. 프론트엔드와 API 명세 공유 시 이 규칙을 먼저 안내할 것.

## 3. Entity / DTO 규칙

- 모든 Entity는 `BaseEntity`를 상속한다. 단, 수정/삭제가 없는 append-only 테이블(AI 요청 로그 등)은 `BaseTimeEntity`를 상속한다.
- PK는 UUID를 사용한다. 단, `User` Entity는 예외적으로 autoincrement id를 PK로 사용한다.
- Setter를 열어두지 않는다. 상태 변경은 의미가 드러나는 메서드로 구현한다.
    - 나쁜 예: `order.setStatus(ACCEPTED)`
    - 좋은 예: `order.accept()` (내부에서 상태 전이 유효성 검사 포함)
- 연관관계는 기본 `LAZY` 로딩. `@ManyToOne(fetch = FetchType.LAZY)`
- 삭제는 반드시 `softDelete(username)` 사용. `deleteById` 직접 호출 금지.
- 상품(메뉴) 숨김은 `isHidden` 필드로 별도 관리 (soft delete와 다른 필드).
- 요청 DTO는 Bean Validation(`@NotBlank`, `@Size`, `@Pattern` 등)으로 유효성 검사를 건다.
- 응답은 항상 `ApiResponse<T>`로 감싸서 반환한다. 컨트롤러 메서드 반환 타입은 `ResponseEntity<ApiResponse<T>>`.

## 4. 검색(Search) API 공통 규칙

- 페이지 크기는 10/30/50만 허용, 그 외 값은 자동으로 10건으로 고정한다 → `PageableFactory.of(page, size, sortBy, direction)` 사용.
- 기본 정렬은 `createdAt DESC`.
- Query Parameter 예시: `GET /api/stores?page=0&size=10&sortBy=createdAt&direction=DESC&category=CHICKEN&keyword=교촌`

## 5. 권한 체크 규칙

- 컨트롤러 메서드에 `@PreAuthorize("hasRole('OWNER')")` 등으로 명시적으로 권한을 건다. 권한 상수는 인증 담당자가 정의하는 `Role` enum을 그대로 사용한다.
- 필드 단위 권한: 예를 들어 주문 상태(`status`)는 `OWNER`/`MANAGER`/`MASTER`만 수정 가능하고 `CUSTOMER`는 불가하다. 이런 경우 요청 DTO를 역할별로 분리하거나(`ReqUpdateOrderStatusDto`는 OWNER 전용), Service 계층에서 현재 로그인 유저의 role을 검사해 `BusinessException(ErrorCode.ACCESS_DENIED)`를 던진다. Setter로 값이 그대로 반영되지 않도록 주의.
- 본인 소유 리소스 체크(예: 가게 주인이 본인 가게만 수정 가능)는 Service 계층에서 `store.getOwnerUsername().equals(currentUsername)` 형태로 검증한다.

## 6. 예외 처리 규칙

- 도메인 로직에서 실패 케이스는 `throw new BusinessException(ErrorCode.XXX)`로 던진다. try-catch로 감싸서 임의 응답을 만들지 않는다.
- 새 에러 코드가 필요하면 `global/exception/ErrorCode.java`에 도메인 접두어(`ORDER_`, `STORE_` 등)로 추가하고 PR을 올린다.

## 7. 테스트 규칙

- Repository: `@DataJpaTest` + H2(or Testcontainers)로 CRUD/커스텀 쿼리 테스트
- Service: Mockito로 Repository를 mock 처리한 단위 테스트. 성공 케이스 + 실패 케이스(예외 발생 조건) 모두 작성
- 테스트 클래스명: `{클래스명}Test`, 메서드명은 `{동작}_{조건}_{기대결과}` 형태 권장 (예: `cancelOrder_5분초과시_예외발생`)

## 8. 이슈 기반 작업

모든 기능 단위 작업은 GitHub Issue를 먼저 생성한 후 진행한다.

작업 흐름:
1. GitHub Issue 생성
2. Issue 번호 확인
3. Issue 번호를 포함한 브랜치 생성
4. 기능 단위로 작업 및 커밋
5. 작업 완료 후 Pull Request 생성
6. PR에서 관련 Issue 연결

```bash
git checkout dev
git pull origin dev
git checkout -b feat/#23/login
```

## 9. 브랜치 전략

- `main`: 배포 브랜치. 항상 배포 가능한 상태 유지. 직접 push 금지, PR + 리뷰 승인 후 merge만 허용
- `dev`: 통합 개발 브랜치. 기능 브랜치들이 여기로 먼저 merge. GitHub Ruleset(`dev-require-pr`)으로 직접 push/force-push/브랜치 삭제가 막혀 있고 PR을 통해서만 merge 가능하다. 단 승인(approval) 강제는 걸려 있지 않으므로(`required_approving_review_count: 0`), 리뷰를 받는 것은 관행(11번 항목 참고)으로 지켜야 한다.
- 브랜치명: `타입/#이슈번호/기능명`

| 타입 | 설명 |
|---|---|
| feat | 기능 추가 |
| fix | 버그 수정 |
| refactor | 리팩토링 |
| docs | 문서 작업 |
| chore | 설정/빌드 관련 |
| test | 테스트 코드 |
| hotfix | 운영 긴급 수정 (main에서 분기 후 main/dev 양쪽에 merge) |

예: `feat/#23/login`, `fix/#31/signup-validation`, `hotfix/#45/payment-timeout`

## 10. 커밋 메시지 규칙 (Conventional Commits 기반)

```
feat: 로그인 API 연동
fix: 페이지 사이즈 검증 로직 오류 수정
docs: ERD 초안 업데이트
test: OrderService 단위 테스트 추가
refactor: BaseEntity soft delete 메서드명 변경
chore: build.gradle 의존성 정리
```

- 커밋은 의미 있는 작업 단위마다 자주 (기능 완성 단위가 아니라 함수 하나, API 연결 완료 등)

## 11. Pull Request 규칙

- 작은 단위로 자주 PR을 올린다 (도메인 전체를 한 번에 올리지 않는다).
- PR 본문에 `close #이슈번호` 작성 → 머지 시 이슈 자동 종료
- PR 템플릿(`.github/pull_request_template.md`) 항목을 채운다.
- **리뷰**: 인프라 담당자 또는 그 외 팀원 최소 1인 이상에게 코드 리뷰를 받고, 리뷰 내용을 반영한다.
- **머지**: 작성자 본인이 아닌 다른 사람이 merge한다.
- 리뷰는 감정을 배제하고 근거를 들어 코멘트한다 (예: "이 부분은 N+1이 발생할 수 있어요, fetch join 고려해주세요").
- `.github/CODEOWNERS`에 등록된 리뷰어(@subeeen)가 PR 생성 시 자동으로 리뷰 요청됨.

## 12. 이슈/PR 템플릿

- 버그 리포트: `.github/ISSUE_TEMPLATE/bug_report.md`
- 기능 요청: `.github/ISSUE_TEMPLATE/feature_request.md`
- PR: `.github/pull_request_template.md`
- 자동 리뷰어 지정: `.github/CODEOWNERS`