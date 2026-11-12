(function() {
    'use strict';

    angular
        .module('omnicanalApp')
        .config(stateConfig);

    stateConfig.$inject = ['$stateProvider'];

    function stateConfig($stateProvider) {
        $stateProvider
        .state('webservice', {
            parent: 'entity',
            url: '/webservice',
            data: {
                authorities: ['ROLE_USER'],
                pageTitle: 'Webservices'
            },
            views: {
                'content@': {
                    templateUrl: 'app/entities/webservice/webservices.html',
                    controller: 'WebserviceController',
                    controllerAs: 'vm'
                }
            },
            resolve: {
            }
        })
        .state('webservice-detail', {
            parent: 'webservice',
            url: '/webservice/{id}',
            data: {
                authorities: ['ROLE_USER'],
                pageTitle: 'Webservice'
            },
            views: {
                'content@': {
                    templateUrl: 'app/entities/webservice/webservice-detail.html',
                    controller: 'WebserviceDetailController',
                    controllerAs: 'vm'
                }
            },
            resolve: {
                entity: ['$stateParams', 'Webservice', function($stateParams, Webservice) {
                    return Webservice.get({id : $stateParams.id}).$promise;
                }],
                previousState: ["$state", function ($state) {
                    var currentStateData = {
                        name: $state.current.name || 'webservice',
                        params: $state.params,
                        url: $state.href($state.current.name, $state.params)
                    };
                    return currentStateData;
                }]
            }
        })
        .state('webservice-detail.edit', {
            parent: 'webservice-detail',
            url: '/detail/edit',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/webservice/webservice-dialog.html',
                    controller: 'WebserviceDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: ['Webservice', function(Webservice) {
                            return Webservice.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('^', {}, { reload: false });
                }, function() {
                    $state.go('^');
                });
            }]
        })
        .state('webservice.new', {
            parent: 'webservice',
            url: '/new',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/webservice/webservice-dialog.html',
                    controller: 'WebserviceDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: function () {
                            return {
                                endPoint: null,
                                useSsl: null,
                                id: null
                            };
                        }
                    }
                }).result.then(function() {
                    $state.go('webservice', null, { reload: 'webservice' });
                }, function() {
                    $state.go('webservice');
                });
            }]
        })
        .state('webservice.edit', {
            parent: 'webservice',
            url: '/{id}/edit',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/webservice/webservice-dialog.html',
                    controller: 'WebserviceDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: ['Webservice', function(Webservice) {
                            return Webservice.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('webservice', null, { reload: 'webservice' });
                }, function() {
                    $state.go('^');
                });
            }]
        })
        .state('webservice.delete', {
            parent: 'webservice',
            url: '/{id}/delete',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/webservice/webservice-delete-dialog.html',
                    controller: 'WebserviceDeleteController',
                    controllerAs: 'vm',
                    size: 'md',
                    resolve: {
                        entity: ['Webservice', function(Webservice) {
                            return Webservice.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('webservice', null, { reload: 'webservice' });
                }, function() {
                    $state.go('^');
                });
            }]
        });
    }

})();
