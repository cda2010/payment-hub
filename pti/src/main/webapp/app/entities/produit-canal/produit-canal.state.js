(function() {
    'use strict';

    angular
        .module('ptiApp')
        .config(stateConfig);

    stateConfig.$inject = ['$stateProvider'];

    function stateConfig($stateProvider) {
        $stateProvider
        .state('produit-canal', {
            parent: 'entity',
            url: '/produit-canal?page&sort&search',
            data: {
                authorities: ['ROLE_USER'],
                pageTitle: 'ptiApp.produitCanal.home.title'
            },
            views: {
                'content@': {
                    templateUrl: 'app/entities/produit-canal/produit-canals.html',
                    controller: 'ProduitCanalController',
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
                    $translatePartialLoader.addPart('produitCanal');
                    $translatePartialLoader.addPart('global');
                    return $translate.refresh();
                }]
            }
        })
        .state('produit-canal-detail', {
            parent: 'produit-canal',
            url: '/produit-canal/{id}',
            data: {
                authorities: ['ROLE_USER'],
                pageTitle: 'ptiApp.produitCanal.detail.title'
            },
            views: {
                'content@': {
                    templateUrl: 'app/entities/produit-canal/produit-canal-detail.html',
                    controller: 'ProduitCanalDetailController',
                    controllerAs: 'vm'
                }
            },
            resolve: {
                translatePartialLoader: ['$translate', '$translatePartialLoader', function ($translate, $translatePartialLoader) {
                    $translatePartialLoader.addPart('produitCanal');
                    return $translate.refresh();
                }],
                entity: ['$stateParams', 'ProduitCanal', function($stateParams, ProduitCanal) {
                    return ProduitCanal.get({id : $stateParams.id}).$promise;
                }],
                previousState: ["$state", function ($state) {
                    var currentStateData = {
                        name: $state.current.name || 'produit-canal',
                        params: $state.params,
                        url: $state.href($state.current.name, $state.params)
                    };
                    return currentStateData;
                }]
            }
        })
        .state('produit-canal-detail.edit', {
            parent: 'produit-canal-detail',
            url: '/detail/edit',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/produit-canal/produit-canal-dialog.html',
                    controller: 'ProduitCanalDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: ['ProduitCanal', function(ProduitCanal) {
                            return ProduitCanal.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('^', {}, { reload: false });
                }, function() {
                    $state.go('^');
                });
            }]
        })
        .state('produit-canal.new', {
            parent: 'produit-canal',
            url: '/new',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/produit-canal/produit-canal-dialog.html',
                    controller: 'ProduitCanalDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: function () {
                            return {
                                id: null
                            };
                        }
                    }
                }).result.then(function() {
                    $state.go('produit-canal', null, { reload: 'produit-canal' });
                }, function() {
                    $state.go('produit-canal');
                });
            }]
        })
        .state('produit-canal.edit', {
            parent: 'produit-canal',
            url: '/{id}/edit',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/produit-canal/produit-canal-dialog.html',
                    controller: 'ProduitCanalDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: ['ProduitCanal', function(ProduitCanal) {
                            return ProduitCanal.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('produit-canal', null, { reload: 'produit-canal' });
                }, function() {
                    $state.go('^');
                });
            }]
        })
        .state('produit-canal.delete', {
            parent: 'produit-canal',
            url: '/{id}/delete',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/produit-canal/produit-canal-delete-dialog.html',
                    controller: 'ProduitCanalDeleteController',
                    controllerAs: 'vm',
                    size: 'md',
                    resolve: {
                        entity: ['ProduitCanal', function(ProduitCanal) {
                            return ProduitCanal.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('produit-canal', null, { reload: 'produit-canal' });
                }, function() {
                    $state.go('^');
                });
            }]
        });
    }

})();
