import type {
  GatewayUi,
  IntegrationScreen,
  Payment2ClientDefaults,
  StartProductRequest
} from './types';

/**
 * Client HTTP headless Product Gateway.
 * Auth = `getToken()` injecté ; UI = {@link GatewayUi} dans {@link Payment2Client.run}.
 */
export class Payment2Client {
  constructor(
    private readonly baseUrl: string,
    private readonly getToken: () => Promise<string> | string,
    private readonly defaults: Payment2ClientDefaults
  ) {}

  private async call<T>(path: string, init: RequestInit = {}): Promise<T> {
    const token = await this.getToken();
    const headers: Record<string, string> = {
      Authorization: `Bearer ${token}`,
      'Content-Type': 'application/json',
      'Accept-Language': this.defaults.acceptLanguage ?? 'fr',
      ...(init.headers as Record<string, string> | undefined)
    };
    const res = await fetch(`${this.baseUrl.replace(/\/$/, '')}/api/v1/gateway${path}`, {
      ...init,
      headers
    });
    if (!res.ok) {
      const err = (await res.json().catch(() => ({}))) as {
        message?: string;
        code?: string;
      };
      throw Object.assign(new Error(err.message ?? res.statusText), {
        code: err.code,
        status: res.status
      });
    }
    return res.json() as Promise<T>;
  }

  start(
    productCode: string,
    body: StartProductRequest = {}
  ): Promise<IntegrationScreen> {
    const idempotencyKey =
      typeof body.idempotencyKey === 'string' && body.idempotencyKey.trim()
        ? body.idempotencyKey
        : crypto.randomUUID();
    return this.call(`/products/${encodeURIComponent(productCode)}`, {
      method: 'POST',
      body: JSON.stringify({
        channel: this.defaults.channel,
        audience: this.defaults.audience,
        ...body,
        idempotencyKey
      })
    });
  }

  get(paymentId: string): Promise<IntegrationScreen> {
    return this.call(`/payments/${encodeURIComponent(paymentId)}`);
  }

  act(
    paymentId: string,
    action: string,
    data: Record<string, unknown> = {}
  ): Promise<IntegrationScreen> {
    return this.call(`/payments/${encodeURIComponent(paymentId)}/actions`, {
      method: 'POST',
      body: JSON.stringify({ action, data })
    });
  }

  resolveDataSource(
    code: string,
    query: Record<string, string>
  ): Promise<unknown> {
    const qs = new URLSearchParams(query).toString();
    return this.call(`/data-sources/${encodeURIComponent(code)}${qs ? `?${qs}` : ''}`);
  }

  /**
   * Boucle générique jusqu'à SUCCESS / FAILURE.
   * Les apps Ionic / PrimeNG implémentent {@link GatewayUi}.
   */
  async run(screen: IntegrationScreen, ui: GatewayUi, maxPolls = 100): Promise<IntegrationScreen> {
    let polls = 0;
    let openedUrl: string | undefined;
    let current = screen;
    for (;;) {
      const id = current.meta?.['paymentId'];
      if (typeof id !== 'string' || !id.trim()) {
        throw new Error('paymentId manquant');
      }
      const type = String(current.type || '').toUpperCase();
      switch (type) {
        case 'SUCCESS':
        case 'FAILURE':
          return current;
        case 'FORM':
        case 'DISPLAY_MESSAGE':
          current = await this.act(id, 'SUBMIT_FORM', await ui.collectForm(current));
          break;
        case 'ENTER_OTP':
          current = await this.act(id, 'SUBMIT_OTP', { otp: await ui.collectOtp(current) });
          break;
        case 'REDIRECT':
        case 'WAIT':
          if (type === 'REDIRECT' && current.url && current.url !== openedUrl) {
            openedUrl = current.url;
            ui.openUrl(current.url);
          } else {
            ui.showWaiting(current.message);
          }
          if (++polls > maxPolls) {
            throw new Error('Délai dépassé');
          }
          await sleep((current.pollAfterSeconds ?? 3) * 1000);
          current = await this.get(id);
          break;
        default:
          throw new Error(`Type d'écran inconnu : ${current.type}`);
      }
      if (type !== 'WAIT' && type !== 'REDIRECT') {
        polls = 0;
      }
    }
  }
}

function sleep(ms: number): Promise<void> {
  return new Promise((resolve) => setTimeout(resolve, ms));
}
