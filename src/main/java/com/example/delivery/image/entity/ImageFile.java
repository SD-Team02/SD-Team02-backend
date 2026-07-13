package com.example.delivery.image.entity;

import java.util.UUID;

import com.example.delivery.global.common.entity.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * MENU/STORE/USER 여러 도메인이 ref_type/ref_id로 느슨하게 참조하는 첨부 이미지 도메인.
 * 특정 도메인 소속이 아니라 별도 도메인으로 둔다 (User가 여러 도메인에서 참조돼도 global이 아니라
 * user 도메인에 있는 것과 같은 이유).
 * ERD상 updated_at/updated_by가 없어 BaseEntity를 상속하되 그 두 필드는 항상 null로 남는다
 * (이미지 교체는 새 row 추가로 처리하고 기존 row를 수정하지 않는다는 전제).
 */
@Getter
@Entity
@Table(name = "p_image_file")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ImageFile extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "image_id")
    private UUID imageId;

    @Enumerated(EnumType.STRING)
    @Column(name = "ref_type", length = 100, nullable = false)
    private RefType refType;

    @Column(name = "ref_id", length = 100, nullable = false)
    private String refId;
    // s3 object key
    @Column(name = "image_key", nullable = false)
    private String imageKey;

    @Column(name = "original_name", nullable = false)
    private String originalName;

    @Column(name = "save_name", nullable = false)
    private String savedName;

    @Column(name = "content_type", length = 50, nullable = false)
    private String contentType;

    @Column(name = "file_size", nullable = false)
    private Long fileSize;

    @Column(name = "display_order", nullable = false)
    private Integer displayOrder;

    @Enumerated(EnumType.STRING)
    @Column(name = "display_status", nullable = false)
    private ImageDisplayStatus displayStatus;

    public ImageFile(RefType refType, String refId, String imageKey, String originalName,
                      String savedName, String contentType, Long fileSize, Integer displayOrder) {
        this.refType = refType;
        this.refId = refId;
        this.imageKey = imageKey;
        this.originalName = originalName;
        this.savedName = savedName;
        this.contentType = contentType;
        this.fileSize = fileSize;
        this.displayOrder = displayOrder;
        this.displayStatus = ImageDisplayStatus.NORMAL;
    }

    // 메뉴 등록 수정 시 refID값 변경
    public void changeRefId(String refId) {
        this.refId = refId;
    }

    public void hide() {
        this.displayStatus = ImageDisplayStatus.HIDDEN;
    }

    public void show() {
        this.displayStatus = ImageDisplayStatus.NORMAL;
    }

    public enum RefType {
        MENU, STORE, USER
    }
}
