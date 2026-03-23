package com.garemobilegb.station.repository;

import com.garemobilegb.station.domain.Station;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StationRepository extends JpaRepository<Station, Long> {}
