(function() {
    'use strict';

    angular
        .module('ptiApp')
        .config(stateConfig);

    stateConfig.$inject = ['$stateProvider'];

    function stateConfig($stateProvider) {
        $stateProvider
        .state('bilan-operation', {
            parent: 'entity',
            url: '/bilan-operation?page&sort&search',
            data: {
                authorities: ['ROLE_USER'],
                pageTitle: 'ptiApp.bilanOperation.home.title'
            },
            views: {
                'content@app': {
                    templateUrl: 'app/entities/bilan-operation/bilan-operations.html',
                    controller: 'BilanOperationController',
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
                    $translatePartialLoader.addPart('bilanOperation');
                    $translatePartialLoader.addPart('global');
                    return $translate.refresh();
                }]
            }
        })
        .state('bilan-operation-detail', {
            parent: 'bilan-operation',
            url: '/bilan-operation/{id}',
            data: {
                authorities: ['ROLE_USER'],
                pageTitle: 'ptiApp.bilanOperation.detail.title'
            },
            views: {
                'content@': {
                    templateUrl: 'app/entities/bilan-operation/bilan-operation-detail.html',
                    controller: 'BilanOperationDetailController',
                    controllerAs: 'vm'
                }
            },
            resolve: {
                translatePartialLoader: ['$translate', '$translatePartialLoader', function ($translate, $translatePartialLoader) {
                    $translatePartialLoader.addPart('bilanOperation');
                    return $translate.refresh();
                }],
                entity: ['$stateParams', 'BilanOperation', function($stateParams, BilanOperation) {
                    return BilanOperation.get({id : $stateParams.id}).$promise;
                }],
                previousState: ["$state", function ($state) {
                    var currentStateData = {
                        name: $state.current.name || 'bilan-operation',
                        params: $state.params,
                        url: $state.href($state.current.name, $state.params)
                    };
                    return currentStateData;
                }]
            }
        })
        .state('bilan-operation-detail.edit', {
            parent: 'bilan-operation-detail',
            url: '/detail/edit',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/bilan-operation/bilan-operation-dialog.html',
                    controller: 'BilanOperationDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: ['BilanOperation', function(BilanOperation) {
                            return BilanOperation.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('^', {}, { reload: false });
                }, function() {
                    $state.go('^');
                });
            }]
        })
        .state('bilan-operation.new', {
            parent: 'bilan-operation',
            url: '/new',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/bilan-operation/bilan-operation-dialog.html',
                    controller: 'BilanOperationDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: function () {
                            return {
                                nombre: null,
                                montant: null,
                                dateDerniereOperation: null,
                                id: null
                            };
                        }
                    }
                }).result.then(function() {
                    $state.go('bilan-operation', null, { reload: 'bilan-operation' });
                }, function() {
                    $state.go('bilan-operation');
                });
            }]
        })
        .state('bilan-operation.edit', {
            parent: 'bilan-operation',
            url: '/{id}/edit',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/bilan-operation/bilan-operation-dialog.html',
                    controller: 'BilanOperationDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: ['BilanOperation', function(BilanOperation) {
                            return BilanOperation.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('bilan-operation', null, { reload: 'bilan-operation' });
                }, function() {
                    $state.go('^');
                });
            }]
        })
        .state('bilan-operation.delete', {
            parent: 'bilan-operation',
            url: '/{id}/delete',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/bilan-operation/bilan-operation-delete-dialog.html',
                    controller: 'BilanOperationDeleteController',
                    controllerAs: 'vm',
                    size: 'md',
                    resolve: {
                        entity: ['BilanOperation', function(BilanOperation) {
                            return BilanOperation.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('bilan-operation', null, { reload: 'bilan-operation' });
                }, function() {
                    $state.go('^');
                });
            }]
        });
    }

})();
