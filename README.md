# RealisticBiomes
This plugin allows *you* to define what it takes for every biome to be what it should. Using RealisticBiomes, you can set what blocks are required for a desert to be considered a desert, or for a plains biome to be what it is!

## Default Configuration
The configuration is fairly straight forward and mostly explained inside the file

```yaml
tasks:
  # How often should we check modified chunks for potential biome changes? Time in seconds
  stamp-interval: 360
  # How often should chunks evolve if all conditions are met? Recommended to keep it the same or higher than the stamp interval
  evolve-interval: 400

biomes:
  # Biome Name goes here
  PLAINS:
  # Blocks and the amount required for each block
    GRASS_BLOCK: 16
    DIRT: 50
    OAK_LOG: 30
    OAK_LEAVES: 30
  # The time it should take to transform, if all blocks are correct. This is in seconds.
    time: 3600
```
