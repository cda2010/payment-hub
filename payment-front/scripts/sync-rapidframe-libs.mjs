#!/usr/bin/env node
/**
 * Copie les packages RapidFrame buildés dans node_modules (vrais dossiers, pas de symlinks).
 * Skip si déjà à jour (évite de relancer le watcher ng serve en boucle).
 */
import { cpSync, existsSync, lstatSync, mkdirSync, readFileSync, readdirSync, rmSync, statSync, writeFileSync } from 'node:fs';
import { createHash } from 'node:crypto';
import { dirname, join } from 'node:path';
import { fileURLToPath } from 'node:url';

const __dirname = dirname(fileURLToPath(import.meta.url));
const frontRoot = join(__dirname, '..');
const rfRoot = join(frontRoot, '../../monApp/rapidframe-frontend');
const destRoot = join(frontRoot, 'node_modules/@rapidframe');
const stampFile = join(destRoot, '.sync-stamp');

const libs = [
  { name: 'frontend-angular', src: join(rfRoot, 'dist/angular') },
  { name: 'bpmn-designer', src: join(rfRoot, 'dist/bpmn-designer') },
  { name: 'formio-designer', src: join(rfRoot, 'dist/formio-designer') },
  { name: 'core', src: join(rfRoot, 'packages/core') },
];

function stampFor(lib) {
  const pkg = join(lib.src, 'package.json');
  if (!existsSync(pkg)) {
    return null;
  }
  const st = statSync(pkg);
  return `${lib.name}:${st.mtimeMs}:${st.size}`;
}

const stamps = libs.map(stampFor);
if (stamps.some((s) => !s)) {
  const missing = libs.find((l, i) => !stamps[i]);
  console.error(`[sync-rapidframe-libs] Manquant: ${missing.src}`);
  console.error('[sync-rapidframe-libs] Lancez: cd ../../monApp/rapidframe-frontend && npm run build:lib');
  process.exit(1);
}

const wanted = createHash('sha1').update(stamps.join('|')).digest('hex');
if (existsSync(stampFile)) {
  const current = readFileSync(stampFile, 'utf8').trim();
  if (current === wanted) {
    console.log('[sync-rapidframe-libs] déjà à jour — skip');
    process.exit(0);
  }
}

mkdirSync(destRoot, { recursive: true });

for (const lib of libs) {
  const dest = join(destRoot, lib.name);
  rmSync(dest, { recursive: true, force: true });
  cpSync(lib.src, dest, { recursive: true });
  const st = lstatSync(dest);
  if (st.isSymbolicLink()) {
    console.error(`[sync-rapidframe-libs] ${lib.name} est encore un symlink`);
    process.exit(1);
  }
  console.log(`[sync-rapidframe-libs] ${lib.name} OK`);
}

writeFileSync(stampFile, wanted);

// Le dev server pré-bundle @rapidframe/* dans .angular/cache/<version>/vite : sans purge,
// il continue de servir l'ancienne version des libs.
const angularCache = join(frontRoot, '.angular/cache');
if (existsSync(angularCache)) {
  for (const version of readdirSync(angularCache)) {
    rmSync(join(angularCache, version, 'vite'), { recursive: true, force: true });
  }
  console.log('[sync-rapidframe-libs] cache Vite purgé — relancez ng serve s\'il tourne');
}
