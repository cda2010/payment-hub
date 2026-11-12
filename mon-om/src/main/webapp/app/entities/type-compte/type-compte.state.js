(function() {
    'use strict';

    angular
        .module('omnicanalApp')
        .config(stateConfig);

    stateConfig.$inject = ['$stateProvider'];

    function stateConfig($stateProvider) {
        $stateProvider
        .state('type-compte', {
            parent: 'entity',
            url: '/type-compte',
            data: {
                authorities: ['ROLE_USER'],
                pageTitle: 'TypeComptes'
            },
            views: {
                'content@app': {
                    templateUrl: 'app/entities/type-compte/type-comptes.html',
                    controller: 'TypeCompteController',
                    controllerAs: 'vm'
                }
            },
            params: {
                page: {
                    value: '1',
                    squash: true
                },
                sort: {
                    value: 'codeParametre,asc',
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
            }
        })
        .state('type-compte-detail', {
            parent: 'type-compte',
            url: '/type-compte/{id}',
            data: {
                authorities: ['ROLE_USER'],
                pageTitle: 'TypeCompte'
            },
            views: {
                'content@app': {
                    templateUrl: 'app/entities/type-compte/type-compte-detail.html',
                    controller: 'TypeCompteDetailController',
                    controllerAs: 'vm'
                }
            },
            resolve: {
                entity: ['$stateParams', 'TypeCompte', function($stateParams, TypeCompte) {
                    return TypeCompte.get({id : $stateParams.id}).$promise;
                }],
                previousState: ["$state", function ($state) {
                    var currentStateData = {
                        name: $state.current.name || 'type-compte',
                        params: $state.params,
                        url: $state.href($state.current.name, $state.params)
                    };
                    return currentStateData;
                }]
            }
        })
        .state('type-compte-detail.edit', {
            parent: 'type-compte-detail',
            url: '/detail/edit',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/type-compte/type-compte-dialog.html',
                    controller: 'TypeCompteDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: ['TypeCompte', function(TypeCompte) {
                            return TypeCompte.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('^', {}, { reload: false });
                }, function() {
                    $state.go('^');
                });
            }]
        })
        .state('type-compte.new', {
            parent: 'type-compte',
            url: '/new',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/type-compte/type-compte-dialog.html',
                    controller: 'TypeCompteDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: function () {
                            return {
                                nomTypeCompte: null,
                                id: null
                            };
                        }
                    }
                }).result.then(function() {
                    $state.go('type-compte', null, { reload: 'type-compte' });
                }, function() {
                    $state.go('type-compte');
                });
            }]
        })
        .state('type-compte.edit', {
            parent: 'type-compte',
            url: '/{id}/edit',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/type-compte/type-compte-dialog.html',
                    controller: 'TypeCompteDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: ['TypeCompte', function(TypeCompte) {
                            return TypeCompte.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('type-compte', null, { reload: 'type-compte' });
                }, function() {
                    $state.go('^');
                });
            }]
        })
        .state('type-compte.delete', {
            parent: 'type-compte',
            url: '/{id}/delete',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/type-compte/type-compte-delete-dialog.html',
                    controller: 'TypeCompteDeleteController',
                    controllerAs: 'vm',
                    size: 'md',
                    resolve: {
                        entity: ['TypeCompte', function(TypeCompte) {
                            return TypeCompte.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('type-compte', null, { reload: 'type-compte' });
                }, function() {
                    $state.go('^');
                });
            }]
        });
    }

})();
