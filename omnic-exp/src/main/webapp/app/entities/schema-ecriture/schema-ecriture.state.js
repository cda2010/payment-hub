(function() {
    'use strict';

    angular
        .module('omnicanalPresentationApp')
        .config(stateConfig);

    stateConfig.$inject = ['$stateProvider'];

    function stateConfig($stateProvider) {
        $stateProvider
        .state('schema-ecriture', {
            parent: 'entity',
            url: '/schema-ecriture',
            data: {
                authorities: ['ROLE_USER'],
                pageTitle: 'SchemaEcritures'
            },
            views: {
                'content@': {
                    templateUrl: 'app/entities/schema-ecriture/schema-ecritures.html',
                    controller: 'SchemaEcritureController',
                    controllerAs: 'vm'
                }
            },
            resolve: {
            }
        })
        .state('schema-ecriture-detail', {
            parent: 'schema-ecriture',
            url: '/schema-ecriture/{id}',
            data: {
                authorities: ['ROLE_USER'],
                pageTitle: 'SchemaEcriture'
            },
            views: {
                'content@': {
                    templateUrl: 'app/entities/schema-ecriture/schema-ecriture-detail.html',
                    controller: 'SchemaEcritureDetailController',
                    controllerAs: 'vm'
                }
            },
            resolve: {
                entity: ['$stateParams', 'SchemaEcriture', function($stateParams, SchemaEcriture) {
                    return SchemaEcriture.get({id : $stateParams.id}).$promise;
                }],
                previousState: ["$state", function ($state) {
                    var currentStateData = {
                        name: $state.current.name || 'schema-ecriture',
                        params: $state.params,
                        url: $state.href($state.current.name, $state.params)
                    };
                    return currentStateData;
                }]
            }
        })
        .state('schema-ecriture-detail.edit', {
            parent: 'schema-ecriture-detail',
            url: '/detail/edit',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/schema-ecriture/schema-ecriture-dialog.html',
                    controller: 'SchemaEcritureDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: ['SchemaEcriture', function(SchemaEcriture) {
                            return SchemaEcriture.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('^', {}, { reload: false });
                }, function() {
                    $state.go('^');
                });
            }]
        })
        .state('schema-ecriture.new', {
            parent: 'schema-ecriture',
            url: '/new',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/schema-ecriture/schema-ecriture-dialog.html',
                    controller: 'SchemaEcritureDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: function () {
                            return {
                                nomSchema: null,
                                codeGroovy: null,
                                id: null
                            };
                        }
                    }
                }).result.then(function() {
                    $state.go('schema-ecriture', null, { reload: 'schema-ecriture' });
                }, function() {
                    $state.go('schema-ecriture');
                });
            }]
        })
        .state('schema-ecriture.edit', {
            parent: 'schema-ecriture',
            url: '/{id}/edit',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/schema-ecriture/schema-ecriture-dialog.html',
                    controller: 'SchemaEcritureDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: ['SchemaEcriture', function(SchemaEcriture) {
                            return SchemaEcriture.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('schema-ecriture', null, { reload: 'schema-ecriture' });
                }, function() {
                    $state.go('^');
                });
            }]
        })
        .state('schema-ecriture.delete', {
            parent: 'schema-ecriture',
            url: '/{id}/delete',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/schema-ecriture/schema-ecriture-delete-dialog.html',
                    controller: 'SchemaEcritureDeleteController',
                    controllerAs: 'vm',
                    size: 'md',
                    resolve: {
                        entity: ['SchemaEcriture', function(SchemaEcriture) {
                            return SchemaEcriture.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('schema-ecriture', null, { reload: 'schema-ecriture' });
                }, function() {
                    $state.go('^');
                });
            }]
        });
    }

})();
