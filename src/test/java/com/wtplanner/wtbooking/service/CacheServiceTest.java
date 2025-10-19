package com.wtplanner.wtbooking.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.StringRedisTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CacheServiceTest {

    @Mock
    private StringRedisTemplate redis;

    @Mock
    private ValueOperations<String, String> ops;

    @InjectMocks
    private CacheService cacheService;

    @Nested
    @DisplayName("invalidate() tests")
    class InvalidateTests {
        @ParameterizedTest
        @ValueSource(strings = {"false"})
        @DisplayName("invalidate should set valid flag to false")
        void invalidate_setsValidFalse(String expected) {
            when(redis.opsForValue()).thenReturn(ops);

            cacheService.invalidate();

            verify(ops).set("available_units_cache_valid", expected);
        }
    }

    @Nested
    @DisplayName("isValid() tests")
    class IsValidTests {
        @ParameterizedTest
        @CsvSource({"true,true", "false,false", ",false"})
        @DisplayName("isValid should return correct boolean")
        void isValid_returnsExpected(String redisValue, boolean expected) {
            when(redis.opsForValue()).thenReturn(ops);
            when(ops.get("available_units_cache_valid")).thenReturn(redisValue);

            boolean result = cacheService.isValid();

            assertThat(result).isEqualTo(expected);
        }
    }

    @Nested
    @DisplayName("getCachedCount() tests")
    class GetCachedCountTests {

        @ParameterizedTest
        @CsvSource({"123,123", ",null"})
        @DisplayName("getCachedCount should parse value or return null")
        void getCachedCount_parsesOrReturnsNull(String redisValue, String expectedStr) {
            when(redis.opsForValue()).thenReturn(ops);
            when(ops.get("available_units_count")).thenReturn(redisValue);

            Long result = cacheService.getCachedCount();

            if (redisValue == null || redisValue.isEmpty()) {
                assertThat(result).isNull();
            } else {
                assertThat(result).isEqualTo(Long.parseLong(redisValue));
            }
        }
    }


    @Nested
    @DisplayName("updateCount() tests")
    class UpdateCountTests {
        @ParameterizedTest
        @CsvSource({"0", "5", "100"})
        @DisplayName("updateCount sets count and marks cache valid")
        void updateCount_updatesCountAndValid(long count) {
            when(redis.opsForValue()).thenReturn(ops);

            cacheService.updateCount(count);

            verify(ops).set("available_units_count", String.valueOf(count));
            verify(ops).set("available_units_cache_valid", "true");
        }
    }

    @Nested
    @DisplayName("adjustCount() tests")
    class AdjustCountTests {
        @ParameterizedTest
        @CsvSource({"10,5,15,true", "0,-1,0,true", ",5,,false"})
        @DisplayName("adjustCount increments/decrements if valid")
        void adjustCount_behavior(String currentVal, int delta, String expectedValStr, boolean expectSuccess) {
            when(redis.opsForValue()).thenReturn(ops);
            when(ops.get("available_units_count")).thenReturn(currentVal);
            lenient().when(ops.get("available_units_cache_valid")).thenReturn(currentVal != null ? "true" : null);

            boolean result = cacheService.adjustCount(delta);

            assertThat(result).isEqualTo(expectSuccess);
            if (expectSuccess) {
                verify(ops).set("available_units_count", expectedValStr);
            }
        }

        @ParameterizedTest
        @CsvSource({"0,10", "5,5"})
        @DisplayName("adjustCount with fallback rebuilds cache if invalid")
        void adjustCount_withFallback(long delta, long fallback) {
            when(redis.opsForValue()).thenReturn(ops);
            when(ops.get("available_units_count")).thenReturn(null);

            cacheService.adjustCount((int) delta, fallback);

            verify(ops).set("available_units_count", String.valueOf(fallback));
            verify(ops).set("available_units_cache_valid", "true");
        }
    }
}
