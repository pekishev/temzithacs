"""Temzit API Client."""
from __future__ import annotations

import asyncio
import logging

_LOGGER = logging.getLogger(__name__)

class TemzitApiClientError(Exception):
    """Exception to indicate a general API error."""


class TemzitApiClientCommunicationError(
    TemzitApiClientError
):
    """Exception to indicate a communication error."""


class TemzitApiClientAuthenticationError(
    TemzitApiClientError
):
    """Exception to indicate an authentication error."""



class TemzitApiClient:
    def __init__(self, ip) -> None:
        self.ip = ip
        self.port = 333

    async def fetch_data(self) -> "temzit_state":
        """Fetch current state from the heat pump."""
        reader = None
        writer = None
        _LOGGER.debug("Connecting to %s:%s", self.ip, self.port)
        try:
            reader, writer = await asyncio.wait_for(
                asyncio.open_connection(host=self.ip, port=self.port),
                timeout=5.0
            )
            _LOGGER.debug("Connected, sending command 0x30 0x00")
            writer.write(b"\x30\x00")
            await writer.drain()

            _LOGGER.debug("Waiting for response...")
            data = await asyncio.wait_for(reader.read(64), timeout=5.0)
            _LOGGER.debug("Received %d bytes", len(data))
            
            if len(data) == 0:
                _LOGGER.error("Empty response from device at %s:%s", self.ip, self.port)
                raise TemzitApiClientCommunicationError("Empty response from device")
            
            if len(data) < 2:
                _LOGGER.error("Invalid response length: %d bytes (expected at least 2)", len(data))
                raise TemzitApiClientCommunicationError("Invalid response length")
            
            _LOGGER.debug("Successfully fetched data from %s:%s", self.ip, self.port)
            return temzit_state(data)
        except (ConnectionError, OSError) as exception:
            _LOGGER.error("Connection error to %s:%s: %s", self.ip, self.port, exception)
            raise TemzitApiClientCommunicationError(
                f"Error connecting to {self.ip}:{self.port}: {exception}"
            ) from exception
        except asyncio.TimeoutError as exception:
            _LOGGER.error("Timeout connecting to %s:%s", self.ip, self.port)
            raise TemzitApiClientCommunicationError(
                f"Timeout connecting to {self.ip}:{self.port}"
            ) from exception
        except TemzitApiClientCommunicationError:
            # Re-raise our own exceptions
            raise
        except Exception as exception:
            _LOGGER.exception("Unexpected error fetching data from %s:%s: %s", self.ip, self.port, exception)
            raise TemzitApiClientCommunicationError(
                f"Unexpected error: {exception}"
            ) from exception
        finally:
            if writer:
                writer.close()
                try:
                    await writer.wait_closed()
                except Exception:
                    pass

    async def fetch_settings(self) -> "temzit_settings":
        """Fetch settings from the heat pump."""
        reader = None
        writer = None
        try:
            reader, writer = await asyncio.wait_for(
                asyncio.open_connection(host=self.ip, port=self.port),
                timeout=5.0
            )
            writer.write(b"\x34\x00")
            await writer.drain()

            data = await asyncio.wait_for(reader.read(64), timeout=5.0)
            if len(data) == 0:
                raise TemzitApiClientCommunicationError("Empty response from device")
            
            if len(data) < 64:
                raise TemzitApiClientCommunicationError(f"Invalid response length: {len(data)} (expected 64 for CONFIG_MAIN)")
            
            # Check response type: CONFIG_MAIN should be 0x02
            if data[0] != 0x02:
                _LOGGER.warning("Unexpected response command: 0x%02X (expected 0x02 CONFIG_MAIN)", data[0])
            
            return temzit_settings(data)
        except (ConnectionError, OSError) as exception:
            raise TemzitApiClientCommunicationError(
                f"Error connecting to {self.ip}:{self.port}"
            ) from exception
        except asyncio.TimeoutError as exception:
            raise TemzitApiClientCommunicationError(
                "Timeout fetching settings"
            ) from exception
        except Exception as exception:
            raise TemzitApiClientCommunicationError(
                f"Unexpected error: {exception}"
            ) from exception
        finally:
            if writer:
                writer.close()
                try:
                    await writer.wait_closed()
                except Exception:
                    pass

    async def _send_settings(self, settings: "temzit_settings", max_retries: int = 3, retry_delay: float = 2.0) -> None:
        """Send settings to the heat pump with retry mechanism.
        
        Protocol CFG 0x35: Send command 0x35 + 30 bytes settings + 1 byte checksum = 32 bytes total
        In response, device sends ACTUAL_STATE (0x01) packet
        Based on protocol documentation at https://temzit.ru/downloads/HM_Protocol.pdf
        
        Args:
            settings: Settings object to send
            max_retries: Maximum number of retry attempts (default: 3)
            retry_delay: Delay between retries in seconds (default: 2.0)
        """
        last_exception = None
        
        for attempt in range(1, max_retries + 1):
            reader = None
            writer = None
            try:
                _LOGGER.debug("Attempt %d/%d: Connecting to %s:%s to send settings", attempt, max_retries, self.ip, self.port)
                reader, writer = await asyncio.wait_for(
                    asyncio.open_connection(host=self.ip, port=self.port),
                    timeout=5.0
                )
                
                settings_bytes = settings.to_bytes()
                if len(settings_bytes) != 32:
                    raise ValueError(f"Invalid settings packet length: {len(settings_bytes)} (expected 32)")
                
                _LOGGER.debug("Sending CFG command (%d bytes): %s", len(settings_bytes), settings_bytes.hex())
                writer.write(settings_bytes)
                await writer.drain()
                
                # Wait for ACTUAL_STATE response (device responds with 0x01 packet)
                response = await asyncio.wait_for(reader.read(64), timeout=5.0)
                _LOGGER.debug("Received response (%d bytes): %s", len(response), response.hex()[:20] if response else "empty")
                
                if len(response) == 0:
                    _LOGGER.warning("No response from device, but settings may have been applied")
                    # Don't raise error - device might not send response in some cases
                    return
                
                # Check if response is ACTUAL_STATE (0x01)
                if len(response) >= 1:
                    if response[0] == 0x01:
                        _LOGGER.debug("Received ACTUAL_STATE response (0x01) - settings accepted")
                    else:
                        _LOGGER.warning("Unexpected response command: 0x%02X (expected 0x01 ACTUAL_STATE)", response[0])
                
                # Success - return
                _LOGGER.info("Settings successfully sent to %s:%s (attempt %d/%d)", self.ip, self.port, attempt, max_retries)
                return
                
            except (ConnectionError, OSError) as exception:
                last_exception = exception
                _LOGGER.warning("Connection error sending settings to %s:%s (attempt %d/%d): %s", 
                              self.ip, self.port, attempt, max_retries, exception)
                if attempt < max_retries:
                    _LOGGER.info("Retrying in %.1f seconds...", retry_delay)
                    await asyncio.sleep(retry_delay)
                    continue
                else:
                    _LOGGER.error("Failed to send settings after %d attempts", max_retries)
                    raise TemzitApiClientCommunicationError(
                        f"Error connecting to {self.ip}:{self.port} after {max_retries} attempts: {exception}"
                    ) from exception
                    
            except asyncio.TimeoutError as exception:
                last_exception = exception
                _LOGGER.warning("Timeout sending settings to %s:%s (attempt %d/%d)", 
                              self.ip, self.port, attempt, max_retries)
                if attempt < max_retries:
                    _LOGGER.info("Retrying in %.1f seconds...", retry_delay)
                    await asyncio.sleep(retry_delay)
                    continue
                else:
                    _LOGGER.error("Timeout sending settings after %d attempts", max_retries)
                    raise TemzitApiClientCommunicationError(
                        f"Timeout sending settings to {self.ip}:{self.port} after {max_retries} attempts"
                    ) from exception
                    
            except Exception as exception:
                last_exception = exception
                _LOGGER.warning("Unexpected error sending settings (attempt %d/%d): %s", attempt, max_retries, exception)
                if attempt < max_retries:
                    _LOGGER.info("Retrying in %.1f seconds...", retry_delay)
                    await asyncio.sleep(retry_delay)
                    continue
                else:
                    _LOGGER.exception("Failed to send settings after %d attempts", max_retries)
                    raise TemzitApiClientCommunicationError(
                        f"Unexpected error after {max_retries} attempts: {exception}"
                    ) from exception
                    
            finally:
                if writer:
                    writer.close()
                    try:
                        await writer.wait_closed()
                    except Exception:
                        pass
        
        # Should not reach here, but just in case
        if last_exception:
            raise TemzitApiClientCommunicationError(
                f"Failed to send settings after {max_retries} attempts"
            ) from last_exception

    async def set_target_temperature(self, temperature: float) -> None:
        """Set target temperature for heating (0-50°C).
        
        Fetches current settings, modifies only the target temperature,
        and sends settings back to preserve other parameters.
        
        Protocol reference: https://temzit.ru/downloads/HM_Protocol.pdf
        """
        # Validate temperature range
        temp_int = int(round(temperature))
        if not 0 <= temp_int <= 50:
            raise ValueError(f"Temperature must be between 0 and 50°C, got {temperature}")
        
        # Fetch current settings to preserve all other parameters
        current_settings = await self.fetch_settings()
        
        # Modify target water temperature
        # According to protocol: Тводы (target water temp) is at offset 2 in settings array
        current_settings.set_byte(2, temp_int)
        
        _LOGGER.info("Setting target heating temperature to %d°C (preserving other settings)", temp_int)
        
        # Send modified settings back
        await self._send_settings(current_settings)

    async def set_target_hotwater_temperature(self, temperature: float) -> None:
        """Set target temperature for hot water (0-60°C).
        
        Fetches current settings, modifies only the hot water target temperature,
        and sends settings back to preserve other parameters.
        
        Protocol reference: https://temzit.ru/downloads/HM_Protocol.pdf
        """
        # Validate temperature range
        temp_int = int(round(temperature))
        if not 0 <= temp_int <= 60:
            raise ValueError(f"Hot water temperature must be between 0 and 60°C, got {temperature}")
        
        # Fetch current settings to preserve all other parameters
        current_settings = await self.fetch_settings()
        
        # Modify target hot water temperature
        # According to protocol: Тгвс (target hot water temp) is at offset 7 in settings array
        current_settings.set_byte(7, temp_int)
        
        _LOGGER.info("Setting target hot water temperature to %d°C (preserving other settings)", temp_int)
        
        # Send modified settings back
        await self._send_settings(current_settings)

    async def set_hvac_mode(self, mode: str) -> None:
        """Set HVAC mode (on/off).
        
        Fetches current settings, modifies only the state,
        and sends settings back to preserve other parameters.
        """
        # Fetch current settings
        current_settings = await self.fetch_settings()
        
        # Modify mode (Режим) at offset 0 in settings array
        # According to protocol: Режим is at offset 0
        # 0 = off, non-zero = on
        state_value = 1 if mode.lower() == "on" else 0
        current_settings.set_byte(0, state_value)
        
        _LOGGER.info("Setting HVAC mode to %s (state=%d)", mode, state_value)
        
        # Send modified settings
        await self._send_settings(current_settings)

class temzit_state:
    def __init__(self, data) -> None:
        self.data = data[2:]

    @property
    def state(self) -> int:
        return self.convert(0, 2)

    @property
    def schedule(self) -> int:
        return self.convert(2, 4)

    @property
    def outdoor_temp(self) -> float:
        return self.convert(4, 6) / 10

    @property
    def indoor_temp(self) -> float:
        return self.convert(6, 8) / 10

    @property
    def hotwater_temp(self) -> float:
        return self.convert(16, 18) / 10

    @property
    def supply_temp(self) -> float:
        return self.convert(8, 10) / 10

    @property
    def return_temp(self) -> float:
        return self.convert(10, 12) / 10

    @property
    def consumption(self) -> float:
        return self.convert(28, 30) / 10

    @property
    def target_indoor_temp(self) -> float:
        return self.convert(49, 50)

    @property
    def target_water_temp(self) -> float:
        return self.convert(50, 51)

    @property
    def target_hotwater_temp(self) -> float:
        return self.convert(51, 52)

    @property
    def heat_power(self) -> float:
        """Calculate output power in kW (from C#: OutputPower = (OutHeatT - InHeatT) * 4200 * LiquidSpeed / 60 / 1000)."""
        if self.flow == 0:
            return 0.0
        # LiquidSpeed is already in L/min, so formula is: (OutHeatT - InHeatT) * 4200 * LiquidSpeed / 60 / 1000
        return (self.supply_temp - self.return_temp) * 4200 * self.flow / 60 / 1000

    @property
    def flow(self) -> float:
        """Liquid speed in L/min (from C#: LiquidSpeed = BitConverter.ToInt16(data, 18) * 4.2f)."""
        return self.convert(18, 20) * 4.2

    @property
    def error(self) -> int:
        """Error code (from C#: Error = BitConverter.ToInt16(data, 30))."""
        return self.convert(30, 32)

    @property
    def compressor1(self) -> int:
        """Compressor 1 status (from C#: Compressor1 = data[22])."""
        return self.data[22] if len(self.data) > 22 else 0

    @property
    def boiler_heater_is_on(self) -> bool:
        return self.convert(26, 28) > 0

    @property
    def main_heater_is_on(self) -> bool:
        return self.convert(24, 26) > 0

    @property
    def cop(self) -> float | None:
        """Coefficient of Performance (from C#: COP = InputPower == 0 ? null : OutputPower / InputPower)."""
        if self.consumption == 0:
            return None
        return self.heat_power / self.consumption

    def convert(self, s: int, e: int) -> int:
        return int.from_bytes(self.data[s:e], byteorder="little", signed=True)


class temzit_settings:
    def __init__(self, data) -> None:
        # Store full response including command byte
        self.raw_data = data
        # Settings data starts from byte 2 (skip command 0x02 and length/reserve)
        # CONFIG_MAIN response: 0x02 | settings 30 bytes | reserve 31 bytes | CS 2 bytes
        # We need only first 30 bytes of settings
        if len(data) > 2:
            # Extract settings array (30 bytes starting from offset 1, after command 0x02)
            self.data = bytearray(data[1:31])  # bytes 1-30 (settings array)
        else:
            self.data = bytearray(30)  # Default: 30 zero bytes

    def convert(self, s: int, e: int) -> int:
        """Convert bytes to integer (for reading from response)."""
        return int.from_bytes(self.data[s:e], byteorder="little", signed=True)
    
    def set_byte(self, offset: int, value: int) -> None:
        """Set a single byte at offset (0-29 for settings array)."""
        if 0 <= offset < 30:
            self.data[offset] = value & 0xFF
        else:
            raise ValueError(f"Offset {offset} out of range (0-29)")
    
    def set_short(self, offset: int, value: int) -> None:
        """Set a 16-bit signed integer at offset (little-endian)."""
        if 0 <= offset < 29:  # Need 2 bytes, so max offset is 29
            value_bytes = int.to_bytes(value, 2, byteorder="little", signed=True)
            self.data[offset:offset+2] = value_bytes
        else:
            raise ValueError(f"Offset {offset} out of range (0-28 for 16-bit value)")
    
    def to_bytes(self) -> bytes:
        """Convert settings to bytes for sending according to protocol.
        
        Protocol CFG 0x35: 
        - Command: 0x35 (1 byte)
        - Settings array: 30 bytes
        - Checksum: 1 byte (sum of all bytes including 0x35, modulo 256)
        Total: 32 bytes
        """
        # Build packet: 0x35 + 30 bytes of settings
        packet = bytearray([0x35]) + self.data
        
        # Calculate checksum: sum of all bytes including 0x35, modulo 256
        checksum = sum(packet) & 0xFF
        
        # Append checksum
        packet.append(checksum)
        
        _LOGGER.debug("Sending CFG packet: cmd=0x%02X, settings_len=%d, checksum=0x%02X", 
                    0x35, len(self.data), checksum)
        
        return bytes(packet)
