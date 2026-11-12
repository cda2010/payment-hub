(function() {
    'use strict';

    angular
        .module('omnicanalPresentationApp')
        .config(stateConfig);

    stateConfig.$inject = ['$stateProvider'];

    function stateConfig($stateProvider) {
        $stateProvider
        .state('traitement', {
            parent: 'entity',
            url: '/traitement',
            data: {
                authorities: ['ROLE_USER'],
                pageTitle: 'Traitements'
            },
            views: {
                'content@': {
                    templateUrl: 'app/entities/traitement/traitements.html',
                    controller: 'TraitementController',
                    controllerAs: 'vm'
                }
            },
            resolve: {
            }
        })
        .state('traitement-detail', {
            parent: 'traitement',
            url: '/traitement/{id}',
            data: {
                authorities: ['ROLE_USER'],
                pageTitle: 'Traitement'
            },
            views: {
                'content@': {
                    templateUrl: 'app/entities/traitement/traitement-detail.html',
                    controller: 'TraitementDetailController',
                    controllerAs: 'vm'
                }
            },
            resolve: {
                entity: ['$stateParams', 'Traitement', function($stateParams, Traitement) {
                    return Traitement.get({id : $stateParams.id}).$promise;
                }],
                previousState: ["$state", function ($state) {
                    var currentStateData = {
                        name: $state.current.name || 'traitement',
                        params: $state.params,
                        url: $state.href($state.current.name, $state.params)
                    };
                    return currentStateData;
                }]
            }
        })
        .state('traitement-detail.edit', {
            parent: 'traitement-detail',
            url: '/detail/edit',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/traitement/traitement-dialog.html',
                    controller: 'TraitementDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: ['Traitement', function(Traitement) {
                            return Traitement.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('^', {}, { reload: false });
                }, function() {
                    $state.go('^');
                });
            }]
        })
        .state('traitement.new', {
            parent: 'traitement',
            url: '/new',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/traitement/transfertName.html',
                    controller: 'TransfertTestController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: function () {
                            return {
                                code: null,
                                id: null
                            };
                        }
                    }
                }).result.then(function() {
                    $state.go('traitement', null, { reload: 'traitement' });
                }, function() {
                    $state.go('traitement');
                });
            }]
        })
        .state('traitement.edit', {
            parent: 'traitement',
            url: '/{id}/edit',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/traitement/traitement-dialog.html',
                    controller: 'TraitementDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: ['Traitement', function(Traitement) {
                            return Traitement.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('traitement', null, { reload: 'traitement' });
                }, function() {
                    $state.go('^');
                });
            }]
        })
        .state('traitement.delete', {
            parent: 'traitement',
            url: '/{id}/delete',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/traitement/traitement-delete-dialog.html',
                    controller: 'TraitementDeleteController',
                    controllerAs: 'vm',
                    size: 'md',
                    resolve: {
                        entity: ['Traitement', function(Traitement) {
                            return Traitement.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('traitement', null, { reload: 'traitement' });
                }, function() {
                    $state.go('^');
                });
            }]
        });
    }

})();
