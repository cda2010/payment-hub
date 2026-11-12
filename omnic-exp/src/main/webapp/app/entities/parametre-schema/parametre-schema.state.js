(function() {
    'use strict';

    angular
        .module('omnicanalPresentationApp')
        .config(stateConfig);

    stateConfig.$inject = ['$stateProvider'];

    function stateConfig($stateProvider) {
        $stateProvider
        .state('parametre-schema', {
            parent: 'entity',
            url: '/parametre-schema/ligne/{idligne}/{idSchema}',
            data: {
                authorities: ['ROLE_USER'],
                pageTitle: 'ParametreSchema'
            },
            views: {
                'content@': {
                    templateUrl: 'app/entities/parametre-schema/parametre-schema.html',
                    controller: 'ParametreSchemaController',
                    controllerAs: 'vm'
                }
            },
            resolve: {
            }
        })
        .state('parametre-schema-detail', {
            parent: 'parametre-schema',
            url: '/parametre-schema/{id}',
            data: {
                authorities: ['ROLE_USER'],
                pageTitle: 'ParametreSchema'
            },
            views: {
                'content@': {
                    templateUrl: 'app/entities/parametre-schema/parametre-schema-detail.html',
                    controller: 'ParametreSchemaDetailController',
                    controllerAs: 'vm'
                }
            },
            resolve: {
                entity: ['$stateParams', 'ParametreSchema', function($stateParams, ParametreSchema) {
                    return ParametreSchema.get({id : $stateParams.id}).$promise;
                }],
                previousState: ["$state", function ($state) {
                    var currentStateData = {
                        name: $state.current.name || 'parametre-schema',
                        params: $state.params,
                        url: $state.href($state.current.name, $state.params)
                    };
                    return currentStateData;
                }]
            }
        })
        .state('parametre-schema-detail.edit', {
            parent: 'parametre-schema-detail',
            url: '/detail/edit',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/parametre-schema/parametre-schema-dialog.html',
                    controller: 'ParametreSchemaDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: ['ParametreSchema', function(ParametreSchema) {
                            return ParametreSchema.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('^', {}, { reload: false });
                }, function() {
                    $state.go('^');
                });
            }]
        })
        .state('parametre-schema.new', {
            parent: 'parametre-schema',
            url: '/new',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/parametre-schema/parametre-schema-dialog.html',
                    controller: 'ParametreSchemaDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: function () {
                            return {
                                nomParametreUtil: null,
                                valeur: null,
                                id: null,
                                ligneSchemaEcriture:{id:$stateParams.idligne}
                            };
                        }
                    }
                }).result.then(function() {
                    $state.go('parametre-schema', null, { reload: 'parametre-schema' });
                }, function() {
                    $state.go('parametre-schema');
                });
            }]
        })
        .state('parametre-schema.edit', {
            parent: 'parametre-schema',
            url: '/{id}/edit',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/parametre-schema/parametre-schema-dialog.html',
                    controller: 'ParametreSchemaDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: ['ParametreSchema', function(ParametreSchema) {
                            return ParametreSchema.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('parametre-schema', null, { reload: 'parametre-schema' });
                }, function() {
                    $state.go('^');
                });
            }]
        })
        .state('parametre-schema.delete', {
            parent: 'parametre-schema',
            url: '/{id}/delete',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/parametre-schema/parametre-schema-delete-dialog.html',
                    controller: 'ParametreSchemaDeleteController',
                    controllerAs: 'vm',
                    size: 'md',
                    resolve: {
                        entity: ['ParametreSchema', function(ParametreSchema) {
                            return ParametreSchema.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('parametre-schema', null, { reload: 'parametre-schema' });
                }, function() {
                    $state.go('^');
                });
            }]
        });
    }

})();
