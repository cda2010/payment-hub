(function() {
    'use strict';

    angular
        .module('omnicanalPresentationApp')
        .config(stateConfig);

    stateConfig.$inject = ['$stateProvider'];

    function stateConfig($stateProvider) {
        $stateProvider
        .state('service-traitement', {
            parent: 'entity',
            url: '/service-traitement',
            data: {
                authorities: ['ROLE_USER'],
                pageTitle: 'ServiceTraitements'
            },
            views: {
                'content@': {
                    templateUrl: 'app/entities/service-traitement/service-traitements.html',
                    controller: 'ServiceTraitementController',
                    controllerAs: 'vm'
                }
            },
            resolve: {
            }
        })
        .state('service-traitement-detail', {
            parent: 'service-traitement',
            url: '/service-traitement/{id}',
            data: {
                authorities: ['ROLE_USER'],
                pageTitle: 'ServiceTraitement'
            },
            views: {
                'content@': {
                    templateUrl: 'app/entities/service-traitement/service-traitement-detail.html',
                    controller: 'ServiceTraitementDetailController',
                    controllerAs: 'vm'
                }
            },
            resolve: {
                entity: ['$stateParams', 'ServiceTraitement', function($stateParams, ServiceTraitement) {
                    return ServiceTraitement.get({id : $stateParams.id}).$promise;
                }],
                previousState: ["$state", function ($state) {
                    var currentStateData = {
                        name: $state.current.name || 'service-traitement',
                        params: $state.params,
                        url: $state.href($state.current.name, $state.params)
                    };
                    return currentStateData;
                }]
            }
        })
        .state('service-traitement-detail.edit', {
            parent: 'service-traitement-detail',
            url: '/detail/edit',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/service-traitement/service-traitement-dialog.html',
                    controller: 'ServiceTraitementDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: ['ServiceTraitement', function(ServiceTraitement) {
                            return ServiceTraitement.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('^', {}, { reload: false });
                }, function() {
                    $state.go('^');
                });
            }]
        })
        .state('service-traitement.new', {
            parent: 'service-traitement',
            url: '/new',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/service-traitement/service-traitement-dialog.html',
                    controller: 'ServiceTraitementDialogController',
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
                    $state.go('service-traitement', null, { reload: 'service-traitement' });
                }, function() {
                    $state.go('service-traitement');
                });
            }]
        })
        .state('service-traitement.edit', {
            parent: 'service-traitement',
            url: '/{id}/edit',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/service-traitement/service-traitement-dialog.html',
                    controller: 'ServiceTraitementDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: ['ServiceTraitement', function(ServiceTraitement) {
                            return ServiceTraitement.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('service-traitement', null, { reload: 'service-traitement' });
                }, function() {
                    $state.go('^');
                });
            }]
        })
        .state('service-traitement.delete', {
            parent: 'service-traitement',
            url: '/{id}/delete',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/service-traitement/service-traitement-delete-dialog.html',
                    controller: 'ServiceTraitementDeleteController',
                    controllerAs: 'vm',
                    size: 'md',
                    resolve: {
                        entity: ['ServiceTraitement', function(ServiceTraitement) {
                            return ServiceTraitement.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('service-traitement', null, { reload: 'service-traitement' });
                }, function() {
                    $state.go('^');
                });
            }]
        });
    }

})();
