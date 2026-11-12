(function() {
    'use strict';

    angular
        .module('ptiApp')
        .config(stateConfig);

    stateConfig.$inject = ['$stateProvider'];

    function stateConfig($stateProvider) {
        $stateProvider
        .state('profil-acteur-tableau-frais', {
            parent: 'entity',
            url: '/profil-acteur-tableau-frais',
            data: {
                authorities: ['ROLE_USER'],
                pageTitle: 'ptiApp.profilActeurTableauFrais.home.title'
            },
            views: {
                'content@app': {
                    templateUrl: 'app/entities/profil-acteur-tableau-frais/profil-acteur-tableau-frais.html',
                    controller: 'ProfilActeurTableauFraisController',
                    controllerAs: 'vm'
                }
            },
            resolve: {
                translatePartialLoader: ['$translate', '$translatePartialLoader', function ($translate, $translatePartialLoader) {
                    $translatePartialLoader.addPart('profilActeurTableauFrais');
                    $translatePartialLoader.addPart('global');
                    return $translate.refresh();
                }]
            }
        })
        .state('profil-acteur-tableau-frais-detail', {
            parent: 'profil-acteur-tableau-frais',
            url: '/profil-acteur-tableau-frais/{id}',
            data: {
                authorities: ['ROLE_USER'],
                pageTitle: 'ptiApp.profilActeurTableauFrais.detail.title'
            },
            views: {
                'content@': {
                    templateUrl: 'app/entities/profil-acteur-tableau-frais/profil-acteur-tableau-frais-detail.html',
                    controller: 'ProfilActeurTableauFraisDetailController',
                    controllerAs: 'vm'
                }
            },
            resolve: {
                translatePartialLoader: ['$translate', '$translatePartialLoader', function ($translate, $translatePartialLoader) {
                    $translatePartialLoader.addPart('profilActeurTableauFrais');
                    return $translate.refresh();
                }],
                entity: ['$stateParams', 'ProfilActeurTableauFrais', function($stateParams, ProfilActeurTableauFrais) {
                    return ProfilActeurTableauFrais.get({id : $stateParams.id}).$promise;
                }],
                previousState: ["$state", function ($state) {
                    var currentStateData = {
                        name: $state.current.name || 'profil-acteur-tableau-frais',
                        params: $state.params,
                        url: $state.href($state.current.name, $state.params)
                    };
                    return currentStateData;
                }]
            }
        })
        .state('profil-acteur-tableau-frais-detail.edit', {
            parent: 'profil-acteur-tableau-frais-detail',
            url: '/detail/edit',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/profil-acteur-tableau-frais/profil-acteur-tableau-frais-dialog.html',
                    controller: 'ProfilActeurTableauFraisDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: ['ProfilActeurTableauFrais', function(ProfilActeurTableauFrais) {
                            return ProfilActeurTableauFrais.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('^', {}, { reload: false });
                }, function() {
                    $state.go('^');
                });
            }]
        })
        .state('profil-acteur-tableau-frais.new', {
            parent: 'profil-acteur-tableau-frais',
            url: '/new',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/profil-acteur-tableau-frais/profil-acteur-tableau-frais-dialog.html',
                    controller: 'ProfilActeurTableauFraisDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: function () {
                            return {
                                id: null
                            };
                        }
                    }
                }).result.then(function() {
                    $state.go('profil-acteur-tableau-frais', null, { reload: 'profil-acteur-tableau-frais' });
                }, function() {
                    $state.go('profil-acteur-tableau-frais');
                });
            }]
        })
        .state('profil-acteur-tableau-frais.edit', {
            parent: 'profil-acteur-tableau-frais',
            url: '/{id}/edit',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/profil-acteur-tableau-frais/profil-acteur-tableau-frais-dialog.html',
                    controller: 'ProfilActeurTableauFraisDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: ['ProfilActeurTableauFrais', function(ProfilActeurTableauFrais) {
                            return ProfilActeurTableauFrais.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('profil-acteur-tableau-frais', null, { reload: 'profil-acteur-tableau-frais' });
                }, function() {
                    $state.go('^');
                });
            }]
        })
        .state('profil-acteur-tableau-frais.delete', {
            parent: 'profil-acteur-tableau-frais',
            url: '/{id}/delete',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/profil-acteur-tableau-frais/profil-acteur-tableau-frais-delete-dialog.html',
                    controller: 'ProfilActeurTableauFraisDeleteController',
                    controllerAs: 'vm',
                    size: 'md',
                    resolve: {
                        entity: ['ProfilActeurTableauFrais', function(ProfilActeurTableauFrais) {
                            return ProfilActeurTableauFrais.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('profil-acteur-tableau-frais', null, { reload: 'profil-acteur-tableau-frais' });
                }, function() {
                    $state.go('^');
                });
            }]
        });
    }

})();
