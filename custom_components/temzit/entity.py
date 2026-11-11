"""TemzitEntity class."""
from __future__ import annotations

from collections.abc import Callable
from typing import TypeVar

from homeassistant.core import callback
from homeassistant.helpers.entity import DeviceInfo
from homeassistant.helpers.update_coordinator import CoordinatorEntity

from .const import ATTRIBUTION, DOMAIN, NAME, VERSION
from .coordinator import TemzitUpdateCoordinator

_T = TypeVar("_T", bound="TemzitEntity")


def temzit_update(func: Callable[[_T], None]) -> Callable[[_T], None]:
    """Skip coordinator updates while the heat pump is offline."""

    @callback
    def wrapper(self: _T) -> None:
        if self.coordinator.data is None:
            self.async_write_ha_state()
            return
        func(self)

    return wrapper


class TemzitEntity(CoordinatorEntity):
    """Base entity for Temzit integration using DataUpdateCoordinator."""

    _attr_attribution = ATTRIBUTION
    _attr_has_entity_name = True

    def __init__(self, coordinator: TemzitUpdateCoordinator) -> None:
        """Initialize."""
        super().__init__(coordinator)
        self._attr_device_info = DeviceInfo(
            identifiers={(DOMAIN, coordinator.config_entry.entry_id)},
            name=NAME,
            model="Temzit Heat Pump",
            manufacturer="Temzit",
        )

    @property
    def available(self) -> bool:
        """Unavailable while the heat pump is not answering polls."""
        return super().available and self.coordinator.data is not None
