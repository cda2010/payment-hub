(function() {
    'use strict';

    angular
        .module('omnicanalPresentationApp')
        .config(stateConfig);

    stateConfig.$inject = ['$stateProvider'];

    function stateConfig($stateProvider) {
        $stateProvider
        .state('methode', {
            parent: 'entity',
            url: '/methode/api/{idapi}',
            data: {
                authorities: ['ROLE_USER'],
                pageTitle: 'Methodes'
            },
            views: {
                'content@': {
                    templateUrl: 'app/entities/methode/methodes.html',
                    controller: 'MethodeController',
                    controllerAs: 'vm'
                }
            },
            resolve: {
            }
        })
        .state('methode-detail', {
            parent: 'methode',
            url: '/methode/{id}',
            data: {
                authorities: ['ROLE_USER'],
                pageTitle: 'Methode'
            },
            views: {
                'content@': {
                    templateUrl: 'app/entities/methode/methode-detail.html',
                    controller: 'MethodeDetailController',
                    controllerAs: 'vm'
                }
            },
            resolve: {
                entity: ['$stateParams', 'Methode', function($stateParams, Methode) {
                    return Methode.get({id : $stateParams.id}).$promise;
                }],
                previousState: ["$state", function ($state) {
                    var currentStateData = {
                        name: $state.current.name || 'methode',
                        params: $state.params,
                        url: $state.href($state.current.name, $state.params)
                    };
                    return currentStateData;
                }]
            }
        })
        .state('methode-detail.edit', {
            parent: 'methode-detail',
            url: '/detail/edit',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/methode/methode-dialog.html',
                    controller: 'MethodeDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: ['Methode', function(Methode) {
                            return Methode.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('^', {}, { reload: false });
                }, function() {
                    $state.go('^');
                });
            }]
        })
        .state('methode.new', {
            parent: 'methode',
            url: '/new',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/methode/methode-dialog.html',
                    controller: 'MethodeDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: function () {
                            return {
                                codeMethode: null,
                                id: null,
                                api:{id:$stateParams.idapi}
                            };
                        }
                    }
                }).result.then(function() {
                    $state.go('methode', null, { reload: 'methode' });
                }, function() {
                    $state.go('methode');
                });
            }]
        })
        .state('methode.edit', {
            parent: 'methode',
            url: '/{id}/edit',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/methode/methode-dialog.html',
                    controller: 'MethodeDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: ['Methode', function(Methode) {
                            return Methode.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('methode', null, { reload: 'methode' });
                }, function() {
                    $state.go('^');
                });
            }]
        })
        .state('methode.delete', {
            parent: 'methode',
            url: '/{id}/delete',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/methode/methode-delete-dialog.html',
                    controller: 'MethodeDeleteController',
                    controllerAs: 'vm',
                    size: 'md',
                    resolve: {
                        entity: ['Methode', function(Methode) {
                            return Methode.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('methode', null, { reload: 'methode' });
                }, function() {
                    $state.go('^');
                });
            }]
        });
    }

})();
