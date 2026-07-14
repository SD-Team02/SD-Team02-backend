-- Flyway 미도입 상태에서, ddl-auto(Hibernate)가 만들지 못하는 조건부(partial) 유니크 인덱스를
-- 앱 기동 시점에 자동으로 적용하기 위한 파일. IF NOT EXISTS라 여러 번 실행돼도 안전하다.
-- (Flyway 도입 시 이 내용을 그대로 첫 마이그레이션 파일로 옮길 것)

-- 삭제되지 않은 리뷰끼리만 유니크 (주문당 활성 리뷰 1개)
CREATE UNIQUE INDEX IF NOT EXISTS ux_review_order_active
ON p_review (order_id) WHERE deleted_at IS NULL;

-- 삭제되지 않은 가게끼리만, 같은 지역(region_id) 내 이름 중복 방지
CREATE UNIQUE INDEX IF NOT EXISTS uk_p_store_name_region_not_deleted
ON p_store (name, region_id) WHERE deleted_at IS NULL;
