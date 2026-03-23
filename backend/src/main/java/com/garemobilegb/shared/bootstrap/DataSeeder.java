package com.garemobilegb.shared.bootstrap;

import com.garemobilegb.auth.domain.Role;
import com.garemobilegb.auth.domain.User;
import com.garemobilegb.auth.repository.UserRepository;
import com.garemobilegb.station.domain.Station;
import com.garemobilegb.station.repository.StationRepository;
import com.garemobilegb.vehicle.domain.Vehicle;
import com.garemobilegb.vehicle.domain.VehicleSeatLayout;
import com.garemobilegb.vehicle.domain.VehicleStatus;
import com.garemobilegb.vehicle.repository.VehicleRepository;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataSeeder {

  private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

  @Bean
  @Order(1)
  CommandLineRunner seedStations(
      StationRepository stations,
      UserRepository users,
      PasswordEncoder passwordEncoder,
      @Value("${app.seed.admin-phone:+24500000001}") String adminPhone,
      @Value("${app.seed.admin-password:dev-admin-change-me}") String adminPassword) {
    return args -> {
      if (!users.existsByPhoneNumber(adminPhone)) {
        users.save(
            new User(adminPhone, passwordEncoder.encode(adminPassword), Role.ADMIN));
        log.info("Compte administrateur seed créé (téléphone suffixe masqué).");
      }
      if (stations.count() > 0) {
        return;
      }
      // Coordonnées approximatives — données de démo Guinée-Bissau
      stations.save(
          new Station(
              "Gare routière de Bissau (Bandim)",
              "Bissau",
              11.8636,
              -15.5986,
              "Principale gare routière de la capitale."));
      stations.save(
          new Station(
              "Gare de Bafatá",
              "Bafatá",
              12.1711,
              -14.6575,
              "Hub routier centre-nord."));
      stations.save(
          new Station(
              "Gare de Gabú",
              "Gabú",
              12.2800,
              -14.2222,
              "Corridor vers l’est du pays."));
      stations.save(
          new Station(
              "Arrêt routier Cacheu",
              "Cacheu",
              12.2700,
              -16.1650,
              "Zone nord-ouest."));
      stations.save(
          new Station(
              "Gare de Bissorã",
              "Bissorã",
              12.2230,
              -15.4480,
              "Plateau de Bissorã."));
      stations.save(
          new Station(
              "Embarcadère Bolama",
              "Bolama",
              11.5779,
              -15.4742,
              "Îles Bijagós — correspondances."));
      stations.save(
          new Station(
              "Gare Bubaque",
              "Bubaque",
              11.3011,
              -15.8333,
              "Île Bubaque."));
      stations.save(
          new Station(
              "Arrêt Buba",
              "Buba",
              11.5883,
              -15.0033,
              "Quinara."));
      stations.save(
          new Station(
              "Gare de Farim",
              "Farim",
              12.4883,
              -15.2206,
              "Nord du Oio."));
      stations.save(
          new Station(
              "Arrêt Mansôa",
              "Mansôa",
              12.3250,
              -15.3180,
              "Route vers le centre."));
      log.info("Seed : {} gares insérées.", stations.count());
    };
  }

  @Bean
  @Order(2)
  CommandLineRunner seedVehicles(VehicleRepository vehicles, StationRepository stations) {
    return args -> {
      if (vehicles.count() > 0) {
        return;
      }
      String[] demoRoutes =
          new String[] {
            "Bissau → Gabú",
            "Bissau → Bafatá",
            "Bafatá → Bissau",
            "Gabú → Bissau",
            "Cacheu → Bissau",
            "Bissau → Buba",
            "Bissorã → Farim",
            "Mansôa → Bissau",
            "Bissau → Bolama",
            "Bubaque → Bissau"
          };
      int i = 0;
      for (Station s : stations.findAll()) {
        VehicleSeatLayout layout = VehicleSeatLayout.values()[i % VehicleSeatLayout.values().length];
        VehicleStatus st = VehicleStatus.values()[i % VehicleStatus.values().length];
        int cap = layout.capacity();
        int occ = Math.min(cap - 1, 1 + (i % Math.max(1, cap - 1)));
        String route = demoRoutes[i % demoRoutes.length];
        int fareXof = 3_500 + (i % 6) * 1_000;
        vehicles.save(
            new Vehicle(
                s,
                String.format("GW-%04d", 100 + i),
                route,
                cap,
                occ,
                layout,
                st,
                Instant.now().plusSeconds(900L + i * 120L),
                s.getLatitude() + ((i % 5) - 2) * 0.005,
                s.getLongitude() + ((i % 5) - 2) * 0.005,
                Instant.now(),
                fareXof));
        i++;
      }
      log.info("Seed : {} véhicules insérés.", vehicles.count());
    };
  }
}
