package by.andd3dfx.templateapp.mappers;

import by.andd3dfx.templateapp.dto.ArticleDto;
import by.andd3dfx.templateapp.dto.ArticleUpdateDto;
import by.andd3dfx.templateapp.persistence.entities.Article;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface ArticleMapper {

    ArticleDto toArticleDto(Article article);

    List<ArticleDto> toArticleDtoList(List<Article> articles);

    Article toArticle(ArticleDto articleDto);

    void toArticle(ArticleUpdateDto articleUpdateDto, @MappingTarget Article article);
}
