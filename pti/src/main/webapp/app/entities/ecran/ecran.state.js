(function() {
    'use strict';

    angular
        .module('ptiApp')
        .config(stateConfig);

    stateConfig.$inject = ['$stateProvider'];

    function stateConfig($stateProvider) {
        $stateProvider
        .state('ecran', {
            parent: 'entity',
            url: '/ecran',
            data: {
                authorities: ['ROLE_USER'],
                pageTitle: 'ptiApp.ecran.home.title'
            },
            views: {
                'content@app': {
                    templateUrl: 'app/entities/ecran/ecrans.html',
                    controller: 'EcranController',
                    controllerAs: 'vm'
                }
            },
            resolve: {
                translatePartialLoader: ['$translate', '$translatePartialLoader', function ($translate, $translatePartialLoader) {
                    $translatePartialLoader.addPart('ecran');
                    $translatePartialLoader.addPart('global');
                    return $translate.refresh();
                }]
            }
        })
        .state('ecran-detail', {
            parent: 'ecran',
            url: '/ecran/{id}',
            data: {
                authorities: ['ROLE_USER'],
                pageTitle: 'ptiApp.ecran.detail.title'
            },
            views: {
                'content@app': {
                    templateUrl: 'app/entities/ecran/ecran-detail.html',
                    controller: 'EcranDetailController',
                    controllerAs: 'vm'
                }
            },
            resolve: {
                translatePartialLoader: ['$translate', '$translatePartialLoader', function ($translate, $translatePartialLoader) {
                    $translatePartialLoader.addPart('ecran');
                    return $translate.refresh();
                }],
                entity: ['$stateParams', 'Ecran', function($stateParams, Ecran) {
                    return Ecran.get({id : $stateParams.id}).$promise;
                }],
                previousState: ["$state", function ($state) {
                    var currentStateData = {
                        name: $state.current.name || 'ecran',
                        params: $state.params,
                        url: $state.href($state.current.name, $state.params)
                    };
                    return currentStateData;
                }]
            }
        })
        .state('ecran-detail.edit', {
            parent: 'ecran-detail',
            url: '/detail/edit',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/ecran/ecran-dialog.html',
                    controller: 'EcranDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'md',
                    resolve: {
                        entity: ['Ecran', function(Ecran) {
                            return Ecran.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('^', {}, { reload: false });
                }, function() {
                    $state.go('^');
                });
            }]
        })
        .state('ecran.new', {
            parent: 'ecran',
            url: '/new',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/ecran/ecran-dialog.html',
                    controller: 'EcranDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'md',
                    resolve: {
                        entity: function () {
                            return {
                                numeroOrdre: null,
                                id: null
                            };
                        }
                    }
                }).result.then(function() {
                    $state.go('ecran', null, { reload: 'ecran' });
                }, function() {
                    $state.go('ecran');
                });
            }]
        })
        .state('ecran.edit', {
            parent: 'ecran',
            url: '/{id}/edit',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/ecran/ecran-dialog.html',
                    controller: 'EcranDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'md',
                    resolve: {
                        entity: ['Ecran', function(Ecran) {
                            return Ecran.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('ecran', null, { reload: 'ecran' });
                }, function() {
                    $state.go('^');
                });
            }]
        })
        .state('ecran.delete', {
            parent: 'ecran',
            url: '/{id}/delete',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/ecran/ecran-delete-dialog.html',
                    controller: 'EcranDeleteController',
                    controllerAs: 'vm',
                    size: 'md',
                    resolve: {
                        entity: ['Ecran', function(Ecran) {
                            return Ecran.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('ecran', null, { reload: 'ecran' });
                }, function() {
                    $state.go('^');
                });
            }]
        });
    }

})();
