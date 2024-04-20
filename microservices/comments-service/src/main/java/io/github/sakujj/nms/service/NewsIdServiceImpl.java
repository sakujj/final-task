package io.github.sakujj.nms.service;

import io.github.sakujj.nms.entity.NewsId;
import io.github.sakujj.nms.repository.NewsIdRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NewsIdServiceImpl implements NewsIdService{

    private final NewsIdRepository newsIdRepository;

    @Override
    @Transactional
    public void deleteById(UUID id) {
        newsIdRepository.deleteById(id);
    }

    @Override
    @Transactional
    public void create(UUID id, UUID authorId) {

        NewsId newsIdToCreate = new NewsId(id, authorId);

        newsIdRepository.save(newsIdToCreate);
    }

    @Override
    public Optional<NewsId> findById(UUID id) {
        return newsIdRepository.findById(id);
    }
}