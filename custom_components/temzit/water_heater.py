from __future__ import annotations

import logging

from homeassistant.components.water_heater import (
    ATTR_TEMPERATURE,
    STATE_ELECTRIC,
    STATE_HEAT_PUMP,
    STATE_PERFORMANCE,
    STATE_OFF,
    WaterHeaterEntity,
    WaterHeaterEntityFeature,
)
from homeassistant.config_entries import ConfigEntry
from homeassistant.const import Platform, UnitOfTemperature
from homeassistant.core import HomeAssistant, callback
from homeassistant.helpers.entity_platform import AddEntitiesCallback

from .entity import TemzitEntity
from .coordinator import TemzitUpdateCoordinator
from .const import DOMAIN

_LOGGER = logging.getLogger(__name__)

async def async_setup_entry(
    hass,
    entry,
    async_add_entities,
) -> None:
    """Add cover for passed config_entry in HA."""

    coordinator = hass.data[DOMAIN][entry.entry_id]
    # Add all entities to HA
    async_add_entities(
        [
            HotWaterHeater(coordinator),
        ]
    )


class HotWaterHeater(TemzitEntity, WaterHeaterEntity):
    """Representation of a Hot Water Heater."""
    _attr_name = "Горячая вода"
    _attr_temperature_unit = UnitOfTemperature.CELSIUS
    _attr_current_operation = STATE_OFF
    _attr_supported_features = WaterHeaterEntityFeature.TARGET_TEMPERATURE
    _attr_min_temp = 20.0
    _attr_max_temp = 60.0
    _attr_target_temperature = 45.0

    def __init__(self, coordinator: TemzitUpdateCoordinator):
        """Pass coordinator to CoordinatorEntity."""
        super().__init__(coordinator)
        self._attr_unique_id = coordinator.config_entry.entry_id + "_hot_water"

    @callback
    def _handle_coordinator_update(self) -> None:
        """Handle updated data from the coordinator."""
        self._attr_target_temperature = self.coordinator.data.target_hotwater_temp
        self._attr_current_temperature = self.coordinator.data.hotwater_temp
        self._attr_current_operation = STATE_ELECTRIC if self.coordinator.data.boiler_heater_is_on else STATE_OFF
        self.async_write_ha_state()

    async def async_set_temperature(self, temperature: float) -> None:
        """Set new target temperature."""
        try:
            await self.coordinator.client.set_target_hotwater_temperature(temperature)
            # Request coordinator update to get new state
            await self.coordinator.async_request_refresh()
        except NotImplementedError:
            _LOGGER.warning("Setting hot water temperature is not yet implemented in the API")
        except Exception as err:
            _LOGGER.error("Error setting hot water temperature: %s", err)
            raise

class MainHeater(TemzitEntity, WaterHeaterEntity):
    """Representation of a Main Heating System."""
    _attr_name = "Обогрев дома"
    _attr_temperature_unit = UnitOfTemperature.CELSIUS
    _attr_current_operation = STATE_OFF
    _attr_supported_features = WaterHeaterEntityFeature.TARGET_TEMPERATURE
    _attr_min_temp = 5.0
    _attr_max_temp = 50.0
    _attr_target_temperature = 25.0

    def __init__(self, coordinator: TemzitUpdateCoordinator):
        """Pass coordinator to CoordinatorEntity."""
        super().__init__(coordinator)
        self._attr_unique_id = coordinator.config_entry.entry_id + "_main"

    @callback
    def _handle_coordinator_update(self) -> None:
        """Handle updated data from the coordinator."""
        self._attr_target_temperature = self.coordinator.data.target_water_temp
        self._attr_current_temperature = self.coordinator.data.return_temp
        self._attr_current_operation = STATE_PERFORMANCE if self.coordinator.data.main_heater_is_on else STATE_HEAT_PUMP
        self.async_write_ha_state()

    async def async_set_temperature(self, temperature: float) -> None:
        """Set new target temperature."""
        try:
            await self.coordinator.client.set_target_temperature(temperature)
            # Request coordinator update to get new state
            await self.coordinator.async_request_refresh()
        except NotImplementedError:
            _LOGGER.warning("Setting temperature is not yet implemented in the API")
        except Exception as err:
            _LOGGER.error("Error setting temperature: %s", err)
            raise