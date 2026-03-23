package com.sprint.mission.discodeit.exception;

import com.sprint.mission.discodeit.exception.binarycontent.BinaryContentNotFoundException;
import com.sprint.mission.discodeit.exception.channel.ChannelNotFoundException;
import com.sprint.mission.discodeit.exception.channel.PrivateChannelUpdateNotAllowedException;
import com.sprint.mission.discodeit.exception.login.InvalidPasswordException;
import com.sprint.mission.discodeit.exception.message.MessageNotFoundException;
import com.sprint.mission.discodeit.exception.readstatus.ReadStatusAlreadyExistsException;
import com.sprint.mission.discodeit.exception.readstatus.ReadStatusNotFoundException;
import com.sprint.mission.discodeit.exception.readstatus.ReadStatusOfUserAndChannelNotFoundException;
import com.sprint.mission.discodeit.exception.repository.DirectoryCreationException;
import com.sprint.mission.discodeit.exception.repository.FileDeleteException;
import com.sprint.mission.discodeit.exception.repository.FileLoadException;
import com.sprint.mission.discodeit.exception.repository.FileSaveException;
import com.sprint.mission.discodeit.exception.user.DuplicateEmailException;
import com.sprint.mission.discodeit.exception.user.DuplicateNameException;
import com.sprint.mission.discodeit.exception.user.UserNotFoundException;
import com.sprint.mission.discodeit.exception.userstatus.UserStatusAlreadyExistsException;
import com.sprint.mission.discodeit.exception.userstatus.UserStatusNotFoundException;
import com.sprint.mission.discodeit.exception.userstatus.UserStatusOfUserNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({
            DuplicateEmailException.class,
            DuplicateNameException.class,
            UserStatusAlreadyExistsException.class,
            InvalidPasswordException.class,
            PrivateChannelUpdateNotAllowedException.class,
            ReadStatusAlreadyExistsException.class
    })
    public ResponseEntity<String> handleBadRequest(RuntimeException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
    }

    @ExceptionHandler({
            UserNotFoundException.class,
            UserStatusOfUserNotFoundException.class,
            UserStatusNotFoundException.class,
            ChannelNotFoundException.class,
            MessageNotFoundException.class,
            ReadStatusNotFoundException.class,
            ReadStatusOfUserAndChannelNotFoundException.class,
            BinaryContentNotFoundException.class
    })
    public ResponseEntity<String> handleNotFound(RuntimeException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
    }

    @ExceptionHandler({
            DirectoryCreationException.class,
            FileDeleteException.class,
            FileLoadException.class,
            FileSaveException.class
    })
    public ResponseEntity<String> handleInternalServerError(RuntimeException e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleException(Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal Server Error");
    }
}
