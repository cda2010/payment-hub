(function() {
    'use strict';

    angular
        .module('ptiApp')
        .config(stateConfig);

    stateConfig.$inject = ['$stateProvider'];

    function stateConfig($stateProvider) {
        $stateProvider
        .state('ligne-chargement', {
            parent: 'entity',
            url: '/ligne-chargement',
            data: {
                authorities: ['ROLE_USER'],
                pageTitle: 'ptiApp.ligneChargement.home.title'
            },
            views: {
                'content@': {
                    templateUrl: 'app/entities/ligne-chargement/ligne-chargements.html',
                    controller: 'LigneChargementController',
                    controllerAs: 'vm'
                }
            },
            resolve: {
                translatePartialLoader: ['$translate', '$translatePartialLoader', function ($translate, $translatePartialLoader) {
                    $translatePartialLoader.addPart('ligneChargement');
                    $translatePartialLoader.addPart('global');
                    return $translate.refresh();
                }]
            }
        })
        .state('ligne-chargement-detail', {
            parent: 'ligne-chargement',
            url: '/ligne-chargement/{id}',
            data: {
                authorities: ['ROLE_USER'],
                pageTitle: 'ptiApp.ligneChargement.detail.title'
            },
            views: {
                'content@': {
                    templateUrl: 'app/entities/ligne-chargement/ligne-chargement-detail.html',
                    controller: 'LigneChargementDetailController',
                    controllerAs: 'vm'
                }
            },
            resolve: {
                translatePartialLoader: ['$translate', '$translatePartialLoader', function ($translate, $translatePartialLoader) {
                    $translatePartialLoader.addPart('ligneChargement');
                    return $translate.refresh();
                }],
                entity: ['$stateParams', 'LigneChargement', function($stateParams, LigneChargement) {
                    return LigneChargement.get({id : $stateParams.id}).$promise;
                }],
                previousState: ["$state", function ($state) {
                    var currentStateData = {
                        name: $state.current.name || 'ligne-chargement',
                        params: $state.params,
                        url: $state.href($state.current.name, $state.params)
                    };
                    return currentStateData;
                }]
            }
        })
        .state('ligne-chargement-detail.edit', {
            parent: 'ligne-chargement-detail',
            url: '/detail/edit',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/ligne-chargement/ligne-chargement-dialog.html',
                    controller: 'LigneChargementDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: ['LigneChargement', function(LigneChargement) {
                            return LigneChargement.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('^', {}, { reload: false });
                }, function() {
                    $state.go('^');
                });
            }]
        })
        .state('ligne-chargement.new', {
            parent: 'ligne-chargement',
            url: '/new',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/ligne-chargement/ligne-chargement-dialog.html',
                    controller: 'LigneChargementDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: function () {
                            return {
                                nom: null,
                                prenom: null,
                                numeroCompte: null,
                                numeroTelephone: null,
                                montant: null,
                                error: null,
                                statutLigne: null,
                                id: null
                            };
                        }
                    }
                }).result.then(function() {
                    $state.go('ligne-chargement', null, { reload: 'ligne-chargement' });
                }, function() {
                    $state.go('ligne-chargement');
                });
            }]
        })
        .state('ligne-chargement.edit', {
            parent: 'ligne-chargement',
            url: '/{id}/edit',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/ligne-chargement/ligne-chargement-dialog.html',
                    controller: 'LigneChargementDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: ['LigneChargement', function(LigneChargement) {
                            return LigneChargement.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('ligne-chargement', null, { reload: 'ligne-chargement' });
                }, function() {
                    $state.go('^');
                });
            }]
        })
        .state('ligne-chargement.delete', {
            parent: 'ligne-chargement',
            url: '/{id}/delete',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/ligne-chargement/ligne-chargement-delete-dialog.html',
                    controller: 'LigneChargementDeleteController',
                    controllerAs: 'vm',
                    size: 'md',
                    resolve: {
                        entity: ['LigneChargement', function(LigneChargement) {
                            return LigneChargement.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('ligne-chargement', null, { reload: 'ligne-chargement' });
                }, function() {
                    $state.go('^');
                });
            }]
        });
    }

})();
