# Autopainter

A Fabric mod that helps you create images using the **ArtMap** plugin. Only 128x128 images are supported.

Because of the limited in-game color palette, each pixel is matched to the closest available color.

A preview tool for painted images is available here: https://anax378.github.io/MC-26.2-fabric-autopainter-for-artmap/

## How to Use

1. Place your image in the `art-images` directory inside your instance directory.
   - The image must be exactly 128x128 pixels.
   - `jpg` and `png` formats are supported. Other formats haven't been tested, but the mod uses Java's `ImageIO` class to load images, so you can check what formats that supports.
2. Load the image:
   ```
   /autopaint load <my-image>
   ```
   Example: `/autopaint load test.png`

   The mod will tell you which items you'll need and estimate how long the painting will take.
3. Place and sit on an easel **FACING SOUTH** with a prepared canvas, then start painting:
   ```
   /autopaint start
   ```
   **Do not move your cursor while painting is in progress.** It's highly recommended to go into freecam, open your menu with `esc`, or otherwise prevent interfering with your player rotation.

The mod automatically detects dyes in your inventory and starts with whatever is available. It will pause automatically if it can't continue without additional dyes/items.

## Commands

| Command | Description |
|---|---|
| `/autopaint load <my-image>` | Load an image from the `art-images` directory. |
| `/autopaint start` | Start painting the loaded image. |
| `/autopaint pause` | Pause the current painting session. Note: this does **not** save progress for the color currently being painted — that color will restart from the beginning when you resume. |
| `/autopaint resume` | Resume a paused session, or continue after the mod requests more dyes/items. |
| `/autopaint skip <item>` | Skip a specific dye/item. Requires the fully qualified item ID (e.g. `minecraft:snow` or `minecraft:light_gray_dye`) — it must match exactly. Example: `/autopaint skip minecraft:ender_eye` |
| `/autopaint status` | Check the current status: paused, running, not loaded, or waiting to start. If a session is active, this also shows the estimated remaining time. |

## Known Issues

- Some pixels are occasionally missed — usually 2–3 per image — and require manual correction.
- The bottom-left pixel is frequently missed and often needs manual correction.

## Showcase

![Showcase](./media/autopainter-recording.gif)

## License

This repository is available under the CC0 license. Feel free to learn from it and incorporate it into your own projects.
