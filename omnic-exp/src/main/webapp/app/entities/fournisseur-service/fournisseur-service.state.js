(function() {
    'use strict';

    angular
        .module('omnicanalPresentationApp')
        .config(stateConfig);

    stateConfig.$inject = ['$stateProvider'];

    function stateConfig($stateProvider) {
        $stateProvider
        .state('fournisseur-service', {
            parent: 'entity',
            url: '/fournisseur-service',
            data: {
                authorities: ['ROLE_USER'],
                pageTitle: 'FournisseurServices'
            },
            views: {
                'content@': {
                    templateUrl: 'app/entities/fournisseur-service/fournisseur-services.html',
                    controller: 'FournisseurServiceController',
                    controllerAs: 'vm'
                }
            },
            resolve: {
            }
        })
        .state('fournisseur-service-detail', {
            parent: 'fournisseur-service',
            url: '/fournisseur-service/{id}',
            data: {
                authorities: ['ROLE_USER'],
                pageTitle: 'FournisseurService'
            },
            views: {
                'content@': {
                    templateUrl: 'app/entities/fournisseur-service/fournisseur-service-detail.html',
                    controller: 'FournisseurServiceDetailController',
                    controllerAs: 'vm'
                }
            },
            resolve: {
                entity: ['$stateParams', 'FournisseurService', function($stateParams, FournisseurService) {
                    return FournisseurService.get({id : $stateParams.id}).$promise;
                }],
                previousState: ["$state", function ($state) {
                    var currentStateData = {
                        name: $state.current.name || 'fournisseur-service',
                        params: $state.params,
                        url: $state.href($state.current.name, $state.params)
                    };
                    return currentStateData;
                }]
            }
        })
        .state('fournisseur-service-detail.edit', {
            parent: 'fournisseur-service-detail',
            url: '/detail/edit',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/fournisseur-service/fournisseur-service-dialog.html',
                    controller: 'FournisseurServiceDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: ['FournisseurService', function(FournisseurService) {
                            return FournisseurService.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('^', {}, { reload: false });
                }, function() {
                    $state.go('^');
                });
            }]
        })
        .state('fournisseur-service.new', {
            parent: 'fournisseur-service',
            url: '/new',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/fournisseur-service/fournisseur-service-dialog.html',
                    controller: 'FournisseurServiceDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: function () {
                            return {
                                codeFse: null,
                                nom: null,
                                statut: null,
                                groupe: null,
                                id: null
                            };
                        }
                    }
                }).result.then(function() {
                    $state.go('fournisseur-service', null, { reload: 'fournisseur-service' });
                }, function() {
                    $state.go('fournisseur-service');
                });
            }]
        })
        .state('fournisseur-service.edit', {
            parent: 'fournisseur-service',
            url: '/{id}/edit',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/fournisseur-service/fournisseur-service-dialog.html',
                    controller: 'FournisseurServiceDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: ['FournisseurService', function(FournisseurService) {
                            return FournisseurService.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('fournisseur-service', null, { reload: 'fournisseur-service' });
                }, function() {
                    $state.go('^');
                });
            }]
        })
        .state('fournisseur-service.delete', {
            parent: 'fournisseur-service',
            url: '/{id}/delete',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/fournisseur-service/fournisseur-service-delete-dialog.html',
                    controller: 'FournisseurServiceDeleteController',
                    controllerAs: 'vm',
                    size: 'md',
                    resolve: {
                        entity: ['FournisseurService', function(FournisseurService) {
                            return FournisseurService.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('fournisseur-service', null, { reload: 'fournisseur-service' });
                }, function() {
                    $state.go('^');
                });
            }]
        });
    }

})();
