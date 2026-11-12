(function() {
    'use strict';

    angular
        .module('omnicanalPresentationApp')
        .config(stateConfig);

    stateConfig.$inject = ['$stateProvider'];

    function stateConfig($stateProvider) {
        $stateProvider
        .state('ligne-operation', {
            parent: 'entity',
            url: '/ligne-operation',
            data: {
                authorities: ['ROLE_USER'],
                pageTitle: 'LigneOperations'
            },
            views: {
                'content@': {
                    templateUrl: 'app/entities/ligne-operation/ligne-operations.html',
                    controller: 'LigneOperationController',
                    controllerAs: 'vm'
                }
            },
            resolve: {
            }
        })
        .state('ligne-operation-detail', {
            parent: 'ligne-operation',
            url: '/ligne-operation/{id}',
            data: {
                authorities: ['ROLE_USER'],
                pageTitle: 'LigneOperation'
            },
            views: {
                'content@': {
                    templateUrl: 'app/entities/ligne-operation/ligne-operation-detail.html',
                    controller: 'LigneOperationDetailController',
                    controllerAs: 'vm'
                }
            },
            resolve: {
                entity: ['$stateParams', 'LigneOperation', function($stateParams, LigneOperation) {
                    return LigneOperation.get({id : $stateParams.id}).$promise;
                }],
                previousState: ["$state", function ($state) {
                    var currentStateData = {
                        name: $state.current.name || 'ligne-operation',
                        params: $state.params,
                        url: $state.href($state.current.name, $state.params)
                    };
                    return currentStateData;
                }]
            }
        })
        .state('ligne-operation-detail.edit', {
            parent: 'ligne-operation-detail',
            url: '/detail/edit',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/ligne-operation/ligne-operation-dialog.html',
                    controller: 'LigneOperationDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: ['LigneOperation', function(LigneOperation) {
                            return LigneOperation.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('^', {}, { reload: false });
                }, function() {
                    $state.go('^');
                });
            }]
        })
        .state('ligne-operation.new', {
            parent: 'ligne-operation',
            url: '/new',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/ligne-operation/ligne-operation-dialog.html',
                    controller: 'LigneOperationDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: function () {
                            return {
                                textLigneTransaction: null,
                                reponse: null,
                                codereponse: null,
                                id: null
                            };
                        }
                    }
                }).result.then(function() {
                    $state.go('ligne-operation', null, { reload: 'ligne-operation' });
                }, function() {
                    $state.go('ligne-operation');
                });
            }]
        })
        .state('ligne-operation.edit', {
            parent: 'ligne-operation',
            url: '/{id}/edit',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/ligne-operation/ligne-operation-dialog.html',
                    controller: 'LigneOperationDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: ['LigneOperation', function(LigneOperation) {
                            return LigneOperation.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('ligne-operation', null, { reload: 'ligne-operation' });
                }, function() {
                    $state.go('^');
                });
            }]
        })
        .state('ligne-operation.delete', {
            parent: 'ligne-operation',
            url: '/{id}/delete',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/ligne-operation/ligne-operation-delete-dialog.html',
                    controller: 'LigneOperationDeleteController',
                    controllerAs: 'vm',
                    size: 'md',
                    resolve: {
                        entity: ['LigneOperation', function(LigneOperation) {
                            return LigneOperation.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('ligne-operation', null, { reload: 'ligne-operation' });
                }, function() {
                    $state.go('^');
                });
            }]
        });
    }

})();
