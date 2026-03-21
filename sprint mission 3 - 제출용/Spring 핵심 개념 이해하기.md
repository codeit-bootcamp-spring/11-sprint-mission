#### `JavaApplication`과 `DiscodeitApplication`에서 Service를 초기화하는 방식의 차이에 대해 다음의 키워드를 중심으로 정리해보세요.
- IoC Container
- Dependency Injection
- Bean


##### 1. JavaApplication (기존 방식)

`UserService userService = new BasicUserService(userRepository);  
`ChannelService channelService = new BasicChannelService(channelRepository);  
`MessageService messageService = new BasicMessageService(messageRepository,userService);
와 같이 new를 사용하여 직접 서비스를 초기화 하고 있다.
이는 개발자가 직접 객체를 생성하고 코드 내부에서 수동으로 DI를 주입하며 생명주기까지 관리하는 형식이다.

단순하지만 결합성이 높아 수정, 테스트, 확장성 측면에서 문제가 있다.



##### 2. DiscodeitApplication (Spring 방식)

또한 `@Bean`, `@Service` , `@Repository` , `@Controller` 애너테이션을 통해 Bean 객체로서의 관리에 들어간 객체들을  `ConfigurableApplicationContext context = SpringApplication.run(DiscodeitApplication.class, args);` 한줄로 인해 생성된 `IoC Container`가 의존관계를 파악하여 자동 주입까지 완료한다.
덕분에 DI를 포함한 제어권이 개발자에서 스프링으로 역전되며, 이를 통해 개발자는 비지니스 로직에 집중할 수 있게되어 생산성이 높아진다.

IoC 컨테이너를 통한 DI와 Bean객체 관리로 인해 느슨한 결합이 체결된다.