(function() {
    'use strict';

    angular
        .module('ptiApp')
        .config(stateConfig);

    stateConfig.$inject = ['$stateProvider'];

    function stateConfig($stateProvider) {
        $stateProvider
        .state('localite', {
            parent: 'entity',
            url: '/localite?page&sort&search',
            data: {
                authorities: ['ROLE_USER'],
                pageTitle: 'ptiApp.localite.home.title'
            },
            views: {
                'content@app': {
                    templateUrl: 'app/entities/localite/localites.html',
                    controller: 'LocaliteController',
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
                    $translatePartialLoader.addPart('localite');
                    $translatePartialLoader.addPart('global');
                    return $translate.refresh();
                }]
            }
        })
        .state('localite-detail', {
            parent: 'localite',
            url: '/localite/{id}',
            data: {
                authorities: ['ROLE_USER'],
                pageTitle: 'ptiApp.localite.detail.title'
            },
            views: {
                'content@app': {
                    templateUrl: 'app/entities/localite/localite-detail.html',
                    controller: 'LocaliteDetailController',
                    controllerAs: 'vm'
                }
            },
            resolve: {
                translatePartialLoader: ['$translate', '$translatePartialLoader', function ($translate, $translatePartialLoader) {
                    $translatePartialLoader.addPart('localite');
                    return $translate.refresh();
                }],
                entity: ['$stateParams', 'Localite', function($stateParams, Localite) {
                    return Localite.get({id : $stateParams.id}).$promise;
                }],
                previousState: ["$state", function ($state) {
                    var currentStateData = {
                        name: $state.current.name || 'localite',
                        params: $state.params,
                        url: $state.href($state.current.name, $state.params)
                    };
                    return currentStateData;
                }]
            }
        })
        .state('localite-detail.edit', {
            parent: 'localite-detail',
            url: '/detail/edit',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/localite/localite-dialog.html',
                    controller: 'LocaliteDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: ['Localite', function(Localite) {
                            return Localite.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('^', {}, { reload: false });
                }, function() {
                    $state.go('^');
                });
            }]
        })
        .state('localite.new', {
            parent: 'localite',
            url: '/new',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/localite/localite-dialog.html',
                    controller: 'LocaliteDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: function () {
                            return {
                                nomRegion: null,
                                nomProvince: null,
                                nomCommune: null,
                                nomVille: null,
                                id: null
                            };
                        }
                    }
                }).result.then(function() {
                    $state.go('localite', null, { reload: 'localite' });
                }, function() {
                    $state.go('localite');
                });
            }]
        })
        .state('localite.edit', {
            parent: 'localite',
            url: '/{id}/edit',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/localite/localite-dialog.html',
                    controller: 'LocaliteDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: ['Localite', function(Localite) {
                            return Localite.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('localite', null, { reload: 'localite' });
                }, function() {
                    $state.go('^');
                });
            }]
        })
        .state('localite.delete', {
            parent: 'localite',
            url: '/{id}/delete',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/localite/localite-delete-dialog.html',
                    controller: 'LocaliteDeleteController',
                    controllerAs: 'vm',
                    size: 'md',
                    resolve: {
                        entity: ['Localite', function(Localite) {
                            return Localite.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('localite', null, { reload: 'localite' });
                }, function() {
                    $state.go('^');
                });
            }]
        });
    }

})();
