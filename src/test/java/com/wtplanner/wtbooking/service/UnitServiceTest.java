package com.wtplanner.wtbooking.service;

import com.wtplanner.wtbooking.model.dict.AccommodationType;
import com.wtplanner.wtbooking.model.dto.UnitDto;
import com.wtplanner.wtbooking.model.entity.Unit;
import com.wtplanner.wtbooking.remote.repository.UnitRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UnitServiceTest {

    @Mock
    private UnitRepository repo;

    @Mock
    private CacheService cacheService;

    @InjectMocks
    private UnitService unitService;

    /* ---------- search(...) tests ---------- */
    @Nested
    @DisplayName("search() tests")
    class SearchTests {

        static Stream<Arguments> searchParams() {
            return Stream.of(
                    Arguments.of(BigDecimal.valueOf(50), BigDecimal.valueOf(150)),
                    Arguments.of(BigDecimal.ZERO, BigDecimal.valueOf(1000))
            );
        }

        @ParameterizedTest
        @MethodSource("searchParams")
        @DisplayName("search should return page of units")
        void search_returnsPage(BigDecimal minCost, BigDecimal maxCost) {
            Pageable pageable = PageRequest.of(0, 10);
            Unit unit = Unit.builder().cost(BigDecimal.valueOf(100)).build();
            Page<Unit> page = new PageImpl<>(Collections.singletonList(unit));

            when(repo.findAllByCostBetweenAndIsAvailableTrue(minCost, maxCost, pageable)).thenReturn(page);

            Page<Unit> result = unitService.search(minCost, maxCost, pageable);

            assertThat(result).isNotNull();
            assertThat(result.getContent()).containsExactly(unit);
            verify(repo).findAllByCostBetweenAndIsAvailableTrue(minCost, maxCost, pageable);
        }
    }

    /* ---------- countAvailable() tests ---------- */
    @Nested
    @DisplayName("countAvailable() tests")
    class CountAvailableTests {

        @ParameterizedTest
        @CsvSource({"true,100", "false,200"})
        @DisplayName("countAvailable returns cached or db count")
        void countAvailable_usesCacheOrDb(boolean cacheValid, long cachedCount) {
            when(cacheService.isValid()).thenReturn(cacheValid);
            when(cacheService.getCachedCount()).thenReturn(cacheValid ? cachedCount : null);

            if (!cacheValid) {
                when(repo.countAvailable()).thenReturn(cachedCount);
            }

            long result = unitService.countAvailable();

            assertThat(result).isEqualTo(cachedCount);

            if (cacheValid) {
                verify(cacheService, never()).updateCount(anyLong());
            } else {
                verify(cacheService).updateCount(cachedCount);
            }
        }
    }

    /* ---------- save(...) tests ---------- */
    @Nested
    @DisplayName("save() tests")
    class SaveTests {

        static Stream<Arguments> saveParams() {
            return Stream.of(
                    Arguments.of(true, BigDecimal.valueOf(120)),
                    Arguments.of(false, BigDecimal.valueOf(80))
            );
        }

        @ParameterizedTest
        @MethodSource("saveParams")
        @DisplayName("save should persist unit and update cache if available")
        void save_unit(boolean available, BigDecimal cost) {
            UnitDto dto = new UnitDto();
            dto.setRooms(2);
            dto.setAccommodationType(AccommodationType.APARTMENTS);
            dto.setFloor(1);
            dto.setIsAvailable(available);
            dto.setCost(cost);
            dto.setBookingMarkupPercent(BigDecimal.valueOf(10));
            dto.setDescription("desc");

            Unit savedUnit = Unit.builder()
                    .rooms(dto.getRooms())
                    .accommodationType(dto.getAccommodationType())
                    .floor(dto.getFloor())
                    .isAvailable(dto.getIsAvailable())
                    .cost(dto.getCost())
                    .bookingMarkupPercent(dto.getBookingMarkupPercent())
                    .description(dto.getDescription())
                    .build();

            when(repo.save(any(Unit.class))).thenReturn(savedUnit);
            lenient().when(repo.countAvailable()).thenReturn(10L);

            Unit result = unitService.save(dto);

            assertThat(result).isEqualTo(savedUnit);
            verify(repo).save(any(Unit.class));
            if (available) {
                verify(cacheService).adjustCount(+1, 10L);
            } else {
                verify(cacheService, never()).adjustCount(anyInt(), anyLong());
            }
        }
    }
}
