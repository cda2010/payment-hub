(function() {
    'use strict';

    angular
        .module('ptiApp')
        .config(stateConfig);

    stateConfig.$inject = ['$stateProvider'];

    function stateConfig($stateProvider) {
        $stateProvider
        .state('composant', {
            parent: 'entity',
            url: '/composant',
            data: {
                authorities: ['ROLE_USER'],
                pageTitle: 'ptiApp.composant.home.title'
            },
            views: {
                'content@app': {
                    templateUrl: 'app/entities/composant/composants.html',
                    controller: 'ComposantController',
                    controllerAs: 'vm'
                }
            },
            resolve: {
                translatePartialLoader: ['$translate', '$translatePartialLoader', function ($translate, $translatePartialLoader) {
                    $translatePartialLoader.addPart('composant');
                    $translatePartialLoader.addPart('typeComposant');
                    $translatePartialLoader.addPart('global');
                    return $translate.refresh();
                }]
            }
        })
        .state('composant-detail', {
            parent: 'composant',
            url: '/composant/{id}',
            data: {
                authorities: ['ROLE_USER'],
                pageTitle: 'ptiApp.composant.detail.title'
            },
            views: {
                'content@app': {
                    templateUrl: 'app/entities/composant/composant-detail.html',
                    controller: 'ComposantDetailController',
                    controllerAs: 'vm'
                }
            },
            resolve: {
                translatePartialLoader: ['$translate', '$translatePartialLoader', function ($translate, $translatePartialLoader) {
                    $translatePartialLoader.addPart('composant');
                    $translatePartialLoader.addPart('typeComposant');
                    return $translate.refresh();
                }],
                entity: ['$stateParams', 'Composant', function($stateParams, Composant) {
                    return Composant.get({id : $stateParams.id}).$promise;
                }],
                previousState: ["$state", function ($state) {
                    var currentStateData = {
                        name: $state.current.name || 'composant',
                        params: $state.params,
                        url: $state.href($state.current.name, $state.params)
                    };
                    return currentStateData;
                }]
            }
        })
        .state('composant-detail.edit', {
            parent: 'composant-detail',
            url: '/detail/edit',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/composant/composant-dialog.html',
                    controller: 'ComposantDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'md',
                    resolve: {
                        entity: ['Composant', function(Composant) {
                            return Composant.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('^', {}, { reload: false });
                }, function() {
                    $state.go('^');
                });
            }]
        })
        .state('composant.new', {
            parent: 'composant',
            url: '/new',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/composant/composant-dialog.html',
                    controller: 'ComposantDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'md',
                    resolve: {
                        entity: function () {
                            return {
                                type: null,
                                label: null,
                                code: null,
                                typeComposant: null,
                                id: null,
                                bouton:{id:null,action:null},
                                liste:{id:null,multiple:false,options:null}
                            };
                        }
                    }
                }).result.then(function() {
                    $state.go('composant', null, { reload: 'composant' });
                }, function() {
                    $state.go('composant');
                });
            }]
        })
        .state('composant.edit', {
            parent: 'composant',
            url: '/{id}/edit',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/composant/composant-dialog.html',
                    controller: 'ComposantDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'md',
                    resolve: {
                        entity: ['Composant', function(Composant) {
                            return Composant.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('composant', null, { reload: 'composant' });
                }, function() {
                    $state.go('^');
                });
            }]
        })
        .state('composant.delete', {
            parent: 'composant',
            url: '/{id}/delete',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/composant/composant-delete-dialog.html',
                    controller: 'ComposantDeleteController',
                    controllerAs: 'vm',
                    size: 'md',
                    resolve: {
                        entity: ['Composant', function(Composant) {
                            return Composant.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('composant', null, { reload: 'composant' });
                }, function() {
                    $state.go('^');
                });
            }]
        });
    }

})();
