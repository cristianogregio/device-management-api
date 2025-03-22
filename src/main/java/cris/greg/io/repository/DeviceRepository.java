package cris.greg.io.repository;

import cris.greg.io.model.DeviceState;
import cris.greg.io.model.Device;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface DeviceRepository extends JpaRepository<Device, UUID> {
    List<Device> findByBrand(String brand);
    List<Device> findByState(DeviceState state);
}