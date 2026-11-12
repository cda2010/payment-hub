(function() {
    'use strict';

    angular
        .module('ptiApp')
        .config(stateConfig);

    stateConfig.$inject = ['$stateProvider'];

    function stateConfig($stateProvider) {
        $stateProvider
        .state('ligne-chargement-transfert', {
            parent: 'entity',
            url: '/ligne-chargement-transfert',
            data: {
                authorities: ['ROLE_USER'],
                pageTitle: 'ptiApp.ligneChargementTransfert.home.title'
            },
            views: {
                'content@': {
                    templateUrl: 'app/entities/ligne-chargement-transfert/ligne-chargement-transferts.html',
                    controller: 'LigneChargementTransfertController',
                    controllerAs: 'vm'
                }
            },
            resolve: {
                translatePartialLoader: ['$translate', '$translatePartialLoader', function ($translate, $translatePartialLoader) {
                    $translatePartialLoader.addPart('ligneChargementTransfert');
                    $translatePartialLoader.addPart('global');
                    return $translate.refresh();
                }]
            }
        })
        .state('ligne-chargement-transfert-detail', {
            parent: 'ligne-chargement-transfert',
            url: '/ligne-chargement-transfert/{id}',
            data: {
                authorities: ['ROLE_USER'],
                pageTitle: 'ptiApp.ligneChargementTransfert.detail.title'
            },
            views: {
                'content@': {
                    templateUrl: 'app/entities/ligne-chargement-transfert/ligne-chargement-transfert-detail.html',
                    controller: 'LigneChargementTransfertDetailController',
                    controllerAs: 'vm'
                }
            },
            resolve: {
                translatePartialLoader: ['$translate', '$translatePartialLoader', function ($translate, $translatePartialLoader) {
                    $translatePartialLoader.addPart('ligneChargementTransfert');
                    return $translate.refresh();
                }],
                entity: ['$stateParams', 'LigneChargementTransfert', function($stateParams, LigneChargementTransfert) {
                    return LigneChargementTransfert.get({id : $stateParams.id}).$promise;
                }],
                previousState: ["$state", function ($state) {
                    var currentStateData = {
                        name: $state.current.name || 'ligne-chargement-transfert',
                        params: $state.params,
                        url: $state.href($state.current.name, $state.params)
                    };
                    return currentStateData;
                }]
            }
        })
        .state('ligne-chargement-transfert-detail.edit', {
            parent: 'ligne-chargement-transfert-detail',
            url: '/detail/edit',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/ligne-chargement-transfert/ligne-chargement-transfert-dialog.html',
                    controller: 'LigneChargementTransfertDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: ['LigneChargementTransfert', function(LigneChargementTransfert) {
                            return LigneChargementTransfert.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('^', {}, { reload: false });
                }, function() {
                    $state.go('^');
                });
            }]
        })
        .state('ligne-chargement-transfert.new', {
            parent: 'ligne-chargement-transfert',
            url: '/new',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/ligne-chargement-transfert/ligne-chargement-transfert-dialog.html',
                    controller: 'LigneChargementTransfertDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: function () {
                            return {
                                nomLigne: null,
                                id: null
                            };
                        }
                    }
                }).result.then(function() {
                    $state.go('ligne-chargement-transfert', null, { reload: 'ligne-chargement-transfert' });
                }, function() {
                    $state.go('ligne-chargement-transfert');
                });
            }]
        })
        .state('ligne-chargement-transfert.edit', {
            parent: 'ligne-chargement-transfert',
            url: '/{id}/edit',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/ligne-chargement-transfert/ligne-chargement-transfert-dialog.html',
                    controller: 'LigneChargementTransfertDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: ['LigneChargementTransfert', function(LigneChargementTransfert) {
                            return LigneChargementTransfert.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('ligne-chargement-transfert', null, { reload: 'ligne-chargement-transfert' });
                }, function() {
                    $state.go('^');
                });
            }]
        })
        .state('ligne-chargement-transfert.delete', {
            parent: 'ligne-chargement-transfert',
            url: '/{id}/delete',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/ligne-chargement-transfert/ligne-chargement-transfert-delete-dialog.html',
                    controller: 'LigneChargementTransfertDeleteController',
                    controllerAs: 'vm',
                    size: 'md',
                    resolve: {
                        entity: ['LigneChargementTransfert', function(LigneChargementTransfert) {
                            return LigneChargementTransfert.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('ligne-chargement-transfert', null, { reload: 'ligne-chargement-transfert' });
                }, function() {
                    $state.go('^');
                });
            }]
        });
    }

})();
