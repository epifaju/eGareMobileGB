package com.garemobilegb.station.repository;

import com.garemobilegb.station.domain.Station;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StationRepository extends JpaRepository<Station, Long> {

  Page<Station> findByArchivedFalse(Pageable pageable);

  Optional<Station> findByIdAndArchivedFalse(long id);

  long countByArchivedFalse();

  long countByArchivedTrue();
}
