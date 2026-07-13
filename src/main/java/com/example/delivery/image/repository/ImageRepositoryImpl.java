package com.example.delivery.image.repository;

import com.example.delivery.image.dto.request.ImageRequestDto;
import com.example.delivery.image.entity.ImageDisplayStatus;
import com.example.delivery.image.entity.ImageFile;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.UUID;

import static com.example.delivery.image.entity.QImageFile.imageFile;

@RequiredArgsConstructor
public class ImageRepositoryImpl implements ImageRepositoryCustom{

    private final JPAQueryFactory queryFactory;

    @Override
    public List<ImageFile> findImages(ImageRequestDto imageRequestDto) {

        return queryFactory
                .selectFrom(imageFile)
                .where(
                        imageIdEq(imageRequestDto.getImageId()),
                        refTypeEq(imageRequestDto.getRefType()),
                        refIdEq(imageRequestDto.getRefId()),
                        displayStatusEq(ImageDisplayStatus.NORMAL),
                        deletedAtIsNull()
                )
                .orderBy(imageFile.displayOrder.asc())
                .fetch();
    }

    private BooleanExpression imageIdEq(UUID imageId) {
        return imageId != null ? imageFile.imageId.eq(imageId) : null;
    }

    private BooleanExpression refTypeEq(ImageFile.RefType refType) {
        return refType != null ? imageFile.refType.eq(refType) : null;
    }

    private BooleanExpression refIdEq(String refId) {
        return StringUtils.hasText(refId) ? imageFile.refId.eq(refId) : null;
    }

    private BooleanExpression displayStatusEq(ImageDisplayStatus displayStatus) {
        return imageFile.displayStatus.eq(displayStatus);
    }

    private BooleanExpression deletedAtIsNull() {
        return imageFile.deletedAt.isNull();
    }

}
