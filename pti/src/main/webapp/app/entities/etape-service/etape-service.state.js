(function() {
    'use strict';

    angular
        .module('ptiApp')
        .config(stateConfig);

    stateConfig.$inject = ['$stateProvider'];

    function stateConfig($stateProvider) {
        $stateProvider
        .state('etape-service', {
            parent: 'entity',
            url: '/etapeService?page&sort&search',
            data: {  
                authorities: ['ROLE_USER'],
                pageTitle: 'ptiApp.serviceProfil.home.title'
            },
            views: {
                'content@app': {
                    templateUrl: 'app/entities/etape-service/etape-service.html',
                    controller: 'EtapeServiceController',
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
        .state('etape-service.new', {
            parent: 'etape-service',
            url: '/new',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/etape-service/etape-service-dialog.html',
                    controller: 'EtapeServiceDialogController',
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
                    $state.go('etape-service', null, { reload: 'etape-service' });
                }, function() {
                    $state.go('etape-service');
                });
            }]
        })
        .state('etape-service.edit', {
            parent: 'etape-service',
            url: '/{id}/edit',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/service-profil/service-profil-dialog.html',
                    controller: 'ServiceProfilDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: ['ServiceProfil', function(ServiceProfil) {
                            return ServiceProfil.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('service-profil', null, { reload: 'service-profil' });
                }, function() {
                    $state.go('^');
                });
            }]
        })
        .state('etape-service.delete', {
            parent: 'service-profil',
            url: '/{id}/delete',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/service-profil/service-profil-delete-dialog.html',
                    controller: 'ServiceProfilDeleteController',
                    controllerAs: 'vm',
                    size: 'md',
                    resolve: {
                        entity: ['ServiceProfil', function(ServiceProfil) {
                            return ServiceProfil.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('service-profil', null, { reload: 'service-profil' });
                }, function() {
                    $state.go('^');
                });
            }]
        });
    }

})();
