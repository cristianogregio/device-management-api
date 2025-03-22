package cris.greg.io.controller;


import cris.greg.io.model.Device;
import cris.greg.io.model.DeviceState;
import cris.greg.io.service.DeviceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/devices")
@RequiredArgsConstructor
public class DeviceController {

    private static final Logger logger = LoggerFactory.getLogger(DeviceController.class);
    private final DeviceService deviceService;

    @Operation(summary = "Create a new device")
    @ApiResponses(value = {@ApiResponse(responseCode = "201", description = "Device created",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = Device.class))}),
            @ApiResponse(responseCode = "400", description = "Invalid input or malformed JSON request",
                    content = @Content)})
    @PostMapping
    public CompletableFuture<ResponseEntity<Device>> createDevice(@RequestBody Device device) {
        logger.info("Recieved request to create device: {}", device);
        return deviceService.saveDevice(device)
                .thenApply(savedDevice -> {
                    logger.info("Device created: {}", savedDevice);
                    return ResponseEntity.status(HttpStatus.CREATED).body(savedDevice);
                });
    }

    @Operation(summary = "Retrieve all devices", description = "Fetches a list of all devices available in the system.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved all devices",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Device.class))),
        @ApiResponse(responseCode = "404", description = "No devices found", content = @Content)
    })
    @GetMapping
    public CompletableFuture<ResponseEntity<List<Device>>> getAllDevices() {
        logger.info("Retrieving all devices");
        return deviceService.getAllDevices()
                .thenApply(ResponseEntity::ok);
    }

    @Operation(summary = "Get a device by ID")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Found the device",
                    content = {@Content(mediaType = "application/json",  schema = @Schema(implementation = Device.class))}),
            @ApiResponse(responseCode = "404", description = "Device not found", content = @Content)})
    @GetMapping("/{id}")
    public CompletableFuture<ResponseEntity<Device>> getDeviceById(@PathVariable("id") UUID id) {
        logger.info("Retrieving device with id: {}", id);
        return deviceService.getDeviceById(id)
                .thenApply(ResponseEntity::ok);
    }

    @Operation(summary = "Update a device")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Device updated", content = {@Content(mediaType = "application/json",
                    schema = @Schema(implementation = Device.class))}),
            @ApiResponse(responseCode = "404", description = "Device not found", content = @Content),
            @ApiResponse(responseCode = "406", description = "Invalid update", content = @Content)})
    @PutMapping("/{id}")
    public CompletableFuture<ResponseEntity<Device>> updateDevice(@PathVariable("id") UUID id, @RequestBody Device updatedDevice) {
        logger.info("Trying to update device with id: {}", id);
        return deviceService.updateDevice(id, updatedDevice)
                .thenApply(ResponseEntity::ok);
    }


    @Operation(summary = "Get devices by brand")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Found devices by brand",
                    content = {@Content(mediaType = "application/json", schema = @Schema(implementation = Device.class))}),
            @ApiResponse(responseCode = "404", description = "No devices found for the specified brand", content = @Content)
    })
    @GetMapping("/brand/{brand}")
    public CompletableFuture<ResponseEntity<List<Device>>> getDevicesByBrand(@PathVariable("brand") String brand) {
        logger.info("Retrieving  all devices with brand: {}", brand);
        return deviceService.getDevicesByBrand(brand)
                .thenApply(ResponseEntity::ok);
    }

    @Operation(summary = "Get devices by state")
    @ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Found devices by state",
            content = {@Content(mediaType = "application/json", schema = @Schema(implementation = Device.class))}),
            @ApiResponse(responseCode = "404", description = "No devices found for the specified state", content = @Content)
    })
    @GetMapping("/state/{state}")
    public CompletableFuture<ResponseEntity<List<Device>>> getDevicesByState(@PathVariable("state") DeviceState state) {
        logger.info("Retrieving  all devices with state: {}", state);
        return deviceService.getDevicesByState(state)
                .thenApply(ResponseEntity::ok);
    }

    @Operation(summary = "Delete a device by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Device deleted", content = @Content),
            @ApiResponse(responseCode = "404", description = "Device not found", content = @Content),
            @ApiResponse(responseCode = "406", description = "Invalid delete", content = @Content)})
    @DeleteMapping("/{id}")
    public CompletableFuture<ResponseEntity<Void>> deleteDevice(@PathVariable("id") UUID id) {
        logger.info("Trying to deleted device with id: {}", id);
        return deviceService.deleteDevice(id)
                .thenApply(v -> ResponseEntity.noContent().build());
    }

    /* Delete all devices is not part of the challenge, but during the tests I implemented it to clean everything */
    @Operation(summary = "Delete all devices")
    @ApiResponses(value = { @ApiResponse(responseCode = "204", description = "All devices deleted", content = @Content)})
    @DeleteMapping("/flush")
    public CompletableFuture<ResponseEntity<Void>> flush() {
        logger.info("Flushing all devices");
        return deviceService.flush().thenApply(clean -> ResponseEntity.noContent().build());
    }
}