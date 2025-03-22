package cris.greg.io.service;

import cris.greg.io.exception.DeviceValidationException;
import cris.greg.io.model.DeviceState;
import cris.greg.io.model.Device;
import cris.greg.io.repository.DeviceRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@Service
@RequiredArgsConstructor
public class DeviceService {

    private static final Logger logger = LoggerFactory.getLogger(DeviceService.class);
    private final DeviceRepository deviceRepository;

    private final Executor executor = Executors.newFixedThreadPool(10);

    /**
     * Saves a device asynchronously.
     *
     * @param device the device to be saved
     * @return a CompletableFuture containing the saved device
     * @throws DeviceValidationException if the device state is not allowed
     */
    public CompletableFuture<Device> saveDevice(Device device) {
        if (!isStateAllowed(device.getState())) {
            logger.error("Invalid state: {}", device.getState());
            throw new DeviceValidationException("Invalid state", HttpStatus.BAD_REQUEST);
        }
        return CompletableFuture.supplyAsync(() -> deviceRepository.save(device), executor);
    }

    /**
     * Retrieves all devices asynchronously.
     *
     * @return a CompletableFuture containing a list of all devices
     * @throws DeviceValidationException if no devices are found
     */
    public CompletableFuture<List<Device>> getAllDevices() {
        return CompletableFuture.supplyAsync(() -> {
            List<Device> devices = deviceRepository.findAll();
            if (devices.isEmpty()) {
                logger.warn("No devices found");
                throw new DeviceValidationException("No devices found", HttpStatus.NOT_FOUND);
            }
            return devices;
        }, executor);
    }

    /**
     * Retrieves a device by its ID asynchronously.
     *
     * @param id the UUID of the device
     * @return a CompletableFuture containing the device
     * @throws DeviceValidationException if the device is not found
     */
    public CompletableFuture<Device> getDeviceById(UUID id) {
        return CompletableFuture.supplyAsync(() ->
                deviceRepository.findById(id)
                        .orElseThrow(() -> {
                            logger.error(String.format("Device with id %s not found", id.toString()));
                            return new DeviceValidationException("Device not found", HttpStatus.NOT_FOUND);
                        }), executor);
    }

    /**
     * Updates a device asynchronously.
     *
     * @param id the UUID of the device to be updated
     * @param updatedDevice the updated device information
     * @return a CompletableFuture containing the updated device
     * @throws DeviceValidationException if the device is not found or cannot be updated
     */
    public CompletableFuture<Device> updateDevice(UUID id, Device updatedDevice) {
        return CompletableFuture.supplyAsync(() -> {
            Device existingDevice = deviceRepository.findById(id)
                    .orElseThrow(() -> {
                        logger.error("Device not found with id: {}", id);
                        return new DeviceValidationException("Device not found", HttpStatus.NOT_FOUND);
                    });

            if (existingDevice.getState() == DeviceState.IN_USE &&
                    (!existingDevice.getName().equals(updatedDevice.getName()) ||
                            !existingDevice.getBrand().equals(updatedDevice.getBrand()))) {
                logger.error("Device is in use and cannot be updated");
                throw new DeviceValidationException("Cannot update name or brand of a device " + DeviceState.IN_USE.name(), HttpStatus.NOT_ACCEPTABLE);
            }

            updatedDevice.setId(id);
            updatedDevice.setCreationTime(existingDevice.getCreationTime());

            return deviceRepository.save(updatedDevice);
        }, executor);
    }

    /**
     * Retrieves devices by their brand asynchronously.
     *
     * @param brand the brand of the devices
     * @return a CompletableFuture containing a list of devices with the specified brand
     */
    public CompletableFuture<List<Device>> getDevicesByBrand(String brand) {
        return CompletableFuture.supplyAsync(() -> {
            logger.info("Getting devices by brand {}", brand);
            List<Device> devices = deviceRepository.findByBrand(brand);
            if (devices.isEmpty()) {
                logger.warn("No devices found for brand {}", brand);
                throw new DeviceValidationException("No devices found for branc " + brand , HttpStatus.NOT_FOUND);
            }
            return devices;
        }, executor);
    }

    /**
     * Retrieves devices by their state asynchronously.
     *
     * @param state the state of the devices
     * @return a CompletableFuture containing a list of devices with the specified state
     */
    public CompletableFuture<List<Device>> getDevicesByState(DeviceState state) {
        return CompletableFuture.supplyAsync(() -> {
            logger.info("Getting devices by state {}", state);
            List<Device> devices = deviceRepository.findByState(state);
            if (devices.isEmpty()) {
                logger.warn("No devices found for state {}", state);
                throw new DeviceValidationException("No devices found for state " + state , HttpStatus.NOT_FOUND);
            }
            return devices;
        }, executor);
    }

    /**
     * Deletes a device by its ID asynchronously.
     *
     * @param id the UUID of the device to be deleted
     * @return a CompletableFuture representing the completion of the deletion
     * @throws DeviceValidationException if the device is not found or cannot be deleted
     */
    public CompletableFuture<Void> deleteDevice(UUID id) {
        return CompletableFuture.runAsync(() -> {
            Device device = deviceRepository.findById(id)
                    .orElseThrow(() -> {
                        logger.error("Device not found with id: {}", id);
                        return new DeviceValidationException("Device not found", HttpStatus.NOT_FOUND);
                    });
            if (device.getState() == DeviceState.IN_USE) {
                logger.error("Device is in use and cannot be deleted");
                throw new DeviceValidationException("In-use devices cannot be deleted", HttpStatus.NOT_ACCEPTABLE);
            }
            deviceRepository.delete(device);
        }, executor);
    }

    /**
     * Deletes all devices asynchronously.
     *
     * @return a CompletableFuture representing the completion of the deletion
     */
    public CompletableFuture<Void> flush() {
        return CompletableFuture.supplyAsync(() -> {
            logger.info("Flushing all devices");
            deviceRepository.deleteAll();
            return null;
        }, executor);
    }

    /**
     * Checks if the given device state is allowed.
     *
     * @param state the state to be checked
     * @return true if the state is allowed, false otherwise
     */
    private boolean isStateAllowed(DeviceState state) {
        for (DeviceState allowedState : DeviceState.values()) {
            if (allowedState == state) return true;
        }
        return false;
    }
}