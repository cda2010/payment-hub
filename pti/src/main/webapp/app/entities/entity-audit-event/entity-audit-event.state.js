(function() {
    'use strict';

    angular
        .module('ptiApp')
        .config(stateConfig);

    stateConfig.$inject = ['$stateProvider'];

    function stateConfig($stateProvider) {
        $stateProvider
        .state('entity-audit-event', {
            parent: 'entity',
            url: '/entity-audit-event?page&sort&search',
            data: {
                authorities: ['ROLE_USER'],
                pageTitle: 'ptiApp.entityAuditEvent.home.title'
            },
            views: {
                'content@app': {
                    templateUrl: 'app/entities/entity-audit-event/entity-audit-events.html',
                    controller: 'EntityAuditEventController',
                    controllerAs: 'vm'
                }
            },
            params: {
                page: {
                    value: '1',
                    squash: true
                },
                sort: {
                    value: 'id,asc',
                    squash: true
                },
                search: null
            },
            resolve: {
                pagingParams: ['$stateParams', 'PaginationUtil', function ($stateParams, PaginationUtil) {
                    return {
                        page: PaginationUtil.parsePage($stateParams.page),
                        sort: $stateParams.sort,
                        predicate: PaginationUtil.parsePredicate($stateParams.sort),
                        ascending: PaginationUtil.parseAscending($stateParams.sort),
                        search: $stateParams.search
                    };
                }],
                translatePartialLoader: ['$translate', '$translatePartialLoader', function ($translate, $translatePartialLoader) {
                    $translatePartialLoader.addPart('entityAuditEvent');
                    $translatePartialLoader.addPart('global');
                    return $translate.refresh();
                }]
            }
        })
        .state('entity-audit-event-detail', {
            parent: 'entity-audit-event',
            url: '/entity-audit-event/{id}',
            data: {
                authorities: ['ROLE_USER'],
                pageTitle: 'ptiApp.entityAuditEvent.detail.title'
            },
            views: {
                'content@app': {
                    templateUrl: 'app/entities/entity-audit-event/entity-audit-event-detail.html',
                    controller: 'EntityAuditEventDetailController',
                    controllerAs: 'vm'
                }
            },
            resolve: {
                translatePartialLoader: ['$translate', '$translatePartialLoader', function ($translate, $translatePartialLoader) {
                    $translatePartialLoader.addPart('entityAuditEvent');
                    return $translate.refresh();
                }],
                entity: ['$stateParams', 'EntityAuditEvent', function($stateParams, EntityAuditEvent) {
                    return EntityAuditEvent.get({id : $stateParams.id}).$promise;
                }],
                previousState: ["$state", function ($state) {
                    var currentStateData = {
                        name: $state.current.name || 'entity-audit-event',
                        params: $state.params,
                        url: $state.href($state.current.name, $state.params)
                    };
                    return currentStateData;
                }]
            }
        })
        .state('entity-audit-event-detail.edit', {
            parent: 'entity-audit-event-detail',
            url: '/detail/edit',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/entity-audit-event/entity-audit-event-dialog.html',
                    controller: 'EntityAuditEventDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: ['EntityAuditEvent', function(EntityAuditEvent) {
                            return EntityAuditEvent.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('^', {}, { reload: false });
                }, function() {
                    $state.go('^');
                });
            }]
        })
        .state('entity-audit-event.new', {
            parent: 'entity-audit-event',
            url: '/new',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/entity-audit-event/entity-audit-event-dialog.html',
                    controller: 'EntityAuditEventDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: function () {
                            return {
                                action: null,
                                commitVersion: null,
                                entityId: null,
                                entityType: null,
                                entityValue: null,
                                adresse: null,
                                modifiedBy: null,
                                modifiedDate: null,
                                id: null
                            };
                        }
                    }
                }).result.then(function() {
                    $state.go('entity-audit-event', null, { reload: 'entity-audit-event' });
                }, function() {
                    $state.go('entity-audit-event');
                });
            }]
        })
        .state('entity-audit-event.edit', {
            parent: 'entity-audit-event',
            url: '/{id}/edit',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/entity-audit-event/entity-audit-event-dialog.html',
                    controller: 'EntityAuditEventDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: ['EntityAuditEvent', function(EntityAuditEvent) {
                            return EntityAuditEvent.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('entity-audit-event', null, { reload: 'entity-audit-event' });
                }, function() {
                    $state.go('^');
                });
            }]
        })
        .state('entity-audit-event.delete', {
            parent: 'entity-audit-event',
            url: '/{id}/delete',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/entity-audit-event/entity-audit-event-delete-dialog.html',
                    controller: 'EntityAuditEventDeleteController',
                    controllerAs: 'vm',
                    size: 'md',
                    resolve: {
                        entity: ['EntityAuditEvent', function(EntityAuditEvent) {
                            return EntityAuditEvent.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('entity-audit-event', null, { reload: 'entity-audit-event' });
                }, function() {
                    $state.go('^');
                });
            }]
        })
            .state('audit-sur-les-profils', {
            parent: 'entity-audit-event',
            url: '/audit-sur-les-profils',
            data: {
                authorities: [],
                pageTitle: 'ptiApp.compte.home.title'
            },
            views: {
                'content@app': {
                    templateUrl: 'app/entities/entity-audit-event/audit-sur-les-profil.html',
                    controller: 'AuditSurProfilController',
                    controllerAs: 'vm'
                }
            },
            params: {},
            resolve: {}
        })
            .state('connexion-denied', {
                parent: 'entity-audit-event',
                url: '/connexion-denied',
                data: {
                    authorities: [],
                    pageTitle: 'ptiApp.compte.home.title'
                },
                views: {
                    'content@app': {
                        templateUrl: 'app/entities/entity-audit-event/connexion-denied.html',
                        controller: 'ConnexionDeniedController',
                        controllerAs: 'vm'
                    }
                },
                params: {},
                resolve: {}
            })
            .state('profil-user', {
                parent: 'entity-audit-event',
                url: '/profil-user',
                data: {
                    authorities: [],
                    pageTitle: 'ptiApp.compte.home.title'
                },
                views: {
                    'content@app': {
                        templateUrl: 'app/entities/entity-audit-event/profil-user.html',
                        controller: 'ProfilUserController',
                        controllerAs: 'vm'
                    }
                },
                params: {},
                resolve: {}
            })
            .state('profil-habilitation', {
                parent: 'entity-audit-event',
                url: '/profil-habilitation',
                data: {
                    authorities: [],
                    pageTitle: 'ptiApp.compte.home.title'
                },
                views: {
                    'content@app': {
                        templateUrl: 'app/entities/entity-audit-event/profil-habilitation.html',
                        controller: 'ProfilHabilitationController',
                        controllerAs: 'vm'
                    }
                },
                params: {},
                resolve: {}
            })
            .state('rapport-activite-user', {
                parent: 'entity-audit-event',
                url: '/rapport-activite-user',
                data: {
                    authorities: [],
                    pageTitle: 'ptiApp.compte.home.title'
                },
                views: {
                    'content@app': {
                        templateUrl: 'app/entities/entity-audit-event/rapport-activite-user.html',
                        controller: 'RapportActiviteUserController',
                        controllerAs: 'vm'
                    }
                },
                params: {},
                resolve: {}
            });
    };
})();
