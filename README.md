# Temzit heat pumps integration for Home Assistant (Тепловые насосы темзит)

[![GitHub Release][releases-shield]][releases]
[![GitHub Activity][commits-shield]][commits]
[![License][license-shield]](LICENSE)

![Project Maintenance][maintenance-shield]

[![Community Forum][forum-shield]][forum]

_Integration to integrate with [integration_blueprint][integration_blueprint]._

**This integration will set up the following platforms.**

Platform | Description
-- | --
`sensor` | Show info from blueprint API.
`switch` | Switch something `True` or `False`.

## Manual Installation

1. Using the tool of choice open the directory (folder) for your HA configuration (where you find `configuration.yaml`).
1. If you do not have a `custom_components` directory (folder) there, you need to create it.
1. In the `custom_components` directory (folder) create a new folder called `integration_blueprint`.
1. Download _all_ the files from the `custom_components/integration_blueprint/` directory (folder) in this repository.
1. Place the files you downloaded in the new directory (folder) you created.
1. Restart Home Assistant
1. In the HA UI go to "Configuration" -> "Integrations" click "+" and search for "Integration blueprint"

## Installation via HACS (recommended)

1. Go to HACS->Integrations.
1. Three dots->User repositories.
1. Paste https://github.com/pekishev/temzithacs with category `Integration`, click add.
1. Reboot Home Assistant
1. Then add new integration via HACS->Add->search `temzit`
1. Insert temzit ip address in local network

## Configuration is done in the UI

<!---->

## Contributions are welcome!

If you want to contribute to this please read the [Contribution guidelines](CONTRIBUTING.md)

***

[integration_blueprint]: https://github.com/pekishev/temzithacs
[buymecoffeebadge]: https://img.shields.io/badge/buy%20me%20a%20coffee-donate-yellow.svg?style=for-the-badge
[commits-shield]: https://img.shields.io/github/commit-activity/y/pekishev/temzithacs.svg?style=for-the-badge
[commits]: https://github.com/pekishev/temzithacs/commits/main
[exampleimg]: example.png
[forum-shield]: https://img.shields.io/badge/community-forum-brightgreen.svg?style=for-the-badge
[forum]: https://community.home-assistant.io/
[license-shield]: https://img.shields.io/github/license/pekishev/temzithacs.svg?style=for-the-badge
[releases-shield]: https://img.shields.io/github/release/pekishev/temzithacs.svg?style=for-the-badge
[releases]: https://github.com/pekishev/temzithacs/releases
