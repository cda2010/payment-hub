/**
 * Point d'entrée des routes métier.
 * Les routes sont scindées en payment-route / admin-route (lazy-load depuis app.routes).
 */
export { default as paymentRoutes } from './payment-route';
export { default as adminRoutes } from './admin-route';
