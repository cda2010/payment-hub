(function() {
    'use strict';

    angular
        .module('omnicanalPresentationApp')
        .config(stateConfig);

    stateConfig.$inject = ['$stateProvider'];

    function stateConfig($stateProvider) {
        $stateProvider
        .state('pays', {
            parent: 'entity',
            url: '/pays',
            data: {
                authorities: ['ROLE_USER'],
                pageTitle: 'Pays'
            },
            views: {
                'content@': {
                    templateUrl: 'app/entities/pays/pays.html',
                    controller: 'PaysController',
                    controllerAs: 'vm'
                }
            },
            resolve: {
            }
        })
        .state('pays-detail', {
            parent: 'pays',
            url: '/pays/{id}',
            data: {
                authorities: ['ROLE_USER'],
                pageTitle: 'Pays'
            },
            views: {
                'content@': {
                    templateUrl: 'app/entities/pays/pays-detail.html',
                    controller: 'PaysDetailController',
                    controllerAs: 'vm'
                }
            },
            resolve: {
                entity: ['$stateParams', 'Pays', function($stateParams, Pays) {
                    return Pays.get({id : $stateParams.id}).$promise;
                }],
                previousState: ["$state", function ($state) {
                    var currentStateData = {
                        name: $state.current.name || 'pays',
                        params: $state.params,
                        url: $state.href($state.current.name, $state.params)
                    };
                    return currentStateData;
                }]
            }
        })
        .state('pays-detail.edit', {
            parent: 'pays-detail',
            url: '/detail/edit',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/pays/pays-dialog.html',
                    controller: 'PaysDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: ['Pays', function(Pays) {
                            return Pays.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('^', {}, { reload: false });
                }, function() {
                    $state.go('^');
                });
            }]
        })
        .state('pays.new', {
            parent: 'pays',
            url: '/new',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/pays/pays-dialog.html',
                    controller: 'PaysDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: function () {
                            return {
                                nomPays: null,
                                id: null
                            };
                        }
                    }
                }).result.then(function() {
                    $state.go('pays', null, { reload: 'pays' });
                }, function() {
                    $state.go('pays');
                });
            }]
        })
        .state('pays.edit', {
            parent: 'pays',
            url: '/{id}/edit',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/pays/pays-dialog.html',
                    controller: 'PaysDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: ['Pays', function(Pays) {
                            return Pays.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('pays', null, { reload: 'pays' });
                }, function() {
                    $state.go('^');
                });
            }]
        })
        .state('pays.delete', {
            parent: 'pays',
            url: '/{id}/delete',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/pays/pays-delete-dialog.html',
                    controller: 'PaysDeleteController',
                    controllerAs: 'vm',
                    size: 'md',
                    resolve: {
                        entity: ['Pays', function(Pays) {
                            return Pays.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('pays', null, { reload: 'pays' });
                }, function() {
                    $state.go('^');
                });
            }]
        });
    }

})();
