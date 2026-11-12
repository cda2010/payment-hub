(function() {
    'use strict';

    angular
        .module('ptiApp')
        .config(stateConfig);

    stateConfig.$inject = ['$stateProvider'];

    function stateConfig($stateProvider) {
        $stateProvider
        .state('categorie-client', {
            parent: 'entity',
            url: '/categorie-client?page&sort&search',
            data: {
                authorities: ['ROLE_USER'],
                pageTitle: 'ptiApp.categorieClient.home.title'
            },
            views: {
                'content@': {
                    templateUrl: 'app/entities/categorie-client/categorie-clients.html',
                    controller: 'CategorieClientController',
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
                    $translatePartialLoader.addPart('categorieClient');
                    $translatePartialLoader.addPart('global');
                    return $translate.refresh();
                }]
            }
        })
        .state('categorie-client-detail', {
            parent: 'categorie-client',
            url: '/categorie-client/{id}',
            data: {
                authorities: ['ROLE_USER'],
                pageTitle: 'ptiApp.categorieClient.detail.title'
            },
            views: {
                'content@': {
                    templateUrl: 'app/entities/categorie-client/categorie-client-detail.html',
                    controller: 'CategorieClientDetailController',
                    controllerAs: 'vm'
                }
            },
            resolve: {
                translatePartialLoader: ['$translate', '$translatePartialLoader', function ($translate, $translatePartialLoader) {
                    $translatePartialLoader.addPart('categorieClient');
                    return $translate.refresh();
                }],
                entity: ['$stateParams', 'CategorieClient', function($stateParams, CategorieClient) {
                    return CategorieClient.get({id : $stateParams.id}).$promise;
                }],
                previousState: ["$state", function ($state) {
                    var currentStateData = {
                        name: $state.current.name || 'categorie-client',
                        params: $state.params,
                        url: $state.href($state.current.name, $state.params)
                    };
                    return currentStateData;
                }]
            }
        })
        .state('categorie-client-detail.edit', {
            parent: 'categorie-client-detail',
            url: '/detail/edit',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/categorie-client/categorie-client-dialog.html',
                    controller: 'CategorieClientDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: ['CategorieClient', function(CategorieClient) {
                            return CategorieClient.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('^', {}, { reload: false });
                }, function() {
                    $state.go('^');
                });
            }]
        })
        .state('categorie-client.new', {
            parent: 'categorie-client',
            url: '/new',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/categorie-client/categorie-client-dialog.html',
                    controller: 'CategorieClientDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: function () {
                            return {
                                libelleCategorie: null,
                                id: null
                            };
                        }
                    }
                }).result.then(function() {
                    $state.go('categorie-client', null, { reload: 'categorie-client' });
                }, function() {
                    $state.go('categorie-client');
                });
            }]
        })
        .state('categorie-client.edit', {
            parent: 'categorie-client',
            url: '/{id}/edit',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/categorie-client/categorie-client-dialog.html',
                    controller: 'CategorieClientDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: ['CategorieClient', function(CategorieClient) {
                            return CategorieClient.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('categorie-client', null, { reload: 'categorie-client' });
                }, function() {
                    $state.go('^');
                });
            }]
        })
        .state('categorie-client.delete', {
            parent: 'categorie-client',
            url: '/{id}/delete',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/categorie-client/categorie-client-delete-dialog.html',
                    controller: 'CategorieClientDeleteController',
                    controllerAs: 'vm',
                    size: 'md',
                    resolve: {
                        entity: ['CategorieClient', function(CategorieClient) {
                            return CategorieClient.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('categorie-client', null, { reload: 'categorie-client' });
                }, function() {
                    $state.go('^');
                });
            }]
        });
    }

})();
