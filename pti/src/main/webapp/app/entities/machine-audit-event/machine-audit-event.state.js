(function() {
    'use strict';

    angular
        .module('ptiApp')
        .config(stateConfig);

    stateConfig.$inject = ['$stateProvider'];

    function stateConfig($stateProvider) {
        $stateProvider
        .state('machine-audit-event', {
            parent: 'entity',
            url: '/machine-audit-event?page&sort&search',
            data: {
                authorities: ['ROLE_USER'],
                pageTitle: 'ptiApp.machineAuditEvent.home.title'
            },
            views: {
                'content@app': {
                    templateUrl: 'app/components/tables/tables.html',
                    controller: 'MachineAuditEventController',
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
                    $translatePartialLoader.addPart('machineAuditEvent');
                    $translatePartialLoader.addPart('statut');
                    $translatePartialLoader.addPart('global');
                    return $translate.refresh();
                }]
            }
        })
        .state('machine-audit-event-detail', {
            parent: 'machine-audit-event',
            url: '/machine-audit-event/{id}',
            data: {
                authorities: ['ROLE_USER'],
                pageTitle: 'ptiApp.machineAuditEvent.detail.title'
            },
            views: {
                'content@': {
                    templateUrl: 'app/entities/machine-audit-event/machine-audit-event-detail.html',
                    controller: 'MachineAuditEventDetailController',
                    controllerAs: 'vm'
                }
            },
            resolve: {
                translatePartialLoader: ['$translate', '$translatePartialLoader', function ($translate, $translatePartialLoader) {
                    $translatePartialLoader.addPart('machineAuditEvent');
                    $translatePartialLoader.addPart('statut');
                    return $translate.refresh();
                }],
                entity: ['$stateParams', 'MachineAuditEvent', function($stateParams, MachineAuditEvent) {
                    return MachineAuditEvent.get({id : $stateParams.id}).$promise;
                }],
                previousState: ["$state", function ($state) {
                    var currentStateData = {
                        name: $state.current.name || 'machine-audit-event',
                        params: $state.params,
                        url: $state.href($state.current.name, $state.params)
                    };
                    return currentStateData;
                }]
            }
        })
        .state('machine-audit-event-detail.edit', {
            parent: 'machine-audit-event-detail',
            url: '/detail/edit',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/machine-audit-event/machine-audit-event-dialog.html',
                    controller: 'MachineAuditEventDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: ['MachineAuditEvent', function(MachineAuditEvent) {
                            return MachineAuditEvent.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('^', {}, { reload: false });
                }, function() {
                    $state.go('^');
                });
            }]
        })
        .state('machine-audit-event.new', {
            parent: 'machine-audit-event',
            url: '/new',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/machine-audit-event/machine-audit-event-dialog.html',
                    controller: 'MachineAuditEventDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: function () {
                            return {
                                statut: null,
                                id: null
                            };
                        }
                    }
                }).result.then(function() {
                    $state.go('machine-audit-event', null, { reload: 'machine-audit-event' });
                }, function() {
                    $state.go('machine-audit-event');
                });
            }]
        })
        .state('machine-audit-event.edit', {
            parent: 'machine-audit-event',
            url: '/{id}/edit',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/machine-audit-event/machine-audit-event-dialog.html',
                    controller: 'MachineAuditEventDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: ['MachineAuditEvent', function(MachineAuditEvent) {
                            return MachineAuditEvent.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('machine-audit-event', null, { reload: 'machine-audit-event' });
                }, function() {
                    $state.go('^');
                });
            }]
        })
        .state('machine-audit-event.delete', {
            parent: 'machine-audit-event',
            url: '/{id}/delete',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/machine-audit-event/machine-audit-event-delete-dialog.html',
                    controller: 'MachineAuditEventDeleteController',
                    controllerAs: 'vm',
                    size: 'md',
                    resolve: {
                        entity: ['MachineAuditEvent', function(MachineAuditEvent) {
                            return MachineAuditEvent.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('machine-audit-event', null, { reload: 'machine-audit-event' });
                }, function() {
                    $state.go('^');
                });
            }]
        });
    }

})();
