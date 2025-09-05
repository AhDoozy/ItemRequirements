## 2025-09-05

### Added
- Configuration option: Tooltip opacity (0–100%) to control background transparency.
- Configuration option: Triangle corner selection (Top-Left, Top-Right, Bottom-Left, Bottom-Right).
- Configuration option: Color pickers for “No requirements met” and “Partial requirements met” indicators.

### Changed
- Default tooltip font size increased from 12 to 15 points.
- Overlay triangle placement now respects the user-selected corner from config.
- Replaced the red "i" indicator with a triangle; indicator colors are now configurable.

### Fixed
- Tooltip overlay now applies configured font size and opacity correctly.
- Hover detection polygon updated to match the configured triangle corner, ensuring alignment between hover behavior and rendered triangle.
