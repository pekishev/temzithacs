"""Platform for climate integration."""
from __future__ import annotations

import logging

from homeassistant.components.climate import ClimateEntity, HVACMode, ClimateEntityFeature
from homeassistant.const import ATTR_TEMPERATURE, UnitOfTemperature
from homeassistant.core import callback

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
            TemzitClimate(coordinator),
        ]
    )


class TemzitClimate(TemzitEntity, ClimateEntity):
    """Representation of a Climate."""
    _attr_name = "Обогрев дома"
    _attr_temperature_unit = UnitOfTemperature.CELSIUS
    _attr_supported_features = ClimateEntityFeature.TARGET_TEMPERATURE
    _attr_hvac_modes = [HVACMode.HEAT, HVACMode.OFF]
    _attr_hvac_mode = HVACMode.OFF
    _attr_is_aux_heat = False
    _attr_min_temp = 5.0
    _attr_max_temp = 50.0
    _attr_target_temperature_step = 1.0

    def __init__(self, coordinator: TemzitUpdateCoordinator):
        """Pass coordinator to CoordinatorEntity."""
        super().__init__(coordinator)
        self._attr_unique_id = coordinator.config_entry.entry_id + "_climate"

    @callback
    def _handle_coordinator_update(self) -> None:
        """Handle updated data from the coordinator."""
        state = self.coordinator.data.state
        self._attr_current_temperature = self.coordinator.data.return_temp
        self._attr_target_temperature = self.coordinator.data.target_water_temp
        # State 0 = off, non-zero = on
        self._attr_hvac_mode = HVACMode.OFF if state == 0 else HVACMode.HEAT
        self._attr_is_aux_heat = self.coordinator.data.main_heater_is_on
        self.async_write_ha_state()

    async def async_set_temperature(self, **kwargs) -> None:
        """Set new target temperature."""
        temperature = kwargs.get(ATTR_TEMPERATURE)
        if temperature is None:
            return
        
        try:
            await self.coordinator.client.set_target_temperature(temperature)
            # Request coordinator update to get new state
            await self.coordinator.async_request_refresh()
        except NotImplementedError:
            _LOGGER.warning("Setting temperature is not yet implemented in the API")
        except Exception as err:
            _LOGGER.error("Error setting temperature: %s", err)
            raise

    async def async_set_hvac_mode(self, hvac_mode: HVACMode | str) -> None:
        """Set new target hvac mode.
        
        Args:
            hvac_mode: HVAC mode as HVACMode enum or string ('off', 'heat', etc.)
        """
        # Validate and normalize hvac_mode
        if hvac_mode is None:
            _LOGGER.error("HVAC mode is None, cannot set")
            raise ValueError("HVAC mode cannot be None")
        
        # Convert string to HVACMode if needed
        if isinstance(hvac_mode, str):
            hvac_mode_str = hvac_mode.lower()
            if hvac_mode_str == "off":
                hvac_mode = HVACMode.OFF
            elif hvac_mode_str == "heat":
                hvac_mode = HVACMode.HEAT
            else:
                _LOGGER.error("Unsupported HVAC mode string: %s", hvac_mode)
                raise ValueError(f"Unsupported HVAC mode: {hvac_mode}")
        
        # Validate that mode is in supported modes
        if hvac_mode not in self._attr_hvac_modes:
            _LOGGER.error("HVAC mode %s is not in supported modes: %s", hvac_mode, self._attr_hvac_modes)
            raise ValueError(f"HVAC mode {hvac_mode} is not supported. Supported modes: {self._attr_hvac_modes}")
        
        try:
            # Convert HVACMode to API string
            mode_str = "on" if hvac_mode == HVACMode.HEAT else "off"
            _LOGGER.debug("Setting HVAC mode: %s -> %s", hvac_mode, mode_str)
            await self.coordinator.client.set_hvac_mode(mode_str)
            # Request coordinator update to get new state
            await self.coordinator.async_request_refresh()
        except NotImplementedError:
            _LOGGER.warning("Setting HVAC mode is not yet implemented in the API")
        except Exception as err:
            _LOGGER.error("Error setting HVAC mode: %s", err)
            raise