

import cris.greg.io.exception.DeviceValidationException;
import cris.greg.io.model.Device;
import cris.greg.io.model.DeviceState;
import cris.greg.io.repository.DeviceRepository;
import cris.greg.io.service.DeviceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class DeviceServiceTest {

    @Mock
    private DeviceRepository deviceRepository;

    @InjectMocks
    private DeviceService deviceService;

    private Device device;
    private UUID deviceId;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        deviceId = UUID.randomUUID();
        device = Device.builder()
                .id(deviceId)
                .name("Device1")
                .brand("BrandA")
                .state(DeviceState.AVAILABLE)
                .build();
    }

    @Test
    void saveDevice_ValidState_ShouldSaveDevice() {
        when(deviceRepository.save(any(Device.class))).thenReturn(device);

        CompletableFuture<Device> result = deviceService.saveDevice(device);

        assertNotNull(result);
        assertEquals(device, result.join());
        verify(deviceRepository, times(1)).save(device);
    }

    @Test
    void saveDevice_InvalidState_ShouldThrowException() {
        device.setState(null);

        DeviceValidationException exception = assertThrows(DeviceValidationException.class, () -> {
            deviceService.saveDevice(device).join();
        });

        assertEquals("Invalid state", exception.getMessage());
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        verify(deviceRepository, never()).save(any(Device.class));
    }

    @Test
    void getAllDevices_ShouldReturnDevices() {
        List<Device> devices = Arrays.asList(device);
        when(deviceRepository.findAll()).thenReturn(devices);

        CompletableFuture<List<Device>> result = deviceService.getAllDevices();

        assertNotNull(result);
        assertEquals(devices, result.join());
        verify(deviceRepository, times(1)).findAll();
    }

    @Test
    void getAllDevices_NoDevicesFound_ShouldThrowException() {
        when(deviceRepository.findAll()).thenReturn(Arrays.asList());

       DeviceValidationException exception = assertThrows(DeviceValidationException.class, () -> {
           try {
               deviceService.getAllDevices().join();
           } catch (CompletionException e) {
               throw (DeviceValidationException) e.getCause();
           }
       });

        assertEquals("No devices found", exception.getMessage());
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
        verify(deviceRepository, times(1)).findAll();
    }

    @Test
    void getDeviceById_ExistingId_ShouldReturnDevice() {
        when(deviceRepository.findById(deviceId)).thenReturn(Optional.of(device));

        CompletableFuture<Device> result = deviceService.getDeviceById(deviceId);

        assertNotNull(result);
        assertEquals(device, result.join());
        verify(deviceRepository, times(1)).findById(deviceId);
    }

    @Test
    void getDeviceById_NonExistingId_ShouldThrowException() {
        when(deviceRepository.findById(deviceId)).thenReturn(Optional.empty());

        DeviceValidationException exception = assertThrows(DeviceValidationException.class, () -> {
            try {
                deviceService.getDeviceById(deviceId).join();
            } catch (CompletionException e) {
                throw (DeviceValidationException) e.getCause();
            }
        });

        assertEquals("Device not found", exception.getMessage());
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
        verify(deviceRepository, times(1)).findById(deviceId);
    }

    @Test
    void updateDevice_ExistingId_ShouldUpdateDevice() {
        Device updatedDevice = Device.builder()
                .id(deviceId)
                .name("UpdatedDevice")
                .brand("UpdatedBrand")
                .state(DeviceState.INACTIVE)
                .build();

        when(deviceRepository.findById(deviceId)).thenReturn(Optional.of(device));
        when(deviceRepository.save(any(Device.class))).thenReturn(updatedDevice);

        CompletableFuture<Device> result = deviceService.updateDevice(deviceId, updatedDevice);

        assertNotNull(result);
        assertEquals(updatedDevice, result.join());
        verify(deviceRepository, times(1)).findById(deviceId);
        verify(deviceRepository, times(1)).save(updatedDevice);
    }

    @Test
    void updateDevice_NonExistingId_ShouldThrowException() {
        Device updatedDevice = Device.builder()
                .id(deviceId)
                .name("UpdatedDevice")
                .brand("UpdatedBrand")
                .state(DeviceState.AVAILABLE)
                .build();

        when(deviceRepository.findById(deviceId)).thenReturn(Optional.empty());

        DeviceValidationException exception = assertThrows(DeviceValidationException.class, () -> {
            try {
                deviceService.updateDevice(deviceId, updatedDevice).join();
            } catch (CompletionException e) {
                throw (DeviceValidationException) e.getCause();
            }
        });

        assertEquals("Device not found", exception.getMessage());
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
        verify(deviceRepository, times(1)).findById(deviceId);
        verify(deviceRepository, never()).save(any(Device.class));
    }

    @Test
    void getDevicesByBrand_ShouldReturnDevices() {
        List<Device> devices = Arrays.asList(device);
        when(deviceRepository.findByBrand("BrandA")).thenReturn(devices);

        CompletableFuture<List<Device>> result = deviceService.getDevicesByBrand("BrandA");

        assertNotNull(result);
        assertEquals(devices, result.join());
        verify(deviceRepository, times(1)).findByBrand("BrandA");
    }

    @Test
    void getDevicesByBrand_NoDevicesFound_ShouldThrowException() {
        when(deviceRepository.findByBrand("BrandA")).thenReturn(Arrays.asList());

        DeviceValidationException exception = assertThrows(DeviceValidationException.class, () -> {
            try {
                deviceService.getDevicesByBrand("BrandA").join();
            } catch (CompletionException e) {
                throw (DeviceValidationException) e.getCause();
            }
        });

        assertEquals("No devices found for branc BrandA", exception.getMessage());
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
        verify(deviceRepository, times(1)).findByBrand("BrandA");
    }

    @Test
    void getDevicesByState_ShouldReturnDevices() {
        List<Device> devices = Arrays.asList(device);
        when(deviceRepository.findByState(DeviceState.AVAILABLE)).thenReturn(devices);

        CompletableFuture<List<Device>> result = deviceService.getDevicesByState(DeviceState.AVAILABLE);

        assertNotNull(result);
        assertEquals(devices, result.join());
        verify(deviceRepository, times(1)).findByState(DeviceState.AVAILABLE);
    }

    @Test
    void getDevicesByState_NoDevicesFound_ShouldThrowException() {
        when(deviceRepository.findByState(DeviceState.AVAILABLE)).thenReturn(Arrays.asList());

        DeviceValidationException exception = assertThrows(DeviceValidationException.class, () -> {
            try {
                deviceService.getDevicesByState(DeviceState.AVAILABLE).join();
            } catch (CompletionException e) {
                throw (DeviceValidationException) e.getCause();
            }
        });

        assertEquals("No devices found for state AVAILABLE", exception.getMessage());
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
        verify(deviceRepository, times(1)).findByState(DeviceState.AVAILABLE);
    }

    @Test
    void deleteDevice_ExistingId_ShouldDeleteDevice() {
        when(deviceRepository.findById(deviceId)).thenReturn(Optional.of(device));

        CompletableFuture<Void> result = deviceService.deleteDevice(deviceId);

        assertNotNull(result);
        result.join();
        verify(deviceRepository, times(1)).findById(deviceId);
        verify(deviceRepository, times(1)).delete(device);
    }

    @Test
    void deleteDevice_NonExistingId_ShouldThrowException() {
        when(deviceRepository.findById(deviceId)).thenReturn(Optional.empty());

       DeviceValidationException exception = assertThrows(DeviceValidationException.class, () -> {
            try {
                deviceService.deleteDevice(deviceId).join();
            } catch (CompletionException e) {
                throw (DeviceValidationException) e.getCause();
            }
        });

        assertEquals("Device not found", exception.getMessage());
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
        verify(deviceRepository, times(1)).findById(deviceId);
        verify(deviceRepository, never()).delete(any(Device.class));
    }
}