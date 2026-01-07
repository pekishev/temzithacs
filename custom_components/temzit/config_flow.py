"""Config flow for Temzit integration."""
from __future__ import annotations

import ipaddress
import voluptuous as vol
from homeassistant import config_entries
from homeassistant.const import CONF_IP_ADDRESS
from homeassistant.helpers import selector

from .api import (
    TemzitApiClient,
    TemzitApiClientAuthenticationError,
    TemzitApiClientCommunicationError,
    TemzitApiClientError,
)
from .const import DOMAIN, LOGGER


class TemzitFlowHandler(config_entries.ConfigFlow, domain=DOMAIN):
    """Handle a config flow for Temzit."""

    VERSION = 1

    async def async_step_user(
        self,
        user_input: dict | None = None,
    ) -> config_entries.FlowResult:
        """Handle a flow initialized by the user."""
        _errors = {}
        if user_input is not None:
            ip_address = user_input[CONF_IP_ADDRESS].strip()
            
            # Validate IP address format
            try:
                ipaddress.ip_address(ip_address)
            except ValueError:
                _errors[CONF_IP_ADDRESS] = "invalid_ip"
                return self.async_show_form(
                    step_id="user",
                    data_schema=vol.Schema(
                        {
                            vol.Required(
                                CONF_IP_ADDRESS,
                                default=ip_address,
                            ): selector.TextSelector(
                                selector.TextSelectorConfig(
                                    type=selector.TextSelectorType.TEXT,
                                    autocomplete="ip_address",
                                ),
                            ),
                        }
                    ),
                    errors=_errors,
                )
            
            # Check if this IP is already configured
            await self.async_set_unique_id(ip_address)
            self._abort_if_unique_id_configured()

            try:
                await self._test_connection(
                    ip=ip_address,
                )
            except TemzitApiClientAuthenticationError as exception:
                LOGGER.warning("Authentication error: %s", exception)
                _errors["base"] = "auth"
            except TemzitApiClientCommunicationError as exception:
                LOGGER.error("Connection error: %s", exception)
                _errors["base"] = "connection"
            except TemzitApiClientError as exception:
                LOGGER.exception("Unknown error: %s", exception)
                _errors["base"] = "unknown"
            except Exception as exception:
                LOGGER.exception("Unexpected error during setup: %s", exception)
                _errors["base"] = "unknown"
            else:
                return self.async_create_entry(
                    title=f"Temzit {ip_address}",
                    data={CONF_IP_ADDRESS: ip_address},
                )

        return self.async_show_form(
            step_id="user",
            data_schema=vol.Schema(
                {
                    vol.Required(
                        CONF_IP_ADDRESS,
                    ): selector.TextSelector(
                        selector.TextSelectorConfig(
                            type=selector.TextSelectorType.TEXT,
                            autocomplete="ip_address",
                        ),
                    ),
                }
            ),
            errors=_errors,
        )

    async def _test_connection(self, ip: str) -> None:
        """Test connection to the heat pump."""
        LOGGER.debug("Testing connection to %s", ip)
        client = TemzitApiClient(ip=ip)
        try:
            await client.fetch_data()
            LOGGER.debug("Successfully connected to %s", ip)
        except Exception as e:
            LOGGER.error("Failed to connect to %s: %s", ip, e)
            raise
