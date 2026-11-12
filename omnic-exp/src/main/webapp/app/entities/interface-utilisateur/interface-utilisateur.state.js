(function() {
    'use strict';

    angular
        .module('omnicanalPresentationApp')
        .config(stateConfig);

    stateConfig.$inject = ['$stateProvider'];

    function stateConfig($stateProvider) {
        $stateProvider
        .state('interface-utilisateur', {
            parent: 'entity',
            url: '/interface-utilisateur',
            data: {
                authorities: ['ROLE_USER'],
                pageTitle: 'InterfaceUtilisateurs'
            },
            views: {
                'content@': {
                    templateUrl: 'app/entities/interface-utilisateur/interface-utilisateurs.html',
                    controller: 'InterfaceUtilisateurController',
                    controllerAs: 'vm'
                }
            },
            resolve: {
            }
        })
        .state('interface-utilisateur-detail', {
            parent: 'interface-utilisateur',
            url: '/interface-utilisateur/{id}',
            data: {
                authorities: ['ROLE_USER'],
                pageTitle: 'InterfaceUtilisateur'
            },
            views: {
                'content@': {
                    templateUrl: 'app/entities/interface-utilisateur/interface-utilisateur-detail.html',
                    controller: 'InterfaceUtilisateurDetailController',
                    controllerAs: 'vm'
                }
            },
            resolve: {
                entity: ['$stateParams', 'InterfaceUtilisateur', function($stateParams, InterfaceUtilisateur) {
                    return InterfaceUtilisateur.get({id : $stateParams.id}).$promise;
                }],
                previousState: ["$state", function ($state) {
                    var currentStateData = {
                        name: $state.current.name || 'interface-utilisateur',
                        params: $state.params,
                        url: $state.href($state.current.name, $state.params)
                    };
                    return currentStateData;
                }]
            }
        })
        .state('interface-utilisateur-detail.edit', {
            parent: 'interface-utilisateur-detail',
            url: '/detail/edit',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/interface-utilisateur/interface-utilisateur-dialog.html',
                    controller: 'InterfaceUtilisateurDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: ['InterfaceUtilisateur', function(InterfaceUtilisateur) {
                            return InterfaceUtilisateur.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('^', {}, { reload: false });
                }, function() {
                    $state.go('^');
                });
            }]
        })
        .state('interface-utilisateur.new', {
            parent: 'interface-utilisateur',
            url: '/new',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/interface-utilisateur/interface-utilisateur-dialog.html',
                    controller: 'InterfaceUtilisateurDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: function () {
                            return {
                                codeInterface: null,
                                nomInterface: null,
                                typeInterface: null,
                                id: null
                            };
                        }
                    }
                }).result.then(function() {
                    $state.go('interface-utilisateur', null, { reload: 'interface-utilisateur' });
                }, function() {
                    $state.go('interface-utilisateur');
                });
            }]
        })
        .state('interface-utilisateur.edit', {
            parent: 'interface-utilisateur',
            url: '/{id}/edit',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/interface-utilisateur/interface-utilisateur-dialog.html',
                    controller: 'InterfaceUtilisateurDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: ['InterfaceUtilisateur', function(InterfaceUtilisateur) {
                            return InterfaceUtilisateur.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('interface-utilisateur', null, { reload: 'interface-utilisateur' });
                }, function() {
                    $state.go('^');
                });
            }]
        })
        .state('interface-utilisateur.delete', {
            parent: 'interface-utilisateur',
            url: '/{id}/delete',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/interface-utilisateur/interface-utilisateur-delete-dialog.html',
                    controller: 'InterfaceUtilisateurDeleteController',
                    controllerAs: 'vm',
                    size: 'md',
                    resolve: {
                        entity: ['InterfaceUtilisateur', function(InterfaceUtilisateur) {
                            return InterfaceUtilisateur.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('interface-utilisateur', null, { reload: 'interface-utilisateur' });
                }, function() {
                    $state.go('^');
                });
            }]
        });
    }

})();
