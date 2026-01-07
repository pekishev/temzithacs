"""Sensor platform for integration_blueprint."""
from __future__ import annotations

from homeassistant.components.sensor import SensorEntity, SensorEntityDescription, SensorDeviceClass, SensorStateClass, UnitOfTemperature
from homeassistant.const import UnitOfPower, UnitOfVolumeFlowRate, UnitOfFrequency
from homeassistant.config_entries import ConfigEntry
from homeassistant.core import HomeAssistant, callback
from homeassistant.helpers import entity_registry as er

from .const import DOMAIN
from .coordinator import TemzitUpdateCoordinator
from .entity import TemzitEntity

ENTITY_DESCRIPTIONS = (
    SensorEntityDescription(
        key="temzit",
        name="Integration Sensor",
        icon="mdi:format-quote-close",
    ),
)


async def async_setup_entry(hass, entry, async_add_devices):
    """Set up the sensor platform."""
    coordinator = hass.data[DOMAIN][entry.entry_id]
    async_add_devices(
        [
            SupplySensor(coordinator),
            ReturnSensor(coordinator),
            IndoorSensor(coordinator),
            OutdoorSensor(coordinator),
            HotWaterSensor(coordinator),
            ConsumptionSensor(coordinator),
            HeatPowerSensor(coordinator),
            COPSensor(coordinator),
            FlowSensor(coordinator),
            CompressorSensor(coordinator),
            ErrorSensor(coordinator),
            # Hidden sensors (скрыты по умолчанию)
            StateSensor(coordinator),
            ScheduleSensor(coordinator),
        ]
    )


class IndoorSensor(TemzitEntity, SensorEntity):
    """Representation of a sensor."""

    _attr_name = "Температура в доме"
    _attr_native_unit_of_measurement = UnitOfTemperature.CELSIUS
    _attr_suggested_display_precision = 1
    _attr_device_class = SensorDeviceClass.TEMPERATURE
    _attr_state_class = SensorStateClass.MEASUREMENT

    def __init__(self, coordinator):
        """Pass coordinator to CoordinatorEntity."""
        super().__init__(coordinator)
        self._attr_unique_id = coordinator.config_entry.entry_id + "_indoor_temp"

    @callback
    def _handle_coordinator_update(self) -> None:
        """Handle updated data from the coordinator."""
        self._attr_native_value = self.coordinator.data.indoor_temp
        self.async_write_ha_state()


class OutdoorSensor(TemzitEntity, SensorEntity):
    """Representation of a sensor."""

    _attr_name = "Температура на улице"
    _attr_native_unit_of_measurement = UnitOfTemperature.CELSIUS
    _attr_suggested_display_precision = 1
    _attr_device_class = SensorDeviceClass.TEMPERATURE
    _attr_state_class = SensorStateClass.MEASUREMENT

    def __init__(self, coordinator):
        """Pass coordinator to CoordinatorEntity."""
        super().__init__(coordinator)
        self._attr_unique_id = coordinator.config_entry.entry_id + "_outdoor_temp"

    @callback
    def _handle_coordinator_update(self) -> None:
        """Handle updated data from the coordinator."""
        self._attr_native_value = self.coordinator.data.outdoor_temp
        self.async_write_ha_state()

class HotWaterSensor(TemzitEntity, SensorEntity):
    """Representation of a sensor."""

    _attr_name = "Температура ГВС"
    _attr_native_unit_of_measurement = UnitOfTemperature.CELSIUS
    _attr_suggested_display_precision = 1
    _attr_device_class = SensorDeviceClass.TEMPERATURE
    _attr_state_class = SensorStateClass.MEASUREMENT

    def __init__(self, coordinator):
        """Pass coordinator to CoordinatorEntity."""
        super().__init__(coordinator)
        self._attr_unique_id = coordinator.config_entry.entry_id + "_hotwater_temp"

    @callback
    def _handle_coordinator_update(self) -> None:
        """Handle updated data from the coordinator."""
        self._attr_native_value = self.coordinator.data.hotwater_temp
        self.async_write_ha_state()

class SupplySensor(TemzitEntity, SensorEntity):
    """Representation of a sensor."""

    _attr_name = "Подача"
    _attr_native_unit_of_measurement = UnitOfTemperature.CELSIUS
    _attr_suggested_display_precision = 1
    _attr_device_class = SensorDeviceClass.TEMPERATURE
    _attr_state_class = SensorStateClass.MEASUREMENT
    _attr_entity_registry_enabled_default = True

    def __init__(self, coordinator):
        """Pass coordinator to CoordinatorEntity."""
        super().__init__(coordinator)
        self._attr_unique_id = coordinator.config_entry.entry_id + "_supply_temp"

    @callback
    def _handle_coordinator_update(self) -> None:
        """Handle updated data from the coordinator."""
        self._attr_native_value = self.coordinator.data.supply_temp
        self.async_write_ha_state()

class ReturnSensor(TemzitEntity, SensorEntity):
    """Representation of a sensor."""

    _attr_name = "Обратка"
    _attr_native_unit_of_measurement = UnitOfTemperature.CELSIUS
    _attr_suggested_display_precision = 1
    _attr_device_class = SensorDeviceClass.TEMPERATURE
    _attr_state_class = SensorStateClass.MEASUREMENT
    _attr_entity_registry_enabled_default = True

    def __init__(self, coordinator):
        """Pass coordinator to CoordinatorEntity."""
        super().__init__(coordinator)
        self._attr_unique_id = coordinator.config_entry.entry_id + "_return_temp"

    @callback
    def _handle_coordinator_update(self) -> None:
        """Handle updated data from the coordinator."""
        self._attr_native_value = self.coordinator.data.return_temp
        self.async_write_ha_state()

class ConsumptionSensor(TemzitEntity, SensorEntity):
    """Representation of a sensor."""

    _attr_name = "Потребление"
    _attr_native_unit_of_measurement = UnitOfPower.KILO_WATT
    _attr_device_class = SensorDeviceClass.POWER
    _attr_state_class = SensorStateClass.MEASUREMENT

    def __init__(self, coordinator):
        """Pass coordinator to CoordinatorEntity."""
        super().__init__(coordinator)
        self._attr_unique_id = coordinator.config_entry.entry_id + "_consumption"

    @callback
    def _handle_coordinator_update(self) -> None:
        """Handle updated data from the coordinator."""
        self._attr_native_value = self.coordinator.data.consumption
        self.async_write_ha_state()


class HeatPowerSensor(TemzitEntity, SensorEntity):
    """Representation of a sensor."""

    _attr_name = "Мощность нагрева"
    _attr_native_unit_of_measurement = UnitOfPower.KILO_WATT
    _attr_device_class = SensorDeviceClass.POWER
    _attr_state_class = SensorStateClass.MEASUREMENT

    def __init__(self, coordinator):
        """Pass coordinator to CoordinatorEntity."""
        super().__init__(coordinator)
        self._attr_unique_id = coordinator.config_entry.entry_id + "_heat_power"

    @callback
    def _handle_coordinator_update(self) -> None:
        """Handle updated data from the coordinator."""
        self._attr_native_value = self.coordinator.data.heat_power
        self.async_write_ha_state()

class FlowSensor(TemzitEntity, SensorEntity):
    """Representation of a sensor."""

    _attr_name = "Проток"
    _attr_native_unit_of_measurement = UnitOfVolumeFlowRate.LITERS_PER_MINUTE
    _attr_state_class = SensorStateClass.MEASUREMENT
    _attr_suggested_display_precision = 1

    def __init__(self, coordinator):
        """Pass coordinator to CoordinatorEntity."""
        super().__init__(coordinator)
        self._attr_unique_id = coordinator.config_entry.entry_id + "_flow"

    @callback
    def _handle_coordinator_update(self) -> None:
        """Handle updated data from the coordinator."""
        self._attr_native_value = self.coordinator.data.flow
        self.async_write_ha_state()


class StateSensor(TemzitEntity, SensorEntity):
    """Representation of a state sensor."""

    _attr_name = "Состояние"
    _attr_icon = "mdi:state-machine"
    _attr_state_class = SensorStateClass.MEASUREMENT
    _attr_entity_registry_enabled_default = False  # Hidden by default

    def __init__(self, coordinator):
        """Pass coordinator to CoordinatorEntity."""
        super().__init__(coordinator)
        self._attr_unique_id = coordinator.config_entry.entry_id + "_state"

    async def async_added_to_hass(self) -> None:
        """When entity is added to hass, hide it."""
        await super().async_added_to_hass()
        registry = er.async_get(self.hass)
        if entity_id := registry.async_get_entity_id("sensor", DOMAIN, self.unique_id):
            registry.async_update_entity(entity_id, hidden_by=er.RegistryEntryHider.INTEGRATION)

    @callback
    def _handle_coordinator_update(self) -> None:
        """Handle updated data from the coordinator."""
        self._attr_native_value = self.coordinator.data.state
        self.async_write_ha_state()


class ScheduleSensor(TemzitEntity, SensorEntity):
    """Representation of a schedule sensor."""

    _attr_name = "Расписание"
    _attr_icon = "mdi:calendar-clock"
    _attr_state_class = SensorStateClass.MEASUREMENT
    _attr_entity_registry_enabled_default = False  # Hidden by default

    def __init__(self, coordinator):
        """Pass coordinator to CoordinatorEntity."""
        super().__init__(coordinator)
        self._attr_unique_id = coordinator.config_entry.entry_id + "_schedule"

    async def async_added_to_hass(self) -> None:
        """When entity is added to hass, hide it."""
        await super().async_added_to_hass()
        registry = er.async_get(self.hass)
        if entity_id := registry.async_get_entity_id("sensor", DOMAIN, self.unique_id):
            registry.async_update_entity(entity_id, hidden_by=er.RegistryEntryHider.INTEGRATION)

    @callback
    def _handle_coordinator_update(self) -> None:
        """Handle updated data from the coordinator."""
        self._attr_native_value = self.coordinator.data.schedule
        self.async_write_ha_state()


class ErrorSensor(TemzitEntity, SensorEntity):
    """Representation of an error sensor."""

    _attr_name = "Ошибка"
    _attr_icon = "mdi:alert-circle"
    _attr_state_class = SensorStateClass.MEASUREMENT

    def __init__(self, coordinator):
        """Pass coordinator to CoordinatorEntity."""
        super().__init__(coordinator)
        self._attr_unique_id = coordinator.config_entry.entry_id + "_error"

    @callback
    def _handle_coordinator_update(self) -> None:
        """Handle updated data from the coordinator."""
        self._attr_native_value = self.coordinator.data.error
        self.async_write_ha_state()


class CompressorSensor(TemzitEntity, SensorEntity):
    """Representation of a compressor sensor."""

    _attr_name = "Компрессор 1"
    _attr_icon = "mdi:fan"
    _attr_native_unit_of_measurement = UnitOfFrequency.HERTZ
    _attr_state_class = SensorStateClass.MEASUREMENT
    _attr_suggested_display_precision = 0

    def __init__(self, coordinator):
        """Pass coordinator to CoordinatorEntity."""
        super().__init__(coordinator)
        self._attr_unique_id = coordinator.config_entry.entry_id + "_compressor1"

    @callback
    def _handle_coordinator_update(self) -> None:
        """Handle updated data from the coordinator."""
        self._attr_native_value = self.coordinator.data.compressor1
        self.async_write_ha_state()


class COPSensor(TemzitEntity, SensorEntity):
    """Representation of a COP (Coefficient of Performance) sensor."""

    _attr_name = "COP"
    _attr_icon = "mdi:efficiency"
    _attr_suggested_display_precision = 2
    _attr_state_class = SensorStateClass.MEASUREMENT

    def __init__(self, coordinator):
        """Pass coordinator to CoordinatorEntity."""
        super().__init__(coordinator)
        self._attr_unique_id = coordinator.config_entry.entry_id + "_cop"

    @callback
    def _handle_coordinator_update(self) -> None:
        """Handle updated data from the coordinator."""
        cop_value = self.coordinator.data.cop
        self._attr_native_value = cop_value
        self.async_write_ha_state()