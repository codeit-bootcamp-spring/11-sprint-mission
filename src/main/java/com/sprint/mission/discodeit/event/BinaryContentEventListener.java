package com.sprint.mission.discodeit.event;

import com.sprint.mission.discodeit.storage.BinaryContentStorage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@RequiredArgsConstructor
@Component
public class BinaryContentEventListener {

  private final BinaryContentStorage binaryContentStorage;

  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void on(BinaryContentCreatedEvent event) {
    this.binaryContentStorage.put(event.binaryContentId(), event.bytes());
  }
}