# Location-based Call Management - Android Sample

Minimal Android sample (Java) demonstrating location-based call management:
- Foreground location monitoring (FusedLocationProvider)
- Safe-zone definition (center + radius)
- Distance calculation utility
- CallScreeningService skeleton (block/allow calls based on safe-zone + whitelist)
- Whitelist storage and basic manager

Open the project in Android Studio and let it sync. Configure runtime permissions on a real device for full testing (location, background location, contacts). CallScreeningService requires API 28+ and you must enable the app as a call-screening app in system settings to test blocking.
