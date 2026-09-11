# team_project_jdbc

팀 프로젝트 전용 저장소

## 📁 File Structure

```text
team_project_jdbc/
│
├── .gradle/
├── .idea/
├── build/
├── gradle/
│
├── src/
│   └── main/
│       └── java/
│           └── com/
│               └── tenco/
│                   ├── dao/
│                   ├── dto/
│                   ├── service/
│                   ├── util/
│                   │   └── DatabaseUtil.java
│                   └── view/
│                       └── Main.java
│
├── .gitignore
├── build.gradle
├── gradlew
├── gradlew.bat
└── settings.gradle
```

## 📄 Role

| 이름 | 담당 역할 |
| :--- | :--- |
| **이진우** | 로그인 & view |
| **구도하** | 회원 관련 기능 |
| **이수현** | 강의 관련 기능 |
| **양현재** | 수강 관련 기능 |
| **최병권** | 성적 관련 기능 |

## 🗄️ Database Schema

### 1. `members` (회원)
| 컬럼명 | 타입 | PK / FK | 설명 |
| :--- | :---: | :---: | :--- |
| `id` | INT | **PK** | 회원 고유 ID |
| `member_id` | VARCHAR | - | 아이디 (학번) |
| `password` | VARCHAR | - | 비밀번호 |
| `name` | VARCHAR | - | 이름 |
| `phone` | VARCHAR | - | 연락처 |
| `major` | VARCHAR | - | 전공 |
| `grade` | INT | - | 학년 / 성적 |
| `admin` | BOOLEAN | - | 관리자 여부 |

### 2. `lectures` (과목)
| 컬럼명 | 타입 | PK / FK | 설명 |
| :--- | :---: | :---: | :--- |
| `id` | INT | **PK** | 과목 고유 ID |
| `lecture_code` | VARCHAR | - | 과목 코드 |
| `lecture_name` | VARCHAR | - | 과목명 |
| `professor` | VARCHAR | - | 담당교수 |
| `credit` | INT | - | 학점 |

### 3. `scores` (성적)
| 컬럼명 | 타입 | PK / FK | 설명 |
| :--- | :---: | :---: | :--- |
| `id` | INT | **PK** | 성적 고유 ID |
| `member_id` | INT | **FK** | 회원 ID (`members.id`) |
| `lecture_id` | INT | **FK** | 과목 ID (`lectures.id`) |
| `score` | INT | - | 성적 |

### 4. `registration` (수강신청)
| 컬럼명 | 타입 | PK / FK | 설명 |
| :--- | :---: | :---: | :--- |
| `id` | INT | **PK** | 수강신청 고유 ID |
| `member_id` | VARCHAR | **FK** | 회원 ID (`members.id`) |
| `lecture_id` | VARCHAR | **FK** | 강의 ID (`lectures.id`) |

## 🏷️ Connection Info

* **DB Name**: `project_team1` (Host: `192.168.5.16:3306`)

### DB 접속 정보
아래 변수명으로 개인 환경 변수 통일

| 변수명 | 설정값 |
| :--- | :--- |
| **PJ_USER** | `team1` |
| **PJ_PASSWORD** | 🔒 **Discord 확인** |

### 사용예시 (`DatabaseUtil.java`)
```java
String user = System.getenv("PJ_USER");
String password = System.getenv("PJ_PASSWORD");
```
