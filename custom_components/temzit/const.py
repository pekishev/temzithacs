"""Constants for Temzit integration."""
from logging import Logger, getLogger

LOGGER: Logger = getLogger(__package__)

NAME = "Temzit heat pump"
DOMAIN = "temzit"
VERSION = "2024.1.1"
ATTRIBUTION = "Data provided by Temzit heat pump"

CONF_FLOW_COEFFICIENT = "flow_coefficient"
DEFAULT_FLOW_COEFFICIENT = 4.2
