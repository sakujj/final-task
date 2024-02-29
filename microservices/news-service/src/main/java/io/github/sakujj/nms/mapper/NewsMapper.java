package io.github.sakujj.nms.mapper;

import io.github.sakujj.nms.dto.NewsRequest;
import io.github.sakujj.nms.dto.NewsResponse;
import io.github.sakujj.nms.entity.News;
import io.github.sakujj.nms.entity.News.Fields;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface NewsMapper {

    @Mapping(target = Fields.ID, ignore = true)
    @Mapping(target = Fields.CREATION_TIME, ignore = true)
    @Mapping(target = Fields.UPDATE_TIME, ignore = true)
    @Mapping(target = Fields.AUTHOR_ID, ignore = true)
    @Mapping(target = Fields.USERNAME, ignore = true)
    News fromRequest(NewsRequest request);

    NewsResponse toResponse(News entity);
}
