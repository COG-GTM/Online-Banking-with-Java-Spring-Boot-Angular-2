// Composes baseline (left) and upgraded (right) screenshots side by side and reports the pixel diff %.
import { readdirSync, readFileSync, writeFileSync, mkdirSync, existsSync } from 'fs';
import { join, dirname } from 'path';
import { fileURLToPath } from 'url';
import { PNG } from 'pngjs';
import pixelmatch from 'pixelmatch';

const root = join(dirname(fileURLToPath(import.meta.url)), '..', 'report');
const base = join(root, 'screens', 'baseline');
const upg = join(root, 'screens', 'upgraded');
const out = join(root, 'side-by-side');
mkdirSync(out, { recursive: true });

const pad = (img, w, h) => {
  const p = new PNG({ width: w, height: h });
  p.data.fill(255);
  PNG.bitblt(img, p, 0, 0, img.width, img.height, 0, 0);
  return p;
};

const results = [];
for (const name of readdirSync(base).filter((f) => f.endsWith('.png')).sort()) {
  if (!existsSync(join(upg, name))) { results.push({ name, diff: null }); continue; }
  const a = PNG.sync.read(readFileSync(join(base, name)));
  const b = PNG.sync.read(readFileSync(join(upg, name)));
  const w = Math.max(a.width, b.width), h = Math.max(a.height, b.height);
  const pa = pad(a, w, h), pb = pad(b, w, h);
  const changed = pixelmatch(pa.data, pb.data, null, w, h, { threshold: 0.1 });
  const gap = 16;
  const sbs = new PNG({ width: w * 2 + gap, height: h });
  sbs.data.fill(200);
  PNG.bitblt(pa, sbs, 0, 0, w, h, 0, 0);
  PNG.bitblt(pb, sbs, 0, 0, w, h, w + gap, 0);
  writeFileSync(join(out, name), PNG.sync.write(sbs));
  results.push({ name, diff: +((changed / (w * h)) * 100).toFixed(2), sizes: `${a.width}x${a.height} / ${b.width}x${b.height}` });
}
writeFileSync(join(root, 'pixel-diff.json'), JSON.stringify(results, null, 2));
console.table(results);
