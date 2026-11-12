(function() {
    'use strict';

    angular
        .module('omnicanalPresentationApp')
        .config(stateConfig);

    stateConfig.$inject = ['$stateProvider'];

    function stateConfig($stateProvider) {
        $stateProvider
        .state('etiquette', {
            parent: 'entity',
            url: '/etiquette',
            data: {
                authorities: ['ROLE_USER'],
                pageTitle: 'Etiquettes'
            },
            views: {
                'content@': {
                    templateUrl: 'app/entities/etiquette/etiquettes.html',
                    controller: 'EtiquetteController',
                    controllerAs: 'vm'
                }
            },
            resolve: {
            }
        })
        .state('etiquette-detail', {
            parent: 'etiquette',
            url: '/etiquette/{id}',
            data: {
                authorities: ['ROLE_USER'],
                pageTitle: 'Etiquette'
            },
            views: {
                'content@': {
                    templateUrl: 'app/entities/etiquette/etiquette-detail.html',
                    controller: 'EtiquetteDetailController',
                    controllerAs: 'vm'
                }
            },
            resolve: {
                entity: ['$stateParams', 'Etiquette', function($stateParams, Etiquette) {
                    return Etiquette.get({id : $stateParams.id}).$promise;
                }],
                previousState: ["$state", function ($state) {
                    var currentStateData = {
                        name: $state.current.name || 'etiquette',
                        params: $state.params,
                        url: $state.href($state.current.name, $state.params)
                    };
                    return currentStateData;
                }]
            }
        })
        .state('etiquette-detail.edit', {
            parent: 'etiquette-detail',
            url: '/detail/edit',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/etiquette/etiquette-dialog.html',
                    controller: 'EtiquetteDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: ['Etiquette', function(Etiquette) {
                            return Etiquette.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('^', {}, { reload: false });
                }, function() {
                    $state.go('^');
                });
            }]
        })
        .state('etiquette.new', {
            parent: 'etiquette',
            url: '/new',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/etiquette/etiquette-dialog.html',
                    controller: 'EtiquetteDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: function () {
                            return {
                                codeEtiquette: null,
                                id: null
                            };
                        }
                    }
                }).result.then(function() {
                    $state.go('etiquette', null, { reload: 'etiquette' });
                }, function() {
                    $state.go('etiquette');
                });
            }]
        })
        .state('etiquette.edit', {
            parent: 'etiquette',
            url: '/{id}/edit',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/etiquette/etiquette-dialog.html',
                    controller: 'EtiquetteDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: ['Etiquette', function(Etiquette) {
                            return Etiquette.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('etiquette', null, { reload: 'etiquette' });
                }, function() {
                    $state.go('^');
                });
            }]
        })
        .state('etiquette.delete', {
            parent: 'etiquette',
            url: '/{id}/delete',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/etiquette/etiquette-delete-dialog.html',
                    controller: 'EtiquetteDeleteController',
                    controllerAs: 'vm',
                    size: 'md',
                    resolve: {
                        entity: ['Etiquette', function(Etiquette) {
                            return Etiquette.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('etiquette', null, { reload: 'etiquette' });
                }, function() {
                    $state.go('^');
                });
            }]
        });
    }

})();
