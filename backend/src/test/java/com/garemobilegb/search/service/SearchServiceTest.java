package com.garemobilegb.search.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.garemobilegb.shared.exceptions.BusinessException;
import com.garemobilegb.vehicle.repository.VehicleRepository;
import com.garemobilegb.vehicle.service.VehicleWaitTimeEstimator;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class SearchServiceTest {

  @Mock VehicleRepository vehicleRepository;
  @Mock VehicleWaitTimeEstimator vehicleWaitTimeEstimator;

  @InjectMocks SearchService searchService;

  @Test
  void suggestDestinations_rejectsShortQuery() {
    assertThatThrownBy(() -> searchService.suggestDestinations("a"))
        .isInstanceOf(BusinessException.class)
        .satisfies(
            ex -> assertThat(((BusinessException) ex).getCode()).isEqualTo("QUERY_TOO_SHORT"));
  }

  @Test
  void suggestDestinations_ok() {
    when(vehicleRepository.findDistinctRouteLabelsContaining(any(), any(Pageable.class)))
        .thenReturn(new PageImpl<>(List.of("Bissau → Gabú")));
    var result = searchService.suggestDestinations("bis");
    assertThat(result).hasSize(1);
    assertThat(result.get(0).label()).isEqualTo("Bissau → Gabú");
  }
}
