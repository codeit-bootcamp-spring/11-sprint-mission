package com.sprint.mission.discodeit.service.jcf;

import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor // final이 붙은 필드를 모아서 생성자를 자동으로 만들어줍니다.
public class JCFUserService implements UserService {

    // 1. 주입받을 필드를 반드시 'private final'로 선언해야 합니다.
    private final UserRepository userRepository;

    // 2. 직접 작성했던 public JCFUserService(UserRepository ...) 생성자 코드를 통째로 삭제하세요!
    // 롬복이 컴파일할 때 자동으로 생성자를 만들어주고, 스프링이 그 생성자를 통해 Repository를 꽂아줍니다.

    @Override
    public void create(User user) {
        userRepository.save(user);
    }
    // ... 나머지 메소드 구현
}