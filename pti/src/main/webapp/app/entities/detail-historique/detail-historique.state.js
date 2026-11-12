(function() {
    'use strict';

    angular
        .module('ptiApp')
        .config(stateConfig);

    stateConfig.$inject = ['$stateProvider'];

    function stateConfig($stateProvider) {
        $stateProvider
        .state('detail-historique', {
            parent: 'entity',
            url: '/detail-historique?page&sort&search',
            data: {
                authorities: ['ROLE_USER'],
                pageTitle: 'ptiApp.detailHistorique.home.title'
            },
            views: {
                'content@app': {
                    templateUrl: 'app/entities/detail-historique/detail-historiques.html',
                    controller: 'DetailHistoriqueController',
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
                    $translatePartialLoader.addPart('detailHistorique');
                    $translatePartialLoader.addPart('global');
                    return $translate.refresh();
                }]
            }
        })
        .state('detail-historique-detail', {
            parent: 'detail-historique',
            url: '/detail-historique/{id}',
            data: {
                authorities: ['ROLE_USER'],
                pageTitle: 'ptiApp.detailHistorique.detail.title'
            },
            views: {
                'content@app': {
                    templateUrl: 'app/entities/detail-historique/detail-historique-detail.html',
                    controller: 'DetailHistoriqueDetailController',
                    controllerAs: 'vm'
                }
            },
            resolve: {
                translatePartialLoader: ['$translate', '$translatePartialLoader', function ($translate, $translatePartialLoader) {
                    $translatePartialLoader.addPart('detailHistorique');
                    return $translate.refresh();
                }],
                entity: ['$stateParams', 'DetailHistorique', function($stateParams, DetailHistorique) {
                    return DetailHistorique.get({id : $stateParams.id}).$promise;
                }],
                previousState: ["$state", function ($state) {
                    var currentStateData = {
                        name: $state.current.name || 'detail-historique',
                        params: $state.params,
                        url: $state.href($state.current.name, $state.params)
                    };
                    return currentStateData;
                }]
            }
        })
        .state('detail-historique-detail.edit', {
            parent: 'detail-historique-detail',
            url: '/detail/edit',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/detail-historique/detail-historique-dialog.html',
                    controller: 'DetailHistoriqueDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: ['DetailHistorique', function(DetailHistorique) {
                            return DetailHistorique.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('^', {}, { reload: false });
                }, function() {
                    $state.go('^');
                });
            }]
        })
        .state('detail-historique.new', {
            parent: 'detail-historique',
            url: '/new',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/detail-historique/detail-historique-dialog.html',
                    controller: 'DetailHistoriqueDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: function () {
                            return {
                                numeroCpt: null,
                                libelle: null,
                                solde: null,
                                id: null
                            };
                        }
                    }
                }).result.then(function() {
                    $state.go('detail-historique', null, { reload: 'detail-historique' });
                }, function() {
                    $state.go('detail-historique');
                });
            }]
        })
        .state('detail-historique.edit', {
            parent: 'detail-historique',
            url: '/{id}/edit',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/detail-historique/detail-historique-dialog.html',
                    controller: 'DetailHistoriqueDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: ['DetailHistorique', function(DetailHistorique) {
                            return DetailHistorique.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('detail-historique', null, { reload: 'detail-historique' });
                }, function() {
                    $state.go('^');
                });
            }]
        })
        .state('detail-historique.delete', {
            parent: 'detail-historique',
            url: '/{id}/delete',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/detail-historique/detail-historique-delete-dialog.html',
                    controller: 'DetailHistoriqueDeleteController',
                    controllerAs: 'vm',
                    size: 'md',
                    resolve: {
                        entity: ['DetailHistorique', function(DetailHistorique) {
                            return DetailHistorique.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('detail-historique', null, { reload: 'detail-historique' });
                }, function() {
                    $state.go('^');
                });
            }]
        });
    }

})();
