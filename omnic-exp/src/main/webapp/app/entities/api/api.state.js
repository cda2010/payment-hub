(function() {
    'use strict';

    angular
        .module('omnicanalPresentationApp')
        .config(stateConfig);

    stateConfig.$inject = ['$stateProvider'];

    function stateConfig($stateProvider) {
        $stateProvider
        .state('api', {
            parent: 'entity',
            url: '/api',
            data: {
                authorities: ['ROLE_USER'],
                pageTitle: 'Apis'
            },
            views: {
                'content@': {
                    templateUrl: 'app/entities/api/apis.html',
                    controller: 'ApiController',
                    controllerAs: 'vm'
                }
            },
            resolve: {
            }
        })
        .state('api-detail', {
            parent: 'api',
            url: '/api/{id}',
            data: {
                authorities: ['ROLE_USER'],
                pageTitle: 'Api'
            },
            views: {
                'content@': {
                    templateUrl: 'app/entities/api/api-detail.html',
                    controller: 'ApiDetailController',
                    controllerAs: 'vm'
                }
            },
            resolve: {
                entity: ['$stateParams', 'Api', function($stateParams, Api) {
                    return Api.get({id : $stateParams.id}).$promise;
                }],
                previousState: ["$state", function ($state) {
                    var currentStateData = {
                        name: $state.current.name || 'api',
                        params: $state.params,
                        url: $state.href($state.current.name, $state.params)
                    };
                    return currentStateData;
                }]
            }
        })
        .state('api-detail.edit', {
            parent: 'api-detail',
            url: '/detail/edit',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/api/api-dialog.html',
                    controller: 'ApiDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: ['Api', function(Api) {
                            return Api.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('^', {}, { reload: false });
                }, function() {
                    $state.go('^');
                });
            }]
        })
        .state('api.new', {
            parent: 'api',
            url: '/new',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/api/api-dialog.html',
                    controller: 'ApiDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: function () {
                            return {
                                nomApi: null,
                                type: null,
                                host: null,
                                port: null,
                                codeApi: null,
                                id: null
                            };
                        }
                    }
                }).result.then(function() {
                    $state.go('api', null, { reload: 'api' });
                }, function() {
                    $state.go('api');
                });
            }]
        })
        .state('api.edit', {
            parent: 'api',
            url: '/{id}/edit',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/api/api-dialog.html',
                    controller: 'ApiDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: ['Api', function(Api) {
                            return Api.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('api', null, { reload: 'api' });
                }, function() {
                    $state.go('^');
                });
            }]
        })
        .state('api.delete', {
            parent: 'api',
            url: '/{id}/delete',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/api/api-delete-dialog.html',
                    controller: 'ApiDeleteController',
                    controllerAs: 'vm',
                    size: 'md',
                    resolve: {
                        entity: ['Api', function(Api) {
                            return Api.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('api', null, { reload: 'api' });
                }, function() {
                    $state.go('^');
                });
            }]
        });
    }

})();
