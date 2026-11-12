import { ApiErrorDetail } from '../../core/interceptors/error.interceptor';
import { StatutUnite, TypeUnite, Zone, ZoneRef } from '../../core/service/network.service';

type Severity = 'success' | 'info' | 'warning' | 'danger' | 'secondary' | 'contrast';

export const TYPES_UNITE: { value: TypeUnite; label: string; icon: string; severity: Severity }[] = [
  { value: 'PAYS', label: 'Pays', icon: 'pi pi-globe', severity: 'contrast' },
  { value: 'FSE', label: 'FSE', icon: 'pi pi-flag', severity: 'info' },
  { value: 'AGENCE', label: 'Agence', icon: 'pi pi-building', severity: 'secondary' },
  { value: 'MASTER', label: 'Master', icon: 'pi pi-star', severity: 'warning' },
  { value: 'MARCHAND', label: 'Marchand', icon: 'pi pi-briefcase', severity: 'success' },
  { value: 'SOUS_DISTRIBUTEUR', label: 'Sous-distributeur', icon: 'pi pi-user', severity: 'secondary' },
  { value: 'REGROUPEMENT', label: 'Regroupement', icon: 'pi pi-th-large', severity: 'danger' }
];

/** Options de sélection d'une zone ; {@code activesSeules} pour y créer un pays ou un regroupement. */
export function optionsZones(zones: ZoneRef[], activesSeules = false): { value: Zone; label: string }[] {
  return zones
    .filter((z) => !activesSeules || z.statut === 'ACTIF')
    .map((z) => ({ value: z.code, label: `${z.code} (${z.devise})${z.statut === 'INACTIF' ? ' · inactive' : ''}` }));
}

export const STATUTS_UNITE: { value: StatutUnite; label: string }[] = [
  { value: 'ACTIF', label: 'Actif' },
  { value: 'INACTIF', label: 'Inactif' }
];

export function typeLabel(t: TypeUnite): string {
  return TYPES_UNITE.find((x) => x.value === t)?.label ?? t;
}

export function typeIcon(t: TypeUnite): string {
  return TYPES_UNITE.find((x) => x.value === t)?.icon ?? 'pi pi-circle';
}

export function typeSeverity(t: TypeUnite): Severity {
  return TYPES_UNITE.find((x) => x.value === t)?.severity ?? 'secondary';
}

export function statutLabel(s: StatutUnite): string {
  return s === 'ACTIF' ? 'Actif' : 'Inactif';
}

export function statutSeverity(s: StatutUnite): Severity {
  return s === 'ACTIF' ? 'success' : 'danger';
}

/** Erreurs de validation de la requête, par champ ; '_' regroupe celles sans champ. */
export function erreursParChamp(details: ApiErrorDetail[]): Record<string, string> {
  const parChamp: Record<string, string> = {};
  for (const d of details) {
    const champ = d.champ || '_';
    parChamp[champ] = parChamp[champ] ? `${parChamp[champ]} ${d.message}` : d.message;
  }
  return parChamp;
}
