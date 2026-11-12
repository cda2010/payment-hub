(function() {
    'use strict';

    angular
        .module('omnicanalPresentationApp')
        .config(stateConfig);

    stateConfig.$inject = ['$stateProvider'];

    function stateConfig($stateProvider) {
        $stateProvider
        .state('ligne-schema-ecriture', {
            parent: 'entity',
            url: '/ligne-schema-ecriture/{idSchema}',
            data: {
                authorities: ['ROLE_USER'],
                pageTitle: 'LigneSchemaEcritures'
            },
            views: {
                'content@': {
                    templateUrl: 'app/entities/ligne-schema-ecriture/ligne-schema-ecritures.html',
                    controller: 'LigneSchemaEcritureController',
                    controllerAs: 'vm'
                }
            },
            resolve: {
            }
        })
        .state('ligne-schema-ecriture-detail', {
            parent: 'ligne-schema-ecriture',
            url: '/ligne-schema-ecriture/{id}',
            data: {
                authorities: ['ROLE_USER'],
                pageTitle: 'LigneSchemaEcriture'
            },
            views: {
                'content@': {
                    templateUrl: 'app/entities/ligne-schema-ecriture/ligne-schema-ecriture-detail.html',
                    controller: 'LigneSchemaEcritureDetailController',
                    controllerAs: 'vm'
                }
            },
            resolve: {
                entity: ['$stateParams', 'LigneSchemaEcriture', function($stateParams, LigneSchemaEcriture) {
                    return LigneSchemaEcriture.get({id : $stateParams.id}).$promise;
                }],
                previousState: ["$state", function ($state) {
                    var currentStateData = {
                        name: $state.current.name || 'ligne-schema-ecriture',
                        params: $state.params,
                        url: $state.href($state.current.name, $state.params)
                    };
                    return currentStateData;
                }]
            }
        })
        .state('ligne-schema-ecriture-detail.edit', {
            parent: 'ligne-schema-ecriture-detail',
            url: '/detail/edit',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/ligne-schema-ecriture/ligne-schema-ecriture-dialog.html',
                    controller: 'LigneSchemaEcritureDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: ['LigneSchemaEcriture', function(LigneSchemaEcriture) {
                            return LigneSchemaEcriture.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('^', {}, { reload: false });
                }, function() {
                    $state.go('^');
                });
            }]
        })
        .state('ligne-schema-ecriture.new', {
            parent: 'ligne-schema-ecriture',
            url: '/new',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/ligne-schema-ecriture/ligne-schema-ecriture-dialog.html',
                    controller: 'LigneSchemaEcritureDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: function () {
                            return {
                                id: null,
                                schemaEcriture:{id:$stateParams.idSchema}
                            };
                        }
                    }
                }).result.then(function() {
                    $state.go('ligne-schema-ecriture', null, { reload: 'ligne-schema-ecriture' });
                }, function() {
                    $state.go('ligne-schema-ecriture');
                });
            }]
        })
        .state('ligne-schema-ecriture.edit', {
            parent: 'ligne-schema-ecriture',
            url: '/{id}/edit',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/ligne-schema-ecriture/ligne-schema-ecriture-dialog.html',
                    controller: 'LigneSchemaEcritureDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: ['LigneSchemaEcriture', function(LigneSchemaEcriture) {
                            return LigneSchemaEcriture.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('ligne-schema-ecriture', null, { reload: 'ligne-schema-ecriture' });
                }, function() {
                    $state.go('^');
                });
            }]
        })
        .state('ligne-schema-ecriture.delete', {
            parent: 'ligne-schema-ecriture',
            url: '/{id}/delete',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/ligne-schema-ecriture/ligne-schema-ecriture-delete-dialog.html',
                    controller: 'LigneSchemaEcritureDeleteController',
                    controllerAs: 'vm',
                    size: 'md',
                    resolve: {
                        entity: ['LigneSchemaEcriture', function(LigneSchemaEcriture) {
                            return LigneSchemaEcriture.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('ligne-schema-ecriture', null, { reload: 'ligne-schema-ecriture' });
                }, function() {
                    $state.go('^');
                });
            }]
        });
    }

})();
