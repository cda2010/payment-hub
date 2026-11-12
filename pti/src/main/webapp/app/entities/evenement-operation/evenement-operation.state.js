(function() {
    'use strict';

    angular
        .module('ptiApp')
        .config(stateConfig);

    stateConfig.$inject = ['$stateProvider'];

    function stateConfig($stateProvider) {
        $stateProvider
        .state('evenement-operation', {
            parent: 'entity',
            url: '/evenement-operation?page&sort&search',
            data: {
                authorities: ['ROLE_USER'],
                pageTitle: 'ptiApp.evenement-operation.home.title'
            },
            views: {
                'content@app': {
                    templateUrl: 'app/entities/evenement-operation/evenement-operations.html',
                    controller: 'EvenementOperationController',
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
                    $translatePartialLoader.addPart('canal');
                    $translatePartialLoader.addPart('global');
                    return $translate.refresh();
                }]
            }
        })
        .state('evenement-operation-detail', {
            parent: 'evenement-operation',
            url: '/evenement-operation/{id}',
            data: {
                authorities: ['ROLE_USER'],
                pageTitle: 'ptiApp.evenement-operation.detail.title'
            },
            views: {
                'content@': {
                    templateUrl: 'app/entities/evenement-operation/evenement-operation-detail.html',
                    controller: 'EvenementOperationDetailController',
                    controllerAs: 'vm'
                }
            },
            resolve: {
                translatePartialLoader: ['$translate', '$translatePartialLoader', function ($translate, $translatePartialLoader) {
                    $translatePartialLoader.addPart('ftp');
                    return $translate.refresh();
                }],
                entity: ['$stateParams', 'EvenementOperation', function($stateParams, EvenementOperation) {
                    return EvenementOperation.get({id : $stateParams.id}).$promise;
                }],
                previousState: ["$state", function ($state) {
                    var currentStateData = {
                        name: $state.current.name || 'evenement-operation',
                        params: $state.params,
                        url: $state.href($state.current.name, $state.params)
                    };
                    return currentStateData;
                }]
            }
        })
        .state('evenement-operation-detail.edit', {
            parent: 'evenement-operation-detail',
            url: '/detail/edit',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/evenement-operation/evenement-operation-dialog.html',
                    controller: 'EvenementOperationDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: ['EvenementOperation', function(EvenementOperation) {
                            return EvenementOperation.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('^', {}, { reload: false });
                }, function() {
                    $state.go('^');
                });
            }]
        })
        .state('evenement-operation.new', {
            parent: 'evenement-operation',
            url: '/new',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/evenement-operation/evenement-operation-dialog.html',
                    controller: 'EvenementOperationDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: function () {
                            return {
                                montant: null,
                                estLevee: null,
                                id: null
                            };
                        }
                    }
                }).result.then(function() {
                    $state.go('evenement-operation', null, { reload: 'evenement-operation' });
                }, function() {
                    $state.go('evenement-operation');
                });
            }]
        })
        .state('evenement-operation.edit', {
            parent: 'evenement-operation',
            url: '/{id}/edit',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/evenement-operation/evenement-operation-dialog.html',
                    controller: 'EvenementOperationDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: ['EvenementOperation', function(EvenementOperation) {
                            return EvenementOperation.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('evenement-operation', null, { reload: 'evenement-operation' });
                }, function() {
                    $state.go('^');
                });
            }]
        })
        .state('evenement-operation.delete', {
            parent: 'evenement-operation',
            url: '/{id}/delete',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/evenement-operation/evenement-operation-delete-dialog.html',
                    controller: 'EvenementOperationDeleteController',
                    controllerAs: 'vm',
                    size: 'md',
                    resolve: {
                        entity: ['EvenementOperation', function(EvenementOperation) {
                            return EvenementOperation.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('evenement-operation', null, { reload: 'evenement-operation' });
                }, function() {
                    $state.go('^');
                });
            }]
        });
    }

})();
