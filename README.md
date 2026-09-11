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
