(function() {
    'use strict';

    angular
        .module('ptiApp')
        .config(stateConfig);

    stateConfig.$inject = ['$stateProvider'];

    function stateConfig($stateProvider) {
        $stateProvider
        .state('ligne-chargement-compte', {
            parent: 'entity',
            url: '/ligne-chargement-compte',
            data: {
                authorities: ['ROLE_USER'],
                pageTitle: 'ptiApp.ligneChargementCompte.home.title'
            },
            views: {
                'content@': {
                    templateUrl: 'app/entities/ligne-chargement-compte/ligne-chargement-comptes.html',
                    controller: 'LigneChargementCompteController',
                    controllerAs: 'vm'
                }
            },
            resolve: {
                translatePartialLoader: ['$translate', '$translatePartialLoader', function ($translate, $translatePartialLoader) {
                    $translatePartialLoader.addPart('ligneChargementCompte');
                    $translatePartialLoader.addPart('typeLignePersonne');
                    $translatePartialLoader.addPart('global');
                    return $translate.refresh();
                }]
            }
        })
        .state('ligne-chargement-compte-detail', {
            parent: 'ligne-chargement-compte',
            url: '/ligne-chargement-compte/{id}',
            data: {
                authorities: ['ROLE_USER'],
                pageTitle: 'ptiApp.ligneChargementCompte.detail.title'
            },
            views: {
                'content@': {
                    templateUrl: 'app/entities/ligne-chargement-compte/ligne-chargement-compte-detail.html',
                    controller: 'LigneChargementCompteDetailController',
                    controllerAs: 'vm'
                }
            },
            resolve: {
                translatePartialLoader: ['$translate', '$translatePartialLoader', function ($translate, $translatePartialLoader) {
                    $translatePartialLoader.addPart('ligneChargementCompte');
                    $translatePartialLoader.addPart('typeLignePersonne');
                    return $translate.refresh();
                }],
                entity: ['$stateParams', 'LigneChargementCompte', function($stateParams, LigneChargementCompte) {
                    return LigneChargementCompte.get({id : $stateParams.id}).$promise;
                }],
                previousState: ["$state", function ($state) {
                    var currentStateData = {
                        name: $state.current.name || 'ligne-chargement-compte',
                        params: $state.params,
                        url: $state.href($state.current.name, $state.params)
                    };
                    return currentStateData;
                }]
            }
        })
        .state('ligne-chargement-compte-detail.edit', {
            parent: 'ligne-chargement-compte-detail',
            url: '/detail/edit',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/ligne-chargement-compte/ligne-chargement-compte-dialog.html',
                    controller: 'LigneChargementCompteDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: ['LigneChargementCompte', function(LigneChargementCompte) {
                            return LigneChargementCompte.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('^', {}, { reload: false });
                }, function() {
                    $state.go('^');
                });
            }]
        })
        .state('ligne-chargement-compte.new', {
            parent: 'ligne-chargement-compte',
            url: '/new',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/ligne-chargement-compte/ligne-chargement-compte-dialog.html',
                    controller: 'LigneChargementCompteDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: function () {
                            return {
                                nom: null,
                                prenom: null,
                                telephone: null,
                                dateNaissance: null,
                                lieuNaissance: null,
                                nomMere: null,
                                personnePrevenir: null,
                                email: null,
                                adresse: null,
                                typePersonne: null,
                                id: null
                            };
                        }
                    }
                }).result.then(function() {
                    $state.go('ligne-chargement-compte', null, { reload: 'ligne-chargement-compte' });
                }, function() {
                    $state.go('ligne-chargement-compte');
                });
            }]
        })
        .state('ligne-chargement-compte.edit', {
            parent: 'ligne-chargement-compte',
            url: '/{id}/edit',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/ligne-chargement-compte/ligne-chargement-compte-dialog.html',
                    controller: 'LigneChargementCompteDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: ['LigneChargementCompte', function(LigneChargementCompte) {
                            return LigneChargementCompte.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('ligne-chargement-compte', null, { reload: 'ligne-chargement-compte' });
                }, function() {
                    $state.go('^');
                });
            }]
        })
        .state('ligne-chargement-compte.delete', {
            parent: 'ligne-chargement-compte',
            url: '/{id}/delete',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/ligne-chargement-compte/ligne-chargement-compte-delete-dialog.html',
                    controller: 'LigneChargementCompteDeleteController',
                    controllerAs: 'vm',
                    size: 'md',
                    resolve: {
                        entity: ['LigneChargementCompte', function(LigneChargementCompte) {
                            return LigneChargementCompte.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('ligne-chargement-compte', null, { reload: 'ligne-chargement-compte' });
                }, function() {
                    $state.go('^');
                });
            }]
        });
    }

})();
