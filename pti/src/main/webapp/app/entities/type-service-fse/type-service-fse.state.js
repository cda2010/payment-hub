(function() {
    'use strict';

    angular
        .module('ptiApp')
        .config(stateConfig);

    stateConfig.$inject = ['$stateProvider'];

    function stateConfig($stateProvider) {
        $stateProvider
        .state('type-service-fse', {
            parent: 'entity',
            url: '/type-service-fse?page&sort&search',
            data: {  
                authorities: ['ROLE_USER'],
                pageTitle: 'ptiApp.serviceProfil.home.title'
            },
            views: {
                'content@app': {
                    templateUrl: 'app/entities/type-service-fse/type-service-fse.html',
                    controller: 'TypeServiceFseController',
                    controllerAs: 'vm'
                }
            },
            params: {
                page: {
                    value: '1',
                    squash: true
                },
                sort: {
                    value: 'id,asc',
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
                translatePartialLoader: ['$translate', '$translatePartialLoader', function ($translate, $translatePartialLoader) {
                    $translatePartialLoader.addPart('serviceProfil');
                    $translatePartialLoader.addPart('global');
                    return $translate.refresh();
                }]
            }
        })
        .state('type-service-fse.new', {
            parent: 'type-service-fse',
            url: '/new',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/type-service-fse/type-service-fse-dialog.html',
                    controller: 'TypeServiceFseDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'md',
                    resolve: {
                        entity: function () {
                            return {
                                actionService: null,
                                id: null,
                                typeService:null
                            };
                        }
                    }
                }).result.then(function() {
                    $state.go('type-service-fse', null, { reload: 'type-service-fse' });
                }, function() {
                    $state.go('type-service-fse');
                });
            }]
        })
        .state('type-service-fse.edit', {
            parent: 'type-service-fse',
            url: '/{id}/edit',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/type-service-fse/type-service-fse-dialog.html',
                    controller: 'TypeServiceFseDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'md',  
                    resolve: {
                        entity: ['TypeServiceFse', function(TypeServiceFse) {
                            return TypeServiceFse.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('type-service-fse', null, { reload: 'type-service-fse' });
                }, function() {
                    $state.go('^');
                });
            }]
        })
        .state('type-service-fse.delete', {
            parent: 'type-service-fse',
            url: '/{id}/delete',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/type-service-fse/type-service-fse-delete-dialog.html',
                    controller: 'TypeServiceFseDeleteController',
                    controllerAs: 'vm',
                    size: 'md',
                    resolve: {
                        entity: ['TypeServiceFse', function(TypeServiceFse) {
                            return TypeServiceFse.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('type-service-fse', null, { reload: 'type-service-fse' });
                }, function() {
                    $state.go('^');
                });
            }]
        });
    }

})();
