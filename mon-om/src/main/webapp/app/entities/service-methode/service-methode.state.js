(function() {
    'use strict';

    angular
        .module('omnicanalApp')
        .config(stateConfig);

    stateConfig.$inject = ['$stateProvider'];

    function stateConfig($stateProvider) {
        $stateProvider
        .state('service-methode', {
            parent: 'entity',
            url: '/service-methode/{idService}',
            data: {
                authorities: ['ROLE_USER'],
                pageTitle: 'ServiceMethodes'
            },
            views: {
                'content@app': {
                    templateUrl: 'app/entities/service-methode/service-methodes.html',
                    controller: 'ServiceMethodeController',
                    controllerAs: 'vm'
                }
            },
            resolve: {
                
            }
        })
        .state('service-methode-detail', {
            parent: 'service-methode',
            url: '/service-methode/{id}',
            data: {
                authorities: ['ROLE_USER'],
                pageTitle: 'ServiceMethode'
            },
            views: {
                'content@': {
                    templateUrl: 'app/entities/service-methode/service-methode-detail.html',
                    controller: 'ServiceMethodeDetailController',
                    controllerAs: 'vm'
                }
            },
            resolve: {
                entity: ['$stateParams', 'ServiceMethode', function($stateParams, ServiceMethode) {
                    return ServiceMethode.get({id : $stateParams.id}).$promise;
                }],
                previousState: ["$state", function ($state) {
                    var currentStateData = {
                        name: $state.current.name || 'service-methode',
                        params: $state.params,
                        url: $state.href($state.current.name, $state.params)
                    };
                    return currentStateData;
                }]
            }
        })
        .state('service-methode-detail.edit', {
            parent: 'service-methode-detail',
            url: '/detail/edit',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/service-methode/service-methode-dialog.html',
                    controller: 'ServiceMethodeDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: ['ServiceMethode', function(ServiceMethode) {
                            return ServiceMethode.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('^', {}, { reload: false });
                }, function() {
                    $state.go('^');
                });
            }]
        })
        .state('service-methode.new', {
            parent: 'service-methode',
            url: '/new',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/service-methode/service-methode-dialog.html',
                    controller: 'ServiceMethodeDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: function () {
                            return {
                                id: null,
                                service:{id:$stateParams.idService}
                            };
                        }
                    }
                }).result.then(function() {
                    $state.go('service-methode', null, { reload: 'service-methode' });
                }, function() {
                    $state.go('service-methode');
                });
            }]
        })
        .state('service-methode.edit', {
            parent: 'service-methode',
            url: '/{id}/edit',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/service-methode/service-methode-dialog.html',
                    controller: 'ServiceMethodeDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: ['ServiceMethode', function(ServiceMethode) {
                            return ServiceMethode.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('service-methode', null, { reload: 'service-methode' });
                }, function() {
                    $state.go('^');
                });
            }]
        })
        .state('service-methode.delete', {
            parent: 'service-methode',
            url: '/{id}/delete',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/service-methode/service-methode-delete-dialog.html',
                    controller: 'ServiceMethodeDeleteController',
                    controllerAs: 'vm',
                    size: 'md',
                    resolve: {
                        entity: ['ServiceMethode', function(ServiceMethode) {
                            return ServiceMethode.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('service-methode', null, { reload: 'service-methode' });
                }, function() {
                    $state.go('^');
                });
            }]
        });
    }

})();
