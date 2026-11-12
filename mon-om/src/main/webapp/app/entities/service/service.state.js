(function() {
    'use strict';

    angular
        .module('omnicanalApp')
        .config(stateConfig);

    stateConfig.$inject = ['$stateProvider'];

    function stateConfig($stateProvider) {
        $stateProvider
        .state('service', {
            parent: 'entity',
            url: '/service',
            data: {
                authorities: ['ROLE_USER'],
                pageTitle: 'Services'
            },
            views: {
                'content@app': {
                    templateUrl: 'app/entities/service/services.html',
                    controller: 'ServiceController',
                    controllerAs: 'vm'
                }
            },
            resolve: {
            }
        })
        .state('service-detail', {
            parent: 'service',
            url: '/service/{id}',
            data: {
                authorities: ['ROLE_USER'],
                pageTitle: 'Service'
            },
            views: {
                'content@app': {
                    templateUrl: 'app/entities/service/service-detail.html',
                    controller: 'ServiceDetailController',
                    controllerAs: 'vm'
                }
            },
            resolve: {
                entity: ['$stateParams', 'Service', function($stateParams, Service) {
                    return Service.get({id : $stateParams.id}).$promise;
                }],
                previousState: ["$state", function ($state) {
                    var currentStateData = {
                        name: $state.current.name || 'service',
                        params: $state.params,
                        url: $state.href($state.current.name, $state.params)
                    };
                    return currentStateData;
                }]
            }
        })
        .state('service-detail.edit', {
            parent: 'service-detail',
            url: '/detail/edit',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/service/service-dialog.html',
                    controller: 'ServiceDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: ['Service', function(Service) {
                            return Service.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('^', {}, { reload: false });
                }, function() {
                    $state.go('^');
                });
            }]
        })
        .state('service.new', {
            parent: 'service',
            url: '/new',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/service/service-dialog.html',
                    controller: 'ServiceDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: function () {
                            return {
                                codeService: null,
                                nomService: null,
                                id: null
                            };
                        }
                    }
                }).result.then(function() {
                    $state.go('service', null, { reload: 'service' });
                }, function() {
                    $state.go('service');
                });
            }]
        })
        .state('service.edit', {
            parent: 'service',
            url: '/{id}/edit',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/service/service-dialog.html',
                    controller: 'ServiceDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: ['Service', function(Service) {
                            return Service.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('service', null, { reload: 'service' });
                }, function() {
                    $state.go('^');
                });
            }]
        })
        .state('service.delete', {
            parent: 'service',
            url: '/{id}/delete',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/service/service-delete-dialog.html',
                    controller: 'ServiceDeleteController',
                    controllerAs: 'vm',
                    size: 'md',
                    resolve: {
                        entity: ['Service', function(Service) {
                            return Service.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('service', null, { reload: 'service' });
                }, function() {
                    $state.go('^');
                });
            }]
        });
    }

})();
