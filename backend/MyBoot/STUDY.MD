- 예외처리 방법은 개별 클래스에서 > Enum으로 변경  
  사유 : 일관성과 유지보수 측면에서 나아서 관리적 측면에서의 비교:  
  개별 예외 클래스 방식의 장점:  
  명확한 타입 안전성: 컴파일 타임에 예외 타입을 명확히 구분  
  세밀한 제어: 각 예외 클래스마다 다른 속성이나 메서드 추가 가능  
  익숙함: 전통적인 Java 예외 처리 방식으로 이해하기 쉬움

- Enum 기반 예외 타입 방식의 장점:

중앙 집중화: 모든 예외 유형과 메시지가 한 곳에서 관리됨  
일관성: 모든 예외가 동일한 패턴으로 처리됨  
유연성: 도메인별로 예외를 쉽게 그룹화할 수 있음 (AuthException, OrderException 등)  
유지보수 용이성: 메시지 변경이나 상태 코드 변경 시 한 곳만 수정하면 됨

어떤 방식이 더 나을까?  
프로젝트 규모가 작고 예외 종류가 적다면:

개별 예외 클래스 방식이 간단하고 직관적일 수 있다.

프로젝트 규모가 크고 다양한 도메인이 있다면:

Enum 기반 방식이 일관성과 유지보수성 측면에서 우수함.

출처 (chatgpt)
- domain 과 enity 차이  
  도메인은 비즈니스 규칙을 담고 있는 영역 전체를 의미함.  
  JPA (혹은 ORM) 관점에서의 @Entity는 DB 테이블과 매핑되는 객체다.  
  user는 하나의 도메인 영역 (즉, 비즈니스 주제 영역)  
  그 안에 포함되는 User, UserLog 등은 도메인 모델 (Entity, VO)

- service + serviceImpl 과 service 단일 방식 차이    
  UserService + UserServiceImpl → ❌ 요즘은 지양 , 차라리 구체적으로 나누기 (구체적 분리 or CQRS 스타일)  
  언제 인터페이스를 쓰는 게 좋을까? 외부 API, Plugin 형태로 대체될 수 있는 서비스 , 여러 구현체가 필요한 경우 (예: DB vs Redis 캐시 전략)

- querydsl + jpa 에서의 네이밍 규칙  
  Spring 공식 방식: custom 구현체(인터페이스) + Impl but 직접 분리 방식(queryImpl 로 단일 분리)은 많이 사용되는 방식 - querydsl에 service vs repository  
  @Repository : 조회 용 , @Service : 비즈니스 로직이 들어가는 경우