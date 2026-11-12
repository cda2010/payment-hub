(function() {
    'use strict';

    angular
        .module('ptiApp')
        .config(stateConfig);

    stateConfig.$inject = ['$stateProvider'];

    function stateConfig($stateProvider) {
        $stateProvider
        .state('chargement-transfert', {
            parent: 'entity',
            url: '/chargement-transfert',
            data: {
                authorities: ['ROLE_USER'],
                pageTitle: 'ptiApp.chargementTransfert.home.title'
            },
            views: {
                'content@app': {
                    templateUrl: 'app/entities/chargement-transfert/chargement-transferts.html',
                    controller: 'ChargementTransfertController',
                    controllerAs: 'vm'
                }
            },
            resolve: {
                translatePartialLoader: ['$translate', '$translatePartialLoader', function ($translate, $translatePartialLoader) {
                    $translatePartialLoader.addPart('chargementTransfert');
                    $translatePartialLoader.addPart('global');
                    return $translate.refresh();
                }]
            }
        })
        .state('chargement-transfert-valide', {
            parent: 'entity',
            url: '/chargement-transfert-valide',
            data: {
                authorities: ['ROLE_USER'],
                pageTitle: 'ptiApp.chargementTransfert.home.title'
            },
            views: {
                'content@app': {
                    templateUrl: 'app/entities/chargement-transfert/chargement-transferts.html',
                    controller: 'ChargementTransfertController',
                    controllerAs: 'vm'
                }
            },
            resolve: {
                translatePartialLoader: ['$translate', '$translatePartialLoader', function ($translate, $translatePartialLoader) {
                    $translatePartialLoader.addPart('chargementTransfert');
                    $translatePartialLoader.addPart('global');
                    return $translate.refresh();
                }]
            }
        })
        .state('chargement-transfert-detail', {
            parent: 'chargement-transfert',
            url: '/chargement-transfert/{id}',
            data: {
                authorities: ['ROLE_USER'],
                pageTitle: 'ptiApp.chargementTransfert.detail.title'
            },
            views: {
                'content@app': {
                    templateUrl: 'app/entities/chargement-transfert/chargement-transfert-detail.html',
                    controller: 'ChargementTransfertDetailController',
                    controllerAs: 'vm'
                }
            },
            resolve: {
                translatePartialLoader: ['$translate', '$translatePartialLoader', function ($translate, $translatePartialLoader) {
                    $translatePartialLoader.addPart('chargementTransfert');
                    return $translate.refresh();
                }],
                entity: ['$stateParams', 'ChargementTransfert', function($stateParams, ChargementTransfert) {
                    return ChargementTransfert.get({id : $stateParams.id}).$promise;
                }],
                previousState: ["$state", function ($state) {
                    var currentStateData = {
                        name: $state.current.name || 'chargement-transfert',
                        params: $state.params,
                        url: $state.href($state.current.name, $state.params)
                    };
                    return currentStateData;
                }]
            }
        })

        .state('chargement-transfert.new', {
            parent: 'chargement-transfert',
            url: '/new',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/chargement-transfert/chargement-transfert-dialog.html',
                    controller: 'ChargementTransfertDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: function () {
                            return {
                                nomFichier: null,
                                montantTotal: null,
                                id: null
                            };
                        }
                    }
                }).result.then(function() {
                    $state.go('chargement-transfert', null, { reload: 'chargement-transfert' });
                }, function() {
                    $state.go('chargement-transfert');
                });
            }]
        })
        .state('chargement-transfert.edit', {
            parent: 'chargement-transfert',
            url: '/{id}/edit',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/chargement-transfert/chargement-transfert-dialog.html',
                    controller: 'ChargementTransfertDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: ['ChargementTransfert', function(ChargementTransfert) {
                            return ChargementTransfert.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('chargement-transfert', null, { reload: 'chargement-transfert' });
                }, function() {
                    $state.go('^');
                });
            }]
        })
        .state('chargement-transfert.delete', {
            parent: 'chargement-transfert',
            url: '/{id}/delete',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/chargement-transfert/chargement-transfert-delete-dialog.html',
                    controller: 'ChargementTransfertDeleteController',
                    controllerAs: 'vm',
                    size: 'md',
                    resolve: {
                        entity: ['ChargementTransfert', function(ChargementTransfert) {
                            return ChargementTransfert.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('chargement-transfert', null, { reload: 'chargement-transfert' });
                }, function() {
                    $state.go('^');
                });
            }]
        });
    }

})();
