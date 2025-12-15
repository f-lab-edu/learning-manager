# 제품 요구사항 명세서 (PRD): Learning Manager

> v.0.0.9 | 25.12.14
---

## 1. 개요 (Overview)

**Learning Manager**는 회원의 온/오프라인 스터디 과정을 지원하는 온라인 서비스입니다. 기존 학원 운영 ERP 시스템의 반복적이고 비효율적인 업무(출석, 일정 관리 등)를 자동화하고, 체계적인 권한
관리를 통해 **학원 관리자와 수강생이 학습 과정 자체에만 집중할 수 있는 인프라를 제공**하는 것을 목표로 합니다.

본 프로젝트는 실제 상용 서비스를 참조하되, '스터디 관리'라는 핵심 컨셉에 맞춰 도메인 용어를 재구성하여 개인 프로젝트로서의 완성을 1차 목표로 합니다.

예를 들어 아래와 같이 실제 학원 ERP 시스템을 참조하되, '스터디 관리 서비스' 컨셉에 맞춰 도메인 용어를 재구성합니다.

- `과정 수강생` → `스터디 회원`
- `매니저` → `스터디 매니저`
- `과정` → `스터디 과정`
- `강의` → `스터디 세션`

---

## 2. 달성 목표 (Metric)

- **목표**: 스터디 운영 효율성 증대 및 학습 경험 향상
- **핵심 평가 지표 (Key Metrics)**:
    - **MAU (Monthly Active Users)**: 월간 활성 사용자 수
    - **스터디 개설 수**: 신규 생성된 스터디 과정의 수
    - **출석률**: 전체 세션 대비 평균 출석률

---

## 3. 기술 스택 (Tech Stacks)

### 언어 및 프레임워크

- **Java**: 21
- **Spring Boot**: 3

### 데이터베이스

- **MySQL**: 운영 RDB (Member, Course, Session 등 관계형 데이터)
- **MongoDB**: 출석 시스템용 NoSQL DB (이벤트 소싱 기반 출석 데이터)
- **H2**: 테스트용 인메모리 RDB

### 데이터 접근 계층

- **Spring Data JPA**: 기본 CRUD 및 단순 쿼리
- **QueryDSL 5.1**: 타입 안전한 동적 쿼리 (복잡한 조건 검색, DTO Projection)
- **Spring Data MongoDB**: MongoDB 접근 (Criteria API 기반 동적 쿼리)

---

## 4. 핵심 기능 명세

### 4.1 MVP

- **P0: 스터디 세션 일정 관리**: 주기적/단발성 세션 생성 및 관리, 월별/커리큘럼별 조회
- **P1: 스터디 과정 출석 관리**: 참여자 출석 여부 체크 및 출석률 통계 제공
- **P2: 각종 일정 알림**: 세션 시작 전 등 주요 일정에 대한 이메일 알림 발송

### 4.2 주요 요구사항

#### 4.2.1 사용자 및 가입

- **(P0) 회원 가입 및 상태 관리**:
    - **가입**: 사용자는 이메일과 비밀번호를 입력하여 시스템에 가입을 요청할 수 있다. 가입 요청 시 회원은 `PENDING` 상태가 된다.
    - **이메일 인증**: 가입 시 입력한 이메일로 인증 링크가 발송된다. 사용자가 링크를 클릭하면 계정이 `ACTIVE` 상태로 변경되어 정상적인 활동이 가능하다.
    - **상태**: 회원은 `ACTIVE`(활동), `INACTIVE`(휴면), `BANNED`(정지), `WITHDRAWN`(탈퇴) 상태를 가진다.
    - **비회원 접근**: 회원이 아니더라도 서비스의 주요 기능이나 정보를 소개하는 랜딩 페이지는 조회할 수 있다.

#### 4.2.2 스터디 과정 (Course)

- **(P1) 스터디 과정 개설**:
    - **스터디장 자격**: `SystemRole`이 `ADMIN`이거나, 별도의 승인 절차를 거친 `MEMBER`만이 스터디를 개설할 수 있는 '스터디장'이 될 수 있다. (스터디장 승인 기능은 MVP 이후
      구현)
    - **생성**: 스터디장은 스터디의 제목, 설명 등을 포함하는 새로운 스터디 과정을 생성할 수 있다.
- **(P0) 스터디 과정 멤버 관리**:
    - **멤버 등록**: 스터디장은 과정에 참여할 멤버를 직접 등록할 수 있다.
        - **역할 부여**: 멤버 등록 시, 스터디장은 해당 멤버에게 `MANAGER`, `MENTOR`, `MENTEE` 등의 `CourseRole`을 부여할 수 있다.

#### 4.2.3 스터디 커리큘럼 (Curriculum)

- **(P0) 커리큘럼 관리**:
    - **구조**: 스터디 과정은 하나 이상의 커리큘럼으로 구성된다. 커리큘럼은 다수의 세션을 효과적으로 그룹화하고 관리하는 역할을 한다.
    - **정보**: 커리큘럼은 진행 기간, 목표, 관련 세션 목록 등의 정보를 포함한다.
    - **통계**: 커리큘럼별 출석 통계를 제공하여 학습 진행 상황을 파악할 수 있도록 돕는다.

#### 4.2.4 스터디 세션 (Session)

- **(P0) 세션 생성 및 제약조건**:
    - **생성 주체**: 세션은 **스터디장** 또는 **시스템 관리자**에 의해 생성될 수 있다.
    - **소속 관계**: 세션은 특정 **커리큘럼**이나 **과정**에 속할 수도 있고, 어느 것에도 속하지 않는 **단독 세션**으로 생성될 수도 있다.
        - **구성**: 세션은 스터디의 최소 실행 단위로, 특정 일정과 콘텐츠(온/오프라인)로 구성된다.
        - **시간 제약**:
            - 세션의 총 진행 시간은 24시간을 초과할 수 없다.
            - 시간 제약 검증은 Clock을 활용하여 Asia/Seoul 시간대 기준으로 수행된다.
        - **계층 구조**:
            - 세션은 0개 이상의 하위 세션을 가질 수 있다.
            - 하위 세션은 또 다른 하위 세션을 가질 수 없다 (최대 깊이 1).
            - 예: `09:00~18:00`의 메인 세션 아래에 `14:00~15:30` (특강), `17:00~18:00` (Q&A) 등의 하위 세션 구성 가능.

- **(P0) 세션 목록 조회 및 필터링**:
    - **조회 범위**: 전체 세션, 특정 과정의 세션, 특정 커리큘럼의 세션 목록을 조회할 수 있다.
    - **필터링**: 세션 타입(온라인/오프라인/하이브리드), 장소, 날짜 범위로 필터링이 가능하다.
    - **정렬**: 일정 순(기본: 최신순), 제목순 등으로 정렬할 수 있다.
    - **페이징**: 대용량 데이터 처리를 위해 페이징을 지원한다 (기본: 20개/페이지, 최대: 100개/페이지).
    - **세션 상태**: 각 세션은 현재 시간을 기준으로 `UPCOMING`(예정), `ONGOING`(진행 중), `COMPLETED`(완료) 상태를 동적으로 계산하여 제공한다.
    - **하위 세션 포함**: 과정/커리큘럼 세션 조회 시 하위 세션 포함 여부를 선택할 수 있다 (기본: 포함).
    - **비동기 처리**: 성능 최적화를 위해 세션 목록 조회는 비동기적으로 처리된다.

#### 4.2.5 출석 (Attendance)

- **(P1) 출석 기록**:
    - **QR 코드**: 스터디장은 각 세션에 대한 출석 확인용 QR 코드를 생성할 수 있다.
    - **입/퇴실 처리**: 스터디 멤버는 세션 시작 및 종료 시점에 해당 QR 코드를 스캔하여 자신의 입실(`checkInTime`)과 퇴실(`checkOutTime`)을 기록한다.
- **(P1) 출석 인정 기준**:
    - **최상위 세션**: 지정된 시간 범위 내에 입/퇴실을 모두 기록해야 한다.
    - **하위 세션이 없는 경우**: 최상위 세션의 출석 기준을 충족하면 해당 세션은 '출석'으로 인정된다.
    - **하위 세션이 있는 경우**: 최상위 세션의 출석 기준을 충족하고, 동시에 해당 세션에 속한 전체 하위 세션 중 50% 이상을 출석해야 최종적으로 '출석'으로 인정된다.
    - **(구현 참고)**: 해당 로직은 출석 데이터를 종합하여 최종 출석 상태를 판별하는 별도의 애플리케이션 서비스에서 처리합니다.

#### 4.2.6 알림 (Notification)

- **(P2) 알림 기능**:
    - **세션 알림**: 회원은 참여하는 세션 시작 전에 이메일 등 지정된 수단으로 알림을 받을 수 있다.
    - **주요 일정 알림**: 과제 마감, 이벤트 등 기타 주요 일정에 대한 알림을 받을 수 있다.

---

## 5. 비기능적 요구사항

### 5.1 에러 핸들링 (Error Handling)

- 모든 API 실패 응답은 Spring Framework의 `ProblemDetail` 클래스를 사용하여 표준화된 형식으로 반환합니다.
- 클라이언트가 원인을 명확히 파악할 수 있도록, 예외 메시지는 구체적인 한국어로 작성합니다. (예: "[System] 사용자를 찾을 수 없습니다.")

### 5.2 보안 (Security)

- **역할 기반 접근 제어 (RBAC)**: 시스템의 모든 주요 기능 및 데이터 접근은 `SystemRole`과 `CourseRole`을 기반으로 통제됩니다.
- **권한 매트릭스 (예시)**:
  | 리소스 | 생성(C) | 조회(R) | 수정(U) | 삭제(D) |
  | :--- | :--- | :--- | :--- | :--- |
  | `Course` | MANAGER | 전체 회원 | MANAGER | MANAGER |
  | `Session` | MANAGER, MENTOR | 과정 참여자 | MANAGER, MENTOR | MANAGER |
  | `Attendance` | 시스템 | MANAGER, MENTOR, 본인 | 시스템 | - |
  | `Member` | (회원가입) | 본인, 관리자 | 본인, 관리자 | (탈퇴) |

### 5.3 데이터 유효성 검사 (Data Validation)

- 모든 도메인 모델의 필드는 명시적인 제약조건(Null 허용 여부, 길이 제한, 값의 범위 등)을 가집니다.
- 제약조건 위반 시, `ProblemDetail`을 통해 어떤 필드가 왜 유효하지 않은지 명확한 에러 메시지를 반환합니다.

### 5.4 감사 (Auditing)

- 모든 주요 도메인 엔티티는 생성 및 수정 이력을 추적하기 위해 아래의 감사 필드를 반드시 포함합니다.
- `createdAt` (Instant): 생성 시각
- `createdBy` (Long): 생성자 ID (Member ID)
- `updatedAt` (Instant): 최종 수정 시각
- `updatedBy` (Long): 최종 수정자 ID (Member ID)

---

## 6. 도메인 모델 상세 정의

### 6.1 Member

#### 6.1.1 `Account` : 사용자 인증 정보

> 시스템 접근에 필요한 이메일과 자격 증명(Credential) 정보를 관리합니다.

- **id** (Long), **memberId** (Long)
- **status** (AccountStatus), **email** (Email)
- **credentials** (List\<Credential\>): 계정에 연결된 자격 증명 목록
- **createdAt**, **createdBy**, **updatedAt**, **updatedBy**

##### provides

- `create()`: 신규 회원가입 요청에 따라 계정을 생성합니다.
- `activate()`: 이메일 인증 완료 시 계정을 활성 상태로 변경합니다.
- `deactivate()`: 휴면 상태로 전환합니다.
- `addCredential()`: 새로운 자격 증명(비밀번호, 소셜 로그인 등)을 추가합니다.
- `removeCredential()`: 기존 자격 증명을 제거합니다.

##### requires

- `save()`: 계정 정보 변경 사항을 DB에 저장(또는 수정)합니다.
- `sendVerificationEmail()`: 계정 활성화를 위한 이메일을 외부 시스템에 요청합니다.

#### 6.1.2 `Email` : 이메일 값 객체

> 이메일 주소의 형식을 검증하고 관리합니다.

- **address** (String): 이메일 주소

##### provides

- `new()`: 정규식 검증을 통해 유효한 이메일 객체를 생성합니다.

#### 6.1.3 `Credential` : 자격 증명 엔티티

> 사용자 인증에 필요한 자격 증명을 관리합니다. 비밀번호뿐만 아니라 소셜 로그인 등 다양한 인증 수단을 지원합니다.

- **id** (Long), **accountId** (Long)
- **type** (CredentialType): 자격 증명 유형
    - `PASSWORD`: 이메일/비밀번호 인증
    - `GOOGLE`: Google OAuth 인증
    - `KAKAO`: Kakao OAuth 인증
    - `GITHUB`: GitHub OAuth 인증
    - `NAVER`: Naver OAuth 인증
- **secret** (String): 암호화된 비밀번호 또는 OAuth 토큰
- **lastUsedAt** (LocalDateTime): 마지막 사용 시각

##### provides

- `createPassword()`: 비밀번호 유형의 자격 증명을 생성합니다. 비밀번호는 암호화되어 저장됩니다.
- `createOAuth()`: OAuth 유형의 자격 증명을 생성합니다.
- `verify()`: 입력된 비밀번호와 저장된 비밀번호의 일치 여부를 확인합니다.
- `updateLastUsedAt()`: 마지막 사용 시각을 갱신합니다.

##### requires

- `PasswordEncoder.encode()`: 문자열을 암호화합니다.
- `PasswordEncoder.match()`: 원본 문자열과 암호화된 문자열을 비교합니다.
- `save()`: 자격 증명 정보 변경 사항을 DB에 저장합니다.

#### 6.1.4 `Member` : 사용자(회원) 정보

> 시스템 사용자의 프로필, 역할, 상태 등 구체적인 정보를 관리합니다.

- **id** (Long)
- **role** (SystemRole), **status** (MemberStatus), **nickname** (Nickname)
- **primaryEmail** (Email): 대표 이메일 주소
- **accounts** (List\<Account\>): 회원에 연결된 계정 목록 (1:N 관계)
- **profileImageUrl** (String), **selfIntroduction** (String)
- **createdAt**, **createdBy**, **updatedAt**, **updatedBy**

##### provides

- `registerDefault()`: 기본 역할과 상태를 가진 회원 정보를 생성합니다.
- `addAccount()`: 회원에 새로운 계정을 추가합니다.
- `removeAccount()`: 회원의 계정을 제거합니다. (대표 이메일은 제거 불가)
- `findAccountByEmail()`: 이메일로 계정을 검색합니다.
- `updateProfile()`: 프로필 이미지와 자기소개를 수정합니다.
- `changeNickname()`: 닉네임을 변경합니다.
- `promoteToAdmin()`: 일반 회원을 관리자로 승격시킵니다.
- `demoteToMember()`: 관리자를 일반 회원으로 강등시킵니다.
- `activate()`: 휴면 상태의 회원을 활동 상태로 변경합니다.
- `deactivate()`: 회원을 휴면 처리합니다.
- `ban()`: 회원을 활동 정지시킵니다.
- `unban()`: 이용 정지 상태의 회원을 활동 상태로 변경합니다.
- `withdraw()`: 회원을 탈퇴 처리합니다.

##### requires

- `save()`: 회원 정보 변경 사항을 DB에 저장(또는 수정)합니다.
- `saveHistory()`: 회원 상태 변경 이력을 DB에 기록합니다.

#### 6.1.5 `MemberStatusHistory` : 회원 상태 변경 이력 (부분 구현)

> ⚠️ **부분 구현**: 도메인 모델은 정의되어 있으나, 실제 상태 변경 시 이력 기록 로직이 구현되지 않음
>
> 회원의 상태가 변경될 때마다 해당 기록을 저장하여 추적합니다.

- **id** (Long), **memberId** (Long)
- **status** (MemberStatus), **reason** (String)
- **changedAt** (Instant)

**구현 상태**: 도메인 모델 정의 완료. 이력 자동 기록 로직 구현 필요

### 6.2 Course

#### 6.2.1 `Course` : 스터디 과정

> 하나 이상의 커리큘럼을 포함하는 최상위 학습 단위입니다.

- **id** (Long), **title** (String), **description** (String)
- **courseMemberList** (List<CourseMember>), **curriculumList** (List<Curriculum>)
- **createdAt**, **createdBy**, **updatedAt**, **updatedBy**

##### provides

- `create()`: 새로운 스터디 과정을 개설합니다.
- `updateTitle()`: 과정명을 수정합니다.
- `updateDescription()`: 과정 설명을 수정합니다.
- `addMember()`: 과정에 신규 멤버를 추가하고 역할을 부여합니다.
- `removeMember()`: 과정에서 기존 멤버를 제외합니다.
- `addCurriculum()`: 과정에 신규 커리큘럼을 추가합니다.
- `removeCurriculum()`: 과정에서 기존 커리큘럼을 제외합니다.

##### requires

- `save()`: 과정 정보 변경 사항을 DB에 저장(또는 수정)합니다.

#### 6.2.2 `CourseMember` : 과정-회원 관계

> 특정 스터디 과정에 어떤 회원이 어떤 역할로 참여하는지를 정의합니다.

- **id** (Long), **memberId** (Long), **courseId** (Long)
- **courseRole** (CourseRole): 과정 내 역할 (`MANAGER`, `MENTOR`, `MENTEE`)
    - **MANAGER**: 과정 관리자 (스터디장) - 과정 생성/수정/삭제, 멤버 관리, 세션 관리 권한
    - **MENTOR**: 과정 멘토 - 세션 진행, 멘티 지도, 출석 관리 권한 (관리 권한 제한)
    - **MENTEE**: 과정 멘티 (수강생) - 세션 참여, 과정 조회 권한

#### 6.2.3 `Curriculum` : 스터디 커리큘럼

> 관련된 세션들을 그룹화하는 중간 단위입니다.

- **id** (Long), **courseId** (Long)
- **title** (String), **description** (String)
- **createdAt**, **createdBy**, **updatedAt**, **updatedBy**

##### provides

- `create()`: 새로운 커리큘람을 생성합니다.
- `updateTitle()`: 커리큘럼명을 수정합니다.
- `updateDescription()`: 커리큘럼 설명을 수정합니다.

##### requires

- `save()`: 커리큘럼 정보 변경 사항을 DB에 저장(또는 수정)합니다.

### 6.3 Session

#### 6.3.1 `Session` : 스터디 세션

> 실제 학습 활동이 이루어지는 최소 시간 단위입니다.

- **id** (Long), **courseId** (Long), **curriculumId** (Long)
- **parent** (Session): 부모 세션 참조 (계층 구조)
- **children** (List<Session>): 하위 세션 목록
- **participants** (List<SessionParticipant>): 세션 참여자 목록
- **title** (String), **scheduledAt** (Instant), **scheduledEndAt** (Instant)
- **type** (SessionType), **location** (SessionLocation), **locationDetails** (String)
- **createdAt**, **createdBy**, **updatedAt**, **updatedBy**

##### provides

- `createStandaloneSession()`: 특정 과정이나 커리큘럼에 속하지 않는 단독 세션을 생성합니다. (주로 시스템 관리자에 의해 호출)
- `createCourseSession()`: 특정 과정에 직접 속하는 세션을 생성합니다.
- `createCurriculumSession()`: 특정 커리큘럼에 속하는 세션을 생성합니다.
- `createChildSession()`: 하위 세션을 생성합니다.
- `reschedule()`: 세션의 진행 시간을 재조정합니다.
- `changeInfo()`: 세션의 제목, 타입 등 기본 정보를 변경합니다.
- `changeLocation()`: 세션의 장소를 변경합니다.
- `addParticipant()`: 세션에 신규 참여자를 추가합니다.
- `removeParticipant()`: 세션에서 참여자를 제외합니다.
- `changeParticipantRole()`: 세션 참여자의 역할을 변경합니다.

##### requires

- `save()`: 세션 정보 변경 사항을 DB에 저장(또는 수정)합니다.

#### 6.3.2 `SessionParticipant` : 세션-회원 관계

> 특정 스터디 세션에 어떤 회원이 어떤 역할로 참여하는지를 정의합니다.

- **id** (Long), **memberId** (Long)
- **session** (Session): 참여하는 세션 참조
- **role** (SessionParticipantRole): 세션 내 역할 (`HOST`, `SPEAKER`, `ATTENDEE`)
- **createdAt**, **createdBy**, **updatedAt**, **updatedBy**

##### provides

- `of()`: 세션, 멤버, 역할을 받아 새로운 참여 관계를 생성합니다.

### 6.4 Attendance

#### 6.4.1 `Attendance` : 출석 기록

> 이벤트 소싱 기반으로 세션별 회원의 모든 출입 이력을 추적하고 최종 출석 상태를 관리합니다. MongoDB에 저장됩니다.

- **id** (String): 고유 식별자 (MongoDB ObjectId 호환)
- **sessionId** (Long), **memberId** (Long): 불변 컨텍스트 정보
- **events** (List<AttendanceEvent>): 시간순 출입 이벤트 목록
- **finalStatus** (AttendanceStatus): 계산된 최종 출석 상태

**저장소**: MongoDB (이벤트 소싱 패턴에 최적화된 스키마리스 저장)

##### provides

- `create(sessionId, memberId)`: 신규 출석 기록을 생성합니다.
- `restore(id, sessionId, memberId, events)`: 기존 이벤트로부터 출석 기록을 재구성합니다.
- `checkIn(Clock)`: 입실 이벤트를 기록하고 상태를 재계산합니다.
- `checkOut(Clock)`: 퇴실 이벤트를 기록하고 상태를 재계산합니다.
- `setId(String)`: 외부에서 생성된 ID를 한 번만 설정합니다.

##### requires

- `save()`: 출석 정보 변경 사항을 DB에 저장(또는 수정)합니다.

#### 6.4.2 `AttendanceEvent` : 출석 이벤트

> 개별 출입 행동을 나타내는 불변 이벤트입니다.

- **Sealed Interface**: `CheckedIn`, `CheckedOut` 구현체만 허용
- **timestamp** (Instant): 이벤트 발생 시점

##### provides

- `checkIn(Clock)`: 입실 이벤트를 생성합니다.
- `checkOut(Clock)`: 퇴실 이벤트를 생성합니다.

#### 6.4.3 `AttendanceStatus` : 최종 출석 상태

> 이벤트 기반으로 계산된 최종 출석 상태를 나타냅니다.

- **PRESENT**: 출석 (체크인 이벤트 존재)
- **ABSENT**: 결석 (체크인 이벤트 없음)
- **LATE**: 지각 (추후 확장)
- **LEFT_EARLY**: 조퇴 (추후 확장)

#### 6.4.4 출석 수정 기능 (Attendance Correction)

> 출석 기록에 오류가 있을 경우, 수강생이 수정을 요청하고 관리자가 승인/거절하는 워크플로우를 지원합니다.

##### provides

- `requestCorrection()`: 수강생이 출석 수정을 요청합니다.
- `approveCorrection()`: 관리자가 출석 수정 요청을 승인합니다.
- `rejectCorrection()`: 관리자가 출석 수정 요청을 거절합니다.

##### 구현 상태

- ✅ 출석 수정 요청 API (`POST /api/v1/attendance/{attendanceId}/correction`)
- ✅ 출석 수정 승인 API (`POST /api/v1/attendance/{attendanceId}/correction/approve`)
- ✅ 출석 수정 거절 API (`POST /api/v1/attendance/{attendanceId}/correction/reject`)

### 6.5 Auth

#### 6.5.1 `RefreshToken` : 리프레시 토큰

> JWT 기반 인증에서 액세스 토큰 갱신을 위한 리프레시 토큰을 관리합니다.

- **id** (Long), **memberId** (Long)
- **token** (String): UUID 기반 고유 토큰 값
- **expiresAt** (Instant): 토큰 만료 시각
- **createdAt** (Instant): 토큰 생성 시각
- **revoked** (boolean): 토큰 폐기 여부

##### provides

- `create(memberId, ttl)`: 지정된 TTL(유효기간)을 가진 새 리프레시 토큰을 생성합니다.
- `isExpired()`: 토큰이 만료되었는지 확인합니다.
- `isUsable()`: 토큰이 사용 가능한 상태인지 확인합니다. (폐기되지 않고 만료되지 않음)
- `revoke()`: 토큰을 폐기 처리합니다.

##### requires

- `save()`: 리프레시 토큰을 DB에 저장합니다.
- `findByToken()`: 토큰 값으로 리프레시 토큰을 조회합니다.
- `deleteByMemberId()`: 특정 회원의 모든 리프레시 토큰을 삭제합니다.

#### 6.5.2 인증 흐름 (Authentication Flow)

> JWT 기반 Stateless 인증 방식을 사용합니다.

**토큰 발급 (Login)**:

1. 사용자가 이메일/비밀번호로 로그인 요청
2. 자격 증명 검증 후 Access Token + Refresh Token 발급
3. Access Token: 짧은 유효기간 (예: 15분)
4. Refresh Token: 긴 유효기간 (예: 7일), DB에 저장

**토큰 갱신 (Refresh)**:

1. Access Token 만료 시 Refresh Token으로 갱신 요청
2. Refresh Token 유효성 검증 (만료, 폐기 여부)
3. 새로운 Access Token 발급

**토큰 폐기 (Logout)**:

1. 로그아웃 시 Refresh Token 폐기 처리
2. 해당 토큰으로 더 이상 Access Token 갱신 불가

---

### 6.6 Admin (시스템 관리)

#### 6.6.1 `SystemRole` : 시스템 역할

> 시스템 전역에서 사용자의 권한을 정의합니다. 역할은 계층 구조를 가집니다.

- **ADMIN** (Tier 1): 최고 관리자 - 모든 시스템 기능 접근 가능
- **SUPERVISOR** (Tier 2): 감독자 - 일부 관리 기능 접근 가능
- **MANAGER** (Tier 3): 매니저 - 과정 관리 기능 접근 가능
- **MENTEE** (Tier 4): 멘티 - 기본 사용자 권한

##### 역할 다중화 (Role Multiplexing)

- 한 사용자가 여러 SystemRole을 가질 수 있습니다.
- 역할은 `member_system_role` 테이블에서 별도로 관리됩니다.

##### provides

- `grantRole()`: 특정 회원에게 시스템 역할을 부여합니다.
- `revokeRole()`: 특정 회원의 시스템 역할을 회수합니다.
- `getRoles()`: 특정 회원의 모든 시스템 역할을 조회합니다.

##### 구현 상태

- ✅ 역할 부여 API (`POST /api/v1/admin/members/{memberId}/roles`)
- ✅ 역할 회수 API (`DELETE /api/v1/admin/members/{memberId}/roles/{role}`)
- ✅ 역할 조회 API (`GET /api/v1/admin/members/{memberId}/roles`)
- ✅ 권한 에스컬레이션 방지 (SUPERVISOR는 ADMIN 부여 불가)

---

### 6.7 Notification (미구현)

#### 6.6.1 `Notification` : 알림 (미구현)

> ⚠️ **현재 미구현**: 알림 도메인은 설계되어 있으나 실제 구현되지 않음. 현재는 인터페이스 레벨에서 참조만 되고 있음.
>
> **향후 구현 예정**: 시스템에서 사용자에게 발송되는 모든 알림의 내용, 상태, 이력을 관리합니다.

**설계된 구조** (향후 구현 시 참고):

- **id** (Long), **memberId** (Long)
- **type** (NotificationType): `EMAIL`, `SMS` 등
- **content** (String): 알림 내용
- **status** (NotificationStatus): `PENDING`, `SUCCESS`, `FAILURE`
- **sentAt** (Instant): 발송 완료 시각
- **createdAt**, **createdBy**, **updatedAt**, **updatedBy**

**구현 계획**:

- 회원가입 인증 이메일 발송
- 비밀번호 재설정 이메일 발송
- 세션 시작 알림
- 과정 멤버 초대 알림

---
