package com.rojek.onlinestore.tag;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class TagService {

    private final TagRepository tagRepository;

    public void findByIdInList(Set<Tag> tagList) throws RuntimeException {
        // no need to return I only care about exception
        tagList.stream()
                .forEach(tag -> tagRepository.findById(tag.getId())
                        .orElseThrow(() -> new RuntimeException(String.format("Tag %s not found", tag))
                )
        );
    }
}
