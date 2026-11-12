(function() {
    'use strict';

    angular
        .module('ptiApp')
        .config(stateConfig);

    stateConfig.$inject = ['$stateProvider'];

    function stateConfig($stateProvider) {
        $stateProvider
        .state('comptes', {
            parent: 'entity',
            url: '/comptes?page&sort&search',
            data: {
                authorities: ['ROLE_USER'],
                pageTitle: 'compte'
            },
            views: {
                'content@app': {
                    templateUrl: 'app/entities/bw-compte/comptess.html',
                    controller: 'ComptesController',
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
                    // $translatePartialLoader.addPart('comptes');
                    $translatePartialLoader.addPart('global');
                    return $translate.refresh();
                }]
            }
        })
        .state('comptes-detail', {
            parent: 'comptes',
            url: '/comptes/{id}',
            data: {
                authorities: ['ROLE_USER'],
                pageTitle: 'comptes'
            },
            views: {
                'content@app': {
                    templateUrl: 'app/entities/bw-compte/comptes-detail.html',
                    controller: 'ComptesDetailController',
                    controllerAs: 'vm'
                }
            },
            resolve: {
                translatePartialLoader: ['$translate', '$translatePartialLoader', function ($translate, $translatePartialLoader) {
                    $translatePartialLoader.addPart('comptes');
                    return $translate.refresh();
                }],
                entity: ['$stateParams', 'Comptes', function($stateParams, Comptes) {
                    return Comptes.get({id : $stateParams.id}).$promise;
                }],
                previousState: ["$state", function ($state) {
                    var currentStateData = {
                        name: $state.current.name || 'comptes',
                        params: $state.params,
                        url: $state.href($state.current.name, $state.params)
                    };
                    return currentStateData;
                }]
            }
        })
        .state('comptes-detail.edit', {
            parent: 'comptes-detail',
            url: '/detail/edit',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/bw-compte/comptes-dialog.html',
                    controller: 'ComptesDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: ['Comptes', function(Comptes) {
                            return Comptes.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('^', {}, { reload: false });
                }, function() {
                    $state.go('^');
                });
            }]
        })
        .state('comptes.new', {
            parent: 'comptes',
            url: '/new',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/bw-compte/comptes-dialog.html',
                    controller: 'ComptesDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: function () {
                            return {
                                solde: null,
                                id: null
                            };
                        }
                    }
                }).result.then(function() {
                    $state.go('comptes', null, { reload: 'comptes' });
                }, function() {
                    $state.go('comptes');
                });
            }]
        })
        .state('comptes.edit', {
            parent: 'comptes',
            url: '/{id}/edit',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/bw-compte/comptes-dialog.html',
                    controller: 'ComptesDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: ['Comptes', function(Comptes) {
                            return Comptes.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('comptes', null, { reload: 'comptes' });
                }, function() {
                    $state.go('^');
                });
            }]
        })
        .state('comptes.delete', {
            parent: 'comptes',
            url: '/{id}/delete',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/bw-compte/comptes-delete-dialog.html',
                    controller: 'ComptesDeleteController',
                    controllerAs: 'vm',
                    size: 'md',
                    resolve: {
                        entity: ['Comptes', function(Comptes) {
                            return Comptes.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('comptes', null, { reload: 'comptes' });
                }, function() {
                    $state.go('^');
                });
            }]
        });
    }

})();
