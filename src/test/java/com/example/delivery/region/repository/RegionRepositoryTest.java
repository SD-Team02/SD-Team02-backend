package com.example.delivery.region.repository;

import static org.assertj.core.api.Assertions.*;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.test.context.ActiveProfiles;

import com.example.delivery.global.config.QuerydslConfig;
import com.example.delivery.region.dto.response.ResGetRegionDto;
import com.example.delivery.region.entity.Region;
import com.example.delivery.region.entity.RegionStatus;

@DataJpaTest
@ActiveProfiles("test")
@Import({QuerydslConfig.class, RegionRepositoryTest.TestAuditConfig.class})
class RegionRepositoryTest {

	@Autowired
	private RegionRepository regionRepository;

	// created_by가 nullable=false라 감사(Auditing) 활성화 + AuditorAware 필요
	@TestConfiguration
	@EnableJpaAuditing
	static class TestAuditConfig {
		@Bean
		AuditorAware<Long> auditorAware() {
			return () -> Optional.of(1L);
		}
	}

	@Test
	@DisplayName("findAllWithParentName - self-join으로 상위 지역명을 단일 쿼리로 채운다")
	void findAllWithParentName_joinsParentInSingleQuery() {
		// given: 서울(최상위) > 강남구(자식)
		Region seoul = regionRepository.save(new Region("서울"));
		regionRepository.save(new Region("강남구", seoul.getRegionId()));
		regionRepository.flush();

		// when
		Page<ResGetRegionDto> page = regionRepository.findAllWithParentName(
			RegionStatus.ACTIVE,
			PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "name"))
		);

		// then
		assertThat(page.getTotalElements()).isEqualTo(2);

		Map<String, ResGetRegionDto> byName = page.getContent().stream()
			.collect(Collectors.toMap(ResGetRegionDto::getName, r -> r));

		// 자식은 상위 지역명이 채워지고, 최상위는 null
		assertThat(byName.get("강남구").getParentRegionName()).isEqualTo("서울");
		assertThat(byName.get("서울").getParentRegionName()).isNull();
	}

	@Test
	@DisplayName("findAllWithParentName - status/soft delete 필터 및 정렬 동작")
	void findAllWithParentName_filtersAndSorts() {
		Region active1 = regionRepository.save(new Region("가지역"));
		Region active2 = regionRepository.save(new Region("나지역"));
		Region deleted = regionRepository.save(new Region("삭제지역"));
		deleted.softDelete(1L);
		regionRepository.flush();

		Page<ResGetRegionDto> page = regionRepository.findAllWithParentName(
			RegionStatus.ACTIVE,
			PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "name"))
		);

		// 삭제된 지역은 제외, 이름 오름차순 정렬
		assertThat(page.getContent()).extracting(ResGetRegionDto::getName)
			.containsExactly("가지역", "나지역");
	}
}
