(function() {
    'use strict';

    angular
        .module('ptiApp')
        .config(stateConfig);

    stateConfig.$inject = ['$stateProvider'];

    function stateConfig($stateProvider) {
        $stateProvider
        .state('recepteur-alarme', {
            parent: 'entity',
            url: '/recepteur-alarme?page&sort&search',
            data: {
                authorities: ['ROLE_USER'],
                pageTitle: 'ptiApp.recepteurAlarme.home.title'
            },
            views: {
                'content@app': {
                    templateUrl: 'app/entities/recepteur-alarme/recepteur-alarmes.html',
                    controller: 'RecepteurAlarmeController',
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
                    $translatePartialLoader.addPart('recepteurAlarme');
                    $translatePartialLoader.addPart('global');
                    return $translate.refresh();
                }]
            }
        })
        .state('recepteur-alarme-detail', {
            parent: 'recepteur-alarme',
            url: '/recepteur-alarme/{id}',
            data: {
                authorities: ['ROLE_USER'],
                pageTitle: 'ptiApp.recepteurAlarme.detail.title'
            },
            views: {
                'content@app': {
                    templateUrl: 'app/entities/recepteur-alarme/recepteur-alarme-detail.html',
                    controller: 'RecepteurAlarmeDetailController',
                    controllerAs: 'vm'
                }
            },
            resolve: {
                translatePartialLoader: ['$translate', '$translatePartialLoader', function ($translate, $translatePartialLoader) {
                    $translatePartialLoader.addPart('recepteurAlarme');
                    return $translate.refresh();
                }],
                entity: ['$stateParams', 'RecepteurAlarme', function($stateParams, RecepteurAlarme) {
                    return RecepteurAlarme.get({id : $stateParams.id}).$promise;
                }],
                previousState: ["$state", function ($state) {
                    var currentStateData = {
                        name: $state.current.name || 'recepteur-alarme',
                        params: $state.params,
                        url: $state.href($state.current.name, $state.params)
                    };
                    return currentStateData;
                }]
            }
        })
        .state('recepteur-alarme-detail.edit', {
            parent: 'recepteur-alarme-detail',
            url: '/detail/edit',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/recepteur-alarme/recepteur-alarme-dialog.html',
                    controller: 'RecepteurAlarmeDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'md',
                    resolve: {
                        entity: ['RecepteurAlarme', function(RecepteurAlarme) {
                            return RecepteurAlarme.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('^', {}, { reload: false });
                }, function() {
                    $state.go('^');
                });
            }]
        })
        .state('recepteur-alarme.new', {
            parent: 'recepteur-alarme',
            url: '/new',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/recepteur-alarme/recepteur-alarme-dialog.html',
                    controller: 'RecepteurAlarmeDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'md',
                    resolve: {
                        entity: function () {
                            return {
                                etiquette: null,
                                emails: null,
                                sms: null,
                                id: null
                            };
                        }
                    }
                }).result.then(function() {
                    $state.go('recepteur-alarme', null, { reload: 'recepteur-alarme' });
                }, function() {
                    $state.go('recepteur-alarme');
                });
            }]
        })
        .state('recepteur-alarme.edit', {
            parent: 'recepteur-alarme',
            url: '/{id}/edit',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/recepteur-alarme/recepteur-alarme-dialog.html',
                    controller: 'RecepteurAlarmeDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'md',
                    resolve: {
                        entity: ['RecepteurAlarme', function(RecepteurAlarme) {
                            return RecepteurAlarme.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('recepteur-alarme', null, { reload: 'recepteur-alarme' });
                }, function() {
                    $state.go('^');
                });
            }]
        })
        .state('recepteur-alarme.delete', {
            parent: 'recepteur-alarme',
            url: '/{id}/delete',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/recepteur-alarme/recepteur-alarme-delete-dialog.html',
                    controller: 'RecepteurAlarmeDeleteController',
                    controllerAs: 'vm',
                    size: 'md',
                    resolve: {
                        entity: ['RecepteurAlarme', function(RecepteurAlarme) {
                            return RecepteurAlarme.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('recepteur-alarme', null, { reload: 'recepteur-alarme' });
                }, function() {
                    $state.go('^');
                });
            }]
        });
    }

})();
