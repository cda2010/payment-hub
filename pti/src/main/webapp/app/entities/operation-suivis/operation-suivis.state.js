(function() {
    'use strict';

    angular
        .module('ptiApp')
        .config(stateConfig);

    stateConfig.$inject = ['$stateProvider'];

    function stateConfig($stateProvider) {
        $stateProvider
        .state('operation-suivis', {
            parent: 'entity',
            url: '/operation-suivis?page&sort&search',
            data: {
                authorities: ['ROLE_USER'],
                pageTitle: 'ptiApp.operationSuivis.home.title'
            },
            views: {
                'content@app': {
                    templateUrl: 'app/entities/operation-suivis/operation-suivis.html',
                    controller: 'OperationSuivisController',
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
                    $translatePartialLoader.addPart('operationSuivis');
                    $translatePartialLoader.addPart('global');
                    return $translate.refresh();
                }]
            }
        })
        .state('operation-suivis-detail', {
            parent: 'operation-suivis',
            url: '/operation-suivis/{id}',
            data: {
                authorities: ['ROLE_USER'],
                pageTitle: 'ptiApp.operationSuivis.detail.title'
            },
            views: {
                'content@app': {
                    templateUrl: 'app/entities/operation-suivis/operation-suivis-detail.html',
                    controller: 'OperationSuivisDetailController',
                    controllerAs: 'vm'
                }
            },
            resolve: {
                translatePartialLoader: ['$translate', '$translatePartialLoader', function ($translate, $translatePartialLoader) {
                    $translatePartialLoader.addPart('operationSuivis');
                    return $translate.refresh();
                }],
                entity: ['$stateParams', 'OperationSuivis', function($stateParams, OperationSuivis) {
                    return OperationSuivis.get({id : $stateParams.id}).$promise;
                }],
                previousState: ["$state", function ($state) {
                    var currentStateData = {
                        name: $state.current.name || 'operation-suivis',
                        params: $state.params,
                        url: $state.href($state.current.name, $state.params)
                    };
                    return currentStateData;
                }]
            }
        })
        .state('operation-suivis-detail.edit', {
            parent: 'operation-suivis-detail',
            url: '/detail/edit',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/operation-suivis/operation-suivis-dialog.html',
                    controller: 'OperationSuivisDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: ['OperationSuivis', function(OperationSuivis) {
                            return OperationSuivis.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('^', {}, { reload: false });
                }, function() {
                    $state.go('^');
                });
            }]
        })
        .state('operation-suivis.new', {
            parent: 'operation-suivis',
            url: '/new',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/operation-suivis/operation-suivis-dialog.html',
                    controller: 'OperationSuivisDialogController',
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
                    $state.go('operation-suivis', null, { reload: 'operation-suivis' });
                }, function() {
                    $state.go('operation-suivis');
                });
            }]
        })
        .state('operation-suivis.edit', {
            parent: 'operation-suivis',
            url: '/{id}/edit',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/operation-suivis/operation-suivis-dialog.html',
                    controller: 'OperationSuivisDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: ['OperationSuivis', function(OperationSuivis) {
                            return OperationSuivis.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('operation-suivis', null, { reload: 'operation-suivis' });
                }, function() {
                    $state.go('^');
                });
            }]
        })
        .state('operation-suivis.delete', {
            parent: 'operation-suivis',
            url: '/{id}/delete',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/operation-suivis/operation-suivis-delete-dialog.html',
                    controller: 'OperationSuivisDeleteController',
                    controllerAs: 'vm',
                    size: 'md',
                    resolve: {
                        entity: ['OperationSuivis', function(OperationSuivis) {
                            return OperationSuivis.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('operation-suivis', null, { reload: 'operation-suivis' });
                }, function() {
                    $state.go('^');
                });
            }]
        });
    }

})();
