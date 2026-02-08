# D-mon Brand Colors

These colors are extracted from [dmon.be](https://www.dmon.be/) and configured in `src/index.css`.

## Color Palette

| Color | HEX | Tailwind Class | Usage |
|-------|-----|----------------|-------|
| **D-mon Blue** | `#054a8f` | `bg-dmon-blue`, `text-dmon-blue`, `border-dmon-blue` | Primary brand color, active states, CTAs |
| **D-mon Red** | `#b6321b` | `bg-dmon-red`, `text-dmon-red`, `border-dmon-red` | Accents, warnings, destructive actions |
| **D-mon Gold** | `#baa369` | `bg-dmon-gold`, `text-dmon-gold`, `border-dmon-gold` | Highlights, success states, special elements |
| **D-mon Slate** | `#36526d` | `bg-dmon-slate`, `text-dmon-slate`, `border-dmon-slate` | Secondary elements, muted text |
| **D-mon Background** | `#f0f2f5` | `bg-dmon-bg` | Page backgrounds, card backgrounds |

## Usage Examples

### Backgrounds
```tsx
<div className="bg-dmon-blue">Blue background</div>
<div className="bg-dmon-bg">Page background</div>
```

### Text
```tsx
<h1 className="text-dmon-blue">Blue heading</h1>
<p className="text-dmon-slate">Muted text</p>
```

### Borders
```tsx
<div className="border-2 border-dmon-blue">Blue border</div>
<button className="border-b-2 border-dmon-gold">Gold underline</button>
```

### Hover States
```tsx
<button className="bg-dmon-blue hover:bg-dmon-slate">
  Button with hover
</button>
```

### Combined
```tsx
<div className="bg-dmon-bg border border-dmon-slate">
  <h2 className="text-dmon-blue">Title</h2>
  <p className="text-gray-600">Content</p>
</div>
```

## Current Implementation

### Global
- ✅ App background uses `dmon-bg` (light grayish-blue)
- ✅ Main title uses `dmon-blue`
- ✅ Navigation tabs use `dmon-blue` for active state

### Buttons
- ✅ Primary actions (Add Trainer, Add Team, Solve) - `dmon-blue`
- ✅ Save buttons - `dmon-gold`
- ✅ Delete/Stop buttons - `dmon-red`
- ✅ Export/Import buttons - white with `dmon-slate` border

### Tables
- ✅ Table headers - `dmon-bg` background
- ✅ Table borders - `dmon-slate`
- ✅ Hover states - `dmon-bg`
- ✅ Edit links - `dmon-blue`
- ✅ Delete links - `dmon-red`

### Forms
- ✅ Form containers - white with `dmon-slate` border
- ✅ Selected checkboxes/chips - `dmon-blue` with white text
- ✅ Age group selections - `dmon-blue`
- ✅ Day selections - `dmon-blue`

### Schedule Page
- ✅ Time slot grid - `dmon-bg` headers, `dmon-slate` borders
- ✅ Selected slots - `dmon-gold` (checkmark cells)
- ✅ Training session cards - `dmon-blue` with 10% opacity background
- ✅ Score badge (feasible) - `dmon-gold` with 20% opacity
- ✅ Status badge - `dmon-bg` with `dmon-slate` text
- ✅ Solving indicator - `dmon-blue` with pulse animation

## Color Psychology

- **Blue** (`#054a8f`): Trust, professionalism, stability
- **Red** (`#b6321b`): Energy, passion, action
- **Gold** (`#baa369`): Premium, achievement, warmth
- **Slate** (`#36526d`): Reliability, sophistication, balance
