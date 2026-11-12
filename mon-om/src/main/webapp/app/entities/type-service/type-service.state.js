(function() {
    'use strict';

    angular
        .module('omnicanalApp')
        .config(stateConfig);

    stateConfig.$inject = ['$stateProvider'];

    function stateConfig($stateProvider) {
        $stateProvider
        .state('type-service', {
            parent: 'entity',
            url: '/type-service',
            data: {
                authorities: ['ROLE_USER'],
                pageTitle: 'type-service'
            },
            views: {
                'content@app': {
                    templateUrl: 'app/entities/type-service/type-service.html',
                    controller: 'TypeServiceController',
                    controllerAs: 'vm'
                }
            },
            params: {
                page: {
                    value: '1',
                    squash: true
                },
                sort: {
                    value: 'codeTypeService,asc',
                    squash: true
                },
                search: null
            },
            resolve: {
                pagingParams: ['$stateParams', 'PaginationUtil', function ($stateParams, PaginationUtil) {
                    return {
                        page: PaginationUtil.parsePage($stateParams.page),
                        sort: $stateParams.sort,
                        predicate: PaginationUtil.parsePredicate($stateParams.sort),
                        ascending: PaginationUtil.parseAscending($stateParams.sort),
                        search: $stateParams.search
                    };
                }],
            }
        })
        .state('type-service-detail', {
            parent: 'type-service',
            url: '/type-service/{id}',
            data: {
                authorities: ['ROLE_USER'],
                pageTitle: 'type-service'
            },
            views: {
                'content@app': {
                    templateUrl: 'app/entities/type-service/type-service-detail.html',
                    controller: 'TypeServiceDetailController',
                    controllerAs: 'vm'
                }
            },
            resolve: {
                entity: ['$stateParams', 'TypeService', function($stateParams, TypeService) {
                    return TypeService.get({id : $stateParams.id}).$promise;
                }],
                previousState: ["$state", function ($state) {
                    var currentStateData = {
                        name: $state.current.name || 'type-service',
                        params: $state.params,
                        url: $state.href($state.current.name, $state.params)
                    };
                    return currentStateData;
                }]
            }
        })
        .state('type-service-detail.edit', {
            parent: 'type-service-detail',
            url: '/detail/edit',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/type-service/type-service-dialog.html',
                    controller: 'TypeServiceDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: ['TypeService', function(TypeService) {
                            return TypeService.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('^', {}, { reload: false });
                }, function() {
                    $state.go('^');
                });
            }]
        })
        .state('type-service.new', {
            parent: 'type-service',
            url: '/new',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/type-service/type-service-dialog.html',
                    controller: 'TypeServiceDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: function () {
                            return {
                                libelleTypeService: null,
                                id: null
                            };
                        }
                    }
                }).result.then(function() {
                    $state.go('type-service', null, { reload: 'type-service' });
                }, function() {
                    $state.go('type-service');
                });
            }]
        })
        .state('type-service.edit', {
            parent: 'type-service',
            url: '/{id}/edit',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/type-service/type-service-dialog.html',
                    controller: 'TypeServiceDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: ['TypeService', function(TypeService) {
                            return TypeService.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('type-service', null, { reload: 'type-service' });
                }, function() {
                    $state.go('^');
                });
            }]
        })
        .state('type-service.delete', {
            parent: 'type-service',
            url: '/{id}/delete',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/type-service/type-service-delete-dialog.html',
                    controller: 'TypeServiceDeleteController',
                    controllerAs: 'vm',
                    size: 'md',
                    resolve: {
                        entity: ['TypeService', function(TypeService) {
                            return TypeService.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('type-service', null, { reload: 'type-service' });
                }, function() {
                    $state.go('^');
                });
            }]
        });
    }

})();
