(function() {
    'use strict';

    angular
        .module('ptiApp')
        .config(stateConfig);

    stateConfig.$inject = ['$stateProvider'];

    function stateConfig($stateProvider) {
        $stateProvider
        .state('bouton', {
            parent: 'entity',
            url: '/bouton',
            data: {
                authorities: ['ROLE_USER'],
                pageTitle: 'ptiApp.bouton.home.title'
            },
            views: {
                'content@app': {
                    templateUrl: 'app/entities/bouton/boutons.html',
                    controller: 'BoutonController',
                    controllerAs: 'vm'
                }
            },
            resolve: {
                translatePartialLoader: ['$translate', '$translatePartialLoader', function ($translate, $translatePartialLoader) {
                    $translatePartialLoader.addPart('bouton');
                    $translatePartialLoader.addPart('global');
                    return $translate.refresh();
                }]
            }
        })
        .state('bouton-detail', {
            parent: 'bouton',
            url: '/bouton/{id}',
            data: {
                authorities: ['ROLE_USER'],
                pageTitle: 'ptiApp.bouton.detail.title'
            },
            views: {
                'content@app': {
                    templateUrl: 'app/entities/bouton/bouton-detail.html',
                    controller: 'BoutonDetailController',
                    controllerAs: 'vm'
                }
            },
            resolve: {
                translatePartialLoader: ['$translate', '$translatePartialLoader', function ($translate, $translatePartialLoader) {
                    $translatePartialLoader.addPart('bouton');
                    return $translate.refresh();
                }],
                entity: ['$stateParams', 'Bouton', function($stateParams, Bouton) {
                    return Bouton.get({id : $stateParams.id}).$promise;
                }],
                previousState: ["$state", function ($state) {
                    var currentStateData = {
                        name: $state.current.name || 'bouton',
                        params: $state.params,
                        url: $state.href($state.current.name, $state.params)
                    };
                    return currentStateData;
                }]
            }
        })
        .state('bouton-detail.edit', {
            parent: 'bouton-detail',
            url: '/detail/edit',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/bouton/bouton-dialog.html',
                    controller: 'BoutonDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'md',
                    resolve: {
                        entity: ['Bouton', function(Bouton) {
                            return Bouton.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('^', {}, { reload: false });
                }, function() {
                    $state.go('^');
                });
            }]
        })
        .state('bouton.new', {
            parent: 'bouton',
            url: '/new',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/bouton/bouton-dialog.html',
                    controller: 'BoutonDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'md',
                    resolve: {
                        entity: function () {
                            return {
                                action: null,
                                id: null
                            };
                        }
                    }
                }).result.then(function() {
                    $state.go('bouton', null, { reload: 'bouton' });
                }, function() {
                    $state.go('bouton');
                });
            }]
        })
        .state('bouton.edit', {
            parent: 'bouton',
            url: '/{id}/edit',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/bouton/bouton-dialog.html',
                    controller: 'BoutonDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'md',
                    resolve: {
                        entity: ['Bouton', function(Bouton) {
                            return Bouton.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('bouton', null, { reload: 'bouton' });
                }, function() {
                    $state.go('^');
                });
            }]
        })
        .state('bouton.delete', {
            parent: 'bouton',
            url: '/{id}/delete',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/bouton/bouton-delete-dialog.html',
                    controller: 'BoutonDeleteController',
                    controllerAs: 'vm',
                    size: 'md',
                    resolve: {
                        entity: ['Bouton', function(Bouton) {
                            return Bouton.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('bouton', null, { reload: 'bouton' });
                }, function() {
                    $state.go('^');
                });
            }]
        });
    }

})();
