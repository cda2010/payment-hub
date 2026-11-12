(function() {
    'use strict';

    angular
        .module('ptiApp')
        .config(stateConfig);

    stateConfig.$inject = ['$stateProvider'];

    function stateConfig($stateProvider) {
        $stateProvider
        .state('historique', {
            parent: 'entity',
            url: '/historique?page&sort&search',
            data: {
                authorities: ['ROLE_USER'],
                pageTitle: 'ptiApp.historique.home.title'
            },
            views: {
                'content@': {
                    templateUrl: 'app/entities/historique/historiques.html',
                    controller: 'HistoriqueController',
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
                    $translatePartialLoader.addPart('historique');
                    $translatePartialLoader.addPart('historyType');
                    $translatePartialLoader.addPart('global');
                    return $translate.refresh();
                }]
            }
        })
        .state('historique-detail', {
            parent: 'historique',
            url: '/historique/{id}',
            data: {
                authorities: ['ROLE_USER'],
                pageTitle: 'ptiApp.historique.detail.title'
            },
            views: {
                'content@': {
                    templateUrl: 'app/entities/historique/historique-detail.html',
                    controller: 'HistoriqueDetailController',
                    controllerAs: 'vm'
                }
            },
            resolve: {
                translatePartialLoader: ['$translate', '$translatePartialLoader', function ($translate, $translatePartialLoader) {
                    $translatePartialLoader.addPart('historique');
                    $translatePartialLoader.addPart('historyType');
                    return $translate.refresh();
                }],
                entity: ['$stateParams', 'Historique', function($stateParams, Historique) {
                    return Historique.get({id : $stateParams.id}).$promise;
                }],
                previousState: ["$state", function ($state) {
                    var currentStateData = {
                        name: $state.current.name || 'historique',
                        params: $state.params,
                        url: $state.href($state.current.name, $state.params)
                    };
                    return currentStateData;
                }]
            }
        })
        .state('historique-detail.edit', {
            parent: 'historique-detail',
            url: '/detail/edit',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/historique/historique-dialog.html',
                    controller: 'HistoriqueDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: ['Historique', function(Historique) {
                            return Historique.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('^', {}, { reload: false });
                }, function() {
                    $state.go('^');
                });
            }]
        })
        .state('historique.new', {
            parent: 'historique',
            url: '/new',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/historique/historique-dialog.html',
                    controller: 'HistoriqueDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: function () {
                            return {
                                fournisseurService: null,
                                dateHistorique: null,
                                etiquette: null,
                                id: null
                            };
                        }
                    }
                }).result.then(function() {
                    $state.go('historique', null, { reload: 'historique' });
                }, function() {
                    $state.go('historique');
                });
            }]
        })
        .state('historique.edit', {
            parent: 'historique',
            url: '/{id}/edit',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/historique/historique-dialog.html',
                    controller: 'HistoriqueDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: ['Historique', function(Historique) {
                            return Historique.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('historique', null, { reload: 'historique' });
                }, function() {
                    $state.go('^');
                });
            }]
        })
        .state('historique.delete', {
            parent: 'historique',
            url: '/{id}/delete',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/historique/historique-delete-dialog.html',
                    controller: 'HistoriqueDeleteController',
                    controllerAs: 'vm',
                    size: 'md',
                    resolve: {
                        entity: ['Historique', function(Historique) {
                            return Historique.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('historique', null, { reload: 'historique' });
                }, function() {
                    $state.go('^');
                });
            }]
        });
    }

})();
