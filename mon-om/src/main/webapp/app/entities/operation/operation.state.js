(function() {
    'use strict';

    angular
        .module('omnicanalApp')
        .config(stateConfig);

    stateConfig.$inject = ['$stateProvider'];

    function stateConfig($stateProvider) {
        $stateProvider
        .state('operation', {
            parent: 'entity',
            url: '/operation',
            data: {
                authorities: ['ROLE_ADMIN', 'ROLE_MANAGER'],
                pageTitle: 'Visualisation Opération'
            },
            views: {
                'content@app': {
                    templateUrl: 'app/entities/operation/operations.html',
                    controller: 'OperationController',
                    controllerAs: 'vm'
                }
            },
            resolve: {
            }
        })
        .state('operation-detail', {
            parent: 'operation',
            url: '/operation/{id}',
            data: {
                authorities: ['ROLE_USER'],
                pageTitle: 'Operation'
            },
            views: {
                'content@': {
                    templateUrl: 'app/entities/operation/operation-detail.html',
                    controller: 'OperationDetailController',
                    controllerAs: 'vm'
                }
            },
            resolve: {
                entity: ['$stateParams', 'Operation', function($stateParams, Operation) {
                    return Operation.get({id : $stateParams.id}).$promise;
                }],
                previousState: ["$state", function ($state) {
                    var currentStateData = {
                        name: $state.current.name || 'operation',
                        params: $state.params,
                        url: $state.href($state.current.name, $state.params)
                    };
                    return currentStateData;
                }]
            }
        })
        .state('operation-detail.edit', {
            parent: 'operation-detail',
            url: '/detail/edit',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/operation/operation-dialog.html',
                    controller: 'OperationDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: ['Operation', function(Operation) {
                            return Operation.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('^', {}, { reload: false });
                }, function() {
                    $state.go('^');
                });
            }]
        })
        .state('operation.new', {
            parent: 'operation',
            url: '/new',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/operation/operation-dialog.html',
                    controller: 'OperationDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: function () {
                            return {
                                dateOperation: null,
                                fseCodeSource: null,
                                fseCodeDest: null,
                                user_id: null,
                                typeService: null,
                                terminal: null,
                                id: null
                            };
                        }
                    }
                }).result.then(function() {
                    $state.go('operation', null, { reload: 'operation' });
                }, function() {
                    $state.go('operation');
                });
            }]
        })
        .state('operation.edit', {
            parent: 'operation',
            url: '/{id}/edit',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/operation/operation-dialog.html',
                    controller: 'OperationDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: ['Operation', function(Operation) {
                            return Operation.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('operation', null, { reload: 'operation' });
                }, function() {
                    $state.go('^');
                });
            }]
        })
        .state('operation.delete', {
            parent: 'operation',
            url: '/{id}/delete',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/operation/operation-delete-dialog.html',
                    controller: 'OperationDeleteController',
                    controllerAs: 'vm',
                    size: 'md',
                    resolve: {
                        entity: ['Operation', function(Operation) {
                            return Operation.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('operation', null, { reload: 'operation' });
                }, function() {
                    $state.go('^');
                });
            }]
        })
            .state('ligne-operation', {
                parent: 'operation',
                url: '/ligne/operation/{id}',
                data: {
                    authorities: ['ROLE_USER']
                },
                onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                    $uibModal.open({
                        templateUrl: 'app/entities/operation/ligne-operation.html',
                        controller: 'LigneOperationController',
                        controllerAs: 'vm',
                        backdrop: 'static',
                        size: 'xlg',
                        // windowClass: 'my-modal',
                        resolve: {
                            entity: ['Operation', function(Operation) {
                                return Operation.get({id : $stateParams.id}).$promise;
                            }]
                        }
                    }).result.then(function() {
                        $state.go('^', {}, { reload: false });
                    }, function() {
                        $state.go('^');
                    });
                }]
            });
    }

})();
