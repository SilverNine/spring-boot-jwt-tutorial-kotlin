# Spring Boot JWT Tutorial - Kotlin

인프런 강의 [Spring Boot JWT Tutorial](https://www.inflearn.com/course/%EC%8A%A4%ED%94%84%EB%A7%81%EB%B6%80%ED%8A%B8-jwt)의 예제를 Kotlin으로 옮긴 코드입니다.
Spring Boot 4, Spring Security 7, Kotlin 2.4, Java 25 기준으로 다시 정리했습니다.

- Java 버전(강의 원본): [spring-boot-jwt-tutorial](https://github.com/SilverNine/spring-boot-jwt-tutorial)
- 강의 영상과 무엇이 달라졌는지는 Java 레포 README의 [강의 영상과 달라진 점](https://github.com/SilverNine/spring-boot-jwt-tutorial#강의-영상과-달라진-점)에 정리했습니다. 두 레포는 구조와 동작이 같습니다.

## 실행

JDK 25가 필요합니다. Gradle은 래퍼가 들어 있어 따로 설치하지 않아도 됩니다.

```bash
./gradlew bootRun     # http://localhost:8080
./gradlew test        # 테스트 20개
```

H2 메모리 DB를 쓰고, 시작할 때 `data.sql`이 샘플 계정 두 개를 넣습니다.

| 아이디 | 비밀번호 | 권한 |
|---|---|---|
| `admin` | `admin` | ROLE_USER, ROLE_ADMIN |
| `user` | `password` | ROLE_USER |

H2 콘솔은 http://localhost:8080/h2-console 입니다(JDBC URL `jdbc:h2:mem:testdb`).

## API

| 메서드 | 경로 | 인증 | 설명 |
|---|---|---|---|
| GET | `/api/hello` | 없음 | 동작 확인 |
| POST | `/api/signup` | 없음 | 회원가입 (ROLE_USER) |
| POST | `/api/authenticate` | 없음 | 로그인. Access Token과 Refresh Token 발급 |
| POST | `/api/token/refresh` | 없음 | Refresh Token으로 토큰 재발급 |
| POST | `/api/logout` | 없음 | Refresh Token 폐기 |
| GET | `/api/user` | USER 또는 ADMIN | 내 정보 |
| GET | `/api/user/{username}` | ADMIN | 다른 사용자 정보 |

```bash
# 로그인
curl -s -X POST localhost:8080/api/authenticate \
  -H 'Content-Type: application/json' \
  -d '{"username":"admin","password":"admin"}'
# {"token":"eyJhbGciOiJIUzUxMiJ9...","refreshToken":"q3k...Zc"}

# Access Token으로 호출
curl -s localhost:8080/api/user -H "Authorization: Bearer <token>"

# 재발급 (보낸 Refresh Token은 폐기되고 새 토큰이 나옵니다)
curl -s -X POST localhost:8080/api/token/refresh \
  -H 'Content-Type: application/json' \
  -d '{"refreshToken":"<refreshToken>"}'

# 로그아웃
curl -s -X POST localhost:8080/api/logout \
  -H 'Content-Type: application/json' \
  -d '{"refreshToken":"<refreshToken>"}'
```

| 항목 | 값 |
|---|---|
| Access Token | JWT, HS512 서명, 기본 30분 |
| Refresh Token | 256비트 난수 문자열(JWT 아님), 기본 14일, DB에는 SHA-256 해시만 저장 |
| 재발급 | 쓸 때마다 새 Refresh Token으로 교체. 한 번 쓴 토큰은 다시 못 씀 |
| 오류 응답 | RFC 9457 Problem Details (`application/problem+json`) |

상태 코드는 입력값 오류 400, 인증 실패 401, 권한 부족 403, 없는 사용자 404, 중복 가입 409입니다.

## 설정

| 환경변수 | 기본값 | 설명 |
|---|---|---|
| `JWT_SECRET` | 예제용 값 | Base64로 인코딩한 서명 키. HS512는 64바이트 이상 |
| `JWT_ACCESS_TOKEN_VALIDITY` | `30m` | Access Token 수명 (`Duration` 형식) |
| `JWT_REFRESH_TOKEN_VALIDITY` | `14d` | Refresh Token 수명 |

`application.yml`에 들어 있는 키는 예제용이라 공개돼 있습니다. 실제로 배포할 때는 반드시 새로 만들어 환경변수로 넣으세요.

```bash
export JWT_SECRET="$(openssl rand -base64 64 | tr -d '\n')"
```

## Kotlin으로 옮기면서 챙긴 것

| 항목 | 내용 |
|---|---|
| 컴파일러 플러그인 | `kotlin("plugin.spring")`, `kotlin("plugin.jpa")`로 프록시용 `open` 클래스와 기본 생성자를 만듭니다 |
| 애노테이션 대상 | 검증은 `@field:NotBlank`, 생성자 주입 한정자는 `@param:Qualifier`처럼 대상을 적습니다 |
| Null 안전성 | `-Xjsr305=strict`로 Spring API의 null 여부를 컴파일러가 확인합니다 |
| JSON | Spring Boot 4가 쓰는 Jackson 3용 `tools.jackson.module:jackson-module-kotlin` |
| kapt | 선언만 있고 쓰이지 않아 뺐습니다. 빌드가 그만큼 빨라집니다 |

## 참고

- **Jackson 2와 3이 함께 들어 있습니다.** Spring Boot 4는 Jackson 3(`tools.jackson`)을 쓰고, `jjwt-jackson`은 아직 Jackson 2(`com.fasterxml.jackson`)를 씁니다. 패키지 이름이 달라 충돌하지 않습니다.
- **Spring Security에도 JWT 기능이 있습니다.** 이 예제는 원리를 보이려고 필터를 직접 만들지만, Spring Security의 [OAuth 2.0 Resource Server](https://docs.spring.io/spring-security/reference/servlet/oauth2/resource-server/jwt.html)로도 같은 일을 할 수 있습니다.
- 버전과 설정은 2026년 10월 기준 Spring Boot 4.1.2, Spring Security 7.1.2 공식 문서를 따랐습니다.

## 버전

| 구성 요소 | 버전 |
|---|---|
| Java | 25 |
| Kotlin | 2.4.21 |
| Spring Boot | 4.1.2 |
| Spring Security | 7.1.2 |
| jjwt | 0.13.0 |
| Gradle | 9.8.2 |
| DB | H2 (메모리) |

## Author

**SilverNine**

* https://silvernine.me
* https://portfolio.silvernine.me
* https://github.com/silvernine

## Copyright and license

The code is released under the [MIT license](LICENSE?raw=true).
