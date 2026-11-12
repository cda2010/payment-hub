(function() {
    'use strict';

    angular
        .module('ptiApp')
        .config(stateConfig);

    stateConfig.$inject = ['$stateProvider'];

    function stateConfig($stateProvider) {
        $stateProvider
        .state('chargement-compte', {
            parent: 'entity',
            url: '/chargement-compte',
            data: {
                authorities: ['ROLE_USER'],
                pageTitle: 'ptiApp.chargementCompte.home.title'
            },
            views: {
                'content@app': {
                    templateUrl: 'app/entities/chargement-compte/chargement-comptes.html',
                    controller: 'ChargementCompteController',
                    controllerAs: 'vm'
                }
            },
            resolve: {
                translatePartialLoader: ['$translate', '$translatePartialLoader', function ($translate, $translatePartialLoader) {
                    $translatePartialLoader.addPart('chargementCompte');
                    $translatePartialLoader.addPart('global');
                    return $translate.refresh();
                }]
            }
        })
        .state('chargement-compte-detail', {
            parent: 'chargement-compte',
            url: '/chargement-compte/{id}',
            data: {
                authorities: ['ROLE_USER'],
                pageTitle: 'ptiApp.chargementCompte.detail.title'
            },
            views: {
                'content@app': {
                    templateUrl: 'app/entities/chargement-compte/chargement-compte-detail.html',
                    controller: 'ChargementCompteDetailController',
                    controllerAs: 'vm'
                }
            },
            resolve: {
                translatePartialLoader: ['$translate', '$translatePartialLoader', function ($translate, $translatePartialLoader) {
                    $translatePartialLoader.addPart('chargementCompte');
                    return $translate.refresh();
                }],
                entity: ['$stateParams', 'ChargementCompte', function($stateParams, ChargementCompte) {
                    return ChargementCompte.get({id : $stateParams.id}).$promise;
                }],
                previousState: ["$state", function ($state) {
                    var currentStateData = {
                        name: $state.current.name || 'chargement-compte',
                        params: $state.params,
                        url: $state.href($state.current.name, $state.params)
                    };
                    return currentStateData;
                }]
            }
        })
        .state('chargement-compte-detail.edit', {
            parent: 'chargement-compte-detail',
            url: '/detail/edit',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/chargement-compte/chargement-compte-dialog.html',
                    controller: 'ChargementCompteDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: ['ChargementCompte', function(ChargementCompte) {
                            return ChargementCompte.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('^', {}, { reload: false });
                }, function() {
                    $state.go('^');
                });
            }]
        })
        .state('chargement-compte.new', {
            parent: 'chargement-compte',
            url: '/new',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/chargement-compte/chargement-compte-dialog.html',
                    controller: 'ChargementCompteDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: function () {
                            return {
                                nom_fichier: null,
                                id: null
                            };
                        }
                    }
                }).result.then(function() {
                    $state.go('chargement-compte', null, { reload: 'chargement-compte' });
                }, function() {
                    $state.go('chargement-compte');
                });
            }]
        })
        .state('chargement-compte.edit', {
            parent: 'chargement-compte',
            url: '/{id}/edit',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/chargement-compte/chargement-compte-dialog.html',
                    controller: 'ChargementCompteDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: ['ChargementCompte', function(ChargementCompte) {
                            return ChargementCompte.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('chargement-compte', null, { reload: 'chargement-compte' });
                }, function() {
                    $state.go('^');
                });
            }]
        })
        .state('chargement-compte.delete', {
            parent: 'chargement-compte',
            url: '/{id}/delete',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/chargement-compte/chargement-compte-delete-dialog.html',
                    controller: 'ChargementCompteDeleteController',
                    controllerAs: 'vm',
                    size: 'md',
                    resolve: {
                        entity: ['ChargementCompte', function(ChargementCompte) {
                            return ChargementCompte.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('chargement-compte', null, { reload: 'chargement-compte' });
                }, function() {
                    $state.go('^');
                });
            }]
        });
    }

})();
