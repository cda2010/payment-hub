(function() {
    'use strict';

    angular
        .module('omnicanalPresentationApp')
        .config(stateConfig);

    stateConfig.$inject = ['$stateProvider'];

    function stateConfig($stateProvider) {
        $stateProvider
        .state('parametre-utilisateur', {
            parent: 'entity',
            url: '/parametre-utilisateur/interface/{idinterface}',
            data: {
                authorities: ['ROLE_USER'],
                pageTitle: 'ParametreUtiliateurs'
            },
            views: {
                'content@': {
                    templateUrl: 'app/entities/parametre-utilisateur/parametre-utiliateurs.html',
                    controller: 'ParametreUtiliateurController',
                    controllerAs: 'vm'
                }
            },
            resolve: {
            }
        })
        .state('parametre-utilisateur-detail', {
            parent: 'parametre-utilisateur',
            url: '/parametre-utiliateur/{id}',
            data: {
                authorities: ['ROLE_USER'],
                pageTitle: 'ParametreUtiliateur'
            },
            views: {
                'content@': {
                    templateUrl: 'app/entities/parametre-utilisateur/parametre-utiliateur-detail.html',
                    controller: 'ParametreUtiliateurDetailController',
                    controllerAs: 'vm'
                }
            },
            resolve: {
                entity: ['$stateParams', 'ParametreUtiliateur', function($stateParams, ParametreUtiliateur) {
                    return ParametreUtiliateur.get({id : $stateParams.id}).$promise;
                }],
                previousState: ["$state", function ($state) {
                    var currentStateData = {
                        name: $state.current.name || 'parametre-utiliateur',
                        params: $state.params,
                        url: $state.href($state.current.name, $state.params)
                    };
                    return currentStateData;
                }]
            }
        })
        .state('parametre-utilisateur-detail.edit', {
            parent: 'parametre-utilisateur-detail',
            url: '/detail/edit',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/parametre-utilisateur/parametre-utiliateur-dialog.html',
                    controller: 'ParametreUtiliateurDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: ['ParametreUtiliateur', function(ParametreUtiliateur) {
                            return ParametreUtiliateur.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('^', {}, { reload: false });
                }, function() {
                    $state.go('^');
                });
            }]
        })
        .state('parametre-utilisateur.new', {
            parent: 'parametre-utilisateur',
            url: '/new',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/parametre-utilisateur/parametre-utiliateur-dialog.html',
                    controller: 'ParametreUtiliateurDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: function () {
                            return {
                                nomParametreUtil: null,
                                valeur: null,
                                id: null,
                                interfaceUtilisateur:{id:$stateParams.idinterface}
                            };
                        }
                    }
                }).result.then(function() {
                    $state.go('parametre-utilisateur', null, { reload: 'parametre-utilisateur' });
                }, function() {
                    $state.go('parametre-utilisateur');
                });
            }]
        })
        .state('parametre-utilisateur.edit', {
            parent: 'parametre-utilisateur',
            url: '/{id}/edit',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/parametre-utilisateur/parametre-utiliateur-dialog.html',
                    controller: 'ParametreUtiliateurDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: ['ParametreUtiliateur', function(ParametreUtiliateur) {
                            return ParametreUtiliateur.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('parametre-utilisateur', null, { reload: 'parametre-utilisateur' });
                }, function() {
                    $state.go('^');
                });
            }]
        })
        .state('parametre-utilisateur.delete', {
            parent: 'parametre-utilisateur',
            url: '/{id}/delete',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/parametre-utilisateur/parametre-utiliateur-delete-dialog.html',
                    controller: 'ParametreUtiliateurDeleteController',
                    controllerAs: 'vm',
                    size: 'md',
                    resolve: {
                        entity: ['ParametreUtiliateur', function(ParametreUtiliateur) {
                            return ParametreUtiliateur.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('parametre-utilisateur', null, { reload: 'parametre-utilisateur' });
                }, function() {
                    $state.go('^');
                });
            }]
        });
    }

})();
