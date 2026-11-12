(function() {
    'use strict';

    angular
        .module('ptiApp')
        .config(stateConfig);

    stateConfig.$inject = ['$stateProvider'];

    function stateConfig($stateProvider) {
        $stateProvider
        .state('commerciale', {
            parent: 'entity',
            url: '/commerciale',
            data: {
                authorities: ['ROLE_USER'],
                pageTitle: 'ptiApp.commerciale.home.title'
            },
            views: {
                'content@app': {
                    templateUrl: 'app/entities/commerciale/commerciales.html',
                    controller: 'CommercialeController',
                    controllerAs: 'vm'
                }
            },
            resolve: {
                translatePartialLoader: ['$translate', '$translatePartialLoader', function ($translate, $translatePartialLoader) {
                    $translatePartialLoader.addPart('commerciale');
                    $translatePartialLoader.addPart('global');
                    return $translate.refresh();
                }]
            }
        })
        .state('commerciale-detail', {
            parent: 'commerciale',
            url: '/commerciale/{id}',
            data: {
                authorities: ['ROLE_USER'],
                pageTitle: 'ptiApp.commerciale.detail.title'
            },
            views: {
                'content@app': {
                    templateUrl: 'app/entities/commerciale/commerciale-detail.html',
                    controller: 'CommercialeDetailController',
                    controllerAs: 'vm'
                }
            },
            resolve: {
                translatePartialLoader: ['$translate', '$translatePartialLoader', function ($translate, $translatePartialLoader) {
                    $translatePartialLoader.addPart('commerciale');
                    return $translate.refresh();
                }],
                entity: ['$stateParams', 'Commerciale', function($stateParams, Commerciale) {
                    return Commerciale.get({id : $stateParams.id}).$promise;
                }],
                previousState: ["$state", function ($state) {
                    var currentStateData = {
                        name: $state.current.name || 'commerciale',
                        params: $state.params,
                        url: $state.href($state.current.name, $state.params)
                    };
                    return currentStateData;
                }]
            }
        })
        .state('commerciale-detail.edit', {
            parent: 'commerciale-detail',
            url: '/detail/edit',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/commerciale/commerciale-dialog.html',
                    controller: 'CommercialeDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'md',
                    resolve: {
                        entity: ['Commerciale', function(Commerciale) {
                            return Commerciale.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('^', {}, { reload: false });
                }, function() {
                    $state.go('^');
                });
            }]
        })
        .state('commerciale.new', {
            parent: 'commerciale',
            url: '/new',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/commerciale/commerciale-dialog.html',
                    controller: 'CommercialeDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'md',
                    resolve: {
                        entity: function () {
                            return {
                                nom: null,
                                prenom: null,
                                id: null
                            };
                        }
                    }
                }).result.then(function() {
                    $state.go('commerciale', null, { reload: 'commerciale' });
                }, function() {
                    $state.go('commerciale');
                });
            }]
        })
        .state('commerciale.edit', {
            parent: 'commerciale',
            url: '/{id}/edit',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/commerciale/commerciale-dialog.html',
                    controller: 'CommercialeDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'md',
                    resolve: {
                        entity: ['Commerciale', function(Commerciale) {
                            return Commerciale.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('commerciale', null, { reload: 'commerciale' });
                }, function() {
                    $state.go('^');
                });
            }]
        })
        .state('commerciale.delete', {
            parent: 'commerciale',
            url: '/{id}/delete',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/commerciale/commerciale-delete-dialog.html',
                    controller: 'CommercialeDeleteController',
                    controllerAs: 'vm',
                    size: 'md',
                    resolve: {
                        entity: ['Commerciale', function(Commerciale) {
                            return Commerciale.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('commerciale', null, { reload: 'commerciale' });
                }, function() {
                    $state.go('^');
                });
            }]
        });
    }

})();
