(function() {
    'use strict';

    angular
        .module('omnicanalApp')
        .config(stateConfig);

    stateConfig.$inject = ['$stateProvider'];

    function stateConfig($stateProvider) {
        $stateProvider
        .state('rest', {
            parent: 'entity',
            url: '/rest',
            data: {
                authorities: ['ROLE_USER'],
                pageTitle: 'Rests'
            },
            views: {
                'content@': {
                    templateUrl: 'app/entities/rest/rests.html',
                    controller: 'RestController',
                    controllerAs: 'vm'
                }
            },
            resolve: {
            }
        })
        .state('rest-detail', {
            parent: 'rest',
            url: '/rest/{id}',
            data: {
                authorities: ['ROLE_USER'],
                pageTitle: 'Rest'
            },
            views: {
                'content@': {
                    templateUrl: 'app/entities/rest/rest-detail.html',
                    controller: 'RestDetailController',
                    controllerAs: 'vm'
                }
            },
            resolve: {
                entity: ['$stateParams', 'Rest', function($stateParams, Rest) {
                    return Rest.get({id : $stateParams.id}).$promise;
                }],
                previousState: ["$state", function ($state) {
                    var currentStateData = {
                        name: $state.current.name || 'rest',
                        params: $state.params,
                        url: $state.href($state.current.name, $state.params)
                    };
                    return currentStateData;
                }]
            }
        })
        .state('rest-detail.edit', {
            parent: 'rest-detail',
            url: '/detail/edit',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/rest/rest-dialog.html',
                    controller: 'RestDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: ['Rest', function(Rest) {
                            return Rest.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('^', {}, { reload: false });
                }, function() {
                    $state.go('^');
                });
            }]
        })
        .state('rest.new', {
            parent: 'rest',
            url: '/new',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/rest/rest-dialog.html',
                    controller: 'RestDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: function () {
                            return {
                                typeAppel: null,
                                id: null
                            };
                        }
                    }
                }).result.then(function() {
                    $state.go('rest', null, { reload: 'rest' });
                }, function() {
                    $state.go('rest');
                });
            }]
        })
        .state('rest.edit', {
            parent: 'rest',
            url: '/{id}/edit',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/rest/rest-dialog.html',
                    controller: 'RestDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: ['Rest', function(Rest) {
                            return Rest.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('rest', null, { reload: 'rest' });
                }, function() {
                    $state.go('^');
                });
            }]
        })
        .state('rest.delete', {
            parent: 'rest',
            url: '/{id}/delete',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/rest/rest-delete-dialog.html',
                    controller: 'RestDeleteController',
                    controllerAs: 'vm',
                    size: 'md',
                    resolve: {
                        entity: ['Rest', function(Rest) {
                            return Rest.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('rest', null, { reload: 'rest' });
                }, function() {
                    $state.go('^');
                });
            }]
        });
    }

})();
