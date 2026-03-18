package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.dto.binaryContent.BinaryContentCreateRequest;
import com.sprint.mission.discodeit.exception.BinaryContentNotFoundException;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.service.BinaryContentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BasicBinaryContentService implements BinaryContentService {
    private final BinaryContentRepository binaryContentRepository;

    @Override
    public BinaryContent create(BinaryContentCreateRequest request){
        BinaryContent binaryContent;
        if(request.getUserId() !=null){
            binaryContent = BinaryContent.forProfile(request.getUserId(), request.getFileName(), request.getContent(), request.getContentType());
        }else {
            binaryContent = BinaryContent.forMessage(request.getMessageId(), request.getFileName(), request.getContent(), request.getContentType());
        }
        binaryContent.validateService();
        return binaryContentRepository.create(binaryContent);
    }

    @Override
    public BinaryContent read(UUID id){
        BinaryContent binaryContent = binaryContentRepository.readById(id);
        if(binaryContent ==null){
            throw new BinaryContentNotFoundException(id);
        }
        return binaryContent;
    }

    @Override
    public List<BinaryContent> readAllByIdIn(List<UUID> ids){
        return ids.stream()
                .map(id -> binaryContentRepository.readById(id))
                .toList();
    }

    @Override
    public void delete(UUID id){
        binaryContentRepository.delete(id);
    }
}
